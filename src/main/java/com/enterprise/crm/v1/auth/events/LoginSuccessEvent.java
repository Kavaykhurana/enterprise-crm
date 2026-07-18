package com.enterprise.crm.v1.auth.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.UUID;

@Getter
public class LoginSuccessEvent extends ApplicationEvent {
    private final UUID userId;
    private final String email;
    private final String ipAddress;
    private final String userAgent;

    public LoginSuccessEvent(Object source, UUID userId, String email, String ipAddress, String userAgent) {
        super(source);
        this.userId = userId;
        this.email = email;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
    }
}
