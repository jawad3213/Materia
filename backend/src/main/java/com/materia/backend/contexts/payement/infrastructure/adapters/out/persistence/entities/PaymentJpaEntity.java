package com.materia.backend.contexts.payement.infrastructure.adapters.out.persistence.entities;

import com.materia.backend.common.infrastructure.persistence.BaseJpaEntity;
import com.materia.backend.contexts.payement.domain.enums.PaymentStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "payments", indexes = {
        @Index(name = "idx_payment_code", columnList = "payment_code", unique = true),
        @Index(name = "idx_payment_status", columnList = "status"),
        @Index(name = "idx_payment_supplier_id", columnList = "supplier_id")
})
public class PaymentJpaEntity extends BaseJpaEntity {

    @Column(name = "payment_code", nullable = false, unique = true, length = 50)
    private String paymentCode;

    @Column(name = "supplier_id", nullable = false, length = 100)
    private String supplierId;

    @Column(name = "supplier_name", nullable = false, length = 255)
    private String supplierName;

    @Column(name = "supplier_code", length = 50)
    private String supplierCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private PaymentStatus status;

    @Column(name = "total_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal totalAmount;

    @Column(name = "paid_amount", precision = 19, scale = 4)
    private BigDecimal paidAmount;

    @Column(name = "currency_code", nullable = false, length = 3)
    private String currencyCode;

    @Column(name = "payment_date")
    private LocalDateTime paymentDate;

    @Column(name = "confirmed_date")
    private LocalDateTime confirmedDate;

    @Column(name = "bank_reference", length = 100)
    private String bankReference;

    @Column(name = "transaction_id", length = 100)
    private String transactionId;

    @Column(name = "payment_method", length = 50)
    private String paymentMethod;

    @Column(name = "payment_receipt", length = 255)
    private String paymentReceipt;

    @Column(name = "notes", length = 1000)
    private String notes;

    @Column(name = "internal_notes", length = 1000)
    private String internalNotes;

    @OneToMany(mappedBy = "payment", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("lineNumber ASC")
    private List<PaymentLineJpaEntity> lines = new ArrayList<>();

    // Getters and Setters

    public String getPaymentCode() { return paymentCode; }
    public void setPaymentCode(String paymentCode) { this.paymentCode = paymentCode; }

    public String getSupplierId() { return supplierId; }
    public void setSupplierId(String supplierId) { this.supplierId = supplierId; }

    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }

    public String getSupplierCode() { return supplierCode; }
    public void setSupplierCode(String supplierCode) { this.supplierCode = supplierCode; }

    public PaymentStatus getStatus() { return status; }
    public void setStatus(PaymentStatus status) { this.status = status; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public BigDecimal getPaidAmount() { return paidAmount; }
    public void setPaidAmount(BigDecimal paidAmount) { this.paidAmount = paidAmount; }

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

    public List<PaymentLineJpaEntity> getLines() { return lines; }
    public void setLines(List<PaymentLineJpaEntity> lines) {
        if (this.lines == null) {
            this.lines = new ArrayList<>();
        }
        this.lines.clear();
        if (lines != null) {
            for (PaymentLineJpaEntity line : lines) {
                line.setPayment(this);
                this.lines.add(line);
            }
        }
    }

    public void addLine(PaymentLineJpaEntity line) {
        if (this.lines == null) {
            this.lines = new ArrayList<>();
        }
        line.setPayment(this);
        this.lines.add(line);
    }
}
