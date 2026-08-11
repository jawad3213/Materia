package com.materia.backend.contexts.payement.infrastructure.adapters.in.web.dtos;

import jakarta.validation.constraints.NotBlank;

public class PaymentCancelWebRequest {

    @NotBlank(message = "L'ID de l'utilisateur est obligatoire")
    private String userId;
    
    @NotBlank(message = "La raison d'annulation est obligatoire")
    private String reason;

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
