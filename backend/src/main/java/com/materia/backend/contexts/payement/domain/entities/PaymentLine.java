package com.materia.backend.contexts.payement.domain.entities;

import com.materia.backend.common.domain.BaseEntity;
import com.materia.backend.contexts.masterData.domain.valueObjects.Money;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Payment Line Entity
 * Ligne d'un paiement (facture payée)
 * 
 * POJO Pur - Sans annotations Spring/Lombok
 * 
 * @author SAP MM Team
 * @version 1.0
 */
public class PaymentLine extends BaseEntity {
    
    // ============================================================
    // ATTRIBUTS
    // ============================================================
    
    private Integer lineNumber;
    
    // ---- LIEN AVEC LA FACTURE ----
    private String invoiceId;
    private String invoiceCode;
    
    // ---- LIEN AVEC LE FOURNISSEUR ----
    private String supplierId;
    private String supplierName;
    
    // ---- MONTANTS ----
    private Money amount;
    private Money paidAmount;
    private String currencyCode;
    
    // ---- STATUT ----
    private boolean isPaid;
    
    // ---- DIVERS ----
    private String notes;
    
    // ============================================================
    // CONSTRUCTEURS
    // ============================================================
    
    public PaymentLine() {
        super();
    }
    
    public PaymentLine(Builder builder) {
        super();
        this.id = builder.id;
        this.lineNumber = builder.lineNumber;
        this.invoiceId = builder.invoiceId;
        this.invoiceCode = builder.invoiceCode;
        this.supplierId = builder.supplierId;
        this.supplierName = builder.supplierName;
        this.amount = builder.amount;
        this.paidAmount = builder.paidAmount;
        this.currencyCode = builder.currencyCode != null ? builder.currencyCode : "MAD";
        this.isPaid = builder.isPaid;
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
        private String invoiceId;
        private String invoiceCode;
        private String supplierId;
        private String supplierName;
        private Money amount;
        private Money paidAmount;
        private String currencyCode;
        private boolean isPaid;
        private String notes;
        private String createdBy;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        
        public Builder id(UUID id) { this.id = id; return this; }
        public Builder lineNumber(Integer lineNumber) { this.lineNumber = lineNumber; return this; }
        public Builder invoiceId(String invoiceId) { this.invoiceId = invoiceId; return this; }
        public Builder invoiceCode(String invoiceCode) { this.invoiceCode = invoiceCode; return this; }
        public Builder supplierId(String supplierId) { this.supplierId = supplierId; return this; }
        public Builder supplierName(String supplierName) { this.supplierName = supplierName; return this; }
        public Builder amount(Money amount) { this.amount = amount; return this; }
        public Builder paidAmount(Money paidAmount) { this.paidAmount = paidAmount; return this; }
        public Builder currencyCode(String currencyCode) { this.currencyCode = currencyCode; return this; }
        public Builder isPaid(boolean isPaid) { this.isPaid = isPaid; return this; }
        public Builder notes(String notes) { this.notes = notes; return this; }
        public Builder createdBy(String createdBy) { this.createdBy = createdBy; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public Builder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }
        
        public PaymentLine build() {
            validateRequiredFields();
            
            if (this.id == null) this.id = UUID.randomUUID();
            if (this.createdAt == null) this.createdAt = LocalDateTime.now();
            if (this.updatedAt == null) this.updatedAt = LocalDateTime.now();
            if (this.currencyCode == null) this.currencyCode = "MAD";
            
            return new PaymentLine(this);
        }
        
        private void validateRequiredFields() {
            if (this.invoiceId == null || this.invoiceId.trim().isEmpty()) {
                throw new IllegalArgumentException("L'ID de la facture est obligatoire");
            }
            if (this.amount == null) {
                throw new IllegalArgumentException("Le montant est obligatoire");
            }
            if (this.supplierId == null || this.supplierId.trim().isEmpty()) {
                throw new IllegalArgumentException("Le fournisseur est obligatoire");
            }
        }
    }
    
    // ============================================================
    // MÉTHODES MÉTIER
    // ============================================================
    
    public void markAsPaid() {
        this.isPaid = true;
        this.paidAmount = this.amount;
        this.setUpdatedAt(LocalDateTime.now());
    }
    
    public boolean hasRemainingAmount() {
        return paidAmount == null || paidAmount.isLessThan(amount);
    }
    
    // ============================================================
    // EQUALS & HASHCODE
    // ============================================================
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PaymentLine that = (PaymentLine) o;
        return id != null && id.equals(that.id);
    }
    
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
    
    @Override
    public String toString() {
        return "PaymentLine{" +
                "id=" + id +
                ", lineNumber=" + lineNumber +
                ", invoiceCode='" + invoiceCode + '\'' +
                ", amount=" + amount +
                ", isPaid=" + isPaid +
                '}';
    }
    
    // ============================================================
    // GETTERS & SETTERS
    // ============================================================
    
    public Integer getLineNumber() { return lineNumber; }
    public void setLineNumber(Integer lineNumber) { this.lineNumber = lineNumber; }
    
    public String getInvoiceId() { return invoiceId; }
    public void setInvoiceId(String invoiceId) { this.invoiceId = invoiceId; }
    
    public String getInvoiceCode() { return invoiceCode; }
    public void setInvoiceCode(String invoiceCode) { this.invoiceCode = invoiceCode; }
    
    public String getSupplierId() { return supplierId; }
    public void setSupplierId(String supplierId) { this.supplierId = supplierId; }
    
    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }
    
    public Money getAmount() { return amount; }
    public void setAmount(Money amount) { this.amount = amount; }
    
    public Money getPaidAmount() { return paidAmount; }
    public void setPaidAmount(Money paidAmount) { this.paidAmount = paidAmount; }
    
    public String getCurrencyCode() { return currencyCode; }
    public void setCurrencyCode(String currencyCode) { this.currencyCode = currencyCode; }
    
    public boolean isPaid() { return isPaid; }
    public void setPaid(boolean paid) { isPaid = paid; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
