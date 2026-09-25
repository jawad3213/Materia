package com.materia.backend.contexts.invoice.application.dtos;

import com.materia.backend.common.domain.valueObjects.Money;

public class InvoiceLineInput {
    private String purchaseOrderLineId;
    private String goodsReceiptLineId;
    private String materialCode;
    private String materialName;
    private String unitOfMeasure;
    private Integer quantityInvoiced;
    private Money unitPrice;
    private Money taxAmount;
    private String notes;

    public String getPurchaseOrderLineId() { return purchaseOrderLineId; }
    public void setPurchaseOrderLineId(String purchaseOrderLineId) { this.purchaseOrderLineId = purchaseOrderLineId; }

    public String getGoodsReceiptLineId() { return goodsReceiptLineId; }
    public void setGoodsReceiptLineId(String goodsReceiptLineId) { this.goodsReceiptLineId = goodsReceiptLineId; }

    public String getMaterialCode() { return materialCode; }
    public void setMaterialCode(String materialCode) { this.materialCode = materialCode; }

    public String getMaterialName() { return materialName; }
    public void setMaterialName(String materialName) { this.materialName = materialName; }

    public String getUnitOfMeasure() { return unitOfMeasure; }
    public void setUnitOfMeasure(String unitOfMeasure) { this.unitOfMeasure = unitOfMeasure; }

    public Integer getQuantityInvoiced() { return quantityInvoiced; }
    public void setQuantityInvoiced(Integer quantityInvoiced) { this.quantityInvoiced = quantityInvoiced; }

    public Money getUnitPrice() { return unitPrice; }
    public void setUnitPrice(Money unitPrice) { this.unitPrice = unitPrice; }

    public Money getTaxAmount() { return taxAmount; }
    public void setTaxAmount(Money taxAmount) { this.taxAmount = taxAmount; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
