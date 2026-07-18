package com.enterprise.crm.v1.auth.service;

import com.enterprise.crm.v1.auth.dto.*;
import com.enterprise.crm.v1.auth.entity.RefreshToken;
import com.enterprise.crm.v1.auth.events.LoginFailureEvent;
import com.enterprise.crm.v1.auth.events.LoginSuccessEvent;
import com.enterprise.crm.v1.auth.events.UserLockedEvent;
import com.enterprise.crm.v1.auth.mapper.AuthMapper;
import com.enterprise.crm.v1.auth.repository.RefreshTokenRepository;
import com.enterprise.crm.v1.user.entity.User;
import com.enterprise.crm.v1.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthMapper authMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        }

        User user = authMapper.registerRequestToUser(request);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setActive(true);
        user.setTokenVersion(1);
        user.setFailedLoginAttempts(0);
        user.setAccountLocked(false);
        userRepository.save(user);
    }

    @Transactional
    public AuthResponse login(LoginRequest request, HttpServletRequest servletRequest) {
        String ipAddress = servletRequest.getRemoteAddr();
        String userAgent = servletRequest.getHeader("User-Agent");

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    eventPublisher.publishEvent(new LoginFailureEvent(this, request.getEmail(), ipAddress, userAgent));
                    return new BadCredentialsException("Invalid email or password");
                });

        if (user.isAccountLocked()) {
            if (user.getAccountLockedUntil() != null && user.getAccountLockedUntil().isBefore(LocalDateTime.now())) {
                user.setAccountLocked(false);
                user.setFailedLoginAttempts(0);
                user.setAccountLockedUntil(null);
                userRepository.save(user);
            } else {
                throw new LockedException("Account is locked. Try again later.");
            }
        }

        if (!user.isActive()) {
            throw new DisabledException("User account is disabled");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);
            if (user.getFailedLoginAttempts() >= 5) {
                user.setAccountLocked(true);
                user.setAccountLockedUntil(LocalDateTime.now().plusMinutes(15));
                eventPublisher.publishEvent(new UserLockedEvent(this, user.getId(), user.getEmail()));
            }
            userRepository.save(user);
            eventPublisher.publishEvent(new LoginFailureEvent(this, request.getEmail(), ipAddress, userAgent));
            throw new BadCredentialsException("Invalid email or password");
        }

        user.setFailedLoginAttempts(0);
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        // Generate Access and Refresh Tokens
        String accessToken = jwtService.generateAccessToken(user.getId(), user.getEmail(), user.getRole(), user.getTokenVersion());
        String rawRefreshToken = UUID.randomUUID().toString();
        String tokenHash = jwtService.hashToken(rawRefreshToken);

        refreshTokenRepository.deleteByUserId(user.getId()); // Invalidate older tokens

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setId(com.enterprise.crm.v1.common.util.UuidV7Generator.generate());
        refreshToken.setUserId(user.getId());
        refreshToken.setTokenHash(tokenHash);
        refreshToken.setExpiryDate(LocalDateTime.now().plusDays(7));
        refreshToken.setRevoked(false);
        refreshTokenRepository.save(refreshToken);

        eventPublisher.publishEvent(new LoginSuccessEvent(this, user.getId(), user.getEmail(), ipAddress, userAgent));

        AuthResponse response = authMapper.userToAuthResponse(user);
        response.setAccessToken(accessToken);
        response.setRefreshToken(rawRefreshToken);
        return response;
    }

    @Transactional
    public AuthResponse refresh(RefreshRequest request) {
        String tokenHash = jwtService.hashToken(request.getRefreshToken());
        RefreshToken token = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new BadCredentialsException("Invalid refresh token"));

        if (token.isRevoked() || token.getExpiryDate().isBefore(LocalDateTime.now())) {
            if (token.isRevoked()) {
                // Potential reuse/theft breach: Revoke all tokens for this user
                User user = userRepository.findById(token.getUserId()).orElse(null);
                if (user != null) {
                    user.setTokenVersion(user.getTokenVersion() + 1);
                    userRepository.save(user);
                }
                refreshTokenRepository.deleteByUserId(token.getUserId());
            }
            throw new BadCredentialsException("Invalid or expired refresh token");
        }

        // Revoke the old token (RTR)
        token.setRevoked(true);
        refreshTokenRepository.save(token);

        User user = userRepository.findById(token.getUserId())
                .orElseThrow(() -> new BadCredentialsException("User not found"));

        if (!user.isActive()) {
            throw new DisabledException("User account is disabled");
        }

        // Issue new access and refresh tokens
        String newAccessToken = jwtService.generateAccessToken(user.getId(), user.getEmail(), user.getRole(), user.getTokenVersion());
        String newRawRefreshToken = UUID.randomUUID().toString();
        String newHash = jwtService.hashToken(newRawRefreshToken);

        RefreshToken newRefreshToken = new RefreshToken();
        newRefreshToken.setId(com.enterprise.crm.v1.common.util.UuidV7Generator.generate());
        newRefreshToken.setUserId(user.getId());
        newRefreshToken.setTokenHash(newHash);
        newRefreshToken.setExpiryDate(LocalDateTime.now().plusDays(7));
        newRefreshToken.setRevoked(false);
        refreshTokenRepository.save(newRefreshToken);

        AuthResponse response = authMapper.userToAuthResponse(user);
        response.setAccessToken(newAccessToken);
        response.setRefreshToken(newRawRefreshToken);
        return response;
    }

    @Transactional
    public void logout(String rawRefreshToken) {
        String tokenHash = jwtService.hashToken(rawRefreshToken);
        refreshTokenRepository.findByTokenHash(tokenHash).ifPresent(token -> {
            token.setRevoked(true);
            refreshTokenRepository.save(token);
        });
    }
}
