package com.materia.backend.contexts.payement.application.dtos;

import com.materia.backend.common.application.BaseInput;

public class UpdatePaymentInput extends BaseInput {
    private String notes;
    private String internalNotes;

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getInternalNotes() { return internalNotes; }
    public void setInternalNotes(String internalNotes) { this.internalNotes = internalNotes; }
}
