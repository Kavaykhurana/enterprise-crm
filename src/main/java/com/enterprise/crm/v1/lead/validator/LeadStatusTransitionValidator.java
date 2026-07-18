package com.enterprise.crm.v1.lead.validator;

import com.enterprise.crm.v1.common.exception.InvalidLeadStatusTransitionException;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Component
public class LeadStatusTransitionValidator {
    private static final Map<String, Set<String>> RULES = Map.of(
        "NEW", Set.of("CONTACTED", "LOST"),
        "CONTACTED", Set.of("QUALIFIED", "CONTACTED", "LOST"),
        "QUALIFIED", Set.of("PROPOSAL", "LOST"),
        "PROPOSAL", Set.of("NEGOTIATION", "LOST"),
        "NEGOTIATION", Set.of("WON", "LOST"),
        "WON", Set.of(), // Terminal state
        "LOST", Set.of("NEW", "CONTACTED") // Allow reopening
    );

    public void validate(String current, String next) {
        if (current.equals(next)) {
            return;
        }
        Set<String> allowed = RULES.get(current);
        if (allowed == null || !allowed.contains(next)) {
            throw new InvalidLeadStatusTransitionException("Illegal status transition from " + current + " to " + next);
        }
    }
}
