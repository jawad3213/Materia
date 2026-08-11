package com.materia.backend.contexts.payement.application.dtos;

import com.materia.backend.common.application.BaseInput;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class CreatePaymentInput extends BaseInput {
    private String supplierId;
    private String supplierName;
    private String supplierCode;
    private BigDecimal totalAmount;
    private String currencyCode;
    private String notes;
    private String internalNotes;
    
    private List<CreatePaymentLineInput> lines = new ArrayList<>();

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

    public List<CreatePaymentLineInput> getLines() { return lines; }
    public void setLines(List<CreatePaymentLineInput> lines) { this.lines = lines; }
}
