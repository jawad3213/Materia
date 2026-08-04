package com.materia.backend.contexts.goodsReceipt.domain.entities;


import com.materia.backend.contexts.goodsReceipt.domain.enums.QualityStatus;
import com.materia.backend.contexts.goodsReceipt.domain.exceptions.GoodsReceiptInvalidLineException;
import com.materia.backend.contexts.goodsReceipt.domain.exceptions.GoodsReceiptInvalidQuantityException;
import com.materia.backend.contexts.masterData.domain.valueObjects.Money;

import java.time.LocalDate;
import java.util.UUID;


public class GoodsReceiptLine {
    
    // ============================================================
    // ATTRIBUTS - IDENTIFICATION
    // ============================================================
    
    private UUID id;
    private Integer lineNumber;
    private String purchaseOrderLineId;
    
    // ============================================================
    // ATTRIBUTS - MATÉRIAU
    // ============================================================
    
    private String materialCode;
    private UUID materialId;
    private String materialName;
    private String unitOfMeasure;
    
    // ============================================================
    // ATTRIBUTS - QUANTITÉS
    // ============================================================
    
    private Integer quantityOrdered;
    private Integer quantityReceived;
    private Integer quantityRejected;
    private Integer quantityAccepted;
    private Integer quantityPending;
    
    // ============================================================
    // ATTRIBUTS - QUALITÉ
    // ============================================================
    
    private QualityStatus qualityStatus;
    private String qualityNotes;
    private String rejectionReason;
    
    // ============================================================
    // ATTRIBUTS - STOCK
    // ============================================================
    
    private Integer stockBefore;
    private Integer stockAfter;
    
    // ============================================================
    // ATTRIBUTS - PRIX
    // ============================================================
    
    private Money unitPrice;
    private Money lineTotal;
    
    // ============================================================
    // ATTRIBUTS - FOURNISSEUR
    // ============================================================
    
    private String supplierId;
    private String supplierName;
    
    // ============================================================
    // ATTRIBUTS - TRAÇABILITÉ
    // ============================================================
    
    private String batchNumber;
    private LocalDate expiryDate;
    private String storageLocation;
    private String notes;
    
    // ============================================================
    // CONSTRUCTEURS
    // ============================================================
    
    public GoodsReceiptLine() {
        super();
    }
    
    public GoodsReceiptLine(Builder builder) {
        this.id = builder.id;
        this.lineNumber = builder.lineNumber;
        this.purchaseOrderLineId = builder.purchaseOrderLineId;
        
        this.materialCode = builder.materialCode;
        this.materialId = builder.materialId;
        this.materialName = builder.materialName;
        this.unitOfMeasure = builder.unitOfMeasure;
        
        this.quantityOrdered = builder.quantityOrdered;
        this.quantityReceived = builder.quantityReceived;
        this.quantityRejected = builder.quantityRejected;
        this.quantityAccepted = builder.quantityAccepted;
        this.quantityPending = builder.quantityPending;
        
        this.qualityStatus = builder.qualityStatus;
        this.qualityNotes = builder.qualityNotes;
        this.rejectionReason = builder.rejectionReason;
        
        this.stockBefore = builder.stockBefore;
        this.stockAfter = builder.stockAfter;
        
        this.unitPrice = builder.unitPrice;
        this.lineTotal = builder.lineTotal;
        
        this.supplierId = builder.supplierId;
        this.supplierName = builder.supplierName;
        
        this.batchNumber = builder.batchNumber;
        this.expiryDate = builder.expiryDate;
        this.storageLocation = builder.storageLocation;
        this.notes = builder.notes;
    }
    
    // ============================================================
    // BUILDER
    // ============================================================
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
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
        
        private QualityStatus qualityStatus;
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
        
        public Builder id(UUID id) { this.id = id; return this; }
        public Builder lineNumber(Integer lineNumber) { this.lineNumber = lineNumber; return this; }
        public Builder purchaseOrderLineId(String purchaseOrderLineId) { this.purchaseOrderLineId = purchaseOrderLineId; return this; }
        
        public Builder materialCode(String materialCode) { this.materialCode = materialCode; return this; }
        public Builder materialId(UUID materialId) { this.materialId = materialId; return this; }
        public Builder materialName(String materialName) { this.materialName = materialName; return this; }
        public Builder unitOfMeasure(String unitOfMeasure) { this.unitOfMeasure = unitOfMeasure; return this; }
        
        public Builder quantityOrdered(Integer quantityOrdered) { this.quantityOrdered = quantityOrdered; return this; }
        public Builder quantityReceived(Integer quantityReceived) { this.quantityReceived = quantityReceived; return this; }
        public Builder quantityRejected(Integer quantityRejected) { this.quantityRejected = quantityRejected; return this; }
        public Builder quantityAccepted(Integer quantityAccepted) { this.quantityAccepted = quantityAccepted; return this; }
        public Builder quantityPending(Integer quantityPending) { this.quantityPending = quantityPending; return this; }
        
        public Builder qualityStatus(QualityStatus qualityStatus) { this.qualityStatus = qualityStatus; return this; }
        public Builder qualityNotes(String qualityNotes) { this.qualityNotes = qualityNotes; return this; }
        public Builder rejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; return this; }
        
        public Builder stockBefore(Integer stockBefore) { this.stockBefore = stockBefore; return this; }
        public Builder stockAfter(Integer stockAfter) { this.stockAfter = stockAfter; return this; }
        
        public Builder unitPrice(Money unitPrice) { this.unitPrice = unitPrice; return this; }
        public Builder lineTotal(Money lineTotal) { this.lineTotal = lineTotal; return this; }
        
        public Builder supplierId(String supplierId) { this.supplierId = supplierId; return this; }
        public Builder supplierName(String supplierName) { this.supplierName = supplierName; return this; }
        
        public Builder batchNumber(String batchNumber) { this.batchNumber = batchNumber; return this; }
        public Builder expiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; return this; }
        public Builder storageLocation(String storageLocation) { this.storageLocation = storageLocation; return this; }
        public Builder notes(String notes) { this.notes = notes; return this; }
        
        public GoodsReceiptLine build() {
            validateRequiredFields();
            calculateAcceptedQuantity();
            calculatePendingQuantity();
            
            if (this.id == null) {
                this.id = UUID.randomUUID();
            }
            
            return new GoodsReceiptLine(this);
        }
        
        private void validateRequiredFields() {
            if (this.materialCode == null || this.materialCode.trim().isEmpty()) {
                throw new GoodsReceiptInvalidLineException("Le code du materiau est obligatoire");
            }
            if (this.quantityReceived == null || this.quantityReceived < 0) {
                throw new GoodsReceiptInvalidQuantityException(
                    "La quantite recue doit etre positive ou nulle"
                );
            }
            if (this.quantityRejected == null || this.quantityRejected < 0) {
                throw new GoodsReceiptInvalidQuantityException(
                    "La quantite rejetee doit etre positive ou nulle"
                );
            }
            if (this.quantityRejected > this.quantityReceived) {
                throw new GoodsReceiptInvalidQuantityException(
                    "La quantite rejetee ne peut pas depasser la quantite recue"
                );
            }
            if (this.quantityOrdered != null && this.quantityReceived > this.quantityOrdered) {
                throw new GoodsReceiptInvalidQuantityException(
                    "La quantite recue ne peut pas depasser la quantite commandee"
                );
            }
        }
        
        private void calculateAcceptedQuantity() {
            if (quantityReceived != null && quantityRejected != null) {
                this.quantityAccepted = quantityReceived - quantityRejected;
            }
        }
        
        private void calculatePendingQuantity() {
            if (quantityOrdered != null && quantityReceived != null) {
                this.quantityPending = quantityOrdered - quantityReceived;
            }
        }
    }
    
    // ============================================================
    // MÉTHODES MÉTIER
    // ============================================================
    
    public void calculateAcceptedQuantity() {
        if (quantityReceived != null && quantityRejected != null) {
            this.quantityAccepted = quantityReceived - quantityRejected;
        }
    }
    
    public void calculatePendingQuantity() {
        if (quantityOrdered != null && quantityReceived != null) {
            this.quantityPending = quantityOrdered - quantityReceived;
        }
    }
    
    public boolean isAccepted() {
        return qualityStatus == QualityStatus.ACCEPTED;
    }
    
    public boolean isRejected() {
        return qualityStatus == QualityStatus.REJECTED;
    }
    
    public boolean isPending() {
        return qualityStatus == QualityStatus.UNDER_REVIEW;
    }
    
    public boolean isPartial() {
        return qualityStatus == QualityStatus.PARTIAL;
    }
    
    public boolean hasQuantityDiscrepancy() {
        return quantityReceived != null && quantityOrdered != null &&
               !quantityReceived.equals(quantityOrdered);
    }
    
    public boolean hasRejection() {
        return quantityRejected != null && quantityRejected > 0;
    }
    
    // ============================================================
    // EQUALS & HASHCODE
    // ============================================================
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GoodsReceiptLine that = (GoodsReceiptLine) o;
        return id != null && id.equals(that.id);
    }
    
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
    
    @Override
    public String toString() {
        return "GoodsReceiptLine{" +
                "id=" + id +
                ", lineNumber=" + lineNumber +
                ", materialCode='" + materialCode + '\'' +
                ", materialName='" + materialName + '\'' +
                ", quantityReceived=" + quantityReceived +
                ", quantityRejected=" + quantityRejected +
                ", qualityStatus=" + qualityStatus +
                '}';
    }
    
    // ============================================================
    // GETTERS & SETTERS
    // ============================================================
    
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
    
    public QualityStatus getQualityStatus() { return qualityStatus; }
    public void setQualityStatus(QualityStatus qualityStatus) { this.qualityStatus = qualityStatus; }
    
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
