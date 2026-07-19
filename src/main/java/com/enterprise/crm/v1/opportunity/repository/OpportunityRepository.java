package com.enterprise.crm.v1.opportunity.repository;

import com.enterprise.crm.v1.opportunity.entity.Opportunity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OpportunityRepository extends JpaRepository<Opportunity, UUID> {
    List<Opportunity> findByAssignedSalesRepId(UUID assignedSalesRepId);
    List<Opportunity> findByStageIn(List<String> stages);
    List<Opportunity> findByAssignedSalesRepIdAndStageIn(UUID assignedSalesRepId, List<String> stages);
}
