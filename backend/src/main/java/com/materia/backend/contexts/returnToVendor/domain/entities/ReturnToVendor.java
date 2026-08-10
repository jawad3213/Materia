package com.materia.backend.contexts.returnToVendor.domain.entities;

import com.materia.backend.common.domain.BaseEntity;
import com.materia.backend.contexts.returnToVendor.domain.enums.ResolutionType;
import com.materia.backend.contexts.returnToVendor.domain.enums.ReturnStatus;
import com.materia.backend.contexts.returnToVendor.domain.exceptions.ReturnToVendorInvalidLineException;
import com.materia.backend.contexts.returnToVendor.domain.exceptions.ReturnToVendorInvalidStatusTransitionException;
import com.materia.backend.contexts.returnToVendor.domain.exceptions.ReturnToVendorLineRequiredException;
import com.materia.backend.contexts.returnToVendor.domain.exceptions.ReturnToVendorNotModifiableException;
import com.materia.backend.contexts.returnToVendor.domain.exceptions.ReturnToVendorValidationException;
import com.materia.backend.contexts.returnToVendor.domain.valueObjects.ReturnCode;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;


public class ReturnToVendor extends BaseEntity {
    
    // ============================================================
    // CONSTANTES
    // ============================================================
    
    public static final int MAX_REASON_LENGTH = 500;
    public static final int MAX_NOTES_LENGTH = 1000;
    public static final int MAX_RESPONSE_LENGTH = 500;
    
    // ============================================================
    // ATTRIBUTS
    // ============================================================
    
    // ---- IDENTIFICATION ----
    private ReturnCode returnCode;
    private String goodsReceiptId;
    private String goodsReceiptCode;
    private String purchaseOrderId;
    private String purchaseOrderCode;
    
    // ---- FOURNISSEUR ----
    private String supplierId;
    private String supplierName;
    private String supplierCode;
    
    // ---- STATUT ----
    private ReturnStatus status;
    private ResolutionType resolutionType;
    
    // ---- DATES ----
    private LocalDate returnDate;
    private LocalDate resolutionDate;
    
    // ---- DÉTAILS ----
    private String returnReason;
    private String supplierResponse;
    private String rejectionSummary;
    
    // ---- RÉSOLUTION ----
    private String creditNoteReference;
    private String creditNoteAmount;
    private String replacementPurchaseOrderReference;
    private String replacementPurchaseOrderCode;
    
    // ---- COMMUNICATION ----
    private String notes;
    private String internalNotes;
    
    
    
    // ============================================================
    // ATTRIBUTS - LIGNES
    // ============================================================
    
    private List<ReturnToVendorLine> lines = new ArrayList<>();
    
    // ============================================================
    // CONSTRUCTEURS
    // ============================================================
    
    public ReturnToVendor() {
        super();
    }
    
    public ReturnToVendor(Builder builder) {
        super();
        this.id = builder.id;
        this.returnCode = builder.returnCode;
        this.goodsReceiptId = builder.goodsReceiptId;
        this.goodsReceiptCode = builder.goodsReceiptCode;
        this.purchaseOrderId = builder.purchaseOrderId;
        this.purchaseOrderCode = builder.purchaseOrderCode;
        this.supplierId = builder.supplierId;
        this.supplierName = builder.supplierName;
        this.supplierCode = builder.supplierCode;
        this.status = builder.status != null ? builder.status : ReturnStatus.DRAFT;
        this.resolutionType = builder.resolutionType;
        this.returnDate = builder.returnDate;
        this.resolutionDate = builder.resolutionDate;
        this.returnReason = builder.returnReason;
        this.supplierResponse = builder.supplierResponse;
        this.rejectionSummary = builder.rejectionSummary;
        this.creditNoteReference = builder.creditNoteReference;
        this.creditNoteAmount = builder.creditNoteAmount;
        this.replacementPurchaseOrderReference = builder.replacementPurchaseOrderReference;
        this.replacementPurchaseOrderCode = builder.replacementPurchaseOrderCode;
        this.notes = builder.notes;
        this.internalNotes = builder.internalNotes;
        this.lines = builder.lines != null ? new ArrayList<>(builder.lines) : new ArrayList<>();
        
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
    
    // ============================================================
    // BUILDER
    // ============================================================
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private UUID id;
        private ReturnCode returnCode;
        private String goodsReceiptId;
        private String goodsReceiptCode;
        private String purchaseOrderId;
        private String purchaseOrderCode;
        private String supplierId;
        private String supplierName;
        private String supplierCode;
        private ReturnStatus status;
        private ResolutionType resolutionType;
        private LocalDate returnDate;
        private LocalDate resolutionDate;
        private String returnReason;
        private String supplierResponse;
        private String rejectionSummary;
        private String creditNoteReference;
        private String creditNoteAmount;
        private String replacementPurchaseOrderReference;
        private String replacementPurchaseOrderCode;
        private String notes;
        private String internalNotes;
        private List<ReturnToVendorLine> lines = new ArrayList<>();
        
        private String createdBy;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        
        public Builder id(UUID id) { this.id = id; return this; }
        
        public Builder returnCode(ReturnCode returnCode) { 
            this.returnCode = returnCode; 
            return this; 
        }
        
        public Builder returnCode(String returnCode) {
            this.returnCode = ReturnCode.of(returnCode);
            return this;
        }
        
        public Builder goodsReceiptId(String goodsReceiptId) { this.goodsReceiptId = goodsReceiptId; return this; }
        public Builder goodsReceiptCode(String goodsReceiptCode) { this.goodsReceiptCode = goodsReceiptCode; return this; }
        public Builder purchaseOrderId(String purchaseOrderId) { this.purchaseOrderId = purchaseOrderId; return this; }
        public Builder purchaseOrderCode(String purchaseOrderCode) { this.purchaseOrderCode = purchaseOrderCode; return this; }
        
        public Builder supplierId(String supplierId) { this.supplierId = supplierId; return this; }
        public Builder supplierName(String supplierName) { this.supplierName = supplierName; return this; }
        public Builder supplierCode(String supplierCode) { this.supplierCode = supplierCode; return this; }
        
        public Builder status(ReturnStatus status) { this.status = status; return this; }
        public Builder resolutionType(ResolutionType resolutionType) { this.resolutionType = resolutionType; return this; }
        
        public Builder returnDate(LocalDate returnDate) { this.returnDate = returnDate; return this; }
        public Builder resolutionDate(LocalDate resolutionDate) { this.resolutionDate = resolutionDate; return this; }
        
        public Builder returnReason(String returnReason) { this.returnReason = returnReason; return this; }
        public Builder supplierResponse(String supplierResponse) { this.supplierResponse = supplierResponse; return this; }
        public Builder rejectionSummary(String rejectionSummary) { this.rejectionSummary = rejectionSummary; return this; }
        
        public Builder creditNoteReference(String creditNoteReference) { 
            this.creditNoteReference = creditNoteReference; 
            return this; 
        }
        public Builder creditNoteAmount(String creditNoteAmount) { 
            this.creditNoteAmount = creditNoteAmount; 
            return this; 
        }
        public Builder replacementPurchaseOrderReference(String replacementPurchaseOrderReference) { 
            this.replacementPurchaseOrderReference = replacementPurchaseOrderReference; 
            return this; 
        }
        public Builder replacementPurchaseOrderCode(String replacementPurchaseOrderCode) { 
            this.replacementPurchaseOrderCode = replacementPurchaseOrderCode; 
            return this; 
        }
        
        public Builder notes(String notes) { this.notes = notes; return this; }
        public Builder internalNotes(String internalNotes) { this.internalNotes = internalNotes; return this; }
        
        
        public Builder addLine(ReturnToVendorLine line) {
            if (line == null) {
                throw new ReturnToVendorInvalidLineException("La ligne ne peut pas être nulle");
            }
            if (this.lines == null) {
                this.lines = new ArrayList<>();
            }
            this.lines.add(line);
            return this;
        }
        
        public Builder lines(List<ReturnToVendorLine> lines) {
            if (lines == null) {
                throw new ReturnToVendorLineRequiredException("La liste des lignes ne peut pas être nulle");
            }
            this.lines = new ArrayList<>(lines);
            return this;
        }
        
        public Builder createdBy(String createdBy) { this.createdBy = createdBy; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public Builder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }
        
        public ReturnToVendor build() {
            validateRequiredFields();
            validateLines();
            
            if (this.id == null) this.id = UUID.randomUUID();
            
            if (this.returnCode == null) {
                this.returnCode = ReturnCode.createDefault();
            }
            
            if (this.returnDate == null) {
                this.returnDate = LocalDate.now();
            }
            
            if (this.createdAt == null) {
                this.createdAt = LocalDateTime.now();
            }
            if (this.updatedAt == null) {
                this.updatedAt = LocalDateTime.now();
            }
            
            if (this.status == null) {
                this.status = ReturnStatus.DRAFT;
            }
            
            return new ReturnToVendor(this);
        }
        
        private void validateRequiredFields() {
            if (this.goodsReceiptId == null || this.goodsReceiptId.trim().isEmpty()) {
                throw new ReturnToVendorValidationException("La réception est obligatoire");
            }
            if (this.supplierId == null || this.supplierId.trim().isEmpty()) {
                throw new ReturnToVendorValidationException("Le fournisseur est obligatoire");
            }
            if (this.supplierName == null || this.supplierName.trim().isEmpty()) {
                throw new ReturnToVendorValidationException("Le nom du fournisseur est obligatoire");
            }
            if (this.returnReason == null || this.returnReason.trim().isEmpty()) {
                throw new ReturnToVendorValidationException("La raison du retour est obligatoire");
            }
        }
        
        private void validateLines() {
            if (this.lines == null || this.lines.isEmpty()) {
                throw new ReturnToVendorLineRequiredException("Au moins une ligne est requise");
            }
            for (int i = 0; i < this.lines.size(); i++) {
                ReturnToVendorLine line = this.lines.get(i);
                if (line.getMaterialCode() == null || line.getMaterialCode().trim().isEmpty()) {
                    throw new ReturnToVendorInvalidLineException(
                        "Le code du matériau est obligatoire pour la ligne " + (i + 1)
                    );
                }
                if (line.getRejectionReason() == null || line.getRejectionReason().trim().isEmpty()) {
                    throw new ReturnToVendorInvalidLineException(
                        "La raison du rejet est obligatoire pour la ligne " + (i + 1)
                    );
                }
            }
        }
    }
    
    // ============================================================
    // DOMAINE BEHAVIOR - Version Simplifiée
    // ============================================================
    
    public void submit(String userId) {
        if (status != ReturnStatus.DRAFT) {
            throw new ReturnToVendorInvalidStatusTransitionException("Seul un retour en brouillon peut être soumis");
        }
        this.status = ReturnStatus.PENDING;
        this.setUpdatedAt(LocalDateTime.now());
        this.setUpdatedBy(userId);
    }
    
    public void resolve(String userId, ResolutionType resolutionType, String reference) {
        if (status != ReturnStatus.PENDING) {
            throw new ReturnToVendorInvalidStatusTransitionException("Seul un retour en attente peut être résolu");
        }
        this.status = ReturnStatus.RESOLVED;
        this.resolutionType = resolutionType;
        this.resolutionDate = LocalDate.now();
        
        if (resolutionType == ResolutionType.REPLACEMENT) {
            this.replacementPurchaseOrderReference = reference;
        } else if (resolutionType == ResolutionType.CREDIT_NOTE) {
            this.creditNoteReference = reference;
        }
        
        this.setUpdatedAt(LocalDateTime.now());
        this.setUpdatedBy(userId);
    }
    
    public void cancel(String userId, String reason) {
        if (status == ReturnStatus.RESOLVED) {
            throw new ReturnToVendorNotModifiableException("Un retour résolu ne peut pas être annulé");
        }
        this.status = ReturnStatus.CANCELLED;
        this.notes = (this.notes != null ? this.notes + " " : "") + "Annulé: " + reason;
        this.setUpdatedAt(LocalDateTime.now());
        this.setUpdatedBy(userId);
    }
    
    public boolean isModifiable() {
        return status != null && status.isModifiable();
    }
    
    public boolean isActive() {
        return status != null && status.isActive();
    }
    
    public boolean isClosed() {
        return status != null && status.isClosed();
    }
    
    public boolean hasResolution() {
        return resolutionType != null;
    }
    
    public boolean isReplacement() {
        return resolutionType == ResolutionType.REPLACEMENT;
    }
    
    public boolean isCreditNote() {
        return resolutionType == ResolutionType.CREDIT_NOTE;
    }
    
    // ============================================================
    // EQUALS & HASHCODE
    // ============================================================
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReturnToVendor that = (ReturnToVendor) o;
        return Objects.equals(getId(), that.getId()) ||
               Objects.equals(returnCode, that.returnCode);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(getId(), returnCode);
    }
    
    @Override
    public String toString() {
        return "ReturnToVendor{" +
                "id=" + getId() +
                ", returnCode=" + returnCode +
                ", supplierName='" + supplierName + '\'' +
                ", status=" + status +
                ", resolutionType=" + resolutionType +
                ", returnDate=" + returnDate +
                '}';
    }
    
    // ============================================================
    // GETTERS & SETTERS
    // ============================================================
    
    public ReturnCode getReturnCode() { return returnCode; }
    public void setReturnCode(ReturnCode returnCode) { this.returnCode = returnCode; }
    
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
    
    public ReturnStatus getStatus() { return status; }
    public void setStatus(ReturnStatus status) { this.status = status; }
    
    public ResolutionType getResolutionType() { return resolutionType; }
    public void setResolutionType(ResolutionType resolutionType) { this.resolutionType = resolutionType; }
    
    public LocalDate getReturnDate() { return returnDate; }
    public void setReturnDate(LocalDate returnDate) { this.returnDate = returnDate; }
    
    public LocalDate getResolutionDate() { return resolutionDate; }
    public void setResolutionDate(LocalDate resolutionDate) { this.resolutionDate = resolutionDate; }
    
    public String getReturnReason() { return returnReason; }
    public void setReturnReason(String returnReason) { this.returnReason = returnReason; }
    
    public String getSupplierResponse() { return supplierResponse; }
    public void setSupplierResponse(String supplierResponse) { this.supplierResponse = supplierResponse; }
    
    public String getRejectionSummary() { return rejectionSummary; }
    public void setRejectionSummary(String rejectionSummary) { this.rejectionSummary = rejectionSummary; }
    
    public String getCreditNoteReference() { return creditNoteReference; }
    public void setCreditNoteReference(String creditNoteReference) { this.creditNoteReference = creditNoteReference; }
    
    public String getCreditNoteAmount() { return creditNoteAmount; }
    public void setCreditNoteAmount(String creditNoteAmount) { this.creditNoteAmount = creditNoteAmount; }
    
    public String getReplacementPurchaseOrderReference() { return replacementPurchaseOrderReference; }
    public void setReplacementPurchaseOrderReference(String replacementPurchaseOrderReference) { 
        this.replacementPurchaseOrderReference = replacementPurchaseOrderReference; 
    }
    
    public String getReplacementPurchaseOrderCode() { return replacementPurchaseOrderCode; }
    public void setReplacementPurchaseOrderCode(String replacementPurchaseOrderCode) { 
        this.replacementPurchaseOrderCode = replacementPurchaseOrderCode; 
    }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    public String getInternalNotes() { return internalNotes; }
    public void setInternalNotes(String internalNotes) { this.internalNotes = internalNotes; }
    

    public List<ReturnToVendorLine> getLines() { return lines; }
    public void setLines(List<ReturnToVendorLine> lines) { this.lines = lines; }
}
