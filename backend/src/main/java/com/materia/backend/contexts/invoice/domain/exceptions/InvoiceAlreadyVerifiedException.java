package com.materia.backend.contexts.invoice.domain.exceptions;

/**
 * Exception levée lorsqu'une facture est déjà vérifiée
 */
public class InvoiceAlreadyVerifiedException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    public InvoiceAlreadyVerifiedException(String invoiceId) {
        super("La facture " + invoiceId + " a déjà été vérifiée");
    }
}
