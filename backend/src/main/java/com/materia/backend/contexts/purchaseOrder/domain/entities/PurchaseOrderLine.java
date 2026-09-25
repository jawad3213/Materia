package com.materia.backend.contexts.purchaseOrder.domain.entities;

import com.materia.backend.contexts.purchaseOrder.domain.exceptions.PurchaseOrderInvalidLineException;
import com.materia.backend.common.domain.valueObjects.Money;
import com.materia.backend.common.domain.enums.CurrencyCode;

import java.time.LocalDate;
import java.util.UUID;

public class PurchaseOrderLine {
    
    // ============================================================
    // ATTRIBUTS - IDENTIFICATION
    // ============================================================
    
    private UUID id;
    private Integer lineNumber;
    private UUID requisitionLineId;
    
    // ============================================================
    // ATTRIBUTS - MATÉRIAU
    // ============================================================
    
    private String materialCode;
    private UUID materialId;
    private String materialName;
    private String materialDescription;
    private String unitOfMeasure;
    
    // ============================================================
    // ATTRIBUTS - QUANTITÉ COMMANDÉE (UNIQUEMENT)
    // ============================================================
    
    private Integer quantity;           
    
    
    // ============================================================
    // ATTRIBUTS - PRIX
    // ============================================================
    
    private Money unitPrice;
    private Money lineTotal;
    private String currencyCode;
    
    // ============================================================
    // ATTRIBUTS - FOURNISSEUR
    // ============================================================
    
    private UUID supplierId;
    private String supplierName;
    
    // ============================================================
    // ATTRIBUTS - DATES
    // ============================================================
    
    private LocalDate expectedDeliveryDate;
    
    // ============================================================
    // ATTRIBUTS - DIVERS
    // ============================================================
    
    private String notes;
    
    // ============================================================
    // CONSTRUCTEUR PAR DÉFAUT
    // ============================================================
    
    public PurchaseOrderLine() {
        super();
    }
    
    // ============================================================
    // CONSTRUCTEUR AVEC BUILDER
    // ============================================================
    
    public PurchaseOrderLine(Builder builder) {
        this.id = builder.id;
        this.lineNumber = builder.lineNumber;
        this.requisitionLineId = builder.requisitionLineId;
        
        this.materialCode = builder.materialCode;
        this.materialId = builder.materialId;
        this.materialName = builder.materialName;
        this.materialDescription = builder.materialDescription;
        this.unitOfMeasure = builder.unitOfMeasure;
        
        this.quantity = builder.quantity;
        
        this.unitPrice = builder.unitPrice;
        this.lineTotal = builder.lineTotal;
        this.currencyCode = builder.currencyCode;
        
        this.supplierId = builder.supplierId;
        this.supplierName = builder.supplierName;
        
        this.expectedDeliveryDate = builder.expectedDeliveryDate;
        
        this.notes = builder.notes;
    }
    
    // ============================================================
    // BUILDER PATTERN
    // ============================================================
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private UUID id;
        private Integer lineNumber;
        private UUID requisitionLineId;
        
        private String materialCode;
        private UUID materialId;
        private String materialName;
        private String materialDescription;
        private String unitOfMeasure;
        
        private Integer quantity;
        
        private Money unitPrice;
        private Money lineTotal;
        private String currencyCode;
        
        private UUID supplierId;
        private String supplierName;
        
        private LocalDate expectedDeliveryDate;
        
        private String notes;
        
        public Builder id(UUID id) {
            this.id = id;
            return this;
        }
        
        public Builder lineNumber(Integer lineNumber) {
            this.lineNumber = lineNumber;
            return this;
        }
        
        public Builder requisitionLineId(UUID requisitionLineId) {
            this.requisitionLineId = requisitionLineId;
            return this;
        }
        
        public Builder materialCode(String materialCode) {
            this.materialCode = materialCode;
            return this;
        }
        
        public Builder materialId(UUID materialId) {
            this.materialId = materialId;
            return this;
        }
        
        public Builder materialName(String materialName) {
            this.materialName = materialName;
            return this;
        }
        
        public Builder materialDescription(String materialDescription) {
            this.materialDescription = materialDescription;
            return this;
        }
        
        public Builder unitOfMeasure(String unitOfMeasure) {
            this.unitOfMeasure = unitOfMeasure;
            return this;
        }
        
        public Builder quantity(Integer quantity) {
            this.quantity = quantity;
            return this;
        }
        
        public Builder unitPrice(Money unitPrice) {
            this.unitPrice = unitPrice;
            return this;
        }
        
        public Builder lineTotal(Money lineTotal) {
            this.lineTotal = lineTotal;
            return this;
        }
        
        public Builder currencyCode(String currencyCode) {
            this.currencyCode = currencyCode;
            return this;
        }
        
        public Builder supplierId(UUID supplierId) {
            this.supplierId = supplierId;
            return this;
        }
        
        public Builder supplierName(String supplierName) {
            this.supplierName = supplierName;
            return this;
        }
        
        public Builder expectedDeliveryDate(LocalDate expectedDeliveryDate) {
            this.expectedDeliveryDate = expectedDeliveryDate;
            return this;
        }
        
        public Builder notes(String notes) {
            this.notes = notes;
            return this;
        }
        
        public PurchaseOrderLine build() {
            validateRequiredFields();
            calculateLineTotal();
            
            if (this.id == null) {
                this.id = UUID.randomUUID();
            }
            
            return new PurchaseOrderLine(this);
        }
        
        private void validateRequiredFields() {
            if (this.materialCode == null || this.materialCode.trim().isEmpty()) {
                throw new PurchaseOrderInvalidLineException("Material is required");
            }
            if (this.quantity == null || this.quantity <= 0) {
                throw new PurchaseOrderInvalidLineException("Quantity must be positive");
            }
            if (this.unitPrice == null) {
                throw new PurchaseOrderInvalidLineException("Unit price is required");
            }
            if (this.currencyCode != null && this.unitPrice != null
                    && !this.currencyCode.equalsIgnoreCase(this.unitPrice.getCurrencyCode())) {
                throw new PurchaseOrderInvalidLineException("Line currency code must match unit price currency");
            }
        }
        
        private void calculateLineTotal() {
            if (unitPrice != null && quantity != null) {
                this.lineTotal = unitPrice.multiply(quantity);
            }
        }
    }
    
    // ============================================================
    // MÉTHODES MÉTIER
    // ============================================================
    
    public void calculateLineTotal() {
        if (unitPrice != null && quantity != null) {
            this.lineTotal = unitPrice.multiply(quantity);
        }
    }
    
    // ============================================================
    // EQUALS & HASHCODE
    // ============================================================
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PurchaseOrderLine that = (PurchaseOrderLine) o;
        return id != null && id.equals(that.id);
    }
    
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
    
    @Override
    public String toString() {
        return "PurchaseOrderLine{" +
                "id=" + id +
                ", lineNumber=" + lineNumber +
                ", materialCode='" + materialCode + '\'' +
                ", materialName='" + materialName + '\'' +
                ", quantity=" + quantity +
                ", lineTotal=" + lineTotal +
                '}';
    }
    
    // ============================================================
    // GETTERS & SETTERS
    // ============================================================
    
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public Integer getLineNumber() {
        return lineNumber;
    }
    
    public void setLineNumber(Integer lineNumber) {
        this.lineNumber = lineNumber;
    }
    
    public UUID getRequisitionLineId() {
        return requisitionLineId;
    }

    public void setRequisitionLineId(UUID requisitionLineId) {
        this.requisitionLineId = requisitionLineId;
    }
    
    public String getMaterialCode() {
        return materialCode;
    }
    
    public void setMaterialCode(String materialCode) {
        this.materialCode = materialCode;
    }
    
    public UUID getMaterialId() {
        return materialId;
    }
    
    public void setMaterialId(UUID materialId) {
        this.materialId = materialId;
    }
    
    public String getMaterialName() {
        return materialName;
    }
    
    public void setMaterialName(String materialName) {
        this.materialName = materialName;
    }
    
    public String getMaterialDescription() {
        return materialDescription;
    }
    
    public void setMaterialDescription(String materialDescription) {
        this.materialDescription = materialDescription;
    }
    
    public String getUnitOfMeasure() {
        return unitOfMeasure;
    }
    
    public void setUnitOfMeasure(String unitOfMeasure) {
        this.unitOfMeasure = unitOfMeasure;
    }
    
    public Integer getQuantity() {
        return quantity;
    }
    
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
    
    public Money getUnitPrice() {
        return unitPrice;
    }
    
    public void setUnitPrice(Money unitPrice) {
        this.unitPrice = unitPrice;
    }
    
    public Money getLineTotal() {
        return lineTotal;
    }
    
    public void setLineTotal(Money lineTotal) {
        this.lineTotal = lineTotal;
    }
    
    public String getCurrencyCode() {
        return currencyCode;
    }
    
    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode != null ? CurrencyCode.fromCode(currencyCode.trim()).getCode() : null;
    }
    
    public UUID getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(UUID supplierId) {
        this.supplierId = supplierId;
    }
    
    public String getSupplierName() {
        return supplierName;
    }
    
    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }
    
    public LocalDate getExpectedDeliveryDate() {
        return expectedDeliveryDate;
    }
    
    public void setExpectedDeliveryDate(LocalDate expectedDeliveryDate) {
        this.expectedDeliveryDate = expectedDeliveryDate;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
}
