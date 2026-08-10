package com.materia.backend.contexts.returnToVendor.application.dtos;

import com.materia.backend.common.application.BaseInput;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CreateReturnToVendorInput extends BaseInput {

    private String returnCode;
    private String goodsReceiptId;
    private String goodsReceiptCode;
    private String purchaseOrderId;
    private String purchaseOrderCode;
    private String supplierId;
    private String supplierName;
    private String supplierCode;
    private LocalDate returnDate;
    private String returnReason;
    private String rejectionSummary;
    private String notes;
    private String internalNotes;
    private List<ReturnToVendorLineInput> lines = new ArrayList<>();

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
