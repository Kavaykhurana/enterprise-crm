package com.enterprise.crm.v1.task.service;

import com.enterprise.crm.v1.task.dto.*;

import java.util.List;
import java.util.UUID;

public interface TaskService {
    TaskResponse createTask(CreateTaskRequest request);
    TaskResponse updateTask(UUID id, CreateTaskRequest request);
    TaskResponse getTaskById(UUID id);
    void deleteTask(UUID id);
    TaskCommentResponse addComment(UUID taskId, TaskCommentRequest request);
    List<TaskCommentResponse> getComments(UUID taskId);
}
