package com.materia.backend.contexts.purchaseOrder.infrastructure.adapters.in.web.dtos.purchaseOrder;

import jakarta.validation.constraints.NotBlank;

/**
 * Web request DTO for assigning a receiver to a purchase order.
 */
public class PurchaseOrderAssignReceiverWebRequest {

    private String userId;

    private String userName;

    @NotBlank(message = "Assigned user ID is mandatory")
    private String assignedUserId;

    /** Ignored by the server, which takes the name from the receiver's account. */
    private String assignedUserName;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getAssignedUserId() {
        return assignedUserId;
    }

    public void setAssignedUserId(String assignedUserId) {
        this.assignedUserId = assignedUserId;
    }

    public String getAssignedUserName() {
        return assignedUserName;
    }

    public void setAssignedUserName(String assignedUserName) {
        this.assignedUserName = assignedUserName;
    }
}
