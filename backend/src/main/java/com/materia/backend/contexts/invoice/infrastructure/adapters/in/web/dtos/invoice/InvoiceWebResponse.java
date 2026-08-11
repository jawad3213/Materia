package com.materia.backend.contexts.invoice.infrastructure.adapters.in.web.dtos.invoice;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class InvoiceWebResponse {

    private UUID id;
    private String invoiceCode;
    private String purchaseOrderId;
    private String purchaseOrderCode;
    private String goodsReceiptId;
    private String goodsReceiptCode;
    private String supplierId;
    private String supplierName;
    private String supplierCode;
    private String invoiceType;
    private String status;
    private String externalReference;
    
    private LocalDate invoiceDate;
    private LocalDate dueDate;
    private LocalDate receivedDate;
    private LocalDate paymentDate;
    
    private BigDecimal totalAmount;
    private BigDecimal totalTaxAmount;
    private BigDecimal totalAmountWithTax;
    private String currencyCode;
    
    private boolean isVerified;
    private boolean hasDiscrepancy;
    private String discrepancySummary;
    private LocalDateTime verificationDate;
    private String verifiedBy;
    private String verifiedByName;
    
    private BigDecimal paidAmount;
    private LocalDateTime paidAt;
    private String paidBy;
    private String paidByName;
    
    private String notes;
    private String internalNotes;
    
    private List<InvoiceLineWebResponse> lines = new ArrayList<>();

    // Getters and Setters

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

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

    public String getInvoiceType() { return invoiceType; }
    public void setInvoiceType(String invoiceType) { this.invoiceType = invoiceType; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

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

    public List<InvoiceLineWebResponse> getLines() { return lines; }
    public void setLines(List<InvoiceLineWebResponse> lines) { this.lines = lines != null ? new ArrayList<>(lines) : new ArrayList<>(); }
}
