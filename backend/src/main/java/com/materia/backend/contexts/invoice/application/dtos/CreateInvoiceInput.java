package com.materia.backend.contexts.invoice.application.dtos;

import com.materia.backend.common.application.BaseInput;
import com.materia.backend.contexts.invoice.domain.enums.InvoiceType;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CreateInvoiceInput extends BaseInput {
    private String purchaseOrderId;
    private String purchaseOrderCode;
    private String goodsReceiptId;
    private String goodsReceiptCode;
    private String supplierId;
    private String supplierName;
    private String supplierCode;
    private InvoiceType invoiceType;
    private String externalReference;
    private LocalDate invoiceDate;
    private LocalDate dueDate;
    private String currencyCode;
    private String notes;
    private String internalNotes;
    private List<InvoiceLineInput> lines = new ArrayList<>();

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

    public List<InvoiceLineInput> getLines() { return lines; }
    public void setLines(List<InvoiceLineInput> lines) { this.lines = lines != null ? new ArrayList<>(lines) : new ArrayList<>(); }
}
