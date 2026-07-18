package com.enterprise.crm.v1.dashboard.service;

import com.enterprise.crm.v1.customer.entity.Customer;
import com.enterprise.crm.v1.customer.repository.CustomerRepository;
import com.enterprise.crm.v1.dashboard.dto.DashboardMetricsResponse;
import com.enterprise.crm.v1.lead.entity.Lead;
import com.enterprise.crm.v1.lead.repository.LeadRepository;
import com.enterprise.crm.v1.opportunity.entity.Opportunity;
import com.enterprise.crm.v1.opportunity.repository.OpportunityRepository;
import com.enterprise.crm.v1.task.entity.Task;
import com.enterprise.crm.v1.task.repository.TaskRepository;
import com.enterprise.crm.v1.user.entity.User;
import com.enterprise.crm.v1.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class DashboardServiceTest {

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LeadRepository leadRepository;

    @Autowired
    private OpportunityRepository opportunityRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private com.enterprise.crm.v1.user.repository.SessionLogRepository sessionLogRepository;

    @Autowired
    private com.enterprise.crm.v1.auth.repository.RefreshTokenRepository refreshTokenRepository;

    private User repA;
    private User repB;
    private User manager;

    @BeforeEach
    public void setup() {
        taskRepository.deleteAll();
        opportunityRepository.deleteAll();
        customerRepository.deleteAll();
        leadRepository.deleteAll();
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

        manager = new User();
        manager.setEmail("mgr@example.com");
        manager.setPasswordHash("HashPassword123!");
        manager.setFirstName("Mgr");
        manager.setLastName("User");
        manager.setRole("SALES_MANAGER");
        manager.setTokenVersion(1);
        manager = userRepository.save(manager);

        userRepository.flush();
    }

    private void authenticateUser(User user) {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                user, null, List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole()))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    public void testPersonalizedDashboardForSalesExecutive() {
        // Setup data for Rep A
        // 1. Lead: 1 converted, 1 total
        Lead leadA = new Lead();
        leadA.setFirstName("LeadA");
        leadA.setLastName("User");
        leadA.setEmail("leada@example.com");
        leadA.setSource("WEB");
        leadA.setPriority("HIGH");
        leadA.setStatus("WON");
        leadA.setAssignedSalesRepId(repA.getId());
        leadA.setConvertedCustomerId(UUID.randomUUID());
        leadRepository.save(leadA);

        // 2. Opportunity: 1 Won (Expected 200,000, Stage WON)
        Opportunity oppA = new Opportunity();
        oppA.setCustomerId(UUID.randomUUID());
        oppA.setAssignedSalesRepId(repA.getId());
        oppA.setName("Opp A");
        oppA.setExpectedRevenue(new BigDecimal("200000.00"));
        oppA.setProbability(100);
        oppA.setStage("WON");
        oppA.setExpectedCloseDate(LocalDate.now());
        opportunityRepository.save(oppA);

        // 3. Overdue Task: 1 overdue (due yesterday, status TODO)
        Task taskA = new Task();
        taskA.setTitle("Task A");
        taskA.setDueDate(LocalDateTime.now().minusDays(1));
        taskA.setStatus("TODO");
        taskA.setAssignedUserId(repA.getId());
        taskRepository.save(taskA);


        // Setup data for Rep B
        // 1. Lead: 1 active, 0 converted
        Lead leadB = new Lead();
        leadB.setFirstName("LeadB");
        leadB.setLastName("User");
        leadB.setEmail("leadb@example.com");
        leadB.setSource("WEB");
        leadB.setPriority("MEDIUM");
        leadB.setStatus("NEW");
        leadB.setAssignedSalesRepId(repB.getId());
        leadRepository.save(leadB);

        // 2. Opportunity: 1 open (Expected 100,000, 50% probability, stage PROPOSAL)
        Opportunity oppB = new Opportunity();
        oppB.setCustomerId(UUID.randomUUID());
        oppB.setAssignedSalesRepId(repB.getId());
        oppB.setName("Opp B");
        oppB.setExpectedRevenue(new BigDecimal("100000.00"));
        oppB.setProbability(50);
        oppB.setStage("PROPOSAL");
        oppB.setExpectedCloseDate(LocalDate.now().plusMonths(1));
        opportunityRepository.save(oppB);

        opportunityRepository.flush();
        leadRepository.flush();
        taskRepository.flush();

        // Query metrics for Rep A
        authenticateUser(repA);
        DashboardMetricsResponse repAMetrics = dashboardService.getMetrics();

        // Won / Closed = 1 / 1 = 100% (since opp A is Won, and no other reps' opps are counted)
        assertEquals(100.0, repAMetrics.getWinRate());
        // Converted / Total = 1 / 1 = 100%
        assertEquals(100.0, repAMetrics.getLeadConversionRate());
        // Pipeline weighted value: opp A is WON (excluded), so 0
        assertEquals(0, repAMetrics.getPipelineWeightedValue().compareTo(BigDecimal.ZERO));
        // Overdue tasks = 1
        assertEquals(1, repAMetrics.getOverdueTasksCount());


        // Query metrics for Manager (Global View)
        authenticateUser(manager);
        DashboardMetricsResponse globalMetrics = dashboardService.getMetrics();

        // Won / Closed = 1 / 1 = 100% (Opp A is closed-WON, Opp B is open-PROPOSAL)
        assertEquals(100.0, globalMetrics.getWinRate());
        // Converted / Total = 1 / 2 = 50% (Lead A converted, Lead B new)
        assertEquals(50.0, globalMetrics.getLeadConversionRate());
        // Pipeline weighted value: Opp B is open (100,000 * 50% = 50,000)
        assertEquals(0, globalMetrics.getPipelineWeightedValue().compareTo(new BigDecimal("50000.00")));
        // Overdue tasks = 1 (Rep A task is overdue, Rep B task doesn't exist)
        assertEquals(1, globalMetrics.getOverdueTasksCount());
    }
}
