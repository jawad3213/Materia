package com.materia.backend.contexts.returnToVendor.domain.events;

import com.materia.backend.common.domain.DomainEvent;

import java.util.UUID;

public class ReturnToVendorSubmittedEvent extends DomainEvent {

    private final String returnCode;
    private final String title = "Retour soumis";
    private final String description = "📤 Retour soumis - En attente de traitement";
    private final String role = "Acheteur";
    private final String submittedBy;

    public ReturnToVendorSubmittedEvent(UUID returnToVendorId, String returnCode, String submittedBy) {
        super(returnToVendorId);
        this.returnCode = returnCode;
        this.submittedBy = submittedBy;
    }

    public String getReturnCode() {
        return returnCode;
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

    public String getSubmittedBy() {
        return submittedBy;
    }
}
