package com.materia.backend.contexts.returnToVendor.infrastructure.adapters.in.web.dtos.returnToVendor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ReturnToVendorWebResponse {

    private UUID id;
    private String returnCode;
    private String goodsReceiptId;
    private String goodsReceiptCode;
    private String purchaseOrderId;
    private String purchaseOrderCode;
    private String supplierId;
    private String supplierName;
    private String supplierCode;
    private String status;
    private String resolutionType;
    private LocalDate returnDate;
    private LocalDate resolutionDate;
    private String returnReason;
    private String supplierResponse;
    private String rejectionSummary;
    private String creditNoteReference;
    private String creditNoteAmount;
    private String replacementPurchaseOrderReference;
    private String replacementPurchaseOrderCode;
    private String notes;
    private String internalNotes;
    private String createdBy;
    private LocalDateTime createdAt;
    private String updatedBy;
    private LocalDateTime updatedAt;
    private List<ReturnToVendorLineWebResponse> lines = new ArrayList<>();

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getReturnCode() { return returnCode; }
    public void setReturnCode(String returnCode) { this.returnCode = returnCode; }
    public String getGoodsReceiptId() { return goodsReceiptId; }
    public void setGoodsReceiptId(String goodsReceiptId) { this.goodsReceiptId = goodsReceiptId; }
    public String getGoodsReceiptCode() { return goodsReceiptCode; }
    public void setGoodsReceiptCode(String goodsReceiptCode) { this.goodsReceiptCode = goodsReceiptCode; }
    public String getPurchaseOrderId() { return purchaseOrderId; }
    public void setPurchaseOrderId(String purchaseOrderId) { this.purchaseOrderId = purchaseOrderId; }
    public String getPurchaseOrderCode() { return purchaseOrderCode; }
    public void setPurchaseOrderCode(String purchaseOrderCode) { this.purchaseOrderCode = purchaseOrderCode; }
    public String getSupplierId() { return supplierId; }
    public void setSupplierId(String supplierId) { this.supplierId = supplierId; }
    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }
    public String getSupplierCode() { return supplierCode; }
    public void setSupplierCode(String supplierCode) { this.supplierCode = supplierCode; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getResolutionType() { return resolutionType; }
    public void setResolutionType(String resolutionType) { this.resolutionType = resolutionType; }
    public LocalDate getReturnDate() { return returnDate; }
    public void setReturnDate(LocalDate returnDate) { this.returnDate = returnDate; }
    public LocalDate getResolutionDate() { return resolutionDate; }
    public void setResolutionDate(LocalDate resolutionDate) { this.resolutionDate = resolutionDate; }
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
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public List<ReturnToVendorLineWebResponse> getLines() { return lines; }
    public void setLines(List<ReturnToVendorLineWebResponse> lines) {
        this.lines = lines != null ? new ArrayList<>(lines) : new ArrayList<>();
    }
}
