package com.wooSeok.devdesk.exception;

public class OptimisticLockException extends RuntimeException {
    public OptimisticLockException() {
        super("Ticket was modified by another user. Please refresh and try again.");
    }
}