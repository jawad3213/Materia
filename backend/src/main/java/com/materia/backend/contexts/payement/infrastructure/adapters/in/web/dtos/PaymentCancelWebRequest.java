package com.materia.backend.contexts.payement.infrastructure.adapters.in.web.dtos;

import jakarta.validation.constraints.NotBlank;

public class PaymentCancelWebRequest {

    
    @NotBlank(message = "La raison d'annulation est obligatoire")
    private String reason;


    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
