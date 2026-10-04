package com.materia.backend.contexts.returnToVendor.application.dtos;

import com.materia.backend.common.application.BaseInput;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Changes to a draft return; the goods receipt it comes from cannot change. */
public class UpdateReturnToVendorInput extends BaseInput {

    private LocalDate returnDate;
    private String returnReason;
    private String rejectionSummary;
    private String notes;
    private String internalNotes;
    private List<ReturnToVendorLineInput> lines = new ArrayList<>();

    public LocalDate getReturnDate() { return returnDate; }
    public void setReturnDate(LocalDate returnDate) { this.returnDate = returnDate; }

    public String getReturnReason() { return returnReason; }
    public void setReturnReason(String returnReason) { this.returnReason = returnReason; }

    public String getRejectionSummary() { return rejectionSummary; }
    public void setRejectionSummary(String rejectionSummary) { this.rejectionSummary = rejectionSummary; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getInternalNotes() { return internalNotes; }
    public void setInternalNotes(String internalNotes) { this.internalNotes = internalNotes; }

    public List<ReturnToVendorLineInput> getLines() { return lines; }
    public void setLines(List<ReturnToVendorLineInput> lines) {
        this.lines = lines != null ? new ArrayList<>(lines) : new ArrayList<>();
    }
}
