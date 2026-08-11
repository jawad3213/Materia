package com.materia.backend.contexts.payement.infrastructure.adapters.in.web.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class CreatePaymentLineWebRequest {

    @NotBlank(message = "L'ID de la facture est obligatoire")
    private String invoiceId;
    
    private String invoiceCode;
    
    @NotBlank(message = "Le fournisseur est obligatoire")
    private String supplierId;
    
    private String supplierName;
    
    @NotNull(message = "Le montant est obligatoire")
    private BigDecimal amount;
    
    private String notes;

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

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
