package com.materia.backend.contexts.invoice.application.dtos;

import com.materia.backend.common.domain.valueObjects.Money;
import java.util.UUID;

public class InvoiceLineOutput {
    private UUID id;
    private Integer lineNumber;
    private String purchaseOrderLineId;
    private String goodsReceiptLineId;
    private String materialCode;
    private String materialName;
    private String unitOfMeasure;
    private Integer quantityOrdered;
    private Integer quantityReceived;
    private Integer quantityInvoiced;
    private Integer quantityDiscrepancy;
    private Money unitPrice;
    private Money lineTotal;
    private Money taxAmount;
    private Money lineTotalWithTax;
    private String currencyCode;
    private boolean hasQuantityDiscrepancy;
    private String discrepancyNotes;
    private String notes;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Integer getLineNumber() { return lineNumber; }
    public void setLineNumber(Integer lineNumber) { this.lineNumber = lineNumber; }

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

    public Integer getQuantityOrdered() { return quantityOrdered; }
    public void setQuantityOrdered(Integer quantityOrdered) { this.quantityOrdered = quantityOrdered; }

    public Integer getQuantityReceived() { return quantityReceived; }
    public void setQuantityReceived(Integer quantityReceived) { this.quantityReceived = quantityReceived; }

    public Integer getQuantityInvoiced() { return quantityInvoiced; }
    public void setQuantityInvoiced(Integer quantityInvoiced) { this.quantityInvoiced = quantityInvoiced; }

    public Integer getQuantityDiscrepancy() { return quantityDiscrepancy; }
    public void setQuantityDiscrepancy(Integer quantityDiscrepancy) { this.quantityDiscrepancy = quantityDiscrepancy; }

    public Money getUnitPrice() { return unitPrice; }
    public void setUnitPrice(Money unitPrice) { this.unitPrice = unitPrice; }

    public Money getLineTotal() { return lineTotal; }
    public void setLineTotal(Money lineTotal) { this.lineTotal = lineTotal; }

    public Money getTaxAmount() { return taxAmount; }
    public void setTaxAmount(Money taxAmount) { this.taxAmount = taxAmount; }

    public Money getLineTotalWithTax() { return lineTotalWithTax; }
    public void setLineTotalWithTax(Money lineTotalWithTax) { this.lineTotalWithTax = lineTotalWithTax; }

    public String getCurrencyCode() { return currencyCode; }
    public void setCurrencyCode(String currencyCode) { this.currencyCode = currencyCode; }

    public boolean isHasQuantityDiscrepancy() { return hasQuantityDiscrepancy; }
    public void setHasQuantityDiscrepancy(boolean hasQuantityDiscrepancy) { this.hasQuantityDiscrepancy = hasQuantityDiscrepancy; }

    public String getDiscrepancyNotes() { return discrepancyNotes; }
    public void setDiscrepancyNotes(String discrepancyNotes) { this.discrepancyNotes = discrepancyNotes; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
