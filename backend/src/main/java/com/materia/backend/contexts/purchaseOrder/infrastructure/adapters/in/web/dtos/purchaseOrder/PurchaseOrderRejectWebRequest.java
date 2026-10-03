package com.materia.backend.contexts.purchaseOrder.infrastructure.adapters.in.web.dtos.purchaseOrder;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Web request DTO for recording a supplier's rejection of a purchase order.
 */
public class PurchaseOrderRejectWebRequest {

    @NotBlank(message = "Rejection reason is mandatory")
    @Size(max = 500, message = "Rejection reason must not exceed 500 characters")
    private String reason;

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
