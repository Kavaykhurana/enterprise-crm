package com.enterprise.crm.v1.task.mapper;

import com.enterprise.crm.v1.task.dto.*;
import com.enterprise.crm.v1.task.entity.Task;
import com.enterprise.crm.v1.task.entity.TaskComment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface TaskMapper {
    TaskMapper INSTANCE = Mappers.getMapper(TaskMapper.class);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    Task createRequestToTask(CreateTaskRequest request);

    TaskResponse taskToResponse(Task task);

    TaskCommentResponse commentToResponse(TaskComment comment);
}
