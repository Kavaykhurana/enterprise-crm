package com.enterprise.crm.v1.customer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerResponse {
    private UUID id;
    private String companyName;
    private String companySize;
    private String customerStatus;
    private UUID assignedSalesRepId;
    private String taxIdentifier;
    private String description;
    private List<ContactResponse> contacts;
    private List<AddressRequest> addresses;
    private Set<String> tags;
}
