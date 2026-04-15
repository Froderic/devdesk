package com.wooSeok.devdesk.domain.enums;

import java.util.Map;
import java.util.Set;

public enum TicketStatus {
    OPEN, IN_PROGRESS, IN_REVIEW, RESOLVED, CLOSED;

    private static final Map<TicketStatus, Set<TicketStatus>> ALLOWED_TRANSITIONS = Map.of(
            OPEN,        Set.of(IN_PROGRESS),
            IN_PROGRESS, Set.of(IN_REVIEW, OPEN),
            IN_REVIEW,   Set.of(RESOLVED, IN_PROGRESS),
            RESOLVED,    Set.of(CLOSED, IN_PROGRESS),
            CLOSED,      Set.of()
    );

    public boolean canTransitionTo(TicketStatus next) {
        return ALLOWED_TRANSITIONS.get(this).contains(next);
    }
}