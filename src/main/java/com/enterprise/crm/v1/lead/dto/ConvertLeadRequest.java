package com.enterprise.crm.v1.lead.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConvertLeadRequest {
    private boolean createOpportunity;

    @Valid
    private OpportunityRequest opportunity;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OpportunityRequest {
        @NotBlank(message = "Opportunity name is required")
        private String name;

        @NotNull(message = "Expected revenue is required")
        @PositiveOrZero(message = "Expected revenue must be positive or zero")
        private BigDecimal expectedRevenue;

        @Min(value = 0, message = "Probability must be at least 0")
        @Max(value = 100, message = "Probability must be at most 100")
        private int probability;

        @NotNull(message = "Expected close date is required")
        @FutureOrPresent(message = "Expected close date must be in the future or present")
        private LocalDate expectedCloseDate;

        private UUID assignedSalesRepId;
    }
}
