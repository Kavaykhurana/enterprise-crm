package com.enterprise.crm.v1.activity.entity;

import com.enterprise.crm.v1.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "activity_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ActivityLog extends BaseEntity {
    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType; // CUSTOMER_CREATED, LEAD_CONVERTED

    @Column(name = "entity_type", nullable = false, length = 50)
    private String entityType; // CUSTOMER, LEAD

    @Column(name = "entity_id", nullable = false)
    private UUID entityId;

    @Column(name = "actor_email", nullable = false)
    private String actorEmail;

    @Column(columnDefinition = "TEXT")
    private String payload; // text/JSON snapshot of changes
}
