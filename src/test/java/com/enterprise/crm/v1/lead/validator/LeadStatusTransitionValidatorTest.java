package com.enterprise.crm.v1.lead.validator;

import com.enterprise.crm.v1.common.exception.InvalidLeadStatusTransitionException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class LeadStatusTransitionValidatorTest {

    private final LeadStatusTransitionValidator validator = new LeadStatusTransitionValidator();

    @Test
    public void testValidTransitions() {
        assertDoesNotThrow(() -> validator.validate("NEW", "CONTACTED"));
        assertDoesNotThrow(() -> validator.validate("NEW", "LOST"));
        assertDoesNotThrow(() -> validator.validate("CONTACTED", "QUALIFIED"));
        assertDoesNotThrow(() -> validator.validate("QUALIFIED", "PROPOSAL"));
        assertDoesNotThrow(() -> validator.validate("PROPOSAL", "NEGOTIATION"));
        assertDoesNotThrow(() -> validator.validate("NEGOTIATION", "WON"));
        assertDoesNotThrow(() -> validator.validate("LOST", "NEW"));
    }

    @Test
    public void testInvalidTransitions() {
        assertThrows(InvalidLeadStatusTransitionException.class, () -> validator.validate("NEW", "QUALIFIED"));
        assertThrows(InvalidLeadStatusTransitionException.class, () -> validator.validate("NEW", "WON"));
        assertThrows(InvalidLeadStatusTransitionException.class, () -> validator.validate("QUALIFIED", "NEW"));
        assertThrows(InvalidLeadStatusTransitionException.class, () -> validator.validate("WON", "CONTACTED"));
    }

    @Test
    public void testIdenticalStateNoOp() {
        assertDoesNotThrow(() -> validator.validate("NEW", "NEW"));
        assertDoesNotThrow(() -> validator.validate("WON", "WON"));
    }
}
