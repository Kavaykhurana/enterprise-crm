package com.enterprise.crm.v1.activity.listener;

import com.enterprise.crm.v1.activity.entity.ActivityLog;
import com.enterprise.crm.v1.activity.repository.ActivityLogRepository;
import com.enterprise.crm.v1.lead.events.LeadConvertedEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ActivityLogEventListener {

    private final ActivityLogRepository activityLogRepository;
    private final ObjectMapper objectMapper;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleLeadConverted(LeadConvertedEvent event) {
        log.info("ActivityLogEventListener: Logging LeadConvertedEvent for Lead: {}", event.getLeadId());

        try {
            Map<String, String> data = new HashMap<>();
            data.put("customerId", event.getCustomerId().toString());
            data.put("contactId", event.getContactId().toString());
            if (event.getOpportunityId() != null) {
                data.put("opportunityId", event.getOpportunityId().toString());
            }
            String payload = objectMapper.writeValueAsString(data);

            ActivityLog logRecord = new ActivityLog();
            logRecord.setEventType("LEAD_CONVERTED");
            logRecord.setEntityType("LEAD");
            logRecord.setEntityId(event.getLeadId());
            logRecord.setActorEmail(event.getConvertedBy());
            logRecord.setPayload(payload);

            activityLogRepository.save(logRecord);
        } catch (Exception e) {
            log.error("Failed to persist ActivityLog for Lead: {}", event.getLeadId(), e);
        }
    }
}
