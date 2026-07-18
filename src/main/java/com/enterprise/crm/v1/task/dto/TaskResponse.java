package com.enterprise.crm.v1.task.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskResponse {
    private UUID id;
    private String title;
    private String description;
    private LocalDateTime dueDate;
    private String status;
    private UUID assignedUserId;
    private String relatedEntityType;
    private UUID relatedEntityId;
    private String recurrenceRule;
}
