package com.materia.backend.contexts.invoice.domain.exceptions;

/**
 * Exception levée lorsqu'une facture ne peut pas être vérifiée
 */
public class InvoiceNotVerifiableException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    public InvoiceNotVerifiableException(String invoiceId, String status) {
        super("La facture " + invoiceId + " ne peut pas être vérifiée. Statut actuel: " + status);
    }
}
