package com.materia.backend.contexts.purchaseOrder.infrastructure.adapters.in.web.dtos.purchaseOrder;

import jakarta.validation.constraints.NotBlank;

/**
 * Web request DTO for confirming purchase order receipt.
 */
public class PurchaseOrderConfirmReceiptWebRequest {

    @NotBlank(message = "Receiver ID is mandatory")
    private String receiverId;

    @NotBlank(message = "Receiver name is mandatory")
    private String receiverName;

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }
}
