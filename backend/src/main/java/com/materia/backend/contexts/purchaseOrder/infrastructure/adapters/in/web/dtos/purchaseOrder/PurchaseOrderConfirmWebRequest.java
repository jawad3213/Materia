package com.materia.backend.contexts.purchaseOrder.infrastructure.adapters.in.web.dtos.purchaseOrder;

import jakarta.validation.constraints.NotBlank;

/**
 * Web request DTO for confirming a purchase order.
 */
public class PurchaseOrderConfirmWebRequest {

    @NotBlank(message = "User ID is mandatory")
    private String userId;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
