package com.enterprise.crm.v1.auth.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class LoginFailureEvent extends ApplicationEvent {
    private final String email;
    private final String ipAddress;
    private final String userAgent;

    public LoginFailureEvent(Object source, String email, String ipAddress, String userAgent) {
        super(source);
        this.email = email;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
    }
}
