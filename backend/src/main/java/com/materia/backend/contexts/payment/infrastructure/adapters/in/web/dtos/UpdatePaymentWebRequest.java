package com.materia.backend.contexts.payment.infrastructure.adapters.in.web.dtos;

import jakarta.validation.constraints.NotBlank;

public class UpdatePaymentWebRequest {

    private String notes;
    private String internalNotes;
    

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getInternalNotes() { return internalNotes; }
    public void setInternalNotes(String internalNotes) { this.internalNotes = internalNotes; }

}
