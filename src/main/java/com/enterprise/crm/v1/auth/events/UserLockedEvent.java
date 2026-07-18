package com.enterprise.crm.v1.auth.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.UUID;

@Getter
public class UserLockedEvent extends ApplicationEvent {
    private final UUID userId;
    private final String email;

    public UserLockedEvent(Object source, UUID userId, String email) {
        super(source);
        this.userId = userId;
        this.email = email;
    }
}
