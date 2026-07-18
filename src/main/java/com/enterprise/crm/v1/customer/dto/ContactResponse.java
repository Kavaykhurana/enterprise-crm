package com.enterprise.crm.v1.customer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContactResponse {
    private UUID id;
    private UUID customerId;
    private String firstName;
    private String lastName;
    private String email;
    private String mobilePhone;
    private String workPhone;
    private String jobTitle;
    private String department;
    private String linkedinUrl;
    private boolean primaryContact;
}
