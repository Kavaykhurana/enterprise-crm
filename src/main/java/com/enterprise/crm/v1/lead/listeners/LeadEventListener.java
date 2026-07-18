package com.enterprise.crm.v1.lead.listeners;

import com.enterprise.crm.v1.lead.events.LeadConvertedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
public class LeadEventListener {

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleLeadConvertedEvent(LeadConvertedEvent event) {
        log.info("Decoupled Event Received: Lead {} converted to Customer {} by user {} at {}. Correlation ID: {}",
                event.getLeadId(), event.getCustomerId(), event.getConvertedBy(), event.getConvertedAt(), event.getCorrelationId());
    }
}
