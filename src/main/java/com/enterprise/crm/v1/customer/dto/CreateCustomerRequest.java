package com.enterprise.crm.v1.customer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateCustomerRequest {
    @NotBlank(message = "Company name is required")
    @Size(max = 200, message = "Company name must be less than 200 characters")
    private String companyName;

    @NotBlank(message = "Company size is required")
    private String companySize; // SMALL, MEDIUM, ENTERPRISE

    @NotBlank(message = "Customer status is required")
    private String customerStatus; // PROSPECT, ACTIVE, INACTIVE

    private UUID assignedSalesRepId;

    @Size(max = 50, message = "Tax identifier must be less than 50 characters")
    private String taxIdentifier;

    private String description;
}
