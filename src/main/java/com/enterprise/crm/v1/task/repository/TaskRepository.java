package com.enterprise.crm.v1.task.repository;

import com.enterprise.crm.v1.task.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {
    List<Task> findByAssignedUserId(UUID assignedUserId);
    
    long countByDueDateBeforeAndStatusNot(LocalDateTime dueDate, String status);
    
    long countByAssignedUserIdAndDueDateBeforeAndStatusNot(UUID assignedUserId, LocalDateTime dueDate, String status);

    @Query(value = "SELECT * FROM tasks WHERE id = ?1", nativeQuery = true)
    Optional<Task> findByIdWithSoftDeleted(UUID id);
}
