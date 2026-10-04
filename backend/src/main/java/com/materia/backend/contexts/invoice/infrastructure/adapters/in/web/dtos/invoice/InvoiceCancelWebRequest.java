package com.materia.backend.contexts.invoice.infrastructure.adapters.in.web.dtos.invoice;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Cancels an invoice. The person cancelling is the authenticated user, not a field of the request. */
public class InvoiceCancelWebRequest {

    @NotBlank(message = "Reason is mandatory")
    @Size(max = 1000, message = "Reason must not exceed 1000 characters")
    private String reason;

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
