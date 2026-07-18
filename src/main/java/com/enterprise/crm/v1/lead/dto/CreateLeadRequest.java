package com.enterprise.crm.v1.lead.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateLeadRequest {
    @Size(max = 200)
    private String companyName;

    @NotBlank(message = "First name is required")
    @Size(max = 100)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 100)
    private String lastName;

    @Email(message = "Email must be valid")
    @NotBlank(message = "Email is required")
    @Size(max = 150)
    private String email;

    @Size(max = 20)
    private String phone;

    @NotBlank(message = "Source is required")
    private String source; // WEB, REFERRAL, COLD_CALL, PARTNER, CONFERENCE, OTHER

    @NotBlank(message = "Priority is required")
    private String priority; // LOW, MEDIUM, HIGH

    @NotBlank(message = "Status is required")
    private String status; // NEW, CONTACTED, QUALIFIED, PROPOSAL, NEGOTIATION, WON, LOST

    private UUID assignedSalesRepId;

    @PositiveOrZero(message = "Expected budget must be positive or zero")
    private BigDecimal expectedBudget;

    private LocalDate expectedCloseDate;
    private LocalDateTime nextFollowUpDate;
}
