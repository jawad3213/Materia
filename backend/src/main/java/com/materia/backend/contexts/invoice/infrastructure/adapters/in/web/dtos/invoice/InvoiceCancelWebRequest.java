package com.materia.backend.contexts.invoice.infrastructure.adapters.in.web.dtos.invoice;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class InvoiceCancelWebRequest {

    @NotBlank(message = "Reason is mandatory")
    @Size(max = 1000, message = "Reason must not exceed 1000 characters")
    private String reason;

    @NotBlank(message = "User ID is mandatory")
    private String userId;

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
