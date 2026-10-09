package com.materia.backend.contexts.invoice.domain.exceptions;

/**
 * Exception levée lorsque la date d'échéance est invalide
 */
public class InvoiceDueDateException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    public InvoiceDueDateException(String message) {
        super("Invalid due date: " + message);
    }
}
