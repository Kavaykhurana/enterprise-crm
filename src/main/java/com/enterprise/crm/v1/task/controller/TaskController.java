package com.enterprise.crm.v1.task.controller;

import com.enterprise.crm.v1.common.dto.ApiResponse;
import com.enterprise.crm.v1.task.dto.*;
import com.enterprise.crm.v1.task.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    public ResponseEntity<ApiResponse<TaskResponse>> createTask(@Valid @RequestBody CreateTaskRequest request) {
        String traceId = MDC.get("traceId");
        TaskResponse response = taskService.createTask(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Task created successfully", response, traceId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TaskResponse>> updateTask(
            @PathVariable UUID id,
            @Valid @RequestBody CreateTaskRequest request) {
        String traceId = MDC.get("traceId");
        TaskResponse response = taskService.updateTask(id, request);
        return ResponseEntity.ok(ApiResponse.success("Task updated successfully", response, traceId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TaskResponse>> getTaskById(@PathVariable UUID id) {
        String traceId = MDC.get("traceId");
        TaskResponse response = taskService.getTaskById(id);
        return ResponseEntity.ok(ApiResponse.success("Task retrieved successfully", response, traceId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable UUID id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/comments")
    public ResponseEntity<ApiResponse<TaskCommentResponse>> addComment(
            @PathVariable UUID id,
            @Valid @RequestBody TaskCommentRequest request) {
        String traceId = MDC.get("traceId");
        TaskCommentResponse response = taskService.addComment(id, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Comment added successfully", response, traceId));
    }

    @GetMapping("/{id}/comments")
    public ResponseEntity<ApiResponse<List<TaskCommentResponse>>> getComments(@PathVariable UUID id) {
        String traceId = MDC.get("traceId");
        List<TaskCommentResponse> comments = taskService.getComments(id);
        return ResponseEntity.ok(ApiResponse.success("Comments retrieved successfully", comments, traceId));
    }
}
