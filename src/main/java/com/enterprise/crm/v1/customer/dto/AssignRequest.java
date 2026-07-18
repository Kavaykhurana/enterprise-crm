package com.enterprise.crm.v1.customer.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssignRequest {
    @NotNull(message = "Sales representative ID is required")
    private UUID assignedSalesRepId;
}
