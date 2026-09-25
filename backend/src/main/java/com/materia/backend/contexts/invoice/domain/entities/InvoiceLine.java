package com.materia.backend.contexts.invoice.domain.entities;

import com.materia.backend.common.domain.BaseEntity;
import com.materia.backend.common.domain.valueObjects.Money;

import java.time.LocalDateTime;
import java.util.UUID;


public class InvoiceLine extends BaseEntity {
    
    // ============================================================
    // ATTRIBUTS
    // ============================================================
    
    private Integer lineNumber;
    
    // ---- LIEN AVEC LA COMMANDE ET LA RÉCEPTION ----
    private String purchaseOrderLineId;
    private String goodsReceiptLineId;
    
    // ---- MATÉRIAU ----
    private String materialCode;
    private String materialName;
    private String unitOfMeasure;
    
    // ---- QUANTITÉS ----
    private Integer quantityOrdered;
    private Integer quantityReceived;
    private Integer quantityInvoiced;
    private Integer quantityDiscrepancy;
    
    // ---- PRIX ----
    private Money unitPrice;
    private Money lineTotal;
    private Money taxAmount;
    private Money lineTotalWithTax;
    private String currencyCode;
    
    // ---- ÉCARTS ----
    private boolean hasQuantityDiscrepancy;
    private String discrepancyNotes;
    
    // ---- DIVERS ----
    private String notes;
    
    // ============================================================
    // CONSTRUCTEURS
    // ============================================================
    
    public InvoiceLine() {
        super();
    }
    
    public InvoiceLine(Builder builder) {
        super();
        this.id = builder.id;
        this.lineNumber = builder.lineNumber;
        this.purchaseOrderLineId = builder.purchaseOrderLineId;
        this.goodsReceiptLineId = builder.goodsReceiptLineId;
        this.materialCode = builder.materialCode;
        this.materialName = builder.materialName;
        this.unitOfMeasure = builder.unitOfMeasure;
        this.quantityOrdered = builder.quantityOrdered;
        this.quantityReceived = builder.quantityReceived;
        this.quantityInvoiced = builder.quantityInvoiced;
        this.quantityDiscrepancy = builder.quantityDiscrepancy;
        this.unitPrice = builder.unitPrice;
        this.lineTotal = builder.lineTotal;
        this.taxAmount = builder.taxAmount;
        this.lineTotalWithTax = builder.lineTotalWithTax;
        this.currencyCode = builder.currencyCode;
        this.hasQuantityDiscrepancy = builder.hasQuantityDiscrepancy;
        this.discrepancyNotes = builder.discrepancyNotes;
        this.notes = builder.notes;
        
        if (builder.createdAt != null) this.setCreatedAt(builder.createdAt);
        if (builder.updatedAt != null) this.setUpdatedAt(builder.updatedAt);
        if (builder.createdBy != null) {
            this.setCreatedBy(builder.createdBy);
            this.setUpdatedBy(builder.createdBy);
        }
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
        private String goodsReceiptLineId;
        private String materialCode;
        private String materialName;
        private String unitOfMeasure;
        private Integer quantityOrdered;
        private Integer quantityReceived;
        private Integer quantityInvoiced;
        private Integer quantityDiscrepancy;
        private Money unitPrice;
        private Money lineTotal;
        private Money taxAmount;
        private Money lineTotalWithTax;
        private String currencyCode;
        private boolean hasQuantityDiscrepancy;
        private String discrepancyNotes;
        private String notes;
        private String createdBy;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        
        public Builder id(UUID id) { this.id = id; return this; }
        public Builder lineNumber(Integer lineNumber) { this.lineNumber = lineNumber; return this; }
        public Builder purchaseOrderLineId(String purchaseOrderLineId) { this.purchaseOrderLineId = purchaseOrderLineId; return this; }
        public Builder goodsReceiptLineId(String goodsReceiptLineId) { this.goodsReceiptLineId = goodsReceiptLineId; return this; }
        public Builder materialCode(String materialCode) { this.materialCode = materialCode; return this; }
        public Builder materialName(String materialName) { this.materialName = materialName; return this; }
        public Builder unitOfMeasure(String unitOfMeasure) { this.unitOfMeasure = unitOfMeasure; return this; }
        public Builder quantityOrdered(Integer quantityOrdered) { this.quantityOrdered = quantityOrdered; return this; }
        public Builder quantityReceived(Integer quantityReceived) { this.quantityReceived = quantityReceived; return this; }
        public Builder quantityInvoiced(Integer quantityInvoiced) { this.quantityInvoiced = quantityInvoiced; return this; }
        public Builder quantityDiscrepancy(Integer quantityDiscrepancy) { this.quantityDiscrepancy = quantityDiscrepancy; return this; }
        public Builder unitPrice(Money unitPrice) { this.unitPrice = unitPrice; return this; }
        public Builder lineTotal(Money lineTotal) { this.lineTotal = lineTotal; return this; }
        public Builder taxAmount(Money taxAmount) { this.taxAmount = taxAmount; return this; }
        public Builder lineTotalWithTax(Money lineTotalWithTax) { this.lineTotalWithTax = lineTotalWithTax; return this; }
        public Builder currencyCode(String currencyCode) { this.currencyCode = currencyCode; return this; }
        public Builder hasQuantityDiscrepancy(boolean hasQuantityDiscrepancy) { this.hasQuantityDiscrepancy = hasQuantityDiscrepancy; return this; }
        public Builder discrepancyNotes(String discrepancyNotes) { this.discrepancyNotes = discrepancyNotes; return this; }
        public Builder notes(String notes) { this.notes = notes; return this; }
        public Builder createdBy(String createdBy) { this.createdBy = createdBy; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public Builder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }
        
        public InvoiceLine build() {
            validateRequiredFields();
            calculateDiscrepancies();
            calculateTotals();
            
            if (this.id == null) this.id = UUID.randomUUID();
            if (this.createdAt == null) this.createdAt = LocalDateTime.now();
            if (this.updatedAt == null) this.updatedAt = LocalDateTime.now();
            
            return new InvoiceLine(this);
        }
        
        private void validateRequiredFields() {
            if (this.materialCode == null || this.materialCode.trim().isEmpty()) {
                throw new IllegalArgumentException("Le code du matériau est obligatoire");
            }
            if (this.quantityInvoiced == null || this.quantityInvoiced <= 0) {
                throw new IllegalArgumentException("La quantité facturée doit être positive");
            }
            if (this.unitPrice == null) {
                throw new IllegalArgumentException("Le prix unitaire est obligatoire");
            }
        }
        
        private void calculateDiscrepancies() {
            if (quantityInvoiced != null && quantityReceived != null) {
                this.hasQuantityDiscrepancy = !quantityInvoiced.equals(quantityReceived);
                this.quantityDiscrepancy = quantityInvoiced - quantityReceived;
            }
        }
        
        private void calculateTotals() {
            if (unitPrice != null && quantityInvoiced != null) {
                this.lineTotal = unitPrice.multiply(quantityInvoiced);
                if (taxAmount != null) {
                    this.lineTotalWithTax = lineTotal.add(taxAmount);
                } else {
                    this.lineTotalWithTax = lineTotal;
                }
            }
        }
    }
    
    // ============================================================
    // MÉTHODES MÉTIER
    // ============================================================
    
    public boolean hasDiscrepancy() {
        return hasQuantityDiscrepancy;
    }
    
    // ============================================================
    // EQUALS & HASHCODE
    // ============================================================
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InvoiceLine that = (InvoiceLine) o;
        return id != null && id.equals(that.id);
    }
    
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
    
    @Override
    public String toString() {
        return "InvoiceLine{" +
                "id=" + id +
                ", lineNumber=" + lineNumber +
                ", materialCode='" + materialCode + '\'' +
                ", quantityInvoiced=" + quantityInvoiced +
                ", lineTotal=" + lineTotal +
                '}';
    }
    
    // ============================================================
    // GETTERS & SETTERS
    // ============================================================
    
    public Integer getLineNumber() { return lineNumber; }
    public void setLineNumber(Integer lineNumber) { this.lineNumber = lineNumber; }
    
    public String getPurchaseOrderLineId() { return purchaseOrderLineId; }
    public void setPurchaseOrderLineId(String purchaseOrderLineId) { this.purchaseOrderLineId = purchaseOrderLineId; }
    
    public String getGoodsReceiptLineId() { return goodsReceiptLineId; }
    public void setGoodsReceiptLineId(String goodsReceiptLineId) { this.goodsReceiptLineId = goodsReceiptLineId; }
    
    public String getMaterialCode() { return materialCode; }
    public void setMaterialCode(String materialCode) { this.materialCode = materialCode; }
    
    public String getMaterialName() { return materialName; }
    public void setMaterialName(String materialName) { this.materialName = materialName; }
    
    public String getUnitOfMeasure() { return unitOfMeasure; }
    public void setUnitOfMeasure(String unitOfMeasure) { this.unitOfMeasure = unitOfMeasure; }
    
    public Integer getQuantityOrdered() { return quantityOrdered; }
    public void setQuantityOrdered(Integer quantityOrdered) { this.quantityOrdered = quantityOrdered; }
    
    public Integer getQuantityReceived() { return quantityReceived; }
    public void setQuantityReceived(Integer quantityReceived) { this.quantityReceived = quantityReceived; }
    
    public Integer getQuantityInvoiced() { return quantityInvoiced; }
    public void setQuantityInvoiced(Integer quantityInvoiced) { this.quantityInvoiced = quantityInvoiced; }
    
    public Integer getQuantityDiscrepancy() { return quantityDiscrepancy; }
    public void setQuantityDiscrepancy(Integer quantityDiscrepancy) { this.quantityDiscrepancy = quantityDiscrepancy; }
    
    public Money getUnitPrice() { return unitPrice; }
    public void setUnitPrice(Money unitPrice) { this.unitPrice = unitPrice; }
    
    public Money getLineTotal() { return lineTotal; }
    public void setLineTotal(Money lineTotal) { this.lineTotal = lineTotal; }
    
    public Money getTaxAmount() { return taxAmount; }
    public void setTaxAmount(Money taxAmount) { this.taxAmount = taxAmount; }
    
    public Money getLineTotalWithTax() { return lineTotalWithTax; }
    public void setLineTotalWithTax(Money lineTotalWithTax) { this.lineTotalWithTax = lineTotalWithTax; }
    
    public String getCurrencyCode() { return currencyCode; }
    public void setCurrencyCode(String currencyCode) { this.currencyCode = currencyCode; }
    
    public boolean isHasQuantityDiscrepancy() { return hasQuantityDiscrepancy; }
    public void setHasQuantityDiscrepancy(boolean hasQuantityDiscrepancy) { this.hasQuantityDiscrepancy = hasQuantityDiscrepancy; }
    
    public String getDiscrepancyNotes() { return discrepancyNotes; }
    public void setDiscrepancyNotes(String discrepancyNotes) { this.discrepancyNotes = discrepancyNotes; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
