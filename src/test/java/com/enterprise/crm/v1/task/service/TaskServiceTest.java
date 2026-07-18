package com.enterprise.crm.v1.task.service;

import com.enterprise.crm.v1.common.exception.DuplicateResourceException;
import com.enterprise.crm.v1.customer.entity.Customer;
import com.enterprise.crm.v1.customer.repository.CustomerRepository;
import com.enterprise.crm.v1.task.dto.CreateTaskRequest;
import com.enterprise.crm.v1.task.dto.TaskResponse;
import com.enterprise.crm.v1.task.entity.Task;
import com.enterprise.crm.v1.task.repository.TaskRepository;
import com.enterprise.crm.v1.user.entity.User;
import com.enterprise.crm.v1.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class TaskServiceTest {

    @Autowired
    private TaskService taskService;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private com.enterprise.crm.v1.user.repository.SessionLogRepository sessionLogRepository;

    @Autowired
    private com.enterprise.crm.v1.auth.repository.RefreshTokenRepository refreshTokenRepository;

    private User repA;
    private User repB;
    private Customer customer;

    @BeforeEach
    public void setup() {
        taskRepository.deleteAll();
        customerRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        sessionLogRepository.deleteAll();
        userRepository.deleteAll();
        userRepository.flush();

        repA = new User();
        repA.setEmail("repa@example.com");
        repA.setPasswordHash("HashPassword123!");
        repA.setFirstName("Rep");
        repA.setLastName("A");
        repA.setRole("SALES_EXECUTIVE");
        repA.setTokenVersion(1);
        repA = userRepository.save(repA);

        repB = new User();
        repB.setEmail("repb@example.com");
        repB.setPasswordHash("HashPassword123!");
        repB.setFirstName("Rep");
        repB.setLastName("B");
        repB.setRole("SALES_EXECUTIVE");
        repB.setTokenVersion(1);
        repB = userRepository.save(repB);

        customer = new Customer();
        customer.setCompanyName("Acme Corp");
        customer.setCompanySize("SMALL");
        customer.setCustomerStatus("ACTIVE");
        customer.setAssignedSalesRepId(repA.getId());
        customer = customerRepository.save(customer);

        userRepository.flush();
        customerRepository.flush();
    }

    private void authenticateUser(User user) {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                user, null, List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole()))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    public void testTaskCreationPolymorphicValidationFailure() {
        authenticateUser(repA);

        CreateTaskRequest request = new CreateTaskRequest(
                "Follow up", "Desc", LocalDateTime.now().plusDays(2), "TODO",
                repA.getId(), "CUSTOMER", UUID.randomUUID(), "NONE" // Random UUID doesn't exist
        );

        assertThrows(IllegalArgumentException.class, () -> {
            taskService.createTask(request);
        });
    }

    @Test
    public void testTaskOwnershipAccessDenied() {
        authenticateUser(repA);

        // Rep A creates a task
        CreateTaskRequest request = new CreateTaskRequest(
                "Call Acme", "Call CEO", LocalDateTime.now().plusDays(1), "TODO",
                repA.getId(), "CUSTOMER", customer.getId(), "NONE"
        );
        TaskResponse response = taskService.createTask(request);

        // Authenticate as Rep B and try to modify Rep A's task
        authenticateUser(repB);

        CreateTaskRequest updateReq = new CreateTaskRequest(
                "Call Acme Modified", "Call CEO", LocalDateTime.now().plusDays(1), "TODO",
                repA.getId(), "CUSTOMER", customer.getId(), "NONE"
        );

        assertThrows(AccessDeniedException.class, () -> {
            taskService.updateTask(response.getId(), updateReq);
        });
    }

    @Test
    public void testTaskRecurrenceEngineGeneratesNextTask() {
        authenticateUser(repA);

        LocalDateTime now = LocalDateTime.of(2026, 7, 18, 12, 0);

        // Create recurring task (WEEKLY)
        CreateTaskRequest request = new CreateTaskRequest(
                "Weekly Sync", "Discuss progress", now, "TODO",
                repA.getId(), "CUSTOMER", customer.getId(), "WEEKLY"
        );
        TaskResponse response = taskService.createTask(request);

        // Complete the task
        CreateTaskRequest completeRequest = new CreateTaskRequest(
                "Weekly Sync", "Discuss progress", now, "COMPLETED",
                repA.getId(), "CUSTOMER", customer.getId(), "WEEKLY"
        );
        taskService.updateTask(response.getId(), completeRequest);

        // Verify that a new task is automatically created in the database for next week
        List<Task> tasks = taskRepository.findByAssignedUserId(repA.getId());
        assertEquals(2, tasks.size());

        // Find the newly spawned instance
        Task nextTask = tasks.stream()
                .filter(t -> t.getStatus().equals("TODO"))
                .findFirst()
                .orElse(null);

        assertNotNull(nextTask);
        assertEquals("Weekly Sync", nextTask.getTitle());
        assertEquals("WEEKLY", nextTask.getRecurrenceRule());
        assertEquals(now.plusWeeks(1), nextTask.getDueDate());
    }
}
