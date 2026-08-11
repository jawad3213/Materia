package com.materia.backend.contexts.payement.infrastructure.adapters.in.web.dtos;

import jakarta.validation.constraints.NotBlank;

public class UpdatePaymentWebRequest {

    private String notes;
    private String internalNotes;
    
    @NotBlank(message = "L'ID de l'utilisateur est obligatoire")
    private String userId;

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getInternalNotes() { return internalNotes; }
    public void setInternalNotes(String internalNotes) { this.internalNotes = internalNotes; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
}
