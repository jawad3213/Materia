package com.materia.backend.contexts.payement.application.dtos;

import com.materia.backend.common.application.BaseOutput;
import com.materia.backend.contexts.payement.domain.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PaymentOutput extends BaseOutput {
    private UUID id;
    private String paymentCode;
    private String supplierId;
    private String supplierName;
    private String supplierCode;
    private PaymentStatus status;
    
    private BigDecimal totalAmount;
    private BigDecimal paidAmount;
    private String currencyCode;
    
    private LocalDateTime paymentDate;
    private LocalDateTime confirmedDate;
    
    private String bankReference;
    private String transactionId;
    private String paymentMethod;
    private String paymentReceipt;
    
    private String notes;
    private String internalNotes;
    
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;
    
    private List<PaymentLineOutput> lines = new ArrayList<>();

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

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

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }

    public List<PaymentLineOutput> getLines() { return lines; }
    public void setLines(List<PaymentLineOutput> lines) { this.lines = lines; }
}
