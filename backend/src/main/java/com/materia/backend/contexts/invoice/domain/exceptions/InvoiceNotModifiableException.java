package com.materia.backend.contexts.invoice.domain.exceptions;

/**
 * Exception levée lorsqu'une facture ne peut pas être modifiée
 */
public class InvoiceNotModifiableException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    public InvoiceNotModifiableException(String invoiceId, String status) {
        super("La facture " + invoiceId + " ne peut pas être modifiée. Statut actuel: " + status);
    }
}
