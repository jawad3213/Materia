package com.materia.backend.contexts.payement.domain.exceptions;

/** A payment operation refused by a business rule (answered with 409, like invoices and purchase orders). */
public class PaymentRuleViolationException extends RuntimeException {

    public PaymentRuleViolationException(String message) {
        super(message);
    }
}
