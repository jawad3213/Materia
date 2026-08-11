package com.materia.backend.contexts.payement.infrastructure.adapters.out.persistence.entities;

import com.materia.backend.common.infrastructure.persistence.BaseJpaEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "payment_lines")
public class PaymentLineJpaEntity extends BaseJpaEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false)
    private PaymentJpaEntity payment;

    @Column(name = "line_number", nullable = false)
    private Integer lineNumber;

    @Column(name = "invoice_id", nullable = false, length = 100)
    private String invoiceId;

    @Column(name = "invoice_code", length = 100)
    private String invoiceCode;

    @Column(name = "supplier_id", nullable = false, length = 100)
    private String supplierId;

    @Column(name = "supplier_name", length = 255)
    private String supplierName;

    @Column(name = "amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(name = "paid_amount", precision = 19, scale = 4)
    private BigDecimal paidAmount;

    @Column(name = "currency_code", nullable = false, length = 3)
    private String currencyCode;

    @Column(name = "is_paid", nullable = false)
    private boolean isPaid;

    @Column(name = "notes", length = 1000)
    private String notes;

    // Getters and Setters

    public PaymentJpaEntity getPayment() { return payment; }
    public void setPayment(PaymentJpaEntity payment) { this.payment = payment; }

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

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public BigDecimal getPaidAmount() { return paidAmount; }
    public void setPaidAmount(BigDecimal paidAmount) { this.paidAmount = paidAmount; }

    public String getCurrencyCode() { return currencyCode; }
    public void setCurrencyCode(String currencyCode) { this.currencyCode = currencyCode; }

    public boolean isPaid() { return isPaid; }
    public void setPaid(boolean paid) { isPaid = paid; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
