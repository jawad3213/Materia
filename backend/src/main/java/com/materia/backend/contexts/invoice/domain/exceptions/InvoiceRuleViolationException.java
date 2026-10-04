package com.materia.backend.contexts.invoice.domain.exceptions;

/** An invoice operation refused by a business rule (answered with 409, like purchase orders and receipts). */
public class InvoiceRuleViolationException extends RuntimeException {

    public InvoiceRuleViolationException(String message) {
        super(message);
    }
}
