package com.materia.backend.contexts.invoice.domain.exceptions;

/**
 * Exception levée lorsque les données d'une facture sont invalides
 */
public class InvoiceValidationException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    private final String field;
    private final String errorMessage;
    
    public InvoiceValidationException(String field, String errorMessage) {
        super("Validation échouée pour le champ '" + field + "': " + errorMessage);
        this.field = field;
        this.errorMessage = errorMessage;
    }
    
    public String getField() { return field; }
    public String getErrorMessage() { return errorMessage; }
}
