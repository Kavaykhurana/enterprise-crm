package com.enterprise.crm.v1.task.entity;

import com.enterprise.crm.v1.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tasks")
@SQLDelete(sql = "UPDATE tasks SET deleted_at = NOW() WHERE id = ? AND version = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Task extends BaseEntity {
    @Column(nullable = false, length = 255)
    private String title;

    private String description;

    @Column(name = "due_date", nullable = false)
    private LocalDateTime dueDate;

    @Column(nullable = false, length = 30)
    private String status = "TODO"; // TODO, IN_PROGRESS, COMPLETED

    @Column(name = "assigned_user_id")
    private UUID assignedUserId;

    @Column(name = "related_entity_type", length = 50)
    private String relatedEntityType; // CUSTOMER, LEAD

    @Column(name = "related_entity_id")
    private UUID relatedEntityId;

    @Column(name = "recurrence_rule", length = 50)
    private String recurrenceRule = "NONE"; // NONE, DAILY, WEEKLY, MONTHLY
}
