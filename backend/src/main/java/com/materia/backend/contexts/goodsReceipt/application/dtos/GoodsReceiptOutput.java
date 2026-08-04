package com.materia.backend.contexts.goodsReceipt.application.dtos;

import com.materia.backend.common.application.BaseOutput;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Response DTO for goods receipt operations. */
public class GoodsReceiptOutput extends BaseOutput {

    private UUID id;
    private String receiptCode;
    private String purchaseOrderId;
    private String purchaseOrderCode;
    private String status;
    private LocalDate receiptDate;
    private LocalDate expectedDeliveryDate;
    private String receivedBy;
    private String receivedByName;
    private String notes;
    private String supplierId;
    private String supplierName;
    private Integer totalQuantityOrdered;
    private Integer totalQuantityReceived;
    private Integer totalQuantityRejected;
    private Integer totalQuantityAccepted;
    private boolean hasDiscrepancy;
    private String discrepancyNotes;
    private String createdBy;
    private LocalDateTime createdAt;
    private String updatedBy;
    private LocalDateTime updatedAt;
    private List<GoodsReceiptLineOutput> lines = new ArrayList<>();

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getReceiptCode() { return receiptCode; }
    public void setReceiptCode(String receiptCode) { this.receiptCode = receiptCode; }
    public String getPurchaseOrderId() { return purchaseOrderId; }
    public void setPurchaseOrderId(String purchaseOrderId) { this.purchaseOrderId = purchaseOrderId; }
    public String getPurchaseOrderCode() { return purchaseOrderCode; }
    public void setPurchaseOrderCode(String purchaseOrderCode) { this.purchaseOrderCode = purchaseOrderCode; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
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
    public Integer getTotalQuantityOrdered() { return totalQuantityOrdered; }
    public void setTotalQuantityOrdered(Integer totalQuantityOrdered) { this.totalQuantityOrdered = totalQuantityOrdered; }
    public Integer getTotalQuantityReceived() { return totalQuantityReceived; }
    public void setTotalQuantityReceived(Integer totalQuantityReceived) { this.totalQuantityReceived = totalQuantityReceived; }
    public Integer getTotalQuantityRejected() { return totalQuantityRejected; }
    public void setTotalQuantityRejected(Integer totalQuantityRejected) { this.totalQuantityRejected = totalQuantityRejected; }
    public Integer getTotalQuantityAccepted() { return totalQuantityAccepted; }
    public void setTotalQuantityAccepted(Integer totalQuantityAccepted) { this.totalQuantityAccepted = totalQuantityAccepted; }
    public boolean isHasDiscrepancy() { return hasDiscrepancy; }
    public void setHasDiscrepancy(boolean hasDiscrepancy) { this.hasDiscrepancy = hasDiscrepancy; }
    public String getDiscrepancyNotes() { return discrepancyNotes; }
    public void setDiscrepancyNotes(String discrepancyNotes) { this.discrepancyNotes = discrepancyNotes; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public List<GoodsReceiptLineOutput> getLines() { return lines; }
    public void setLines(List<GoodsReceiptLineOutput> lines) {
        this.lines = lines != null ? new ArrayList<>(lines) : new ArrayList<>();
    }
}
