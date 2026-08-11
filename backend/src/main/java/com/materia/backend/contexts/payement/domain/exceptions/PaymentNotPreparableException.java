package com.materia.backend.contexts.payement.domain.exceptions;

/**
 * Exception levée lorsque le paiement ne peut pas être préparé
 */
public class PaymentNotPreparableException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    public PaymentNotPreparableException(String paymentId, String reason) {
        super("Le paiement avec l'ID " + paymentId + " ne peut pas être préparé : " + reason);
    }
}
