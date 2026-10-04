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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Goods rejected at receipt and sent back to the supplier.
 *
 * <p>Lifecycle: a DRAFT is prepared from a completed goods receipt; submitting it (PENDING) records that the
 * goods were shipped back; the supplier then resolves it (RESOLVED) either by sending a replacement or by
 * issuing a credit note. A draft or pending return can be cancelled.
 */
public class ReturnToVendor extends BaseEntity {

    public static final int MAX_REASON_LENGTH = 500;
    public static final int MAX_NOTES_LENGTH = 1000;
    public static final int MAX_RESPONSE_LENGTH = 500;

    // ---- Identification ----
    private ReturnCode returnCode;
    private String goodsReceiptId;
    private String goodsReceiptCode;
    private String purchaseOrderId;
    private String purchaseOrderCode;

    // ---- Supplier ----
    private String supplierId;
    private String supplierName;
    private String supplierCode;
    private String currencyCode;

    // ---- Status ----
    private ReturnStatus status;
    private ResolutionType resolutionType;

    // ---- Dates ----
    private LocalDate returnDate;
    private LocalDate resolutionDate;

    // ---- Details ----
    private String returnReason;
    private String supplierResponse;
    private String rejectionSummary;

    // ---- Resolution ----
    private String creditNoteReference;
    private String creditNoteAmount;
    private String replacementPurchaseOrderReference;
    private String replacementPurchaseOrderCode;

    // ---- Communication ----
    private String notes;
    private String internalNotes;

    private List<ReturnToVendorLine> lines = new ArrayList<>();

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
        this.currencyCode = builder.currencyCode;
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
        private ReturnCode returnCode;
        private String goodsReceiptId;
        private String goodsReceiptCode;
        private String purchaseOrderId;
        private String purchaseOrderCode;
        private String supplierId;
        private String supplierName;
        private String supplierCode;
        private String currencyCode;
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
        private Long version;

        private String createdBy;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder returnCode(ReturnCode returnCode) { this.returnCode = returnCode; return this; }
        public Builder returnCode(String returnCode) { this.returnCode = ReturnCode.of(returnCode); return this; }
        public Builder goodsReceiptId(String goodsReceiptId) { this.goodsReceiptId = goodsReceiptId; return this; }
        public Builder goodsReceiptCode(String goodsReceiptCode) { this.goodsReceiptCode = goodsReceiptCode; return this; }
        public Builder purchaseOrderId(String purchaseOrderId) { this.purchaseOrderId = purchaseOrderId; return this; }
        public Builder purchaseOrderCode(String purchaseOrderCode) { this.purchaseOrderCode = purchaseOrderCode; return this; }
        public Builder supplierId(String supplierId) { this.supplierId = supplierId; return this; }
        public Builder supplierName(String supplierName) { this.supplierName = supplierName; return this; }
        public Builder supplierCode(String supplierCode) { this.supplierCode = supplierCode; return this; }
        public Builder currencyCode(String currencyCode) { this.currencyCode = currencyCode; return this; }
        public Builder status(ReturnStatus status) { this.status = status; return this; }
        public Builder resolutionType(ResolutionType resolutionType) { this.resolutionType = resolutionType; return this; }
        public Builder returnDate(LocalDate returnDate) { this.returnDate = returnDate; return this; }
        public Builder resolutionDate(LocalDate resolutionDate) { this.resolutionDate = resolutionDate; return this; }
        public Builder returnReason(String returnReason) { this.returnReason = returnReason; return this; }
        public Builder supplierResponse(String supplierResponse) { this.supplierResponse = supplierResponse; return this; }
        public Builder rejectionSummary(String rejectionSummary) { this.rejectionSummary = rejectionSummary; return this; }
        public Builder creditNoteReference(String creditNoteReference) { this.creditNoteReference = creditNoteReference; return this; }
        public Builder creditNoteAmount(String creditNoteAmount) { this.creditNoteAmount = creditNoteAmount; return this; }
        public Builder replacementPurchaseOrderReference(String reference) { this.replacementPurchaseOrderReference = reference; return this; }
        public Builder replacementPurchaseOrderCode(String code) { this.replacementPurchaseOrderCode = code; return this; }
        public Builder notes(String notes) { this.notes = notes; return this; }
        public Builder internalNotes(String internalNotes) { this.internalNotes = internalNotes; return this; }
        public Builder version(Long version) { this.version = version; return this; }

        public Builder addLine(ReturnToVendorLine line) {
            if (line == null) {
                throw new ReturnToVendorInvalidLineException("A return line cannot be null");
            }
            this.lines.add(line);
            return this;
        }

        public Builder lines(List<ReturnToVendorLine> lines) {
            if (lines == null) {
                throw new ReturnToVendorLineRequiredException("The list of return lines cannot be null");
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
            if (this.returnCode == null) this.returnCode = ReturnCode.createDefault();
            if (this.returnDate == null) this.returnDate = LocalDate.now();
            if (this.createdAt == null) this.createdAt = LocalDateTime.now();
            if (this.updatedAt == null) this.updatedAt = LocalDateTime.now();
            if (this.status == null) this.status = ReturnStatus.DRAFT;
            return new ReturnToVendor(this);
        }

        private void validateRequiredFields() {
            if (isBlank(this.goodsReceiptId)) {
                throw new ReturnToVendorValidationException("The goods receipt is required");
            }
            if (isBlank(this.supplierId)) {
                throw new ReturnToVendorValidationException("The supplier is required");
            }
            if (isBlank(this.supplierName)) {
                throw new ReturnToVendorValidationException("The supplier name is required");
            }
            if (isBlank(this.returnReason)) {
                throw new ReturnToVendorValidationException("The return reason is required");
            }
            if (this.returnReason.length() > MAX_REASON_LENGTH) {
                throw new ReturnToVendorValidationException(
                        "The return reason cannot exceed " + MAX_REASON_LENGTH + " characters");
            }
        }

        private void validateLines() {
            if (this.lines == null || this.lines.isEmpty()) {
                throw new ReturnToVendorLineRequiredException("A return must contain at least one line");
            }
        }
    }

    // ============================================================
    // DOMAIN BEHAVIOUR
    // ============================================================

    /** The goods were shipped back to the supplier. */
    public void submit(String userId) {
        if (status != ReturnStatus.DRAFT) {
            throw new ReturnToVendorInvalidStatusTransitionException("Only a draft return can be submitted");
        }
        lines.forEach(ReturnToVendorLine::markShipped);
        this.status = ReturnStatus.PENDING;
        updateAudit(userId);
    }

    /**
     * The supplier settled the return: a replacement delivery (the reference is the supplier's delivery or
     * order reference) or a credit note (the reference is the credit note number; its amount is the value of
     * the returned goods at purchase-order prices).
     */
    public void resolve(String userId, ResolutionType type, String reference, String response) {
        if (status != ReturnStatus.PENDING) {
            throw new ReturnToVendorInvalidStatusTransitionException("Only a pending return can be resolved");
        }
        if (type == null) {
            throw new ReturnToVendorValidationException("The resolution type is required");
        }
        if (isBlank(reference)) {
            throw new ReturnToVendorValidationException(type == ResolutionType.CREDIT_NOTE
                    ? "The credit note number is required"
                    : "The replacement reference is required");
        }
        if (response != null && response.length() > MAX_RESPONSE_LENGTH) {
            throw new ReturnToVendorValidationException(
                    "The supplier response cannot exceed " + MAX_RESPONSE_LENGTH + " characters");
        }

        this.status = ReturnStatus.RESOLVED;
        this.resolutionType = type;
        this.resolutionDate = LocalDate.now();
        if (response != null && !response.isBlank()) {
            this.supplierResponse = response.trim();
        }
        if (type == ResolutionType.REPLACEMENT) {
            this.replacementPurchaseOrderReference = reference.trim();
            lines.forEach(line -> line.setReplaced(true));
        } else {
            this.creditNoteReference = reference.trim();
            this.creditNoteAmount = totalValue().toPlainString();
            lines.forEach(line -> line.setCreditNote(true));
        }
        updateAudit(userId);
    }

    public void cancel(String userId, String reason) {
        if (status == ReturnStatus.RESOLVED) {
            throw new ReturnToVendorNotModifiableException("A resolved return cannot be cancelled");
        }
        if (status == ReturnStatus.CANCELLED) {
            throw new ReturnToVendorInvalidStatusTransitionException("This return is already cancelled");
        }
        if (isBlank(reason)) {
            throw new ReturnToVendorValidationException("A cancellation reason is required");
        }
        this.status = ReturnStatus.CANCELLED;
        this.notes = (isBlank(this.notes) ? "" : this.notes + "\n") + "Cancelled: " + reason.trim();
        updateAudit(userId);
    }

    /** Value of the returned goods at purchase-order prices. */
    public BigDecimal totalValue() {
        return lines.stream().map(ReturnToVendorLine::lineValue).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public int totalQuantity() {
        return lines.stream().mapToInt(line -> line.getQuantityToReturn() != null ? line.getQuantityToReturn() : 0).sum();
    }

    /** Draft and pending returns hold their quantities; cancelled ones release them. */
    public boolean holdsQuantities() {
        return status != ReturnStatus.CANCELLED;
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

    public boolean isReplacement() {
        return resolutionType == ResolutionType.REPLACEMENT;
    }

    public boolean isCreditNote() {
        return resolutionType == ResolutionType.CREDIT_NOTE;
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReturnToVendor that = (ReturnToVendor) o;
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }

    @Override
    public String toString() {
        return "ReturnToVendor{id=" + getId() + ", returnCode=" + returnCode + ", supplierName='" + supplierName
                + "', status=" + status + ", resolutionType=" + resolutionType + '}';
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

    public String getCurrencyCode() { return currencyCode; }
    public void setCurrencyCode(String currencyCode) { this.currencyCode = currencyCode; }

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
    public void setReplacementPurchaseOrderReference(String reference) { this.replacementPurchaseOrderReference = reference; }

    public String getReplacementPurchaseOrderCode() { return replacementPurchaseOrderCode; }
    public void setReplacementPurchaseOrderCode(String code) { this.replacementPurchaseOrderCode = code; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getInternalNotes() { return internalNotes; }
    public void setInternalNotes(String internalNotes) { this.internalNotes = internalNotes; }

    public List<ReturnToVendorLine> getLines() { return lines; }
    public void setLines(List<ReturnToVendorLine> lines) { this.lines = lines != null ? lines : new ArrayList<>(); }
}
