package com.enterprise.crm.v1.lead.service;

import com.enterprise.crm.v1.common.auth.AuthorizationService;
import com.enterprise.crm.v1.lead.dto.*;
import com.enterprise.crm.v1.lead.entity.Lead;
import com.enterprise.crm.v1.lead.mapper.LeadMapper;
import com.enterprise.crm.v1.lead.repository.LeadRepository;
import com.enterprise.crm.v1.lead.specification.LeadSpecification;
import com.enterprise.crm.v1.lead.validator.LeadStatusTransitionValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LeadServiceImpl implements LeadService {

    private final LeadRepository leadRepository;
    private final LeadMapper leadMapper;
    private final LeadStatusTransitionValidator transitionValidator;
    private final AuthorizationService authorizationService;

    @Override
    @Transactional
    public LeadResponse createLead(CreateLeadRequest request) {
        Lead lead = leadMapper.createRequestToLead(request);
        Lead saved = leadRepository.save(lead);
        return leadMapper.leadToResponse(saved);
    }

    @Override
    @Transactional
    public LeadResponse updateLead(UUID id, CreateLeadRequest request) {
        Lead lead = leadRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Lead not found"));

        authorizationService.verifyRecordAccess(lead.getAssignedSalesRepId());

        lead.setCompanyName(request.getCompanyName());
        lead.setFirstName(request.getFirstName());
        lead.setLastName(request.getLastName());
        lead.setEmail(request.getEmail());
        lead.setPhone(request.getPhone());
        lead.setSource(request.getSource());
        lead.setPriority(request.getPriority());
        lead.setExpectedBudget(request.getExpectedBudget());
        lead.setExpectedCloseDate(request.getExpectedCloseDate());
        lead.setNextFollowUpDate(request.getNextFollowUpDate());

        // Validate state change if request alters status
        if (!lead.getStatus().equals(request.getStatus())) {
            transitionValidator.validate(lead.getStatus(), request.getStatus());
            lead.setStatus(request.getStatus());
        }

        Lead updated = leadRepository.save(lead);
        return leadMapper.leadToResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public LeadResponse getLeadById(UUID id) {
        Lead lead = leadRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Lead not found"));

        authorizationService.verifyRecordAccess(lead.getAssignedSalesRepId());

        return leadMapper.leadToResponse(lead);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LeadResponse> searchLeads(
            String companyName,
            String status,
            UUID assignedSalesRepId,
            String tag,
            LocalDateTime startCreated,
            LocalDateTime endCreated,
            LocalDateTime startUpdated,
            LocalDateTime endUpdated,
            String source,
            String email,
            Pageable pageable) {

        UUID repFilterId = assignedSalesRepId;
        var currentUserOpt = com.enterprise.crm.v1.common.auth.SecurityUtil.getCurrentUserOptional();
        if (currentUserOpt.isPresent() && currentUserOpt.get().getRole().equals("SALES_EXECUTIVE")) {
            repFilterId = currentUserOpt.get().getId();
        }

        Specification<Lead> spec = LeadSpecification.filter(
                companyName, status, repFilterId, tag,
                startCreated, endCreated, startUpdated, endUpdated,
                source, email
        );

        return leadRepository.findAll(spec, pageable)
                .map(leadMapper::leadToResponse);
    }

    @Override
    @Transactional
    public void updateLeadStatus(UUID id, String status) {
        Lead lead = leadRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Lead not found"));

        authorizationService.verifyRecordAccess(lead.getAssignedSalesRepId());

        transitionValidator.validate(lead.getStatus(), status);
        lead.setStatus(status);
        leadRepository.save(lead);
    }

    @Override
    @Transactional
    public void deleteLead(UUID id) {
        Lead lead = leadRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Lead not found"));

        authorizationService.verifyRecordAccess(lead.getAssignedSalesRepId());

        leadRepository.delete(lead);
    }

    @Override
    @Transactional
    public void restoreLead(UUID id) {
        Lead lead = leadRepository.findByIdWithSoftDeleted(id)
                .orElseThrow(() -> new IllegalArgumentException("Lead not found"));

        lead.setDeletedAt(null);
        leadRepository.save(lead);
    }

    @Override
    @Transactional
    public void assignLead(UUID id, UUID assignedSalesRepId) {
        com.enterprise.crm.v1.user.entity.User currentUser = com.enterprise.crm.v1.common.auth.SecurityUtil.getCurrentUser();
        if (!currentUser.getRole().equals("ADMIN") && !currentUser.getRole().equals("SALES_MANAGER")) {
            throw new AccessDeniedException("Only Admins and Sales Managers can assign leads");
        }

        Lead lead = leadRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Lead not found"));

        lead.setAssignedSalesRepId(assignedSalesRepId);
        leadRepository.save(lead);
    }
}
