package com.enterprise.crm.v1.lead.service;

import com.enterprise.crm.v1.lead.dto.ConversionResponse;
import com.enterprise.crm.v1.lead.dto.ConvertLeadRequest;

import java.util.UUID;

public interface LeadConversionService {
    ConversionResponse convertLead(UUID leadId, ConvertLeadRequest request);
}
