package com.materia.backend.contexts.payement.domain.exceptions;

/**
 * Exception levée lorsqu'un paiement est déjà effectué
 */
public class PaymentAlreadyCompletedException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    public PaymentAlreadyCompletedException(String paymentId) {
        super("Le paiement avec l'ID " + paymentId + " est déjà effectué");
    }
}
