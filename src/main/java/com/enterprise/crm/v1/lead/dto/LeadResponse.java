package com.enterprise.crm.v1.lead.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeadResponse {
    private UUID id;
    private String companyName;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String source;
    private String priority;
    private String status;
    private UUID assignedSalesRepId;
    private UUID convertedCustomerId;
    private BigDecimal expectedBudget;
    private LocalDate expectedCloseDate;
    private LocalDateTime lastContactedAt;
    private LocalDateTime nextFollowUpDate;
    private Set<String> tags;
}
