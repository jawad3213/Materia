package com.materia.backend.contexts.returnToVendor.domain.entities;

import com.materia.backend.common.domain.BaseEntity;
import com.materia.backend.contexts.returnToVendor.domain.exceptions.ReturnToVendorInvalidQuantityException;
import com.materia.backend.contexts.returnToVendor.domain.exceptions.ReturnToVendorValidationException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * One rejected goods-receipt line sent back to the supplier. The material, the rejected quantity, the
 * purchase-order line and its unit price are copied from the goods receipt when the return is created.
 */
public class ReturnToVendorLine extends BaseEntity {

    private Integer lineNumber;

    // ---- Link with the goods receipt and the purchase order ----
    private String goodsReceiptLineId;
    private String purchaseOrderLineId;
    private String materialId;
    private String materialCode;
    private String materialName;
    private String unitOfMeasure;

    // ---- Quantities ----
    private Integer rejectedQuantity;
    private Integer quantityToReturn;
    private Integer quantityAlreadyReturned;

    /** Purchase-order unit price, used to value a credit note. */
    private BigDecimal unitPrice;

    // ---- Quality ----
    private String rejectionReason;
    private String qualityNotes;
    private String defectDescription;

    // ---- Resolution ----
    private boolean isReplaced;
    private boolean isCreditNote;

    private String notes;

    public ReturnToVendorLine() {
        super();
    }

    public ReturnToVendorLine(Builder builder) {
        super();
        this.id = builder.id;
        this.lineNumber = builder.lineNumber;
        this.goodsReceiptLineId = builder.goodsReceiptLineId;
        this.purchaseOrderLineId = builder.purchaseOrderLineId;
        this.materialId = builder.materialId;
        this.materialCode = builder.materialCode;
        this.materialName = builder.materialName;
        this.unitOfMeasure = builder.unitOfMeasure;
        this.rejectedQuantity = builder.rejectedQuantity;
        this.quantityToReturn = builder.quantityToReturn;
        this.quantityAlreadyReturned = builder.quantityAlreadyReturned != null ? builder.quantityAlreadyReturned : 0;
        this.unitPrice = builder.unitPrice;
        this.rejectionReason = builder.rejectionReason;
        this.qualityNotes = builder.qualityNotes;
        this.defectDescription = builder.defectDescription;
        this.isReplaced = builder.isReplaced;
        this.isCreditNote = builder.isCreditNote;
        this.notes = builder.notes;
        this.version = builder.version;

        if (builder.createdAt != null) {
            this.setCreatedAt(builder.createdAt);
        }
        if (builder.updatedAt != null) {
            this.setUpdatedAt(builder.updatedAt);
        }
        if (builder.createdBy != null) {
            this.setCreatedBy(builder.createdBy);
            this.setUpdatedBy(builder.createdBy);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id;
        private Integer lineNumber;
        private String goodsReceiptLineId;
        private String purchaseOrderLineId;
        private String materialId;
        private String materialCode;
        private String materialName;
        private String unitOfMeasure;
        private Integer rejectedQuantity;
        private Integer quantityToReturn;
        private Integer quantityAlreadyReturned;
        private BigDecimal unitPrice;
        private String rejectionReason;
        private String qualityNotes;
        private String defectDescription;
        private boolean isReplaced;
        private boolean isCreditNote;
        private String notes;
        private Long version;

        private String createdBy;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder lineNumber(Integer lineNumber) { this.lineNumber = lineNumber; return this; }
        public Builder goodsReceiptLineId(String goodsReceiptLineId) { this.goodsReceiptLineId = goodsReceiptLineId; return this; }
        public Builder purchaseOrderLineId(String purchaseOrderLineId) { this.purchaseOrderLineId = purchaseOrderLineId; return this; }
        public Builder materialId(String materialId) { this.materialId = materialId; return this; }
        public Builder materialCode(String materialCode) { this.materialCode = materialCode; return this; }
        public Builder materialName(String materialName) { this.materialName = materialName; return this; }
        public Builder unitOfMeasure(String unitOfMeasure) { this.unitOfMeasure = unitOfMeasure; return this; }
        public Builder rejectedQuantity(Integer rejectedQuantity) { this.rejectedQuantity = rejectedQuantity; return this; }
        public Builder quantityToReturn(Integer quantityToReturn) { this.quantityToReturn = quantityToReturn; return this; }
        public Builder quantityAlreadyReturned(Integer quantityAlreadyReturned) { this.quantityAlreadyReturned = quantityAlreadyReturned; return this; }
        public Builder unitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; return this; }
        public Builder rejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; return this; }
        public Builder qualityNotes(String qualityNotes) { this.qualityNotes = qualityNotes; return this; }
        public Builder defectDescription(String defectDescription) { this.defectDescription = defectDescription; return this; }
        public Builder isReplaced(boolean isReplaced) { this.isReplaced = isReplaced; return this; }
        public Builder isCreditNote(boolean isCreditNote) { this.isCreditNote = isCreditNote; return this; }
        public Builder notes(String notes) { this.notes = notes; return this; }
        public Builder version(Long version) { this.version = version; return this; }
        public Builder createdBy(String createdBy) { this.createdBy = createdBy; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public Builder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }

        public ReturnToVendorLine build() {
            validateRequiredFields();
            validateQuantity();

            if (this.id == null) {
                this.id = UUID.randomUUID();
            }
            if (this.quantityAlreadyReturned == null) {
                this.quantityAlreadyReturned = 0;
            }
            if (this.createdAt == null) {
                this.createdAt = LocalDateTime.now();
            }
            if (this.updatedAt == null) {
                this.updatedAt = LocalDateTime.now();
            }
            return new ReturnToVendorLine(this);
        }

        private void validateRequiredFields() {
            if (this.materialCode == null || this.materialCode.trim().isEmpty()) {
                throw new ReturnToVendorValidationException("The material code is required");
            }
            if (this.materialName == null || this.materialName.trim().isEmpty()) {
                throw new ReturnToVendorValidationException("The material name is required");
            }
            if (this.quantityToReturn == null || this.quantityToReturn <= 0) {
                throw new ReturnToVendorInvalidQuantityException("The quantity to return must be positive");
            }
            if (this.rejectionReason == null || this.rejectionReason.trim().isEmpty()) {
                throw new ReturnToVendorValidationException("The rejection reason is required");
            }
        }

        private void validateQuantity() {
            if (this.rejectedQuantity != null && this.quantityToReturn > this.rejectedQuantity) {
                throw new ReturnToVendorInvalidQuantityException(
                        "The quantity to return (" + this.quantityToReturn
                                + ") cannot exceed the rejected quantity (" + this.rejectedQuantity + ")");
            }
            if (this.quantityAlreadyReturned != null && this.quantityAlreadyReturned > this.quantityToReturn) {
                throw new ReturnToVendorInvalidQuantityException(
                        "The quantity already returned (" + this.quantityAlreadyReturned
                                + ") cannot exceed the quantity to return (" + this.quantityToReturn + ")");
            }
        }
    }

    // ============================================================
    // BUSINESS METHODS
    // ============================================================

    /** The goods left the warehouse: the whole quantity is now with the supplier. */
    public void markShipped() {
        this.quantityAlreadyReturned = quantityToReturn;
    }

    /** Quantity times the purchase-order unit price; zero when the price is unknown. */
    public BigDecimal lineValue() {
        if (unitPrice == null || quantityToReturn == null) {
            return BigDecimal.ZERO;
        }
        return unitPrice.multiply(BigDecimal.valueOf(quantityToReturn)).setScale(2, RoundingMode.HALF_UP);
    }

    public boolean isFullyReturned() {
        return quantityAlreadyReturned != null && quantityToReturn != null
                && quantityAlreadyReturned >= quantityToReturn;
    }

    public int getRemainingQuantity() {
        if (quantityToReturn == null) {
            return 0;
        }
        int alreadyReturned = quantityAlreadyReturned != null ? quantityAlreadyReturned : 0;
        return Math.max(0, quantityToReturn - alreadyReturned);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReturnToVendorLine that = (ReturnToVendorLine) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "ReturnToVendorLine{id=" + id + ", lineNumber=" + lineNumber + ", materialCode='" + materialCode
                + "', quantityToReturn=" + quantityToReturn + '}';
    }

    // ============================================================
    // GETTERS & SETTERS
    // ============================================================

    public Integer getLineNumber() { return lineNumber; }
    public void setLineNumber(Integer lineNumber) { this.lineNumber = lineNumber; }

    public String getGoodsReceiptLineId() { return goodsReceiptLineId; }
    public void setGoodsReceiptLineId(String goodsReceiptLineId) { this.goodsReceiptLineId = goodsReceiptLineId; }

    public String getPurchaseOrderLineId() { return purchaseOrderLineId; }
    public void setPurchaseOrderLineId(String purchaseOrderLineId) { this.purchaseOrderLineId = purchaseOrderLineId; }

    public String getMaterialId() { return materialId; }
    public void setMaterialId(String materialId) { this.materialId = materialId; }

    public String getMaterialCode() { return materialCode; }
    public void setMaterialCode(String materialCode) { this.materialCode = materialCode; }

    public String getMaterialName() { return materialName; }
    public void setMaterialName(String materialName) { this.materialName = materialName; }

    public String getUnitOfMeasure() { return unitOfMeasure; }
    public void setUnitOfMeasure(String unitOfMeasure) { this.unitOfMeasure = unitOfMeasure; }

    public Integer getRejectedQuantity() { return rejectedQuantity; }
    public void setRejectedQuantity(Integer rejectedQuantity) { this.rejectedQuantity = rejectedQuantity; }

    public Integer getQuantityToReturn() { return quantityToReturn; }
    public void setQuantityToReturn(Integer quantityToReturn) { this.quantityToReturn = quantityToReturn; }

    public Integer getQuantityAlreadyReturned() { return quantityAlreadyReturned; }
    public void setQuantityAlreadyReturned(Integer quantityAlreadyReturned) { this.quantityAlreadyReturned = quantityAlreadyReturned; }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }

    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }

    public String getQualityNotes() { return qualityNotes; }
    public void setQualityNotes(String qualityNotes) { this.qualityNotes = qualityNotes; }

    public String getDefectDescription() { return defectDescription; }
    public void setDefectDescription(String defectDescription) { this.defectDescription = defectDescription; }

    public boolean isReplaced() { return isReplaced; }
    public void setReplaced(boolean replaced) { isReplaced = replaced; }

    public boolean isCreditNote() { return isCreditNote; }
    public void setCreditNote(boolean creditNote) { isCreditNote = creditNote; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
