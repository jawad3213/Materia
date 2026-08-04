package com.materia.backend.contexts.goodsReceipt.application.dtos;

import com.materia.backend.common.application.BaseOutput;
import com.materia.backend.contexts.masterData.domain.valueObjects.Money;

import java.time.LocalDate;
import java.util.UUID;

/** Nested response DTO for a goods receipt line. */
public class GoodsReceiptLineOutput extends BaseOutput {

    private UUID id;
    private Integer lineNumber;
    private String purchaseOrderLineId;
    private String materialCode;
    private UUID materialId;
    private String materialName;
    private String unitOfMeasure;
    private Integer quantityOrdered;
    private Integer quantityReceived;
    private Integer quantityRejected;
    private Integer quantityAccepted;
    private Integer quantityPending;
    private String qualityStatus;
    private String qualityNotes;
    private String rejectionReason;
    private Integer stockBefore;
    private Integer stockAfter;
    private Money unitPrice;
    private Money lineTotal;
    private String supplierId;
    private String supplierName;
    private String batchNumber;
    private LocalDate expiryDate;
    private String storageLocation;
    private String notes;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public Integer getLineNumber() { return lineNumber; }
    public void setLineNumber(Integer lineNumber) { this.lineNumber = lineNumber; }
    public String getPurchaseOrderLineId() { return purchaseOrderLineId; }
    public void setPurchaseOrderLineId(String purchaseOrderLineId) { this.purchaseOrderLineId = purchaseOrderLineId; }
    public String getMaterialCode() { return materialCode; }
    public void setMaterialCode(String materialCode) { this.materialCode = materialCode; }
    public UUID getMaterialId() { return materialId; }
    public void setMaterialId(UUID materialId) { this.materialId = materialId; }
    public String getMaterialName() { return materialName; }
    public void setMaterialName(String materialName) { this.materialName = materialName; }
    public String getUnitOfMeasure() { return unitOfMeasure; }
    public void setUnitOfMeasure(String unitOfMeasure) { this.unitOfMeasure = unitOfMeasure; }
    public Integer getQuantityOrdered() { return quantityOrdered; }
    public void setQuantityOrdered(Integer quantityOrdered) { this.quantityOrdered = quantityOrdered; }
    public Integer getQuantityReceived() { return quantityReceived; }
    public void setQuantityReceived(Integer quantityReceived) { this.quantityReceived = quantityReceived; }
    public Integer getQuantityRejected() { return quantityRejected; }
    public void setQuantityRejected(Integer quantityRejected) { this.quantityRejected = quantityRejected; }
    public Integer getQuantityAccepted() { return quantityAccepted; }
    public void setQuantityAccepted(Integer quantityAccepted) { this.quantityAccepted = quantityAccepted; }
    public Integer getQuantityPending() { return quantityPending; }
    public void setQuantityPending(Integer quantityPending) { this.quantityPending = quantityPending; }
    public String getQualityStatus() { return qualityStatus; }
    public void setQualityStatus(String qualityStatus) { this.qualityStatus = qualityStatus; }
    public String getQualityNotes() { return qualityNotes; }
    public void setQualityNotes(String qualityNotes) { this.qualityNotes = qualityNotes; }
    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
    public Integer getStockBefore() { return stockBefore; }
    public void setStockBefore(Integer stockBefore) { this.stockBefore = stockBefore; }
    public Integer getStockAfter() { return stockAfter; }
    public void setStockAfter(Integer stockAfter) { this.stockAfter = stockAfter; }
    public Money getUnitPrice() { return unitPrice; }
    public void setUnitPrice(Money unitPrice) { this.unitPrice = unitPrice; }
    public Money getLineTotal() { return lineTotal; }
    public void setLineTotal(Money lineTotal) { this.lineTotal = lineTotal; }
    public String getSupplierId() { return supplierId; }
    public void setSupplierId(String supplierId) { this.supplierId = supplierId; }
    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }
    public String getBatchNumber() { return batchNumber; }
    public void setBatchNumber(String batchNumber) { this.batchNumber = batchNumber; }
    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }
    public String getStorageLocation() { return storageLocation; }
    public void setStorageLocation(String storageLocation) { this.storageLocation = storageLocation; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
