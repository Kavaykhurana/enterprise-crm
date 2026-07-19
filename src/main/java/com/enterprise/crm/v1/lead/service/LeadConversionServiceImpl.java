package com.enterprise.crm.v1.lead.service;

import com.enterprise.crm.v1.common.auth.AuthorizationService;
import com.enterprise.crm.v1.common.auth.SecurityUtil;
import com.enterprise.crm.v1.common.exception.DuplicateResourceException;
import com.enterprise.crm.v1.customer.entity.Contact;
import com.enterprise.crm.v1.customer.entity.Customer;
import com.enterprise.crm.v1.customer.repository.ContactRepository;
import com.enterprise.crm.v1.customer.repository.CustomerRepository;
import com.enterprise.crm.v1.lead.dto.ConversionResponse;
import com.enterprise.crm.v1.lead.dto.ConvertLeadRequest;
import com.enterprise.crm.v1.lead.entity.Lead;
import com.enterprise.crm.v1.lead.events.LeadConvertedEvent;
import com.enterprise.crm.v1.lead.repository.LeadRepository;
import com.enterprise.crm.v1.lead.validator.LeadStatusTransitionValidator;
import com.enterprise.crm.v1.opportunity.entity.Opportunity;
import com.enterprise.crm.v1.opportunity.repository.OpportunityRepository;
import com.enterprise.crm.v1.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LeadConversionServiceImpl implements LeadConversionService {

    private final LeadRepository leadRepository;
    private final CustomerRepository customerRepository;
    private final ContactRepository contactRepository;
    private final OpportunityRepository opportunityRepository;
    private final LeadStatusTransitionValidator transitionValidator;
    private final AuthorizationService authorizationService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ConversionResponse convertLead(UUID leadId, ConvertLeadRequest request) {
        // 1. Validate Lead Preconditions (Fail Fast)
        Lead lead = leadRepository.findById(leadId)
                .orElseThrow(() -> new IllegalArgumentException("Lead not found"));

        if (lead.getConvertedCustomerId() != null) {
            throw new DuplicateResourceException("Lead already converted");
        }

        // Validate state change eligibility if not already WON
        if (!lead.getStatus().equals("WON")) {
            transitionValidator.validate(lead.getStatus(), "WON");
        }

        // Enforce ownership checks
        authorizationService.verifyRecordAccess(lead.getAssignedSalesRepId());

        User currentUser = SecurityUtil.getCurrentUser();
        String traceId = MDC.get("traceId");

        // 2. Create Customer
        Customer customer = new Customer();
        customer.setCompanyName(lead.getCompanyName() != null ? lead.getCompanyName() : (lead.getFirstName() + " " + lead.getLastName()));
        customer.setCompanySize("SMALL"); // Default fallback
        customer.setCustomerStatus("ACTIVE");
        customer.setAssignedSalesRepId(lead.getAssignedSalesRepId());
        Customer savedCustomer = customerRepository.save(customer);

        // 3. Create Primary Contact
        Contact contact = new Contact();
        contact.setCustomerId(savedCustomer.getId());
        contact.setFirstName(lead.getFirstName());
        contact.setLastName(lead.getLastName());
        contact.setEmail(lead.getEmail());
        contact.setMobilePhone(lead.getPhone());
        contact.setPrimaryContact(true);
        Contact savedContact = contactRepository.save(contact);

        // 4. Optionally Create Opportunity
        UUID opportunityId = null;
        if (request.isCreateOpportunity()) {
            if (request.getOpportunity() == null) {
                throw new IllegalArgumentException("Opportunity details are required when createOpportunity is true");
            }
            ConvertLeadRequest.OpportunityRequest oppReq = request.getOpportunity();

            if (oppReq.getName() == null || oppReq.getName().trim().isEmpty()) {
                throw new IllegalArgumentException("Opportunity name cannot be empty");
            }

            Opportunity opportunity = new Opportunity();
            opportunity.setCustomerId(savedCustomer.getId());
            opportunity.setAssignedSalesRepId(oppReq.getAssignedSalesRepId() != null ? oppReq.getAssignedSalesRepId() : lead.getAssignedSalesRepId());
            opportunity.setName(oppReq.getName());
            opportunity.setExpectedRevenue(oppReq.getExpectedRevenue());
            opportunity.setProbability(oppReq.getProbability());
            opportunity.setStage("QUALIFICATION");
            opportunity.setExpectedCloseDate(oppReq.getExpectedCloseDate());
            
            Opportunity savedOpp = opportunityRepository.save(opportunity);
            opportunityId = savedOpp.getId();
        }

        // 5. Update Lead
        lead.setConvertedCustomerId(savedCustomer.getId());
        lead.setStatus("WON");
        leadRepository.save(lead);

        // 6. Flush persistence context to resolve DB constraints
        leadRepository.flush();
        customerRepository.flush();
        contactRepository.flush();
        if (request.isCreateOpportunity()) {
            opportunityRepository.flush();
        }

        // 7. Publish LeadConvertedEvent
        LeadConvertedEvent convertedEvent = new LeadConvertedEvent(
                this,
                lead.getId(),
                savedCustomer.getId(),
                savedContact.getId(),
                opportunityId,
                currentUser.getEmail(),
                LocalDateTime.now(),
                traceId
        );
        eventPublisher.publishEvent(convertedEvent);

        // 8. Return Response
        return ConversionResponse.builder()
                .customerId(savedCustomer.getId())
                .contactId(savedContact.getId())
                .opportunityId(opportunityId)
                .leadStatus("WON")
                .traceId(traceId)
                .message("Lead converted successfully.")
                .build();
    }
}
