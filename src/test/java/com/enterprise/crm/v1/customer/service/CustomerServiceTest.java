package com.enterprise.crm.v1.customer.service;

import com.enterprise.crm.v1.common.auth.SecurityUtil;
import com.enterprise.crm.v1.common.exception.DuplicateResourceException;
import com.enterprise.crm.v1.customer.dto.CreateCustomerRequest;
import com.enterprise.crm.v1.customer.dto.CustomerResponse;
import com.enterprise.crm.v1.customer.entity.Customer;
import com.enterprise.crm.v1.customer.repository.CustomerRepository;
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

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class CustomerServiceTest {

    @Autowired
    private CustomerService customerService;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private UserRepository userRepository;

    private User adminUser;

    @BeforeEach
    public void setup() {
        customerRepository.deleteAll();
        userRepository.deleteAll();

        adminUser = new User();
        adminUser.setEmail("admin@example.com");
        adminUser.setPasswordHash("HashPassword123!");
        adminUser.setFirstName("Admin");
        adminUser.setLastName("User");
        adminUser.setRole("ADMIN");
        adminUser.setTokenVersion(1);
        adminUser = userRepository.save(adminUser);
    }

    private void authenticateUser(User user) {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                user, null, List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole()))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    @Transactional
    public void testCustomerCreationDuplicateTaxIdFails() {
        authenticateUser(adminUser);

        CreateCustomerRequest c1 = new CreateCustomerRequest(
                "Acme Inc", "ENTERPRISE", "ACTIVE", adminUser.getId(), "TAX-111", "Desc"
        );
        customerService.createCustomer(c1);

        CreateCustomerRequest c2 = new CreateCustomerRequest(
                "Beta Inc", "SMALL", "ACTIVE", adminUser.getId(), "TAX-111", "Desc"
        );

        assertThrows(DuplicateResourceException.class, () -> {
            customerService.createCustomer(c2);
        });
    }

    @Test
    @Transactional
    public void testCustomerRestoreUniqueConstraintConflict() {
        authenticateUser(adminUser);

        // Create c1 (active)
        CreateCustomerRequest c1 = new CreateCustomerRequest(
                "Acme Inc", "ENTERPRISE", "ACTIVE", adminUser.getId(), "TAX-999", "Desc"
        );
        customerService.createCustomer(c1);

        // Create c2 (soft deleted)
        Customer c2 = new Customer();
        c2.setCompanyName("Beta Inc");
        c2.setCompanySize("SMALL");
        c2.setCustomerStatus("ACTIVE");
        c2.setAssignedSalesRepId(adminUser.getId());
        c2.setTaxIdentifier("TAX-999");
        c2 = customerRepository.save(c2);
        customerService.deleteCustomer(c2.getId());

        // Attempting to restore c2 should fail because c1 already occupies the active "TAX-999" tax identifier
        final UUID c2Id = c2.getId();
        assertThrows(DuplicateResourceException.class, () -> {
            customerService.restoreCustomer(c2Id);
        });
    }
}
