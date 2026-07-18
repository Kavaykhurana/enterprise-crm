package com.enterprise.crm.v1.dashboard.service;

import com.enterprise.crm.v1.common.auth.SecurityUtil;
import com.enterprise.crm.v1.customer.entity.Customer;
import com.enterprise.crm.v1.customer.repository.CustomerRepository;
import com.enterprise.crm.v1.dashboard.dto.DashboardMetricsResponse;
import com.enterprise.crm.v1.lead.repository.LeadRepository;
import com.enterprise.crm.v1.opportunity.entity.Opportunity;
import com.enterprise.crm.v1.opportunity.repository.OpportunityRepository;
import com.enterprise.crm.v1.task.repository.TaskRepository;
import com.enterprise.crm.v1.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final CustomerRepository customerRepository;
    private final LeadRepository leadRepository;
    private final OpportunityRepository opportunityRepository;
    private final TaskRepository taskRepository;

    @Override
    @Transactional(readOnly = true)
    public DashboardMetricsResponse getMetrics() {
        User currentUser = SecurityUtil.getCurrentUser();
        UUID salesRepId = null;

        // Sales Executives only see their own metrics
        if (currentUser.getRole().equals("SALES_EXECUTIVE")) {
            salesRepId = currentUser.getId();
        }

        // 1. Compute Win Rate
        List<Opportunity> opportunities;
        if (salesRepId != null) {
            opportunities = opportunityRepository.findByAssignedSalesRepId(salesRepId);
        } else {
            opportunities = opportunityRepository.findAll();
        }

        long wonCount = opportunities.stream().filter(o -> "WON".equalsIgnoreCase(o.getStage())).count();
        long closedCount = opportunities.stream().filter(o -> "WON".equalsIgnoreCase(o.getStage()) || "LOST".equalsIgnoreCase(o.getStage())).count();
        double winRate = closedCount > 0 ? ((double) wonCount / closedCount) * 100.0 : 0.0;

        // 2. Compute Lead Conversion Rate
        long convertedLeads;
        long totalLeads;
        if (salesRepId != null) {
            convertedLeads = leadRepository.countByAssignedSalesRepIdAndConvertedCustomerIdNotNull(salesRepId);
            totalLeads = leadRepository.countByAssignedSalesRepId(salesRepId);
        } else {
            convertedLeads = leadRepository.countByConvertedCustomerIdNotNull();
            totalLeads = leadRepository.count();
        }
        double leadConversionRate = totalLeads > 0 ? ((double) convertedLeads / totalLeads) * 100.0 : 0.0;

        // 3. Compute Pipeline Weighted Value (for open opportunities)
        BigDecimal pipelineWeightedValue = opportunities.stream()
                .filter(o -> !"WON".equalsIgnoreCase(o.getStage()) && !"LOST".equalsIgnoreCase(o.getStage()))
                .map(o -> {
                    BigDecimal revenue = o.getExpectedRevenue() != null ? o.getExpectedRevenue() : BigDecimal.ZERO;
                    BigDecimal probability = BigDecimal.valueOf(o.getProbability());
                    return revenue.multiply(probability).divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 4. Compute Overdue Tasks Count
        LocalDateTime now = LocalDateTime.now();
        long overdueTasksCount;
        if (salesRepId != null) {
            overdueTasksCount = taskRepository.countByAssignedUserIdAndDueDateBeforeAndStatusNot(salesRepId, now, "COMPLETED");
        } else {
            overdueTasksCount = taskRepository.countByDueDateBeforeAndStatusNot(now, "COMPLETED");
        }

        // 5. Compute Monthly Customer Acquisition
        List<Customer> customers;
        if (salesRepId != null) {
            customers = customerRepository.findByAssignedSalesRepId(salesRepId);
        } else {
            customers = customerRepository.findAll();
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
        Map<String, Long> monthlyCustomerAcquisition = customers.stream()
                .filter(c -> c.getCreatedAt() != null)
                .collect(Collectors.groupingBy(
                        c -> c.getCreatedAt().format(formatter),
                        TreeMap::new, // Sorted keys
                        Collectors.counting()
                ));

        return DashboardMetricsResponse.builder()
                .winRate(winRate)
                .leadConversionRate(leadConversionRate)
                .pipelineWeightedValue(pipelineWeightedValue)
                .overdueTasksCount(overdueTasksCount)
                .monthlyCustomerAcquisition(monthlyCustomerAcquisition)
                .build();
    }
}
