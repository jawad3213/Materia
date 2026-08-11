package com.materia.backend.contexts.payement.domain.exceptions;

/**
 * Exception levée lorsqu'un paiement existe déjà
 */
public class PaymentAlreadyExistsException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    public PaymentAlreadyExistsException(String paymentCode) {
        super("Le paiement avec le code " + paymentCode + " existe déjà");
    }
}
