package com.enterprise.crm.v1.lead.service;

import com.enterprise.crm.v1.common.auth.AuthorizationService;
import com.enterprise.crm.v1.common.exception.DuplicateResourceException;
import com.enterprise.crm.v1.customer.entity.Contact;
import com.enterprise.crm.v1.customer.entity.Customer;
import com.enterprise.crm.v1.customer.repository.ContactRepository;
import com.enterprise.crm.v1.customer.repository.CustomerRepository;
import com.enterprise.crm.v1.lead.dto.ConversionResponse;
import com.enterprise.crm.v1.lead.dto.ConvertLeadRequest;
import com.enterprise.crm.v1.lead.entity.Lead;
import com.enterprise.crm.v1.lead.repository.LeadRepository;
import com.enterprise.crm.v1.opportunity.entity.Opportunity;
import com.enterprise.crm.v1.opportunity.repository.OpportunityRepository;
import com.enterprise.crm.v1.user.entity.User;
import com.enterprise.crm.v1.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class LeadConversionServiceTest {

    @Autowired
    private LeadConversionService leadConversionService;

    @Autowired
    private LeadRepository leadRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ContactRepository contactRepository;

    @Autowired
    private OpportunityRepository opportunityRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private com.enterprise.crm.v1.user.repository.SessionLogRepository sessionLogRepository;

    @Autowired
    private com.enterprise.crm.v1.auth.repository.RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private jakarta.persistence.EntityManager entityManager;

    private User salesRep;
    private User managerUser;

    @BeforeEach
    public void setup() {
        opportunityRepository.deleteAll();
        contactRepository.deleteAll();
        customerRepository.deleteAll();
        leadRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        sessionLogRepository.deleteAll();
        userRepository.deleteAll();
        userRepository.flush();

        salesRep = new User();
        salesRep.setEmail("rep@example.com");
        salesRep.setPasswordHash("HashPassword123!");
        salesRep.setFirstName("Rep");
        salesRep.setLastName("User");
        salesRep.setRole("SALES_EXECUTIVE");
        salesRep.setTokenVersion(1);
        salesRep = userRepository.save(salesRep);

        managerUser = new User();
        managerUser.setEmail("mgr@example.com");
        managerUser.setPasswordHash("HashPassword123!");
        managerUser.setFirstName("Mgr");
        managerUser.setLastName("User");
        managerUser.setRole("SALES_MANAGER");
        managerUser.setTokenVersion(1);
        managerUser = userRepository.save(managerUser);
    }

    private void authenticateUser(User user) {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                user, null, List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole()))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    @Transactional
    public void testSuccessfulLeadConversionNoOpportunity() {
        authenticateUser(salesRep);

        Lead lead = new Lead();
        lead.setCompanyName("Acme Corp");
        lead.setFirstName("John");
        lead.setLastName("Doe");
        lead.setEmail("john.doe@example.com");
        lead.setPhone("+1234567890");
        lead.setSource("WEB");
        lead.setPriority("HIGH");
        lead.setStatus("NEGOTIATION");
        lead.setAssignedSalesRepId(salesRep.getId());
        lead = leadRepository.save(lead);

        ConvertLeadRequest request = new ConvertLeadRequest();
        request.setCreateOpportunity(false);

        ConversionResponse response = leadConversionService.convertLead(lead.getId(), request);

        assertNotNull(response.getCustomerId());
        assertNotNull(response.getContactId());
        assertNull(response.getOpportunityId());
        assertEquals("WON", response.getLeadStatus());

        Customer customer = customerRepository.findById(response.getCustomerId()).orElse(null);
        assertNotNull(customer);
        assertEquals("Acme Corp", customer.getCompanyName());
        assertEquals(salesRep.getId(), customer.getAssignedSalesRepId());

        Contact contact = contactRepository.findById(response.getContactId()).orElse(null);
        assertNotNull(contact);
        assertEquals("John", contact.getFirstName());
        assertEquals("john.doe@example.com", contact.getEmail());
        assertTrue(contact.isPrimaryContact());
    }

    @Test
    @Transactional
    public void testSuccessfulLeadConversionWithOpportunity() {
        authenticateUser(salesRep);

        Lead lead = new Lead();
        lead.setCompanyName("Beta Ltd");
        lead.setFirstName("Jane");
        lead.setLastName("Smith");
        lead.setEmail("jane.smith@example.com");
        lead.setSource("PARTNER");
        lead.setPriority("MEDIUM");
        lead.setStatus("NEGOTIATION");
        lead.setAssignedSalesRepId(salesRep.getId());
        lead = leadRepository.save(lead);

        ConvertLeadRequest request = new ConvertLeadRequest();
        request.setCreateOpportunity(true);

        ConvertLeadRequest.OpportunityRequest oppReq = new ConvertLeadRequest.OpportunityRequest();
        oppReq.setName("Enterprise Software License");
        oppReq.setExpectedRevenue(new BigDecimal("150000.00"));
        oppReq.setProbability(70);
        oppReq.setExpectedCloseDate(LocalDate.now().plusMonths(3));
        request.setOpportunity(oppReq);

        ConversionResponse response = leadConversionService.convertLead(lead.getId(), request);

        assertNotNull(response.getCustomerId());
        assertNotNull(response.getContactId());
        assertNotNull(response.getOpportunityId());
        assertEquals("WON", response.getLeadStatus());

        Opportunity opportunity = opportunityRepository.findById(response.getOpportunityId()).orElse(null);
        assertNotNull(opportunity);
        assertEquals("Enterprise Software License", opportunity.getName());
        assertEquals(0, opportunity.getExpectedRevenue().compareTo(new BigDecimal("150000.00")));
        assertEquals("QUALIFICATION", opportunity.getStage());
    }

    @Test
    @Transactional
    public void testConversionAccessDeniedForNonOwner() {
        User otherRep = new User();
        otherRep.setEmail("rep2@example.com");
        otherRep.setPasswordHash("HashPassword123!");
        otherRep.setFirstName("Other");
        otherRep.setLastName("Rep");
        otherRep.setRole("SALES_EXECUTIVE");
        otherRep.setTokenVersion(1);
        otherRep = userRepository.save(otherRep);

        authenticateUser(otherRep);

        Lead lead = new Lead();
        lead.setCompanyName("Acme Corp");
        lead.setFirstName("John");
        lead.setLastName("Doe");
        lead.setEmail("john.doe@example.com");
        lead.setSource("WEB");
        lead.setPriority("HIGH");
        lead.setStatus("NEGOTIATION");
        lead.setAssignedSalesRepId(salesRep.getId()); // Owned by salesRep
        lead = leadRepository.save(lead);

        ConvertLeadRequest request = new ConvertLeadRequest();
        request.setCreateOpportunity(false);

        final Lead targetLead = lead;
        assertThrows(AccessDeniedException.class, () -> {
            leadConversionService.convertLead(targetLead.getId(), request);
        });
    }

    @Test
    @Transactional
    public void testConversionConflictIfAlreadyConverted() {
        authenticateUser(salesRep);

        Lead lead = new Lead();
        lead.setCompanyName("Acme Corp");
        lead.setFirstName("John");
        lead.setLastName("Doe");
        lead.setEmail("john.doe@example.com");
        lead.setSource("WEB");
        lead.setPriority("HIGH");
        lead.setStatus("WON");
        lead.setAssignedSalesRepId(salesRep.getId());
        lead.setConvertedCustomerId(UUID.randomUUID()); // Already converted
        lead = leadRepository.save(lead);

        ConvertLeadRequest request = new ConvertLeadRequest();
        request.setCreateOpportunity(false);

        final Lead targetLead = lead;
        assertThrows(DuplicateResourceException.class, () -> {
            leadConversionService.convertLead(targetLead.getId(), request);
        });
    }

    @Test
    public void testConversionRollbackOnOpportunityFailure() {
        authenticateUser(salesRep);

        Lead lead = new Lead();
        lead.setCompanyName("Acme Corp");
        lead.setFirstName("John");
        lead.setLastName("Doe");
        lead.setEmail("john.doe@example.com");
        lead.setSource("WEB");
        lead.setPriority("HIGH");
        lead.setStatus("NEGOTIATION");
        lead.setAssignedSalesRepId(salesRep.getId());
        lead = leadRepository.save(lead);

        ConvertLeadRequest request = new ConvertLeadRequest();
        request.setCreateOpportunity(true);

        ConvertLeadRequest.OpportunityRequest oppReq = new ConvertLeadRequest.OpportunityRequest();
        oppReq.setName(null); // NULL name will cause SQL integrity violation on flush
        oppReq.setExpectedRevenue(new BigDecimal("1000.00"));
        oppReq.setProbability(50);
        oppReq.setExpectedCloseDate(LocalDate.now().plusMonths(1));
        request.setOpportunity(oppReq);

        final Lead targetLead = lead;
        assertThrows(RuntimeException.class, () -> {
            leadConversionService.convertLead(targetLead.getId(), request);
        });

        // Since it rolled back, verify Customer was NOT saved
        assertEquals(0, customerRepository.count());
        assertEquals(0, contactRepository.count());
    }

    @Test
    @Transactional
    public void testLeadOptimisticLockingConflict() {
        Lead lead = new Lead();
        lead.setCompanyName("Acme Corp");
        lead.setFirstName("John");
        lead.setLastName("Doe");
        lead.setEmail("john.doe@example.com");
        lead.setSource("WEB");
        lead.setPriority("HIGH");
        lead.setStatus("NEW");
        lead.setAssignedSalesRepId(salesRep.getId());
        lead = leadRepository.saveAndFlush(lead);

        // Fetch session A
        Lead leadA = leadRepository.findById(lead.getId()).orElse(null);
        assertNotNull(leadA);
        entityManager.clear(); // Decouple from session

        // Fetch session B
        Lead leadB = leadRepository.findById(lead.getId()).orElse(null);
        assertNotNull(leadB);
        entityManager.clear(); // Decouple from session

        // Update lead in session A (committed to DB, version is incremented)
        leadA.setPriority("LOW");
        leadRepository.saveAndFlush(leadA);
        entityManager.clear(); // Clear so it doesn't affect merge of leadB

        // Try updating lead in session B (which has stale version)
        leadB.setPriority("MEDIUM");
        assertThrows(ObjectOptimisticLockingFailureException.class, () -> {
            leadRepository.saveAndFlush(leadB);
        });
    }
}
