package com.enterprise.crm.v1.lead.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class LeadConvertedEvent extends ApplicationEvent {
    private final UUID leadId;
    private final UUID customerId;
    private final UUID contactId;
    private final UUID opportunityId;
    private final String convertedBy;
    private final LocalDateTime convertedAt;
    private final String correlationId;

    public LeadConvertedEvent(
            Object source,
            UUID leadId,
            UUID customerId,
            UUID contactId,
            UUID opportunityId,
            String convertedBy,
            LocalDateTime convertedAt,
            String correlationId) {
        super(source);
        this.leadId = leadId;
        this.customerId = customerId;
        this.contactId = contactId;
        this.opportunityId = opportunityId;
        this.convertedBy = convertedBy;
        this.convertedAt = convertedAt;
        this.correlationId = correlationId;
    }
}
