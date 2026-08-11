package com.materia.backend.contexts.invoice.domain.exceptions;

/**
 * Exception levée lorsqu'une ligne de facture est invalide
 */
public class InvoiceLineValidationException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    private final int lineNumber;
    
    public InvoiceLineValidationException(int lineNumber, String message) {
        super("Ligne " + lineNumber + " invalide: " + message);
        this.lineNumber = lineNumber;
    }
    
    public int getLineNumber() { return lineNumber; }
}
