package com.enterprise.crm.v1.dashboard.controller;

import com.enterprise.crm.v1.common.dto.ApiResponse;
import com.enterprise.crm.v1.dashboard.dto.DashboardMetricsResponse;
import com.enterprise.crm.v1.dashboard.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/metrics")
    public ResponseEntity<ApiResponse<DashboardMetricsResponse>> getMetrics() {
        String traceId = MDC.get("traceId");
        DashboardMetricsResponse response = dashboardService.getMetrics();
        return ResponseEntity.ok(ApiResponse.success("Dashboard metrics aggregated successfully", response, traceId));
    }
}
