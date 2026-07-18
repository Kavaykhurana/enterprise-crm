package com.enterprise.crm.v1.task.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskCommentRequest {
    @NotBlank(message = "Comment content is required")
    private String content;
}
