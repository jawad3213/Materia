package com.materia.backend.contexts.invoice.infrastructure.adapters.in.web.dtos.invoice;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

/**
 * A payment against a verified invoice. Only the amount is taken from the request: the payer is the
 * authenticated user, named from their account.
 */
public class InvoicePayWebRequest {

    @NotNull(message = "Amount is mandatory")
    @DecimalMin(value = "0.01", message = "Payment amount must be greater than zero")
    private Double amount;

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }
}
