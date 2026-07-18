package com.enterprise.crm.v1.customer.service;

import com.enterprise.crm.v1.customer.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.UUID;

public interface CustomerService {
    CustomerResponse createCustomer(CreateCustomerRequest request);
    CustomerResponse updateCustomer(UUID id, CreateCustomerRequest request);
    CustomerResponse getCustomerById(UUID id);
    Page<CustomerResponse> searchCustomers(
            String companyName,
            String customerStatus,
            UUID assignedSalesRepId,
            String tag,
            LocalDateTime startCreated,
            LocalDateTime endCreated,
            LocalDateTime startUpdated,
            LocalDateTime endUpdated,
            String companySize,
            String taxIdentifier,
            Pageable pageable
    );
    void deleteCustomer(UUID id);
    void restoreCustomer(UUID id);
    void assignCustomer(UUID id, UUID assignedSalesRepId);
}
