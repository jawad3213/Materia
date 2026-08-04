package com.materia.backend.contexts.purchaseOrder.infrastructure.adapters.in.web.dtos.purchaseOrder;

import jakarta.validation.constraints.NotBlank;

/**
 * Web request DTO for updating a purchase order delivery status.
 */
public class UpdatePurchaseOrderDeliveryStatusWebRequest {

    @NotBlank(message = "Delivery status is mandatory")
    private String deliveryStatus;

    @NotBlank(message = "User ID is mandatory")
    private String userId;

    public String getDeliveryStatus() {
        return deliveryStatus;
    }

    public void setDeliveryStatus(String deliveryStatus) {
        this.deliveryStatus = deliveryStatus;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
