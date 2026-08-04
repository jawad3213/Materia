package com.materia.backend.contexts.purchaseOrder.infrastructure.adapters.in.web.dtos.purchaseOrder;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Web request DTO for cancelling a purchase order.
 */
public class PurchaseOrderCancelWebRequest {

    @NotBlank(message = "User ID is mandatory")
    private String userId;

    @NotBlank(message = "Cancellation reason is mandatory")
    @Size(max = 1000, message = "Cancellation reason must not exceed 1000 characters")
    private String reason;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
