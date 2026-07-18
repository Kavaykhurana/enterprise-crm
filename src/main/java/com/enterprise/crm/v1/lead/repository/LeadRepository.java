package com.enterprise.crm.v1.lead.repository;

import com.enterprise.crm.v1.lead.entity.Lead;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface LeadRepository extends JpaRepository<Lead, UUID>, JpaSpecificationExecutor<Lead> {
    @Query(value = "SELECT * FROM leads WHERE id = ?1", nativeQuery = true)
    Optional<Lead> findByIdWithSoftDeleted(UUID id);

    long countByAssignedSalesRepId(UUID assignedSalesRepId);
    long countByConvertedCustomerIdNotNull();
    long countByAssignedSalesRepIdAndConvertedCustomerIdNotNull(UUID assignedSalesRepId);
}
