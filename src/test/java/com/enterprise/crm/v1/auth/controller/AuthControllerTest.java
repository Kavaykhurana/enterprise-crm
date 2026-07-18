package com.enterprise.crm.v1.auth.controller;

import com.enterprise.crm.v1.auth.dto.LoginRequest;
import com.enterprise.crm.v1.auth.dto.RegisterRequest;
import com.enterprise.crm.v1.user.entity.User;
import com.enterprise.crm.v1.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setup() {
        userRepository.deleteAll();
    }

    @Test
    public void testUserRegistrationSuccess() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "valid.user@example.com",
                "StrongPassword123!", // Min 12, Upper, Lower, Digit, Special
                "John",
                "Doe",
                "+1234567890",
                "http://example.com/image.png",
                "SALES_EXECUTIVE"
        );

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("User registered successfully")));

        assertTrue(userRepository.existsByEmail("valid.user@example.com"));
    }

    @Test
    public void testUserRegistrationWeakPassword() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "weak.user@example.com",
                "weak123", // Too short, no upper, no special
                "John",
                "Doe",
                null,
                null,
                "SALES_EXECUTIVE"
        );

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Validation failed")));
    }

    @Test
    public void testUserLoginSuccess() throws Exception {
        User user = new User();
        user.setEmail("login.user@example.com");
        user.setPasswordHash(passwordEncoder.encode("StrongPassword123!"));
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setRole("SALES_EXECUTIVE");
        user.setActive(true);
        userRepository.save(user);

        LoginRequest loginRequest = new LoginRequest("login.user@example.com", "StrongPassword123!");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.email", is("login.user@example.com")));
    }

    @Test
    public void testUserAccountLockoutAfterFiveAttempts() throws Exception {
        User user = new User();
        user.setEmail("lock.user@example.com");
        user.setPasswordHash(passwordEncoder.encode("StrongPassword123!"));
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setRole("SALES_EXECUTIVE");
        user.setActive(true);
        userRepository.save(user);

        LoginRequest wrongLogin = new LoginRequest("lock.user@example.com", "WrongPassword!");

        // Try 5 failed logins
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(wrongLogin)))
                    .andExpect(status().isUnauthorized());
        }

        // Verify account is locked
        User lockedUser = userRepository.findByEmail("lock.user@example.com").orElseThrow();
        assertTrue(lockedUser.isAccountLocked());
        assertTrue(lockedUser.getAccountLockedUntil().isAfter(LocalDateTime.now()));

        // Try to log in with correct credentials now - should be blocked
        LoginRequest correctLogin = new LoginRequest("lock.user@example.com", "StrongPassword123!");
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(correctLogin)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message", is("Account is locked. Try again later.")));
    }
}
