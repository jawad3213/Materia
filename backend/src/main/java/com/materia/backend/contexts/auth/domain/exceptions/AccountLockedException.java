package com.materia.backend.contexts.auth.domain.exceptions;

public class AccountLockedException extends RuntimeException {
    public AccountLockedException() {
        super("The account is locked. Please try again later.");
    }

    public AccountLockedException(String message) {
        super(message);
    }
}
