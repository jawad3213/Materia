package com.materia.backend.contexts.returnToVendor.domain.events;

import com.materia.backend.common.domain.DomainEvent;

import java.util.UUID;

public class ReturnToVendorCreatedEvent extends DomainEvent {

    private final String returnCode;
    private final String title = "Retour créé";
    private final String description = "📦 Nouveau retour fournisseur créé";
    private final String role = "Acheteur";
    private final String createdBy;

    public ReturnToVendorCreatedEvent(UUID returnToVendorId, String returnCode, String createdBy) {
        super(returnToVendorId);
        this.returnCode = returnCode;
        this.createdBy = createdBy;
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

    public String getCreatedBy() {
        return createdBy;
    }
}
