package com.materia.backend.contexts.goodsReceipt.infrastructure.adapters.in.web.dtos;

import com.materia.backend.common.application.BaseOutput;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Web response for a goods receipt. */
public class GoodsReceiptWebResponse extends BaseOutput {
    private UUID id; private String receiptCode; private String purchaseOrderId; private String purchaseOrderCode;
    private String status; private LocalDate receiptDate; private LocalDate expectedDeliveryDate;
    private String receivedBy; private String receivedByName; private String notes; private String supplierId; private String supplierName;
    private Integer totalQuantityOrdered; private Integer totalQuantityReceived; private Integer totalQuantityRejected; private Integer totalQuantityAccepted;
    private boolean hasDiscrepancy; private String discrepancyNotes; private String createdBy; private LocalDateTime createdAt;
    private String updatedBy; private LocalDateTime updatedAt; private List<GoodsReceiptLineWebResponse> lines = new ArrayList<>();
    public UUID getId() { return id; } public void setId(UUID value) { id = value; }
    public String getReceiptCode() { return receiptCode; } public void setReceiptCode(String value) { receiptCode = value; }
    public String getPurchaseOrderId() { return purchaseOrderId; } public void setPurchaseOrderId(String value) { purchaseOrderId = value; }
    public String getPurchaseOrderCode() { return purchaseOrderCode; } public void setPurchaseOrderCode(String value) { purchaseOrderCode = value; }
    public String getStatus() { return status; } public void setStatus(String value) { status = value; }
    public LocalDate getReceiptDate() { return receiptDate; } public void setReceiptDate(LocalDate value) { receiptDate = value; }
    public LocalDate getExpectedDeliveryDate() { return expectedDeliveryDate; } public void setExpectedDeliveryDate(LocalDate value) { expectedDeliveryDate = value; }
    public String getReceivedBy() { return receivedBy; } public void setReceivedBy(String value) { receivedBy = value; }
    public String getReceivedByName() { return receivedByName; } public void setReceivedByName(String value) { receivedByName = value; }
    public String getNotes() { return notes; } public void setNotes(String value) { notes = value; }
    public String getSupplierId() { return supplierId; } public void setSupplierId(String value) { supplierId = value; }
    public String getSupplierName() { return supplierName; } public void setSupplierName(String value) { supplierName = value; }
    public Integer getTotalQuantityOrdered() { return totalQuantityOrdered; } public void setTotalQuantityOrdered(Integer value) { totalQuantityOrdered = value; }
    public Integer getTotalQuantityReceived() { return totalQuantityReceived; } public void setTotalQuantityReceived(Integer value) { totalQuantityReceived = value; }
    public Integer getTotalQuantityRejected() { return totalQuantityRejected; } public void setTotalQuantityRejected(Integer value) { totalQuantityRejected = value; }
    public Integer getTotalQuantityAccepted() { return totalQuantityAccepted; } public void setTotalQuantityAccepted(Integer value) { totalQuantityAccepted = value; }
    public boolean isHasDiscrepancy() { return hasDiscrepancy; } public void setHasDiscrepancy(boolean value) { hasDiscrepancy = value; }
    public String getDiscrepancyNotes() { return discrepancyNotes; } public void setDiscrepancyNotes(String value) { discrepancyNotes = value; }
    public String getCreatedBy() { return createdBy; } public void setCreatedBy(String value) { createdBy = value; }
    public LocalDateTime getCreatedAt() { return createdAt; } public void setCreatedAt(LocalDateTime value) { createdAt = value; }
    public String getUpdatedBy() { return updatedBy; } public void setUpdatedBy(String value) { updatedBy = value; }
    public LocalDateTime getUpdatedAt() { return updatedAt; } public void setUpdatedAt(LocalDateTime value) { updatedAt = value; }
    public List<GoodsReceiptLineWebResponse> getLines() { return lines; }
    public void setLines(List<GoodsReceiptLineWebResponse> value) { lines = value != null ? new ArrayList<>(value) : new ArrayList<>(); }
}
