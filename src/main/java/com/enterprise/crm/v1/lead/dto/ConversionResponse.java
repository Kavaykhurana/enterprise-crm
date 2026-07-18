package com.enterprise.crm.v1.lead.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversionResponse {
    private UUID customerId;
    private UUID contactId;
    private UUID opportunityId;
    private String leadStatus;
    private String traceId;
    private String message;
}
