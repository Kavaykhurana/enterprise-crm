package com.enterprise.crm.v1.lead.controller;

import com.enterprise.crm.v1.common.dto.ApiResponse;
import com.enterprise.crm.v1.common.dto.PageResponse;
import com.enterprise.crm.v1.customer.dto.AssignRequest;
import com.enterprise.crm.v1.lead.dto.*;
import com.enterprise.crm.v1.lead.service.LeadConversionService;
import com.enterprise.crm.v1.lead.service.LeadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/leads")
@RequiredArgsConstructor
public class LeadController {

    private final LeadService leadService;
    private final LeadConversionService leadConversionService;

    @PostMapping("/{id}/convert")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ConversionResponse>> convertLead(
            @PathVariable UUID id,
            @Valid @RequestBody ConvertLeadRequest request) {
        String traceId = MDC.get("traceId");
        ConversionResponse response = leadConversionService.convertLead(id, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Lead converted successfully", response, traceId));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<LeadResponse>> createLead(@Valid @RequestBody CreateLeadRequest request) {
        String traceId = MDC.get("traceId");
        LeadResponse response = leadService.createLead(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Lead created successfully", response, traceId));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<LeadResponse>> updateLead(
            @PathVariable UUID id,
            @Valid @RequestBody CreateLeadRequest request) {
        String traceId = MDC.get("traceId");
        LeadResponse response = leadService.updateLead(id, request);
        return ResponseEntity.ok(ApiResponse.success("Lead updated successfully", response, traceId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<LeadResponse>> getLeadById(@PathVariable UUID id) {
        String traceId = MDC.get("traceId");
        LeadResponse response = leadService.getLeadById(id);
        return ResponseEntity.ok(ApiResponse.success("Lead retrieved successfully", response, traceId));
    }

    @GetMapping
    public ResponseEntity<PageResponse<LeadResponse>> searchLeads(
            @RequestParam(required = false) String companyName,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) UUID assignedSalesRepId,
            @RequestParam(required = false) String tag,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startCreated,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endCreated,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startUpdated,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endUpdated,
            @RequestParam(required = false) String source,
            @RequestParam(required = false) String email,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "companyName") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("DESC") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        PageRequest pageRequest = PageRequest.of(page, size, sort);

        Page<LeadResponse> leadPage = leadService.searchLeads(
                companyName, status, assignedSalesRepId, tag,
                startCreated, endCreated, startUpdated, endUpdated,
                source, email, pageRequest
        );

        return ResponseEntity.ok(PageResponse.fromPage(leadPage));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<String>> updateLeadStatus(
            @PathVariable UUID id,
            @RequestParam String status) {
        String traceId = MDC.get("traceId");
        leadService.updateLeadStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success("Lead status updated successfully", traceId));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES_MANAGER')")
    public ResponseEntity<Void> deleteLead(@PathVariable UUID id) {
        leadService.deleteLead(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/restore")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES_MANAGER')")
    public ResponseEntity<ApiResponse<String>> restoreLead(@PathVariable UUID id) {
        String traceId = MDC.get("traceId");
        leadService.restoreLead(id);
        return ResponseEntity.ok(ApiResponse.success("Lead restored successfully", traceId));
    }

    @PutMapping("/{id}/assign")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES_MANAGER')")
    public ResponseEntity<ApiResponse<String>> assignLead(
            @PathVariable UUID id,
            @Valid @RequestBody AssignRequest request) {
        String traceId = MDC.get("traceId");
        leadService.assignLead(id, request.getAssignedSalesRepId());
        return ResponseEntity.ok(ApiResponse.success("Lead assigned successfully", traceId));
    }
}
