package com.enterprise.crm.v1.customer.repository;

import com.enterprise.crm.v1.customer.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, UUID>, JpaSpecificationExecutor<Customer> {
    Optional<Customer> findByTaxIdentifier(String taxIdentifier);
    boolean existsByTaxIdentifier(String taxIdentifier);

    @Query(value = "SELECT * FROM customers WHERE id = ?1", nativeQuery = true)
    Optional<Customer> findByIdWithSoftDeleted(UUID id);

    @Query(value = "SELECT EXISTS(SELECT 1 FROM customers WHERE tax_identifier = ?1 AND deleted_at IS NULL)", nativeQuery = true)
    boolean existsActiveTaxIdentifier(String taxIdentifier);

    java.util.List<Customer> findByAssignedSalesRepId(UUID assignedSalesRepId);
}
