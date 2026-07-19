package com.enterprise.crm.v1.task.service;

import com.enterprise.crm.v1.common.auth.AuthorizationService;
import com.enterprise.crm.v1.common.auth.SecurityUtil;
import com.enterprise.crm.v1.common.dto.PageResponse;
import com.enterprise.crm.v1.customer.repository.CustomerRepository;
import com.enterprise.crm.v1.lead.repository.LeadRepository;
import com.enterprise.crm.v1.task.dto.*;
import com.enterprise.crm.v1.task.entity.Task;
import com.enterprise.crm.v1.task.entity.TaskComment;
import com.enterprise.crm.v1.task.mapper.TaskMapper;
import com.enterprise.crm.v1.task.repository.TaskCommentRepository;
import com.enterprise.crm.v1.task.repository.TaskRepository;
import com.enterprise.crm.v1.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final TaskCommentRepository taskCommentRepository;
    private final CustomerRepository customerRepository;
    private final LeadRepository leadRepository;
    private final TaskMapper taskMapper;
    private final AuthorizationService authorizationService;

    @Override
    @Transactional
    public TaskResponse createTask(CreateTaskRequest request) {
        validatePolymorphicRelation(request.getRelatedEntityType(), request.getRelatedEntityId());

        Task task = taskMapper.createRequestToTask(request);
        Task saved = taskRepository.save(task);
        return taskMapper.taskToResponse(saved);
    }

    @Override
    @Transactional
    public TaskResponse updateTask(UUID id, CreateTaskRequest request) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));

        authorizationService.verifyRecordAccess(task.getAssignedUserId());
        validatePolymorphicRelation(request.getRelatedEntityType(), request.getRelatedEntityId());

        boolean statusChangedToCompleted = !"COMPLETED".equals(task.getStatus()) && "COMPLETED".equals(request.getStatus());

        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setDueDate(request.getDueDate());
        task.setStatus(request.getStatus());
        task.setAssignedUserId(request.getAssignedUserId());
        task.setRelatedEntityType(request.getRelatedEntityType());
        task.setRelatedEntityId(request.getRelatedEntityId());
        task.setRecurrenceRule(request.getRecurrenceRule());

        Task updated = taskRepository.save(task);

        // Handle recurrence engine trigger
        if (statusChangedToCompleted && !"NONE".equals(updated.getRecurrenceRule())) {
            createNewRecurringTaskInstance(updated);
        }

        return taskMapper.taskToResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public TaskResponse getTaskById(UUID id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));

        authorizationService.verifyRecordAccess(task.getAssignedUserId());
        return taskMapper.taskToResponse(task);
    }

    @Override
    @Transactional
    public void deleteTask(UUID id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));

        authorizationService.verifyRecordAccess(task.getAssignedUserId());
        taskRepository.delete(task);
    }

    @Override
    @Transactional
    public TaskCommentResponse addComment(UUID taskId, TaskCommentRequest request) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));

        authorizationService.verifyRecordAccess(task.getAssignedUserId());
        User currentUser = SecurityUtil.getCurrentUser();

        TaskComment comment = new TaskComment();
        comment.setTaskId(taskId);
        comment.setContent(request.getContent());
        comment.setAuthorEmail(currentUser.getEmail());

        TaskComment saved = taskCommentRepository.save(comment);
        return taskMapper.commentToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskCommentResponse> getComments(UUID taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));

        authorizationService.verifyRecordAccess(task.getAssignedUserId());

        return taskCommentRepository.findByTaskId(taskId).stream()
                .map(taskMapper::commentToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskResponse> searchTasks(UUID assignedUserId) {
        UUID effectiveUserId = assignedUserId;

        var currentUserOpt = SecurityUtil.getCurrentUserOptional();
        if (currentUserOpt.isPresent()) {
            User currentUser = currentUserOpt.get();
            if (!currentUser.getRole().equals("ADMIN") && !currentUser.getRole().equals("SALES_MANAGER")) {
                effectiveUserId = currentUser.getId();
            }
        }

        List<Task> tasks = effectiveUserId != null
                ? taskRepository.findByAssignedUserId(effectiveUserId)
                : taskRepository.findAll();

        return tasks.stream()
                .map(taskMapper::taskToResponse)
                .collect(Collectors.toList());
    }

    private void validatePolymorphicRelation(String type, UUID id) {
        if (type == null || id == null) {
            return;
        }
        if ("CUSTOMER".equalsIgnoreCase(type)) {
            if (!customerRepository.existsById(id)) {
                throw new IllegalArgumentException("Related Customer entity not found: " + id);
            }
        } else if ("LEAD".equalsIgnoreCase(type)) {
            if (!leadRepository.existsById(id)) {
                throw new IllegalArgumentException("Related Lead entity not found: " + id);
            }
        } else {
            throw new IllegalArgumentException("Unsupported polymorphic entity type: " + type);
        }
    }

    private void createNewRecurringTaskInstance(Task task) {
        LocalDateTime nextDueDate;
        switch (task.getRecurrenceRule().toUpperCase()) {
            case "DAILY":
                nextDueDate = task.getDueDate().plusDays(1);
                break;
            case "WEEKLY":
                nextDueDate = task.getDueDate().plusWeeks(1);
                break;
            case "MONTHLY":
                nextDueDate = task.getDueDate().plusMonths(1);
                break;
            default:
                return;
        }

        Task nextTask = new Task();
        nextTask.setTitle(task.getTitle());
        nextTask.setDescription(task.getDescription());
        nextTask.setDueDate(nextDueDate);
        nextTask.setStatus("TODO");
        nextTask.setAssignedUserId(task.getAssignedUserId());
        nextTask.setRelatedEntityType(task.getRelatedEntityType());
        nextTask.setRelatedEntityId(task.getRelatedEntityId());
        nextTask.setRecurrenceRule(task.getRecurrenceRule());

        taskRepository.save(nextTask);
    }
}
