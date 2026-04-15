package com.wooSeok.devdesk.exception;

import com.wooSeok.devdesk.domain.enums.TicketStatus;

public class InvalidStatusTransitionException extends RuntimeException {
    public InvalidStatusTransitionException(TicketStatus from, TicketStatus to) {
        super("Invalid status transition from " + from + " to " + to);
    }
}
