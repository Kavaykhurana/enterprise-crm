package com.enterprise.crm.v1.lead.service;

import com.enterprise.crm.v1.lead.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.UUID;

public interface LeadService {
    LeadResponse createLead(CreateLeadRequest request);
    LeadResponse updateLead(UUID id, CreateLeadRequest request);
    LeadResponse getLeadById(UUID id);
    Page<LeadResponse> searchLeads(
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
            Pageable pageable
    );
    void updateLeadStatus(UUID id, String status);
    void deleteLead(UUID id);
    void restoreLead(UUID id);
    void assignLead(UUID id, UUID assignedSalesRepId);
}
