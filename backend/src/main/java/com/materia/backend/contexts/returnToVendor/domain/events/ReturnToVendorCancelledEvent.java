package com.materia.backend.contexts.returnToVendor.domain.events;

import com.materia.backend.common.domain.DomainEvent;

import java.util.UUID;

public class ReturnToVendorCancelledEvent extends DomainEvent {

    private final String returnCode;
    private final String reason;
    private final String title = "Retour annulé";
    private final String description = "❌ Retour annulé";
    private final String role = "Acheteur";
    private final String cancelledBy;

    public ReturnToVendorCancelledEvent(UUID returnToVendorId, String returnCode, String reason, String cancelledBy) {
        super(returnToVendorId);
        this.returnCode = returnCode;
        this.reason = reason;
        this.cancelledBy = cancelledBy;
    }

    public String getReturnCode() {
        return returnCode;
    }

    public String getReason() {
        return reason;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getRole() {
        return role;
    }

    public String getCancelledBy() {
        return cancelledBy;
    }
}
