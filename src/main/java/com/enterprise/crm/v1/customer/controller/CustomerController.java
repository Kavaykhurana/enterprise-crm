package com.enterprise.crm.v1.customer.controller;

import com.enterprise.crm.v1.common.dto.ApiResponse;
import com.enterprise.crm.v1.common.dto.PageResponse;
import com.enterprise.crm.v1.customer.dto.*;
import com.enterprise.crm.v1.customer.service.CustomerService;
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
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES_MANAGER')")
    public ResponseEntity<ApiResponse<CustomerResponse>> createCustomer(@Valid @RequestBody CreateCustomerRequest request) {
        String traceId = MDC.get("traceId");
        CustomerResponse response = customerService.createCustomer(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Customer created successfully", response, traceId));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<CustomerResponse>> updateCustomer(
            @PathVariable UUID id,
            @Valid @RequestBody CreateCustomerRequest request) {
        String traceId = MDC.get("traceId");
        CustomerResponse response = customerService.updateCustomer(id, request);
        return ResponseEntity.ok(ApiResponse.success("Customer updated successfully", response, traceId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomerResponse>> getCustomerById(@PathVariable UUID id) {
        String traceId = MDC.get("traceId");
        CustomerResponse response = customerService.getCustomerById(id);
        return ResponseEntity.ok(ApiResponse.success("Customer retrieved successfully", response, traceId));
    }

    @GetMapping
    public ResponseEntity<PageResponse<CustomerResponse>> searchCustomers(
            @RequestParam(required = false) String companyName,
            @RequestParam(required = false) String customerStatus,
            @RequestParam(required = false) UUID assignedSalesRepId,
            @RequestParam(required = false) String tag,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startCreated,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endCreated,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startUpdated,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endUpdated,
            @RequestParam(required = false) String companySize,
            @RequestParam(required = false) String taxIdentifier,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "companyName") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("DESC") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        PageRequest pageRequest = PageRequest.of(page, size, sort);

        Page<CustomerResponse> customerPage = customerService.searchCustomers(
                companyName, customerStatus, assignedSalesRepId, tag,
                startCreated, endCreated, startUpdated, endUpdated,
                companySize, taxIdentifier, pageRequest
        );

        return ResponseEntity.ok(PageResponse.fromPage(customerPage));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES_MANAGER')")
    public ResponseEntity<Void> deleteCustomer(@PathVariable UUID id) {
        customerService.deleteCustomer(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/restore")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES_MANAGER')")
    public ResponseEntity<ApiResponse<String>> restoreCustomer(@PathVariable UUID id) {
        String traceId = MDC.get("traceId");
        customerService.restoreCustomer(id);
        return ResponseEntity.ok(ApiResponse.success("Customer restored successfully", traceId));
    }

    @PutMapping("/{id}/assign")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES_MANAGER')")
    public ResponseEntity<ApiResponse<String>> assignCustomer(
            @PathVariable UUID id,
            @Valid @RequestBody AssignRequest request) {
        String traceId = MDC.get("traceId");
        customerService.assignCustomer(id, request.getAssignedSalesRepId());
        return ResponseEntity.ok(ApiResponse.success("Customer assigned successfully", traceId));
    }
}
