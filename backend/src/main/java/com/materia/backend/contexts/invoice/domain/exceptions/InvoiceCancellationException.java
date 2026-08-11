package com.materia.backend.contexts.invoice.domain.exceptions;

/**
 * Exception levée lorsqu'une facture ne peut pas être annulée
 */
public class InvoiceCancellationException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    public InvoiceCancellationException(String invoiceId, String reason) {
        super("Impossible d'annuler la facture " + invoiceId + ": " + reason);
    }
}
