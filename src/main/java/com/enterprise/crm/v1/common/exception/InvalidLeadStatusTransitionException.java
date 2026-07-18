package com.enterprise.crm.v1.common.exception;

public class InvalidLeadStatusTransitionException extends RuntimeException {
    public InvalidLeadStatusTransitionException(String message) {
        super(message);
    }
}
