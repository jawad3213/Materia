package com.materia.backend.contexts.invoice.infrastructure.adapters.in.web.dtos.invoice;

import jakarta.validation.constraints.NotBlank;

public class InvoiceSubmitWebRequest {

    @NotBlank(message = "User ID is mandatory")
    private String userId;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
