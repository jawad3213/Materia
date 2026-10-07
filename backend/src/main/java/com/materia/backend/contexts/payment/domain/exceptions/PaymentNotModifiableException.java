package com.materia.backend.contexts.payment.domain.exceptions;

/**
 * Exception levée lorsque le paiement n'est pas modifiable
 */
public class PaymentNotModifiableException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    public PaymentNotModifiableException(String paymentId, String status) {
        super("Le paiement avec l'ID " + paymentId + " n'est pas modifiable dans son statut actuel: " + status);
    }
}
