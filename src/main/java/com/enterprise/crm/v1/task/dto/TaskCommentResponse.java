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
public class TaskCommentResponse {
    private UUID id;
    private UUID taskId;
    private String content;
    private String authorEmail;
    private LocalDateTime createdAt;
}
