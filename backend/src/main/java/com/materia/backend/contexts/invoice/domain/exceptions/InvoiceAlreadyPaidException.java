package com.materia.backend.contexts.invoice.domain.exceptions;

/**
 * Exception levée lorsqu'une facture est déjà payée
 */
public class InvoiceAlreadyPaidException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    public InvoiceAlreadyPaidException(String invoiceId) {
        super("La facture " + invoiceId + " a déjà été payée");
    }
}
