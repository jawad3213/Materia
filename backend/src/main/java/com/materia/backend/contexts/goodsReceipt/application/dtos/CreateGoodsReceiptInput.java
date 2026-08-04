package com.materia.backend.contexts.goodsReceipt.application.dtos;

import com.materia.backend.common.application.BaseInput;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Request DTO for creating a goods receipt. */
public class CreateGoodsReceiptInput extends BaseInput {

    private String receiptCode;
    private String purchaseOrderId;
    private String purchaseOrderCode;
    private LocalDate receiptDate;
    private LocalDate expectedDeliveryDate;
    private String receivedBy;
    private String receivedByName;
    private String notes;
    private String supplierId;
    private String supplierName;
    private String discrepancyNotes;
    private List<GoodsReceiptLineInput> lines = new ArrayList<>();

    public String getReceiptCode() { return receiptCode; }
    public void setReceiptCode(String receiptCode) { this.receiptCode = receiptCode; }
    public String getPurchaseOrderId() { return purchaseOrderId; }
    public void setPurchaseOrderId(String purchaseOrderId) { this.purchaseOrderId = purchaseOrderId; }
    public String getPurchaseOrderCode() { return purchaseOrderCode; }
    public void setPurchaseOrderCode(String purchaseOrderCode) { this.purchaseOrderCode = purchaseOrderCode; }
    public LocalDate getReceiptDate() { return receiptDate; }
    public void setReceiptDate(LocalDate receiptDate) { this.receiptDate = receiptDate; }
    public LocalDate getExpectedDeliveryDate() { return expectedDeliveryDate; }
    public void setExpectedDeliveryDate(LocalDate expectedDeliveryDate) { this.expectedDeliveryDate = expectedDeliveryDate; }
    public String getReceivedBy() { return receivedBy; }
    public void setReceivedBy(String receivedBy) { this.receivedBy = receivedBy; }
    public String getReceivedByName() { return receivedByName; }
    public void setReceivedByName(String receivedByName) { this.receivedByName = receivedByName; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public String getSupplierId() { return supplierId; }
    public void setSupplierId(String supplierId) { this.supplierId = supplierId; }
    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }
    public String getDiscrepancyNotes() { return discrepancyNotes; }
    public void setDiscrepancyNotes(String discrepancyNotes) { this.discrepancyNotes = discrepancyNotes; }
    public List<GoodsReceiptLineInput> getLines() { return lines; }
    public void setLines(List<GoodsReceiptLineInput> lines) {
        this.lines = lines != null ? new ArrayList<>(lines) : new ArrayList<>();
    }
}
