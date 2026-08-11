package com.materia.backend.contexts.payement.infrastructure.adapters.in.web.dtos;

import java.math.BigDecimal;
import java.util.UUID;

public class PaymentLineWebResponse {
    
    private UUID id;
    private Integer lineNumber;
    private String invoiceId;
    private String invoiceCode;
    private String supplierId;
    private String supplierName;
    private BigDecimal amount;
    private BigDecimal paidAmount;
    private String currencyCode;
    private boolean isPaid;
    private String notes;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

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
    public void setPaid(boolean paid) { this.isPaid = paid; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
