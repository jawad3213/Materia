package com.materia.backend.contexts.invoice.infrastructure.adapters.in.web.dtos.invoice;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CreateInvoiceWebRequest {

    private String purchaseOrderId;

    @Size(max = 50, message = "Purchase order code must not exceed 50 characters")
    private String purchaseOrderCode;

    private String goodsReceiptId;

    @Size(max = 50, message = "Goods receipt code must not exceed 50 characters")
    private String goodsReceiptCode;

    @NotNull(message = "Supplier ID is mandatory")
    private String supplierId;

    @NotBlank(message = "Supplier name is mandatory")
    @Size(max = 255, message = "Supplier name must not exceed 255 characters")
    private String supplierName;

    @Size(max = 50, message = "Supplier code must not exceed 50 characters")
    private String supplierCode;

    @NotBlank(message = "Invoice type is mandatory")
    private String invoiceType;

    @Size(max = 100, message = "External reference must not exceed 100 characters")
    private String externalReference;

    @NotNull(message = "Invoice date is mandatory")
    private LocalDate invoiceDate;

    private LocalDate dueDate;

    @NotBlank(message = "Currency code is mandatory")
    @Size(min = 3, max = 3, message = "Currency code must be exactly 3 characters")
    private String currencyCode;

    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    private String notes;

    @Size(max = 1000, message = "Internal notes must not exceed 1000 characters")
    private String internalNotes;

    @NotEmpty(message = "At least one invoice line is required")
    @Valid
    private List<InvoiceLineWebRequest> lines = new ArrayList<>();

    @NotBlank(message = "Created by is mandatory")
    private String createdBy;

    // Getters and Setters

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

    public String getExternalReference() { return externalReference; }
    public void setExternalReference(String externalReference) { this.externalReference = externalReference; }

    public LocalDate getInvoiceDate() { return invoiceDate; }
    public void setInvoiceDate(LocalDate invoiceDate) { this.invoiceDate = invoiceDate; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public String getCurrencyCode() { return currencyCode; }
    public void setCurrencyCode(String currencyCode) { this.currencyCode = currencyCode; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getInternalNotes() { return internalNotes; }
    public void setInternalNotes(String internalNotes) { this.internalNotes = internalNotes; }

    public List<InvoiceLineWebRequest> getLines() { return lines; }
    public void setLines(List<InvoiceLineWebRequest> lines) { this.lines = lines != null ? new ArrayList<>(lines) : new ArrayList<>(); }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
}
