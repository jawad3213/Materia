package com.materia.backend.contexts.payement.infrastructure.adapters.in.web.dtos;

import jakarta.validation.constraints.NotBlank;

public class PaymentPrepareWebRequest {

    @NotBlank(message = "L'ID de l'utilisateur est obligatoire")
    private String userId;

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
}
