package com.materia.backend.contexts.payement.domain.entities;

import com.materia.backend.common.domain.BaseEntity;
import com.materia.backend.contexts.payement.domain.enums.PaymentStatus;
import com.materia.backend.contexts.payement.domain.exceptions.PaymentNotCancellableException;
import com.materia.backend.contexts.payement.domain.exceptions.PaymentNotCompletableException;
import com.materia.backend.contexts.payement.domain.exceptions.PaymentNotPreparableException;
import com.materia.backend.contexts.payement.domain.exceptions.PaymentValidationException;
import com.materia.backend.contexts.payement.domain.valueObjects.PaymentCode;
import com.materia.backend.common.domain.valueObjects.Money;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Payment Domain Entity - Version Simplifiée (MVP)
 * 
 * POJO Pur - Sans annotations Spring/Lombok
 * 
 * @author SAP MM Team
 * @version 1.0
 */
public class Payment extends BaseEntity {
    
    // ============================================================
    // CONSTANTES
    // ============================================================
    
    public static final int MAX_NOTES_LENGTH = 1000;
    
    // ============================================================
    // ATTRIBUTS - EN-TÊTE
    // ============================================================
    
    private PaymentCode paymentCode;
    private String supplierId;
    private String supplierName;
    private String supplierCode;
    private PaymentStatus status;
    
    // ============================================================
    // ATTRIBUTS - MONTANTS
    // ============================================================
    
    private Money totalAmount;
    private Money paidAmount;
    private String currencyCode;
    
    // ============================================================
    // ATTRIBUTS - DATES
    // ============================================================
    
    private LocalDateTime paymentDate;
    private LocalDateTime confirmedDate;
    
    // ============================================================
    // ATTRIBUTS - PAIEMENT RÉEL (saisi par l'Admin)
    // ============================================================
    
    private String bankReference;      // Référence du virement
    private String transactionId;      // ID de la transaction bancaire
    private String paymentMethod;      // Virement, Chèque, Espèces
    private String paymentReceipt;     // Lien vers le reçu de paiement
    
    // ============================================================
    // ATTRIBUTS - NOTES
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
    // ATTRIBUTS - LIGNES (Factures payées)
    // ============================================================
    
    private List<PaymentLine> lines = new ArrayList<>();
    
    // ============================================================
    // CONSTRUCTEURS
    // ============================================================
    
    public Payment() {
        super();
    }
    
    public Payment(Builder builder) {
        super();
        this.id = builder.id;
        this.paymentCode = builder.paymentCode;
        this.supplierId = builder.supplierId;
        this.supplierName = builder.supplierName;
        this.supplierCode = builder.supplierCode;
        this.status = builder.status != null ? builder.status : PaymentStatus.DRAFT;
        
        this.totalAmount = builder.totalAmount;
        this.paidAmount = builder.paidAmount;
        this.currencyCode = builder.currencyCode != null ? builder.currencyCode : "MAD";
        
        this.paymentDate = builder.paymentDate;
        this.confirmedDate = builder.confirmedDate;
        
        this.bankReference = builder.bankReference;
        this.transactionId = builder.transactionId;
        this.paymentMethod = builder.paymentMethod;
        this.paymentReceipt = builder.paymentReceipt;
        
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
        private PaymentCode paymentCode;
        private String supplierId;
        private String supplierName;
        private String supplierCode;
        private PaymentStatus status;
        
        private Money totalAmount;
        private Money paidAmount;
        private String currencyCode;
        
        private LocalDateTime paymentDate;
        private LocalDateTime confirmedDate;
        
        private String bankReference;
        private String transactionId;
        private String paymentMethod;
        private String paymentReceipt;
        
        private String notes;
        private String internalNotes;
        
        private LocalDateTime obsoletedAt;
        private String obsoletedBy;
        private String obsoletedReason;
        
        private List<PaymentLine> lines = new ArrayList<>();
        
        private String createdBy;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        
        public Builder id(UUID id) { this.id = id; return this; }
        
        public Builder paymentCode(PaymentCode paymentCode) { 
            this.paymentCode = paymentCode; 
            return this; 
        }
        
        public Builder paymentCode(String paymentCode) {
            this.paymentCode = PaymentCode.of(paymentCode);
            return this;
        }
        
        public Builder supplierId(String supplierId) { this.supplierId = supplierId; return this; }
        public Builder supplierName(String supplierName) { this.supplierName = supplierName; return this; }
        public Builder supplierCode(String supplierCode) { this.supplierCode = supplierCode; return this; }
        public Builder status(PaymentStatus status) { this.status = status; return this; }
        
        public Builder totalAmount(Money totalAmount) { this.totalAmount = totalAmount; return this; }
        public Builder paidAmount(Money paidAmount) { this.paidAmount = paidAmount; return this; }
        public Builder currencyCode(String currencyCode) { this.currencyCode = currencyCode; return this; }
        
        public Builder paymentDate(LocalDateTime paymentDate) { this.paymentDate = paymentDate; return this; }
        public Builder confirmedDate(LocalDateTime confirmedDate) { this.confirmedDate = confirmedDate; return this; }
        
        public Builder bankReference(String bankReference) { this.bankReference = bankReference; return this; }
        public Builder transactionId(String transactionId) { this.transactionId = transactionId; return this; }
        public Builder paymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; return this; }
        public Builder paymentReceipt(String paymentReceipt) { this.paymentReceipt = paymentReceipt; return this; }
        
        public Builder notes(String notes) { this.notes = notes; return this; }
        public Builder internalNotes(String internalNotes) { this.internalNotes = internalNotes; return this; }
        
        public Builder obsoletedAt(LocalDateTime obsoletedAt) { this.obsoletedAt = obsoletedAt; return this; }
        public Builder obsoletedBy(String obsoletedBy) { this.obsoletedBy = obsoletedBy; return this; }
        public Builder obsoletedReason(String obsoletedReason) { this.obsoletedReason = obsoletedReason; return this; }
        
        public Builder addLine(PaymentLine line) {
            if (line == null) {
                throw new IllegalArgumentException("La ligne ne peut pas être nulle");
            }
            if (this.lines == null) {
                this.lines = new ArrayList<>();
            }
            this.lines.add(line);
            return this;
        }
        
        public Builder lines(List<PaymentLine> lines) {
            if (lines == null) {
                throw new IllegalArgumentException("La liste des lignes ne peut pas être nulle");
            }
            this.lines = new ArrayList<>(lines);
            return this;
        }
        
        public Builder createdBy(String createdBy) { this.createdBy = createdBy; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public Builder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }
        
        public Payment build() {
            if (this.currencyCode == null) this.currencyCode = "MAD";
            // The total is derived from the lines (one amount per invoice paid), never trusted from the request.
            if (this.lines != null && !this.lines.isEmpty()) {
                this.totalAmount = sumLines(this.lines, this.currencyCode);
            }
            validateRequiredFields();
            validateLines();
            
            if (this.id == null) this.id = UUID.randomUUID();
            
            if (this.paymentCode == null) {
                this.paymentCode = PaymentCode.createDefault();
            }
            
            if (this.createdAt == null) this.createdAt = LocalDateTime.now();
            if (this.updatedAt == null) this.updatedAt = LocalDateTime.now();
            
            if (this.status == null) this.status = PaymentStatus.DRAFT;
            if (this.currencyCode == null) this.currencyCode = "MAD";
            
            return new Payment(this);
        }
        
        private void validateRequiredFields() {
            if (this.supplierId == null || this.supplierId.trim().isEmpty()) {
                throw new IllegalArgumentException("Le fournisseur est obligatoire");
            }
            // The supplier name is taken from the invoices by the payment service before saving.
            if (this.totalAmount == null) {
                throw new IllegalArgumentException("Le montant total est obligatoire");
            }
        }
        
        private void validateLines() {
            if (this.lines == null || this.lines.isEmpty()) {
                throw new IllegalArgumentException("Au moins une ligne est requise");
            }
            for (int i = 0; i < this.lines.size(); i++) {
                PaymentLine line = this.lines.get(i);
                if (line.getInvoiceId() == null || line.getInvoiceId().trim().isEmpty()) {
                    throw new IllegalArgumentException(
                        "L'ID de la facture est obligatoire pour la ligne " + (i + 1)
                    );
                }
            }
        }
    }
    
    /** Accepted payment methods; transfers and cheques must carry a bank reference. */
    public static final Set<String> PAYMENT_METHODS = Set.of("BANK_TRANSFER", "CHECK", "CASH", "CARD");
    private static final Set<String> METHODS_NEEDING_REFERENCE = Set.of("BANK_TRANSFER", "CHECK");

    /** Sum of the line amounts, in the payment currency. */
    private static Money sumLines(List<PaymentLine> lines, String currencyCode) {
        Money total = Money.zero(currencyCode);
        for (PaymentLine line : lines) {
            if (line != null && line.getAmount() != null) {
                total = total.add(line.getAmount());
            }
        }
        return total;
    }

    /** Recomputes the total from the lines after they changed. */
    public void recalculateTotal() {
        this.totalAmount = sumLines(this.lines != null ? this.lines : List.of(), currencyCode != null ? currencyCode : "MAD");
    }

    // ============================================================
    // DOMAINE BEHAVIOR - Version Simplifiée
    // ============================================================
    
    /**
     * Préparer le paiement (DRAFT → PENDING)
     * L'Admin prépare le paiement, mais ne l'exécute pas encore
     */
    public void prepare(String userId) {
        if (status != PaymentStatus.DRAFT) {
            throw new PaymentNotPreparableException(String.valueOf(getId()), "seul un paiement en brouillon peut être préparé");
        }
        this.status = PaymentStatus.PENDING;
        this.paymentDate = LocalDateTime.now();
        this.setUpdatedAt(LocalDateTime.now());
        this.setUpdatedBy(userId);
    }
    
    /**
     * Marquer le paiement comme effectué (PENDING → COMPLETED)
     * L'Admin a payé dans la vraie vie et l'enregistre
     */
    public void markAsCompleted(String userId, String bankReference, String transactionId, String paymentMethod) {
        if (this.status == PaymentStatus.COMPLETED) {
            throw new PaymentNotCompletableException(String.valueOf(getId()), "ce paiement est déjà marqué comme payé");
        }
        if (this.status != PaymentStatus.PENDING && this.status != PaymentStatus.DRAFT) {
            throw new PaymentNotCompletableException(String.valueOf(getId()),
                    "seul un paiement en attente ou en brouillon peut être marqué comme payé");
        }
        String method = paymentMethod != null ? paymentMethod.trim().toUpperCase(Locale.ROOT) : "";
        if (!PAYMENT_METHODS.contains(method)) {
            throw new PaymentValidationException("Méthode de paiement inconnue : " + paymentMethod
                    + " (attendu : " + String.join(", ", PAYMENT_METHODS) + ")");
        }
        if (METHODS_NEEDING_REFERENCE.contains(method) && (bankReference == null || bankReference.isBlank())) {
            throw new PaymentValidationException("La référence bancaire est obligatoire pour un paiement par " + method);
        }

        this.status = PaymentStatus.COMPLETED;
        this.paymentDate = LocalDateTime.now();
        this.confirmedDate = LocalDateTime.now();
        this.bankReference = bankReference != null && !bankReference.isBlank() ? bankReference.trim() : null;
        this.transactionId = transactionId != null && !transactionId.isBlank() ? transactionId.trim() : null;
        this.paymentMethod = method;
        this.paidAmount = this.totalAmount;
        if (this.lines != null) {
            this.lines.forEach(PaymentLine::markAsPaid);
        }
        
        this.setUpdatedAt(LocalDateTime.now());
        this.setUpdatedBy(userId);
    }
    
    /**
     * Annuler le paiement
     */
    public void cancel(String userId, String reason) {
        if (this.status == PaymentStatus.COMPLETED || this.status == PaymentStatus.CANCELLED) {
            throw new PaymentNotCancellableException(String.valueOf(getId()),
                    this.status == PaymentStatus.COMPLETED ? "un paiement effectué ne peut pas être annulé" : "il est déjà annulé");
        }
        if (reason == null || reason.isBlank()) {
            throw new PaymentValidationException("Le motif d'annulation est obligatoire");
        }
        String updatedNotes = (this.notes != null ? this.notes + " " : "") + "Annulé: " + reason;
        if (updatedNotes.length() > 1000) {
            throw new PaymentValidationException("Le motif est trop long pour les notes du paiement");
        }
        this.status = PaymentStatus.CANCELLED;
        this.notes = updatedNotes;
        this.setUpdatedAt(LocalDateTime.now());
        this.setUpdatedBy(userId);
    }
    
    /**
     * Vérifier si le paiement est modifiable
     */
    public boolean isModifiable() {
        return status != null && status.isModifiable();
    }
    
    /**
     * Vérifier si le paiement est exécutable
     */
    public boolean isExecutable() {
        return status != null && status.isExecutable();
    }
    
    /**
     * Vérifier si le paiement est terminé
     */
    public boolean isCompleted() {
        return status == PaymentStatus.COMPLETED;
    }
    
    /**
     * Vérifier si le paiement peut être annulé
     */
    public boolean isCancellable() {
        return status != null && status.isCancellable();
    }
    
    // ============================================================
    // EQUALS & HASHCODE
    // ============================================================
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Payment that = (Payment) o;
        return Objects.equals(getId(), that.getId()) ||
               Objects.equals(paymentCode, that.paymentCode);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(getId(), paymentCode);
    }
    
    @Override
    public String toString() {
        return "Payment{" +
                "id=" + getId() +
                ", paymentCode=" + paymentCode +
                ", supplierName='" + supplierName + '\'' +
                ", status=" + status +
                ", totalAmount=" + totalAmount +
                ", paymentDate=" + paymentDate +
                '}';
    }
    
    // ============================================================
    // GETTERS & SETTERS
    // ============================================================
    
    public PaymentCode getPaymentCode() { return paymentCode; }
    public void setPaymentCode(PaymentCode paymentCode) { this.paymentCode = paymentCode; }
    
    public String getSupplierId() { return supplierId; }
    public void setSupplierId(String supplierId) { this.supplierId = supplierId; }
    
    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }
    
    public String getSupplierCode() { return supplierCode; }
    public void setSupplierCode(String supplierCode) { this.supplierCode = supplierCode; }
    
    public PaymentStatus getStatus() { return status; }
    public void setStatus(PaymentStatus status) { this.status = status; }
    
    public Money getTotalAmount() { return totalAmount; }
    public void setTotalAmount(Money totalAmount) { this.totalAmount = totalAmount; }
    
    public Money getPaidAmount() { return paidAmount; }
    public void setPaidAmount(Money paidAmount) { this.paidAmount = paidAmount; }
    
    public String getCurrencyCode() { return currencyCode; }
    public void setCurrencyCode(String currencyCode) { this.currencyCode = currencyCode; }
    
    public LocalDateTime getPaymentDate() { return paymentDate; }
    public void setPaymentDate(LocalDateTime paymentDate) { this.paymentDate = paymentDate; }
    
    public LocalDateTime getConfirmedDate() { return confirmedDate; }
    public void setConfirmedDate(LocalDateTime confirmedDate) { this.confirmedDate = confirmedDate; }
    
    public String getBankReference() { return bankReference; }
    public void setBankReference(String bankReference) { this.bankReference = bankReference; }
    
    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
    
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    
    public String getPaymentReceipt() { return paymentReceipt; }
    public void setPaymentReceipt(String paymentReceipt) { this.paymentReceipt = paymentReceipt; }
    
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
    
    public List<PaymentLine> getLines() { return lines; }
    public void setLines(List<PaymentLine> lines) { this.lines = lines; }
}
