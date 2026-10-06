package com.materia.backend.contexts.payment.domain.exceptions;

/**
 * Exception levée lorsqu'aucune facture n'est sélectionnée
 */
public class PaymentNoInvoicesException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    public PaymentNoInvoicesException(String message) {
        super(message);
    }
}
