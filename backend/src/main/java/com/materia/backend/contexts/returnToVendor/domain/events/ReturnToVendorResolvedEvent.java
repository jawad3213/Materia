package com.materia.backend.contexts.returnToVendor.domain.events;

import com.materia.backend.common.domain.DomainEvent;
import com.materia.backend.contexts.returnToVendor.domain.enums.ResolutionType;

import java.util.UUID;

public class ReturnToVendorResolvedEvent extends DomainEvent {

    private final String returnCode;
    private final ResolutionType resolutionType;
    private final String title = "Retour résolu";
    private final String description = "✅ Retour résolu";
    private final String role = "Acheteur";
    private final String resolvedBy;

    public ReturnToVendorResolvedEvent(UUID returnToVendorId, String returnCode, ResolutionType resolutionType, String resolvedBy) {
        super(returnToVendorId);
        this.returnCode = returnCode;
        this.resolutionType = resolutionType;
        this.resolvedBy = resolvedBy;
    }

    public String getReturnCode() {
        return returnCode;
    }

    public ResolutionType getResolutionType() {
        return resolutionType;
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

    public String getResolvedBy() {
        return resolvedBy;
    }
}
