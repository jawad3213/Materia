package com.materia.backend.contexts.payment.infrastructure.adapters.in.web.dtos;

import jakarta.validation.constraints.NotBlank;

public class PaymentCancelWebRequest {

    
    @NotBlank(message = "A cancellation reason is required")
    private String reason;


    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
