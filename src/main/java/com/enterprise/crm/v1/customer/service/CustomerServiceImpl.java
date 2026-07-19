package com.enterprise.crm.v1.customer.service;

import com.enterprise.crm.v1.common.auth.AuthorizationService;
import com.enterprise.crm.v1.common.exception.DuplicateResourceException;
import com.enterprise.crm.v1.customer.dto.CreateCustomerRequest;
import com.enterprise.crm.v1.customer.dto.CustomerResponse;
import com.enterprise.crm.v1.customer.entity.Customer;
import com.enterprise.crm.v1.customer.mapper.CustomerMapper;
import com.enterprise.crm.v1.customer.repository.CustomerRepository;
import com.enterprise.crm.v1.customer.specification.CustomerSpecification;
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
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;
    private final AuthorizationService authorizationService;

    @Override
    @Transactional
    public CustomerResponse createCustomer(CreateCustomerRequest request) {
        if (request.getTaxIdentifier() != null && !request.getTaxIdentifier().trim().isEmpty()) {
            if (customerRepository.existsActiveTaxIdentifier(request.getTaxIdentifier())) {
                throw new DuplicateResourceException("Active customer with tax identifier already exists: " + request.getTaxIdentifier());
            }
        }

        Customer customer = customerMapper.createRequestToCustomer(request);
        Customer saved = customerRepository.save(customer);
        return customerMapper.customerToResponse(saved);
    }

    @Override
    @Transactional
    public CustomerResponse updateCustomer(UUID id, CreateCustomerRequest request) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));

        authorizationService.verifyRecordAccess(customer.getAssignedSalesRepId());

        if (request.getTaxIdentifier() != null && !request.getTaxIdentifier().trim().isEmpty()
                && !request.getTaxIdentifier().equals(customer.getTaxIdentifier())) {
            if (customerRepository.existsActiveTaxIdentifier(request.getTaxIdentifier())) {
                throw new DuplicateResourceException("Active customer with tax identifier already exists: " + request.getTaxIdentifier());
            }
        }

        customer.setCompanyName(request.getCompanyName());
        customer.setCompanySize(request.getCompanySize());
        customer.setCustomerStatus(request.getCustomerStatus());
        customer.setAssignedSalesRepId(request.getAssignedSalesRepId());
        customer.setTaxIdentifier(request.getTaxIdentifier());
        customer.setDescription(request.getDescription());

        Customer updated = customerRepository.save(customer);
        return customerMapper.customerToResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerResponse getCustomerById(UUID id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));

        authorizationService.verifyRecordAccess(customer.getAssignedSalesRepId());

        return customerMapper.customerToResponse(customer);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CustomerResponse> searchCustomers(
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
            Pageable pageable) {

        Specification<Customer> spec = CustomerSpecification.filter(
                companyName, customerStatus, assignedSalesRepId, tag,
                startCreated, endCreated, startUpdated, endUpdated,
                companySize, taxIdentifier
        );

        return customerRepository.findAll(spec, pageable)
                .map(customerMapper::customerToResponse);
    }

    @Override
    @Transactional
    public void deleteCustomer(UUID id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));

        authorizationService.verifyRecordAccess(customer.getAssignedSalesRepId());

        customerRepository.delete(customer);
    }

    @Override
    @Transactional
    public void restoreCustomer(UUID id) {
        Customer customer = customerRepository.findByIdWithSoftDeleted(id)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));

        if (customer.getDeletedAt() == null) {
            return; // Not deleted
        }

        // Verify restoring won't conflict with unique active tax identifier constraint
        if (customer.getTaxIdentifier() != null && !customer.getTaxIdentifier().trim().isEmpty()) {
            if (customerRepository.existsActiveTaxIdentifier(customer.getTaxIdentifier())) {
                throw new DuplicateResourceException("Cannot restore: another active customer with tax identifier already exists.");
            }
        }

        customer.setDeletedAt(null);
        customerRepository.save(customer);
    }

    @Override
    @Transactional
    public void assignCustomer(UUID id, UUID assignedSalesRepId) {
        com.enterprise.crm.v1.user.entity.User currentUser = com.enterprise.crm.v1.common.auth.SecurityUtil.getCurrentUser();
        if (!currentUser.getRole().equals("ADMIN") && !currentUser.getRole().equals("SALES_MANAGER")) {
            throw new AccessDeniedException("Only Admins and Sales Managers can assign customers");
        }

        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));

        customer.setAssignedSalesRepId(assignedSalesRepId);
        customerRepository.save(customer);
    }
}
