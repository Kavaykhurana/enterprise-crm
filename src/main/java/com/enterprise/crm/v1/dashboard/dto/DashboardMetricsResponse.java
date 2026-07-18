package com.enterprise.crm.v1.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardMetricsResponse {
    private double winRate; // won opportunity count / total closed opportunity count
    private double leadConversionRate; // converted lead count / total active lead count
    private BigDecimal pipelineWeightedValue; // sum of (expectedRevenue * probability / 100) of open opportunities
    private long overdueTasksCount; // count of non-completed tasks whose due dates are before now
    private Map<String, Long> monthlyCustomerAcquisition; // monthly customer registration counts (e.g. "2026-07": 12)
}
