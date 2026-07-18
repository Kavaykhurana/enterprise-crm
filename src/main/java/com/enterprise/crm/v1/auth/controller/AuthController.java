package com.enterprise.crm.v1.auth.controller;

import com.enterprise.crm.v1.auth.dto.*;
import com.enterprise.crm.v1.auth.service.AuthService;
import com.enterprise.crm.v1.common.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<String>> register(@Valid @RequestBody RegisterRequest request) {
        String traceId = MDC.get("traceId");
        authService.register(request);
        return ResponseEntity.ok(ApiResponse.success("User registered successfully", traceId));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request, HttpServletRequest servletRequest) {
        String traceId = MDC.get("traceId");
        AuthResponse response = authService.login(request, servletRequest);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response, traceId));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(@Valid @RequestBody RefreshRequest request) {
        String traceId = MDC.get("traceId");
        AuthResponse response = authService.refresh(request);
        return ResponseEntity.ok(ApiResponse.success("Token refreshed successfully", response, traceId));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(@Valid @RequestBody RefreshRequest request) {
        String traceId = MDC.get("traceId");
        authService.logout(request.getRefreshToken());
        return ResponseEntity.ok(ApiResponse.success("Logout successful", traceId));
    }
}
