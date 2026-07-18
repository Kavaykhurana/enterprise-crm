package com.enterprise.crm.v1.task.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateTaskRequest {
    @NotBlank(message = "Title is required")
    @Size(max = 255)
    private String title;

    private String description;

    @NotNull(message = "Due date is required")
    private LocalDateTime dueDate;

    @NotBlank(message = "Status is required")
    private String status; // TODO, IN_PROGRESS, COMPLETED

    private UUID assignedUserId;

    private String relatedEntityType; // CUSTOMER, LEAD
    private UUID relatedEntityId;

    private String recurrenceRule = "NONE"; // NONE, DAILY, WEEKLY, MONTHLY
}
