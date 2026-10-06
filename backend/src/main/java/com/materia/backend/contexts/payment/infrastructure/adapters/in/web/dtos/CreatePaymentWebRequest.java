package com.materia.backend.contexts.payment.infrastructure.adapters.in.web.dtos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class CreatePaymentWebRequest {

    @NotBlank(message = "The supplier is required")
    private String supplierId;
    
    private String supplierName;
    
    private String supplierCode;
    
    private BigDecimal totalAmount;
    
    private String currencyCode;
    private String notes;
    private String internalNotes;
    
    @NotEmpty(message = "At least one payment line is required")
    @Valid
    private List<CreatePaymentLineWebRequest> lines = new ArrayList<>();
    

    public String getSupplierId() { return supplierId; }
    public void setSupplierId(String supplierId) { this.supplierId = supplierId; }

    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }

    public String getSupplierCode() { return supplierCode; }
    public void setSupplierCode(String supplierCode) { this.supplierCode = supplierCode; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public String getCurrencyCode() { return currencyCode; }
    public void setCurrencyCode(String currencyCode) { this.currencyCode = currencyCode; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getInternalNotes() { return internalNotes; }
    public void setInternalNotes(String internalNotes) { this.internalNotes = internalNotes; }

    public List<CreatePaymentLineWebRequest> getLines() { return lines; }
    public void setLines(List<CreatePaymentLineWebRequest> lines) { this.lines = lines; }

}
