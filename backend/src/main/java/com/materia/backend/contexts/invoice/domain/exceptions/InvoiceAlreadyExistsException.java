package com.materia.backend.contexts.invoice.domain.exceptions;

/**
 * Exception levée lorsqu'une facture existe déjà
 */
public class InvoiceAlreadyExistsException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    public InvoiceAlreadyExistsException(String reference) {
        super("An invoice already exists with the reference: " + reference);
    }
}
