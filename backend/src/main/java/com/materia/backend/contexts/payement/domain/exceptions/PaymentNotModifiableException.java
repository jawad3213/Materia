package com.materia.backend.contexts.payement.domain.exceptions;

/**
 * Exception levée lorsque le paiement n'est pas modifiable
 */
public class PaymentNotModifiableException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    public PaymentNotModifiableException(String paymentId, String status) {
        super("Le paiement avec l'ID " + paymentId + " n'est pas modifiable dans son statut actuel: " + status);
    }
}
