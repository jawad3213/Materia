package com.materia.backend.contexts.invoice.domain.entities;

import com.materia.backend.common.domain.BaseEntity;
import com.materia.backend.contexts.invoice.domain.enums.InvoiceStatus;
import com.materia.backend.contexts.invoice.domain.enums.InvoiceType;
import com.materia.backend.contexts.invoice.domain.valueObjects.InvoiceCode;
import com.materia.backend.common.domain.valueObjects.Money;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Invoice Domain Entity - Version MVP
 * 
 * POJO Pur - Sans annotations Spring/Lombok
 * 
 * @author SAP MM Team
 * @version 1.0
 */
public class Invoice extends BaseEntity {
    
    // ============================================================
    // CONSTANTES
    // ============================================================
    
    public static final int MAX_NOTES_LENGTH = 1000;
    
    // ============================================================
    // ATTRIBUTS - EN-TÊTE
    // ============================================================
    
    private InvoiceCode invoiceCode;
    private String purchaseOrderId;
    private String purchaseOrderCode;
    private String goodsReceiptId;
    private String goodsReceiptCode;
    private String supplierId;
    private String supplierName;
    private String supplierCode;
    private InvoiceType invoiceType;
    private InvoiceStatus status;
    private String externalReference;
    
    // ============================================================
    // ATTRIBUTS - DATES
    // ============================================================
    
    private LocalDate invoiceDate;
    private LocalDate dueDate;
    private LocalDate receivedDate;
    private LocalDate paymentDate;
    
    // ============================================================
    // ATTRIBUTS - FINANCES
    // ============================================================
    
    private Money totalAmount;
    private Money totalTaxAmount;
    private Money totalAmountWithTax;
    private String currencyCode;
    
    // ============================================================
    // ATTRIBUTS - VÉRIFICATION
    // ============================================================
    
    private boolean isVerified;
    private boolean hasDiscrepancy;
    private String discrepancySummary;
    private LocalDateTime verificationDate;
    private String verifiedBy;
    private String verifiedByName;
    
    // ============================================================
    // ATTRIBUTS - PAIEMENT
    // ============================================================
    
    private Money paidAmount;
    private LocalDateTime paidAt;
    private String paidBy;
    private String paidByName;
    
    // ============================================================
    // ATTRIBUTS - DIVERS
    // ============================================================
    
    private String notes;
    private String internalNotes;
    
    // ============================================================
    // ATTRIBUTS - OBSOLESCENCE
    // ============================================================
    
    private LocalDateTime obsoletedAt;
    private String obsoletedBy;
    private String obsoletedReason;
    
    // ============================================================
    // ATTRIBUTS - LIGNES
    // ============================================================
    
    private List<InvoiceLine> lines = new ArrayList<>();
    
    // ============================================================
    // CONSTRUCTEURS
    // ============================================================
    
    public Invoice() {
        super();
    }
    
    public Invoice(Builder builder) {
        super();
        this.id = builder.id;
        this.invoiceCode = builder.invoiceCode;
        this.purchaseOrderId = builder.purchaseOrderId;
        this.purchaseOrderCode = builder.purchaseOrderCode;
        this.goodsReceiptId = builder.goodsReceiptId;
        this.goodsReceiptCode = builder.goodsReceiptCode;
        this.supplierId = builder.supplierId;
        this.supplierName = builder.supplierName;
        this.supplierCode = builder.supplierCode;
        this.invoiceType = builder.invoiceType != null ? builder.invoiceType : InvoiceType.STANDARD;
        this.status = builder.status != null ? builder.status : InvoiceStatus.DRAFT;
        this.externalReference = builder.externalReference;
        
        this.invoiceDate = builder.invoiceDate;
        this.dueDate = builder.dueDate;
        this.receivedDate = builder.receivedDate;
        this.paymentDate = builder.paymentDate;
        
        this.totalAmount = builder.totalAmount;
        this.totalTaxAmount = builder.totalTaxAmount;
        this.totalAmountWithTax = builder.totalAmountWithTax;
        this.currencyCode = builder.currencyCode != null ? builder.currencyCode : "MAD";
        
        this.isVerified = builder.isVerified;
        this.hasDiscrepancy = builder.hasDiscrepancy;
        this.discrepancySummary = builder.discrepancySummary;
        this.verificationDate = builder.verificationDate;
        this.verifiedBy = builder.verifiedBy;
        this.verifiedByName = builder.verifiedByName;
        
        this.paidAmount = builder.paidAmount;
        this.paidAt = builder.paidAt;
        this.paidBy = builder.paidBy;
        this.paidByName = builder.paidByName;
        
        this.notes = builder.notes;
        this.internalNotes = builder.internalNotes;
        
        this.obsoletedAt = builder.obsoletedAt;
        this.obsoletedBy = builder.obsoletedBy;
        this.obsoletedReason = builder.obsoletedReason;
        
        this.lines = builder.lines != null ? new ArrayList<>(builder.lines) : new ArrayList<>();
        
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
        private InvoiceCode invoiceCode;
        private String purchaseOrderId;
        private String purchaseOrderCode;
        private String goodsReceiptId;
        private String goodsReceiptCode;
        private String supplierId;
        private String supplierName;
        private String supplierCode;
        private InvoiceType invoiceType;
        private InvoiceStatus status;
        private String externalReference;
        
        private LocalDate invoiceDate;
        private LocalDate dueDate;
        private LocalDate receivedDate;
        private LocalDate paymentDate;
        
        private Money totalAmount;
        private Money totalTaxAmount;
        private Money totalAmountWithTax;
        private String currencyCode;
        
        private boolean isVerified;
        private boolean hasDiscrepancy;
        private String discrepancySummary;
        private LocalDateTime verificationDate;
        private String verifiedBy;
        private String verifiedByName;
        
        private Money paidAmount;
        private LocalDateTime paidAt;
        private String paidBy;
        private String paidByName;
        
        private String notes;
        private String internalNotes;
        
        private LocalDateTime obsoletedAt;
        private String obsoletedBy;
        private String obsoletedReason;
        
        private List<InvoiceLine> lines = new ArrayList<>();
        
        private String createdBy;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        
        public Builder id(UUID id) { this.id = id; return this; }
        
        public Builder invoiceCode(InvoiceCode invoiceCode) { 
            this.invoiceCode = invoiceCode; 
            return this; 
        }
        
        public Builder invoiceCode(String invoiceCode) {
            this.invoiceCode = InvoiceCode.of(invoiceCode);
            return this;
        }
        
        public Builder purchaseOrderId(String purchaseOrderId) { this.purchaseOrderId = purchaseOrderId; return this; }
        public Builder purchaseOrderCode(String purchaseOrderCode) { this.purchaseOrderCode = purchaseOrderCode; return this; }
        public Builder goodsReceiptId(String goodsReceiptId) { this.goodsReceiptId = goodsReceiptId; return this; }
        public Builder goodsReceiptCode(String goodsReceiptCode) { this.goodsReceiptCode = goodsReceiptCode; return this; }
        public Builder supplierId(String supplierId) { this.supplierId = supplierId; return this; }
        public Builder supplierName(String supplierName) { this.supplierName = supplierName; return this; }
        public Builder supplierCode(String supplierCode) { this.supplierCode = supplierCode; return this; }
        public Builder invoiceType(InvoiceType invoiceType) { this.invoiceType = invoiceType; return this; }
        public Builder status(InvoiceStatus status) { this.status = status; return this; }
        public Builder externalReference(String externalReference) { this.externalReference = externalReference; return this; }
        
        public Builder invoiceDate(LocalDate invoiceDate) { this.invoiceDate = invoiceDate; return this; }
        public Builder dueDate(LocalDate dueDate) { this.dueDate = dueDate; return this; }
        public Builder receivedDate(LocalDate receivedDate) { this.receivedDate = receivedDate; return this; }
        public Builder paymentDate(LocalDate paymentDate) { this.paymentDate = paymentDate; return this; }
        
        public Builder totalAmount(Money totalAmount) { this.totalAmount = totalAmount; return this; }
        public Builder totalTaxAmount(Money totalTaxAmount) { this.totalTaxAmount = totalTaxAmount; return this; }
        public Builder totalAmountWithTax(Money totalAmountWithTax) { this.totalAmountWithTax = totalAmountWithTax; return this; }
        public Builder currencyCode(String currencyCode) { this.currencyCode = currencyCode; return this; }
        
        public Builder isVerified(boolean isVerified) { this.isVerified = isVerified; return this; }
        public Builder hasDiscrepancy(boolean hasDiscrepancy) { this.hasDiscrepancy = hasDiscrepancy; return this; }
        public Builder discrepancySummary(String discrepancySummary) { this.discrepancySummary = discrepancySummary; return this; }
        public Builder verificationDate(LocalDateTime verificationDate) { this.verificationDate = verificationDate; return this; }
        public Builder verifiedBy(String verifiedBy) { this.verifiedBy = verifiedBy; return this; }
        public Builder verifiedByName(String verifiedByName) { this.verifiedByName = verifiedByName; return this; }
        
        public Builder paidAmount(Money paidAmount) { this.paidAmount = paidAmount; return this; }
        public Builder paidAt(LocalDateTime paidAt) { this.paidAt = paidAt; return this; }
        public Builder paidBy(String paidBy) { this.paidBy = paidBy; return this; }
        public Builder paidByName(String paidByName) { this.paidByName = paidByName; return this; }
        
        public Builder notes(String notes) { this.notes = notes; return this; }
        public Builder internalNotes(String internalNotes) { this.internalNotes = internalNotes; return this; }
        
        public Builder obsoletedAt(LocalDateTime obsoletedAt) { this.obsoletedAt = obsoletedAt; return this; }
        public Builder obsoletedBy(String obsoletedBy) { this.obsoletedBy = obsoletedBy; return this; }
        public Builder obsoletedReason(String obsoletedReason) { this.obsoletedReason = obsoletedReason; return this; }
        
        public Builder addLine(InvoiceLine line) {
            if (line == null) {
                throw new IllegalArgumentException("La ligne ne peut pas être nulle");
            }
            if (this.lines == null) {
                this.lines = new ArrayList<>();
            }
            this.lines.add(line);
            return this;
        }
        
        public Builder lines(List<InvoiceLine> lines) {
            if (lines == null) {
                throw new IllegalArgumentException("La liste des lignes ne peut pas être nulle");
            }
            this.lines = new ArrayList<>(lines);
            return this;
        }
        
        public Builder createdBy(String createdBy) { this.createdBy = createdBy; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public Builder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }
        
        public Invoice build() {
            validateRequiredFields();
            validateLines();
            calculateTotals();
            
            if (this.id == null) this.id = UUID.randomUUID();
            
            if (this.invoiceCode == null) {
                this.invoiceCode = InvoiceCode.createDefault();
            }
            
            if (this.invoiceDate == null) {
                this.invoiceDate = LocalDate.now();
            }
            
            if (this.receivedDate == null) {
                this.receivedDate = LocalDate.now();
            }
            
            if (this.dueDate == null && this.invoiceDate != null) {
                this.dueDate = this.invoiceDate.plusDays(30);
            }
            
            if (this.createdAt == null) this.createdAt = LocalDateTime.now();
            if (this.updatedAt == null) this.updatedAt = LocalDateTime.now();
            
            if (this.status == null) this.status = InvoiceStatus.DRAFT;
            if (this.currencyCode == null) this.currencyCode = "MAD";
            
            return new Invoice(this);
        }
        
        private void validateRequiredFields() {
            if (this.purchaseOrderId == null || this.purchaseOrderId.trim().isEmpty()) {
                throw new IllegalArgumentException("La commande est obligatoire");
            }
            if (this.supplierId == null || this.supplierId.trim().isEmpty()) {
                throw new IllegalArgumentException("Le fournisseur est obligatoire");
            }
            if (this.supplierName == null || this.supplierName.trim().isEmpty()) {
                throw new IllegalArgumentException("Le nom du fournisseur est obligatoire");
            }
            if (this.totalAmount == null) {
                throw new IllegalArgumentException("Le montant total est obligatoire");
            }
        }
        
        private void validateLines() {
            if (this.lines == null || this.lines.isEmpty()) {
                throw new IllegalArgumentException("Au moins une ligne est requise");
            }
            for (int i = 0; i < this.lines.size(); i++) {
                InvoiceLine line = this.lines.get(i);
                if (line.getMaterialCode() == null || line.getMaterialCode().trim().isEmpty()) {
                    throw new IllegalArgumentException(
                        "Le code du matériau est obligatoire pour la ligne " + (i + 1)
                    );
                }
            }
        }
        
        private void calculateTotals() {
            if (this.lines == null || this.lines.isEmpty()) {
                return;
            }
            
            Money total = Money.zero("MAD");
            Money totalTax = Money.zero("MAD");
            
            for (InvoiceLine line : this.lines) {
                if (line.getLineTotal() != null) {
                    total = total.add(line.getLineTotal());
                }
                if (line.getTaxAmount() != null) {
                    totalTax = totalTax.add(line.getTaxAmount());
                }
            }
            
            this.totalAmount = total;
            this.totalTaxAmount = totalTax;
            this.totalAmountWithTax = total.add(totalTax);
        }
    }
    
    // ============================================================
    // DOMAINE BEHAVIOR - MVP
    // ============================================================
    
    /**
     * Soumettre la facture (DRAFT → SUBMITTED)
     */
    public void submit(String userId) {
        if (status != InvoiceStatus.DRAFT) {
            throw new IllegalStateException("Seule une facture en brouillon peut être soumise");
        }
        this.status = InvoiceStatus.SUBMITTED;
        this.setUpdatedAt(LocalDateTime.now());
        this.setUpdatedBy(userId);
    }
    
    /**
     * Vérifier la facture (SUBMITTED → VERIFIED)
     */
    public void verify(String userId, String userName) {
        if (status != InvoiceStatus.SUBMITTED) {
            throw new IllegalStateException("Seule une facture soumise peut être vérifiée");
        }
        this.status = InvoiceStatus.VERIFIED;
        this.isVerified = true;
        this.verificationDate = LocalDateTime.now();
        this.verifiedBy = userId;
        this.verifiedByName = userName;
        this.setUpdatedAt(LocalDateTime.now());
        this.setUpdatedBy(userId);
    }
    
    /**
     * Payer la facture (VERIFIED → PAID)
     */
    public void pay(String userId, String userName, Money amount) {
        if (status != InvoiceStatus.VERIFIED) {
            throw new IllegalStateException("Seule une facture vérifiée peut être payée");
        }
        this.status = InvoiceStatus.PAID;
        this.paidAmount = amount;
        this.paidAt = LocalDateTime.now();
        this.paidBy = userId;
        this.paidByName = userName;
        this.paymentDate = LocalDate.now();
        this.setUpdatedAt(LocalDateTime.now());
        this.setUpdatedBy(userId);
    }
    
    /**
     * Annuler la facture
     */
    public void cancel(String userId, String reason) {
        if (status == InvoiceStatus.PAID) {
            throw new IllegalStateException("Une facture payée ne peut pas être annulée");
        }
        this.status = InvoiceStatus.CANCELLED;
        this.notes = (this.notes != null ? this.notes + " " : "") + "Annulée: " + reason;
        this.setUpdatedAt(LocalDateTime.now());
        this.setUpdatedBy(userId);
    }
    
    /**
     * Vérifier si la facture est modifiable
     */
    public boolean isModifiable() {
        return status == InvoiceStatus.DRAFT || status == InvoiceStatus.SUBMITTED;
    }
    
    /**
     * Vérifier si la facture est payable
     */
    public boolean isPayable() {
        return status == InvoiceStatus.VERIFIED;
    }
    
    /**
     * Vérifier si la facture est payée
     */
    public boolean isPaid() {
        return status == InvoiceStatus.PAID;
    }
    
    /**
     * Vérifier si la facture a des écarts
     */
    public boolean hasDiscrepancy() {
        return hasDiscrepancy;
    }
    
    // ============================================================
    // EQUALS & HASHCODE
    // ============================================================
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Invoice that = (Invoice) o;
        return Objects.equals(getId(), that.getId()) ||
               Objects.equals(invoiceCode, that.invoiceCode);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(getId(), invoiceCode);
    }
    
    @Override
    public String toString() {
        return "Invoice{" +
                "id=" + getId() +
                ", invoiceCode=" + invoiceCode +
                ", supplierName='" + supplierName + '\'' +
                ", status=" + status +
                ", totalAmount=" + totalAmount +
                ", dueDate=" + dueDate +
                '}';
    }
    
    // ============================================================
    // GETTERS & SETTERS
    // ============================================================

    public InvoiceCode getInvoiceCode() { return invoiceCode; }
    public void setInvoiceCode(InvoiceCode invoiceCode) { this.invoiceCode = invoiceCode; }

    public String getPurchaseOrderId() { return purchaseOrderId; }
    public void setPurchaseOrderId(String purchaseOrderId) { this.purchaseOrderId = purchaseOrderId; }

    public String getPurchaseOrderCode() { return purchaseOrderCode; }
    public void setPurchaseOrderCode(String purchaseOrderCode) { this.purchaseOrderCode = purchaseOrderCode; }

    public String getGoodsReceiptId() { return goodsReceiptId; }
    public void setGoodsReceiptId(String goodsReceiptId) { this.goodsReceiptId = goodsReceiptId; }

    public String getGoodsReceiptCode() { return goodsReceiptCode; }
    public void setGoodsReceiptCode(String goodsReceiptCode) { this.goodsReceiptCode = goodsReceiptCode; }

    public String getSupplierId() { return supplierId; }
    public void setSupplierId(String supplierId) { this.supplierId = supplierId; }

    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }

    public String getSupplierCode() { return supplierCode; }
    public void setSupplierCode(String supplierCode) { this.supplierCode = supplierCode; }

    public InvoiceType getInvoiceType() { return invoiceType; }
    public void setInvoiceType(InvoiceType invoiceType) { this.invoiceType = invoiceType; }

    public InvoiceStatus getStatus() { return status; }
    public void setStatus(InvoiceStatus status) { this.status = status; }

    public String getExternalReference() { return externalReference; }
    public void setExternalReference(String externalReference) { this.externalReference = externalReference; }

    public LocalDate getInvoiceDate() { return invoiceDate; }
    public void setInvoiceDate(LocalDate invoiceDate) { this.invoiceDate = invoiceDate; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public LocalDate getReceivedDate() { return receivedDate; }
    public void setReceivedDate(LocalDate receivedDate) { this.receivedDate = receivedDate; }

    public LocalDate getPaymentDate() { return paymentDate; }
    public void setPaymentDate(LocalDate paymentDate) { this.paymentDate = paymentDate; }

    public Money getTotalAmount() { return totalAmount; }
    public void setTotalAmount(Money totalAmount) { this.totalAmount = totalAmount; }

    public Money getTotalTaxAmount() { return totalTaxAmount; }
    public void setTotalTaxAmount(Money totalTaxAmount) { this.totalTaxAmount = totalTaxAmount; }

    public Money getTotalAmountWithTax() { return totalAmountWithTax; }
    public void setTotalAmountWithTax(Money totalAmountWithTax) { this.totalAmountWithTax = totalAmountWithTax; }

    public String getCurrencyCode() { return currencyCode; }
    public void setCurrencyCode(String currencyCode) { this.currencyCode = currencyCode; }

    public boolean isVerified() { return isVerified; }
    public void setVerified(boolean verified) { isVerified = verified; }

    public boolean isHasDiscrepancy() { return hasDiscrepancy; }
    public void setHasDiscrepancy(boolean hasDiscrepancy) { this.hasDiscrepancy = hasDiscrepancy; }

    public String getDiscrepancySummary() { return discrepancySummary; }
    public void setDiscrepancySummary(String discrepancySummary) { this.discrepancySummary = discrepancySummary; }

    public LocalDateTime getVerificationDate() { return verificationDate; }
    public void setVerificationDate(LocalDateTime verificationDate) { this.verificationDate = verificationDate; }

    public String getVerifiedBy() { return verifiedBy; }
    public void setVerifiedBy(String verifiedBy) { this.verifiedBy = verifiedBy; }

    public String getVerifiedByName() { return verifiedByName; }
    public void setVerifiedByName(String verifiedByName) { this.verifiedByName = verifiedByName; }

    public Money getPaidAmount() { return paidAmount; }
    public void setPaidAmount(Money paidAmount) { this.paidAmount = paidAmount; }

    public LocalDateTime getPaidAt() { return paidAt; }
    public void setPaidAt(LocalDateTime paidAt) { this.paidAt = paidAt; }

    public String getPaidBy() { return paidBy; }
    public void setPaidBy(String paidBy) { this.paidBy = paidBy; }

    public String getPaidByName() { return paidByName; }
    public void setPaidByName(String paidByName) { this.paidByName = paidByName; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getInternalNotes() { return internalNotes; }
    public void setInternalNotes(String internalNotes) { this.internalNotes = internalNotes; }

    public LocalDateTime getObsoletedAt() { return obsoletedAt; }
    public void setObsoletedAt(LocalDateTime obsoletedAt) { this.obsoletedAt = obsoletedAt; }

    public String getObsoletedBy() { return obsoletedBy; }
    public void setObsoletedBy(String obsoletedBy) { this.obsoletedBy = obsoletedBy; }

    public String getObsoletedReason() { return obsoletedReason; }
    public void setObsoletedReason(String obsoletedReason) { this.obsoletedReason = obsoletedReason; }

    public List<InvoiceLine> getLines() { return lines; }
    public void setLines(List<InvoiceLine> lines) { this.lines = lines; }
}
