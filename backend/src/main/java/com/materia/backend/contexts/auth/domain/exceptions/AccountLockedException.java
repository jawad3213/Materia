package com.materia.backend.contexts.auth.domain.exceptions;

public class AccountLockedException extends RuntimeException {
    public AccountLockedException() {
        super("Le compte est verrouillé. Veuillez réessayer plus tard.");
    }

    public AccountLockedException(String message) {
        super(message);
    }
}
