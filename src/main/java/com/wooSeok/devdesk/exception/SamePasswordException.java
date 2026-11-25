package com.wooSeok.devdesk.exception;

public class SamePasswordException extends RuntimeException {
    public SamePasswordException( ) {
        super("New password must be different from the current password");
    }
}
