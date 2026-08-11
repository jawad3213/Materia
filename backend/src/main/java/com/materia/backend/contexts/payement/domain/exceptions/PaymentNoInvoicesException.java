package com.materia.backend.contexts.payement.domain.exceptions;

/**
 * Exception levée lorsqu'aucune facture n'est sélectionnée
 */
public class PaymentNoInvoicesException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    public PaymentNoInvoicesException(String message) {
        super(message);
    }
}
