package com.materia.backend.contexts.invoice.domain.exceptions;

/**
 * Exception levée lorsqu'une facture ne peut pas être payée
 */
public class InvoiceNotPayableException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    public InvoiceNotPayableException(String invoiceId, String status) {
        super("La facture " + invoiceId + " ne peut pas être payée. Statut actuel: " + status);
    }
}
