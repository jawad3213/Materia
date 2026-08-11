package com.materia.backend.contexts.invoice.infrastructure.adapters.out.persistence.entities;

import com.materia.backend.common.infrastructure.persistence.BaseJpaEntity;
import com.materia.backend.contexts.invoice.domain.enums.InvoiceStatus;
import com.materia.backend.contexts.invoice.domain.enums.InvoiceType;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "invoices", indexes = {
        @Index(name = "idx_invoice_code", columnList = "invoice_code", unique = true),
        @Index(name = "idx_invoice_status", columnList = "status"),
        @Index(name = "idx_invoice_supplier_id", columnList = "supplier_id"),
        @Index(name = "idx_invoice_po_id", columnList = "purchase_order_id"),
        @Index(name = "idx_invoice_date", columnList = "invoice_date")
})
public class InvoiceJpaEntity extends BaseJpaEntity {

    @Column(name = "invoice_code", nullable = false, unique = true, length = 50)
    private String invoiceCode;

    @Column(name = "purchase_order_id", length = 100)
    private String purchaseOrderId;

    @Column(name = "purchase_order_code", length = 100)
    private String purchaseOrderCode;

    @Column(name = "goods_receipt_id", length = 100)
    private String goodsReceiptId;

    @Column(name = "goods_receipt_code", length = 100)
    private String goodsReceiptCode;

    @Column(name = "supplier_id", nullable = false, length = 100)
    private String supplierId;

    @Column(name = "supplier_name", nullable = false, length = 255)
    private String supplierName;

    @Column(name = "supplier_code", length = 50)
    private String supplierCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "invoice_type", nullable = false, length = 30)
    private InvoiceType invoiceType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private InvoiceStatus status;

    @Column(name = "external_reference", length = 100)
    private String externalReference;

    @Column(name = "invoice_date", nullable = false)
    private LocalDate invoiceDate;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "received_date")
    private LocalDate receivedDate;

    @Column(name = "payment_date")
    private LocalDate paymentDate;

    @Column(name = "total_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal totalAmount;

    @Column(name = "total_tax_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal totalTaxAmount;

    @Column(name = "total_amount_with_tax", nullable = false, precision = 19, scale = 4)
    private BigDecimal totalAmountWithTax;

    @Column(name = "currency_code", nullable = false, length = 3)
    private String currencyCode;

    @Column(name = "is_verified", nullable = false)
    private boolean isVerified;

    @Column(name = "has_discrepancy", nullable = false)
    private boolean hasDiscrepancy;

    @Column(name = "discrepancy_summary", length = 1000)
    private String discrepancySummary;

    @Column(name = "verification_date")
    private LocalDateTime verificationDate;

    @Column(name = "verified_by", length = 100)
    private String verifiedBy;

    @Column(name = "verified_by_name", length = 255)
    private String verifiedByName;

    @Column(name = "paid_amount", precision = 19, scale = 4)
    private BigDecimal paidAmount;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "paid_by", length = 100)
    private String paidBy;

    @Column(name = "paid_by_name", length = 255)
    private String paidByName;

    @Column(name = "notes", length = 1000)
    private String notes;

    @Column(name = "internal_notes", length = 1000)
    private String internalNotes;

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("lineNumber ASC")
    private List<InvoiceLineJpaEntity> lines = new ArrayList<>();

    // Getters and Setters

    public String getInvoiceCode() { return invoiceCode; }
    public void setInvoiceCode(String invoiceCode) { this.invoiceCode = invoiceCode; }

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

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public BigDecimal getTotalTaxAmount() { return totalTaxAmount; }
    public void setTotalTaxAmount(BigDecimal totalTaxAmount) { this.totalTaxAmount = totalTaxAmount; }

    public BigDecimal getTotalAmountWithTax() { return totalAmountWithTax; }
    public void setTotalAmountWithTax(BigDecimal totalAmountWithTax) { this.totalAmountWithTax = totalAmountWithTax; }

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

    public BigDecimal getPaidAmount() { return paidAmount; }
    public void setPaidAmount(BigDecimal paidAmount) { this.paidAmount = paidAmount; }

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

    public List<InvoiceLineJpaEntity> getLines() { return lines; }
    public void setLines(List<InvoiceLineJpaEntity> lines) {
        if (this.lines == null) {
            this.lines = new ArrayList<>();
        }
        this.lines.clear();
        if (lines != null) {
            for (InvoiceLineJpaEntity line : lines) {
                line.setInvoice(this);
                this.lines.add(line);
            }
        }
    }
    
    public void addLine(InvoiceLineJpaEntity line) {
        if (this.lines == null) {
            this.lines = new ArrayList<>();
        }
        line.setInvoice(this);
        this.lines.add(line);
    }
}
