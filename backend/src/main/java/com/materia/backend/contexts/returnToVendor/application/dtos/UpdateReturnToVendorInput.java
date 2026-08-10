package com.materia.backend.contexts.returnToVendor.application.dtos;

import com.materia.backend.common.application.BaseInput;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class UpdateReturnToVendorInput extends BaseInput {

    private LocalDate returnDate;
    private String returnReason;
    private String supplierResponse;
    private String rejectionSummary;
    private String creditNoteReference;
    private String creditNoteAmount;
    private String replacementPurchaseOrderReference;
    private String replacementPurchaseOrderCode;
    private String notes;
    private String internalNotes;
    private List<ReturnToVendorLineInput> lines = new ArrayList<>();

    public LocalDate getReturnDate() { return returnDate; }
    public void setReturnDate(LocalDate returnDate) { this.returnDate = returnDate; }
    public String getReturnReason() { return returnReason; }
    public void setReturnReason(String returnReason) { this.returnReason = returnReason; }
    public String getSupplierResponse() { return supplierResponse; }
    public void setSupplierResponse(String supplierResponse) { this.supplierResponse = supplierResponse; }
    public String getRejectionSummary() { return rejectionSummary; }
    public void setRejectionSummary(String rejectionSummary) { this.rejectionSummary = rejectionSummary; }
    public String getCreditNoteReference() { return creditNoteReference; }
    public void setCreditNoteReference(String creditNoteReference) { this.creditNoteReference = creditNoteReference; }
    public String getCreditNoteAmount() { return creditNoteAmount; }
    public void setCreditNoteAmount(String creditNoteAmount) { this.creditNoteAmount = creditNoteAmount; }
    public String getReplacementPurchaseOrderReference() { return replacementPurchaseOrderReference; }
    public void setReplacementPurchaseOrderReference(String replacementPurchaseOrderReference) {
        this.replacementPurchaseOrderReference = replacementPurchaseOrderReference;
    }
    public String getReplacementPurchaseOrderCode() { return replacementPurchaseOrderCode; }
    public void setReplacementPurchaseOrderCode(String replacementPurchaseOrderCode) {
        this.replacementPurchaseOrderCode = replacementPurchaseOrderCode;
    }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public String getInternalNotes() { return internalNotes; }
    public void setInternalNotes(String internalNotes) { this.internalNotes = internalNotes; }
    public List<ReturnToVendorLineInput> getLines() { return lines; }
    public void setLines(List<ReturnToVendorLineInput> lines) {
        this.lines = lines != null ? new ArrayList<>(lines) : new ArrayList<>();
    }
}
