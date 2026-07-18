package com.enterprise.crm.v1.auth.listeners;

import com.enterprise.crm.v1.auth.events.LoginFailureEvent;
import com.enterprise.crm.v1.auth.events.LoginSuccessEvent;
import com.enterprise.crm.v1.auth.events.PasswordChangedEvent;
import com.enterprise.crm.v1.auth.events.UserLockedEvent;
import com.enterprise.crm.v1.user.entity.SessionLog;
import com.enterprise.crm.v1.user.repository.SessionLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class SecurityEventListener {

    private final SessionLogRepository sessionLogRepository;

    @Async
    @EventListener
    public void handleLoginSuccess(LoginSuccessEvent event) {
        log.info("SecurityEvent: Successful login for user={} from IP={}", event.getEmail(), event.getIpAddress());
        
        SessionLog sessionLog = new SessionLog();
        sessionLog.setId(com.enterprise.crm.v1.common.util.UuidV7Generator.generate());
        sessionLog.setUserId(event.getUserId());
        sessionLog.setLoginTime(LocalDateTime.now());
        sessionLog.setLastActivity(LocalDateTime.now());
        sessionLog.setDeviceInfo(event.getUserAgent());
        sessionLog.setIpAddress(event.getIpAddress());
        sessionLog.setUserAgent(event.getUserAgent());
        
        sessionLogRepository.save(sessionLog);
    }

    @Async
    @EventListener
    public void handleLoginFailure(LoginFailureEvent event) {
        log.warn("SecurityEvent: Failed login attempt for user={} from IP={}", event.getEmail(), event.getIpAddress());
    }

    @Async
    @EventListener
    public void handlePasswordChanged(PasswordChangedEvent event) {
        log.info("SecurityEvent: Password changed successfully for user={}", event.getEmail());
    }

    @Async
    @EventListener
    public void handleUserLocked(UserLockedEvent event) {
        log.warn("SecurityEvent: User account locked due to too many failed attempts user={}", event.getEmail());
    }
}
