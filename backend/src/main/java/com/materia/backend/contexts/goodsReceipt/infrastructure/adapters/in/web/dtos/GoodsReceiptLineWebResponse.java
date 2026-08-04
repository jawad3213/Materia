package com.materia.backend.contexts.goodsReceipt.infrastructure.adapters.in.web.dtos;

import com.materia.backend.common.application.BaseOutput;

import java.time.LocalDate;
import java.util.UUID;

/** Web response for a goods receipt line. */
public class GoodsReceiptLineWebResponse extends BaseOutput {
    private UUID id; private Integer lineNumber; private String purchaseOrderLineId;
    private String materialCode; private UUID materialId; private String materialName; private String unitOfMeasure;
    private Integer quantityOrdered; private Integer quantityReceived; private Integer quantityRejected;
    private Integer quantityAccepted; private Integer quantityPending; private String qualityStatus;
    private String qualityNotes; private String rejectionReason; private Integer stockBefore; private Integer stockAfter;
    private String unitPrice; private String lineTotal; private String supplierId; private String supplierName;
    private String batchNumber; private LocalDate expiryDate; private String storageLocation; private String notes;
    public UUID getId() { return id; } public void setId(UUID value) { id = value; }
    public Integer getLineNumber() { return lineNumber; } public void setLineNumber(Integer value) { lineNumber = value; }
    public String getPurchaseOrderLineId() { return purchaseOrderLineId; } public void setPurchaseOrderLineId(String value) { purchaseOrderLineId = value; }
    public String getMaterialCode() { return materialCode; } public void setMaterialCode(String value) { materialCode = value; }
    public UUID getMaterialId() { return materialId; } public void setMaterialId(UUID value) { materialId = value; }
    public String getMaterialName() { return materialName; } public void setMaterialName(String value) { materialName = value; }
    public String getUnitOfMeasure() { return unitOfMeasure; } public void setUnitOfMeasure(String value) { unitOfMeasure = value; }
    public Integer getQuantityOrdered() { return quantityOrdered; } public void setQuantityOrdered(Integer value) { quantityOrdered = value; }
    public Integer getQuantityReceived() { return quantityReceived; } public void setQuantityReceived(Integer value) { quantityReceived = value; }
    public Integer getQuantityRejected() { return quantityRejected; } public void setQuantityRejected(Integer value) { quantityRejected = value; }
    public Integer getQuantityAccepted() { return quantityAccepted; } public void setQuantityAccepted(Integer value) { quantityAccepted = value; }
    public Integer getQuantityPending() { return quantityPending; } public void setQuantityPending(Integer value) { quantityPending = value; }
    public String getQualityStatus() { return qualityStatus; } public void setQualityStatus(String value) { qualityStatus = value; }
    public String getQualityNotes() { return qualityNotes; } public void setQualityNotes(String value) { qualityNotes = value; }
    public String getRejectionReason() { return rejectionReason; } public void setRejectionReason(String value) { rejectionReason = value; }
    public Integer getStockBefore() { return stockBefore; } public void setStockBefore(Integer value) { stockBefore = value; }
    public Integer getStockAfter() { return stockAfter; } public void setStockAfter(Integer value) { stockAfter = value; }
    public String getUnitPrice() { return unitPrice; } public void setUnitPrice(String value) { unitPrice = value; }
    public String getLineTotal() { return lineTotal; } public void setLineTotal(String value) { lineTotal = value; }
    public String getSupplierId() { return supplierId; } public void setSupplierId(String value) { supplierId = value; }
    public String getSupplierName() { return supplierName; } public void setSupplierName(String value) { supplierName = value; }
    public String getBatchNumber() { return batchNumber; } public void setBatchNumber(String value) { batchNumber = value; }
    public LocalDate getExpiryDate() { return expiryDate; } public void setExpiryDate(LocalDate value) { expiryDate = value; }
    public String getStorageLocation() { return storageLocation; } public void setStorageLocation(String value) { storageLocation = value; }
    public String getNotes() { return notes; } public void setNotes(String value) { notes = value; }
}
