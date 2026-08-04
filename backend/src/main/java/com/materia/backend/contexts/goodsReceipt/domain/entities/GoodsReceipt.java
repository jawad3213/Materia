package com.materia.backend.contexts.goodsReceipt.domain.entities;


import com.materia.backend.common.domain.BaseEntity;
import com.materia.backend.contexts.goodsReceipt.domain.enums.ReceiptStatus;
import com.materia.backend.contexts.goodsReceipt.domain.exceptions.GoodsReceiptInvalidLineException;
import com.materia.backend.contexts.goodsReceipt.domain.exceptions.GoodsReceiptInvalidQuantityException;
import com.materia.backend.contexts.goodsReceipt.domain.exceptions.GoodsReceiptInvalidStatusTransitionException;
import com.materia.backend.contexts.goodsReceipt.domain.exceptions.GoodsReceiptLineRequiredException;
import com.materia.backend.contexts.goodsReceipt.domain.exceptions.GoodsReceiptNotModifiableException;
import com.materia.backend.contexts.goodsReceipt.domain.exceptions.GoodsReceiptQualityInspectionRequiredException;
import com.materia.backend.contexts.goodsReceipt.domain.exceptions.GoodsReceiptValidationException;
import com.materia.backend.contexts.goodsReceipt.domain.valueObjects.ReceiptCode;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class GoodsReceipt extends BaseEntity {
    
    // ============================================================
    // CONSTANTES
    // ============================================================
    
    public static final int MAX_NOTES_LENGTH = 1000;
    
    // ============================================================
    // ATTRIBUTS - EN-TÊTE
    // ============================================================
    
    private ReceiptCode receiptCode;
    private String purchaseOrderId;
    private String purchaseOrderCode;
    private ReceiptStatus status;
    
    private LocalDate receiptDate;
    private LocalDate expectedDeliveryDate;
    
    private String receivedBy;
    private String receivedByName;
    private String notes;
    
    private String supplierId;
    private String supplierName;
    
    private Integer totalQuantityOrdered;
    private Integer totalQuantityReceived;
    private Integer totalQuantityRejected;
    private Integer totalQuantityAccepted;
    
    private boolean hasDiscrepancy;
    private String discrepancyNotes;
    
    private LocalDateTime obsoletedAt;
    private String obsoletedBy;
    private String obsoletedReason;
    
    // ============================================================
    // ATTRIBUTS - LIGNES
    // ============================================================
    
    private List<GoodsReceiptLine> lines = new ArrayList<>();
    
    // ============================================================
    // CONSTRUCTEURS
    // ============================================================
    
    public GoodsReceipt() {
        super();
    }
    
    public GoodsReceipt(Builder builder) {
        super();
        this.id = builder.id;
        this.receiptCode = builder.receiptCode;
        this.purchaseOrderId = builder.purchaseOrderId;
        this.purchaseOrderCode = builder.purchaseOrderCode;
        this.status = builder.status != null ? builder.status : ReceiptStatus.DRAFT;
        
        this.receiptDate = builder.receiptDate;
        this.expectedDeliveryDate = builder.expectedDeliveryDate;
        
        this.receivedBy = builder.receivedBy;
        this.receivedByName = builder.receivedByName;
        this.notes = builder.notes;
        
        this.supplierId = builder.supplierId;
        this.supplierName = builder.supplierName;
        
        this.totalQuantityOrdered = builder.totalQuantityOrdered;
        this.totalQuantityReceived = builder.totalQuantityReceived;
        this.totalQuantityRejected = builder.totalQuantityRejected;
        this.totalQuantityAccepted = builder.totalQuantityAccepted;
        
        this.hasDiscrepancy = builder.hasDiscrepancy;
        this.discrepancyNotes = builder.discrepancyNotes;
        
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
        private ReceiptCode receiptCode;
        private String purchaseOrderId;
        private String purchaseOrderCode;

        private ReceiptStatus status;
        
        private LocalDate receiptDate;
        private LocalDate expectedDeliveryDate;
        
        private String receivedBy;
        private String receivedByName;
        private String notes;
        
        private String supplierId;
        private String supplierName;
        
        private Integer totalQuantityOrdered;
        private Integer totalQuantityReceived;
        private Integer totalQuantityRejected;
        private Integer totalQuantityAccepted;
        
        private boolean hasDiscrepancy;
        private String discrepancyNotes;
        
        private LocalDateTime obsoletedAt;
        private String obsoletedBy;
        private String obsoletedReason;
        
        private List<GoodsReceiptLine> lines = new ArrayList<>();
        
        private String createdBy;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        
        public Builder id(UUID id) { this.id = id; return this; }
        
        public Builder receiptCode(ReceiptCode receiptCode) { 
            this.receiptCode = receiptCode; 
            return this; 
        }
        
        public Builder receiptCode(String receiptCode) {
            this.receiptCode = ReceiptCode.of(receiptCode);
            return this;
        }
        
        public Builder purchaseOrderId(String purchaseOrderId) { this.purchaseOrderId = purchaseOrderId; return this; }
        public Builder purchaseOrderCode(String purchaseOrderCode) { this.purchaseOrderCode = purchaseOrderCode; return this; }
        public Builder status(ReceiptStatus status) { this.status = status; return this; }
        
        public Builder receiptDate(LocalDate receiptDate) { this.receiptDate = receiptDate; return this; }
        public Builder expectedDeliveryDate(LocalDate expectedDeliveryDate) { this.expectedDeliveryDate = expectedDeliveryDate; return this; }
        
        public Builder receivedBy(String receivedBy) { this.receivedBy = receivedBy; return this; }
        public Builder receivedByName(String receivedByName) { this.receivedByName = receivedByName; return this; }
        public Builder notes(String notes) { this.notes = notes; return this; }
        
        public Builder supplierId(String supplierId) { this.supplierId = supplierId; return this; }
        public Builder supplierName(String supplierName) { this.supplierName = supplierName; return this; }
        
        public Builder totalQuantityOrdered(Integer totalQuantityOrdered) { this.totalQuantityOrdered = totalQuantityOrdered; return this; }
        public Builder totalQuantityReceived(Integer totalQuantityReceived) { this.totalQuantityReceived = totalQuantityReceived; return this; }
        public Builder totalQuantityRejected(Integer totalQuantityRejected) { this.totalQuantityRejected = totalQuantityRejected; return this; }
        public Builder totalQuantityAccepted(Integer totalQuantityAccepted) { this.totalQuantityAccepted = totalQuantityAccepted; return this; }
        
        public Builder hasDiscrepancy(boolean hasDiscrepancy) { this.hasDiscrepancy = hasDiscrepancy; return this; }
        public Builder discrepancyNotes(String discrepancyNotes) { this.discrepancyNotes = discrepancyNotes; return this; }
        
        public Builder obsoletedAt(LocalDateTime obsoletedAt) { this.obsoletedAt = obsoletedAt; return this; }
        public Builder obsoletedBy(String obsoletedBy) { this.obsoletedBy = obsoletedBy; return this; }
        public Builder obsoletedReason(String obsoletedReason) { this.obsoletedReason = obsoletedReason; return this; }
        
        public Builder addLine(GoodsReceiptLine line) {
            if (line == null) {
                throw new GoodsReceiptInvalidLineException("La ligne ne peut pas etre nulle");
            }
            if (this.lines == null) {
                this.lines = new ArrayList<>();
            }
            this.lines.add(line);
            return this;
        }
        
        public Builder lines(List<GoodsReceiptLine> lines) {
            if (lines == null) {
                throw new GoodsReceiptLineRequiredException("La liste des lignes ne peut pas etre nulle");
            }
            this.lines = new ArrayList<>(lines);
            return this;
        }
        
        public Builder createdBy(String createdBy) { this.createdBy = createdBy; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public Builder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }
        
        public GoodsReceipt build() {
            validateRequiredFields();
            validateLines();
            calculateTotals();
            
            if (this.id == null) this.id = UUID.randomUUID();
            
            if (this.receiptCode == null) {
                this.receiptCode = ReceiptCode.createDefault();
            }
            
            if (this.receiptDate == null) {
                this.receiptDate = LocalDate.now();
            }
            
            if (this.createdAt == null) this.createdAt = LocalDateTime.now();
            if (this.updatedAt == null) this.updatedAt = LocalDateTime.now();
            
            if (this.status == null) this.status = ReceiptStatus.DRAFT;
            
            return new GoodsReceipt(this);
        }
        
        private void validateRequiredFields() {
            if (this.purchaseOrderId == null || this.purchaseOrderId.trim().isEmpty()) {
                throw new GoodsReceiptValidationException("La commande d'achat est obligatoire");
            }
            if (this.receivedBy == null || this.receivedBy.trim().isEmpty()) {
                throw new GoodsReceiptValidationException("Le receptionnaire est obligatoire");
            }
            if (this.receivedByName == null || this.receivedByName.trim().isEmpty()) {
                throw new GoodsReceiptValidationException("Le nom du receptionnaire est obligatoire");
            }
        }
        
        private void validateLines() {
            if (this.lines == null || this.lines.isEmpty()) {
                throw new GoodsReceiptLineRequiredException("Au moins une ligne est requise");
            }
            for (int i = 0; i < this.lines.size(); i++) {
                GoodsReceiptLine line = this.lines.get(i);
                if (line.getMaterialCode() == null || line.getMaterialCode().trim().isEmpty()) {
                    throw new GoodsReceiptInvalidLineException("Le materiau est obligatoire pour la ligne " + (i + 1));
                }
                if (line.getQuantityReceived() == null || line.getQuantityReceived() < 0) {
                    throw new GoodsReceiptInvalidQuantityException("La quantite recue est invalide pour la ligne " + (i + 1));
                }
                if (line.getQuantityRejected() == null || line.getQuantityRejected() < 0) {
                    throw new GoodsReceiptInvalidQuantityException("La quantite rejetee est invalide pour la ligne " + (i + 1));
                }
            }
        }
        
        private void calculateTotals() {
            if (this.lines == null || this.lines.isEmpty()) {
                this.totalQuantityOrdered = 0;
                this.totalQuantityReceived = 0;
                this.totalQuantityRejected = 0;
                this.totalQuantityAccepted = 0;
                return;
            }

            this.totalQuantityOrdered = this.lines.stream()
                .mapToInt(line -> line.getQuantityOrdered() != null ? line.getQuantityOrdered() : 0)
                .sum();
            
            this.totalQuantityReceived = this.lines.stream()
                .mapToInt(line -> line.getQuantityReceived() != null ? line.getQuantityReceived() : 0)
                .sum();
            
            this.totalQuantityRejected = this.lines.stream()
                .mapToInt(line -> line.getQuantityRejected() != null ? line.getQuantityRejected() : 0)
                .sum();
            
            this.totalQuantityAccepted = this.totalQuantityReceived - this.totalQuantityRejected;
            
            this.hasDiscrepancy = this.lines.stream().anyMatch(line ->
                (line.getQuantityReceived() != null && line.getQuantityOrdered() != null
                    && !line.getQuantityReceived().equals(line.getQuantityOrdered()))
                    || (line.getQuantityRejected() != null && line.getQuantityRejected() > 0)
            );
        }
    }
    
    // ============================================================
    // DOMAINE BEHAVIOR
    // ============================================================
    
    public void complete(String userId) {
        if (status != ReceiptStatus.IN_PROGRESS && status != ReceiptStatus.DRAFT) {
            throw new GoodsReceiptInvalidStatusTransitionException(
                "Seule une reception en cours ou en brouillon peut etre terminee"
            );
        }
        
        for (GoodsReceiptLine line : this.lines) {
            if (line.getQualityStatus() == null || line.getQualityStatus().isPending()) {
                throw new GoodsReceiptQualityInspectionRequiredException(
                    "Le controle qualite doit etre termine pour la ligne: " + line.getMaterialCode()
                );
            }
            if (line.getQuantityRejected() != null && line.getQuantityRejected() > 0
                    && (line.getRejectionReason() == null || line.getRejectionReason().isBlank())) {
                throw new GoodsReceiptValidationException(
                    "Le motif de rejet est obligatoire pour la ligne: " + line.getMaterialCode()
                );
            }
        }
        
        this.status = this.hasDiscrepancy ? ReceiptStatus.PARTIAL : ReceiptStatus.COMPLETED;
        this.receiptDate = LocalDate.now();
        this.setUpdatedAt(LocalDateTime.now());
        this.setUpdatedBy(userId);
    }
    
    public void cancel(String userId, String reason) {
        if (!status.isCancellable()) {
            throw new GoodsReceiptInvalidStatusTransitionException(
                "Cette reception ne peut pas etre annulee"
            );
        }
        this.status = ReceiptStatus.CANCELLED;
        this.notes = (this.notes != null ? this.notes + " " : "") + "Annulée: " + reason;
        this.setUpdatedAt(LocalDateTime.now());
        this.setUpdatedBy(userId);
    }
    
    public void addLine(GoodsReceiptLine line) {
        if (line == null) {
            throw new GoodsReceiptInvalidLineException("La ligne ne peut pas etre nulle");
        }
        if (status != ReceiptStatus.DRAFT && status != ReceiptStatus.IN_PROGRESS) {
            throw new GoodsReceiptNotModifiableException(
                "Impossible d'ajouter une ligne a une reception terminee"
            );
        }
        this.lines.add(line);
        recalculateTotals();
        this.setUpdatedAt(LocalDateTime.now());
    }
    
    public void removeLine(int index) {
        if (index < 0 || index >= this.lines.size()) {
            throw new GoodsReceiptInvalidLineException("Index de ligne invalide: " + index);
        }
        if (status != ReceiptStatus.DRAFT && status != ReceiptStatus.IN_PROGRESS) {
            throw new GoodsReceiptNotModifiableException(
                "Impossible de supprimer une ligne d'une reception terminee"
            );
        }
        this.lines.remove(index);
        recalculateTotals();
        this.setUpdatedAt(LocalDateTime.now());
    }
    
    public void recalculateTotals() {
        if (this.lines == null || this.lines.isEmpty()) {
            this.totalQuantityOrdered = 0;
            this.totalQuantityReceived = 0;
            this.totalQuantityRejected = 0;
            this.totalQuantityAccepted = 0;
            this.hasDiscrepancy = false;
            return;
        }

        this.totalQuantityOrdered = this.lines.stream()
            .mapToInt(line -> line.getQuantityOrdered() != null ? line.getQuantityOrdered() : 0)
            .sum();
        
        this.totalQuantityReceived = this.lines.stream()
            .mapToInt(line -> line.getQuantityReceived() != null ? line.getQuantityReceived() : 0)
            .sum();
        
        this.totalQuantityRejected = this.lines.stream()
            .mapToInt(line -> line.getQuantityRejected() != null ? line.getQuantityRejected() : 0)
            .sum();
        
        this.totalQuantityAccepted = this.totalQuantityReceived - this.totalQuantityRejected;
        
        this.hasDiscrepancy = this.lines.stream().anyMatch(line ->
            (line.getQuantityReceived() != null && line.getQuantityOrdered() != null
                && !line.getQuantityReceived().equals(line.getQuantityOrdered()))
                || (line.getQuantityRejected() != null && line.getQuantityRejected() > 0)
        );
    }
    
    public boolean isComplete() {
        return status == ReceiptStatus.COMPLETED || status == ReceiptStatus.PARTIAL;
    }
    
    public boolean hasDiscrepancy() {
        return this.hasDiscrepancy;
    }
    
    // ============================================================
    // EQUALS & HASHCODE
    // ============================================================
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GoodsReceipt that = (GoodsReceipt) o;
        return Objects.equals(getId(), that.getId()) ||
               Objects.equals(receiptCode, that.receiptCode);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(getId(), receiptCode);
    }
    
    @Override
    public String toString() {
        return "GoodsReceipt{" +
                "id=" + getId() +
                ", receiptCode=" + receiptCode +
                ", purchaseOrderCode='" + purchaseOrderCode + '\'' +
                ", status=" + status +
                ", receiptDate=" + receiptDate +
                ", totalQuantityReceived=" + totalQuantityReceived +
                ", totalQuantityRejected=" + totalQuantityRejected +
                '}';
    }
    
    // ============================================================
    // GETTERS & SETTERS
    // ============================================================
    
    public ReceiptCode getReceiptCode() { return receiptCode; }
    public void setReceiptCode(ReceiptCode receiptCode) { this.receiptCode = receiptCode; }
    
    public String getPurchaseOrderId() { return purchaseOrderId; }
    public void setPurchaseOrderId(String purchaseOrderId) { this.purchaseOrderId = purchaseOrderId; }
    
    public String getPurchaseOrderCode() { return purchaseOrderCode; }
    public void setPurchaseOrderCode(String purchaseOrderCode) { this.purchaseOrderCode = purchaseOrderCode; }
    
    public ReceiptStatus getStatus() { return status; }
    public void setStatus(ReceiptStatus status) { this.status = status; }
    
    public LocalDate getReceiptDate() { return receiptDate; }
    public void setReceiptDate(LocalDate receiptDate) { this.receiptDate = receiptDate; }
    
    public LocalDate getExpectedDeliveryDate() { return expectedDeliveryDate; }
    public void setExpectedDeliveryDate(LocalDate expectedDeliveryDate) { this.expectedDeliveryDate = expectedDeliveryDate; }
    
    public String getReceivedBy() { return receivedBy; }
    public void setReceivedBy(String receivedBy) { this.receivedBy = receivedBy; }
    
    public String getReceivedByName() { return receivedByName; }
    public void setReceivedByName(String receivedByName) { this.receivedByName = receivedByName; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    public String getSupplierId() { return supplierId; }
    public void setSupplierId(String supplierId) { this.supplierId = supplierId; }
    
    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }
    
    public Integer getTotalQuantityOrdered() { return totalQuantityOrdered; }
    public void setTotalQuantityOrdered(Integer totalQuantityOrdered) { this.totalQuantityOrdered = totalQuantityOrdered; }
    
    public Integer getTotalQuantityReceived() { return totalQuantityReceived; }
    public void setTotalQuantityReceived(Integer totalQuantityReceived) { this.totalQuantityReceived = totalQuantityReceived; }
    
    public Integer getTotalQuantityRejected() { return totalQuantityRejected; }
    public void setTotalQuantityRejected(Integer totalQuantityRejected) { this.totalQuantityRejected = totalQuantityRejected; }
    
    public Integer getTotalQuantityAccepted() { return totalQuantityAccepted; }
    public void setTotalQuantityAccepted(Integer totalQuantityAccepted) { this.totalQuantityAccepted = totalQuantityAccepted; }
    
    public boolean isHasDiscrepancy() { return hasDiscrepancy; }
    public void setHasDiscrepancy(boolean hasDiscrepancy) { this.hasDiscrepancy = hasDiscrepancy; }
    
    public String getDiscrepancyNotes() { return discrepancyNotes; }
    public void setDiscrepancyNotes(String discrepancyNotes) { this.discrepancyNotes = discrepancyNotes; }
    
    public LocalDateTime getObsoletedAt() { return obsoletedAt; }
    public void setObsoletedAt(LocalDateTime obsoletedAt) { this.obsoletedAt = obsoletedAt; }
    
    public String getObsoletedBy() { return obsoletedBy; }
    public void setObsoletedBy(String obsoletedBy) { this.obsoletedBy = obsoletedBy; }
    
    public String getObsoletedReason() { return obsoletedReason; }
    public void setObsoletedReason(String obsoletedReason) { this.obsoletedReason = obsoletedReason; }
    
    public List<GoodsReceiptLine> getLines() { return lines; }
    public void setLines(List<GoodsReceiptLine> lines) { this.lines = lines; }
}
