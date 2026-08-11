package com.materia.backend.contexts.invoice.infrastructure.adapters.in.web.dtos.invoice;

import java.math.BigDecimal;
import java.util.UUID;

public class InvoiceLineWebResponse {

    private UUID id;
    private Integer lineNumber;
    private String purchaseOrderLineId;
    private String goodsReceiptLineId;
    private String materialCode;
    private String materialName;
    private String unitOfMeasure;
    
    private BigDecimal quantityOrdered;
    private BigDecimal quantityReceived;
    private BigDecimal quantityInvoiced;
    private BigDecimal quantityDiscrepancy;
    
    private BigDecimal unitPrice;
    private BigDecimal lineTotal;
    private BigDecimal taxAmount;
    private BigDecimal lineTotalWithTax;
    private String currencyCode;
    
    private boolean hasQuantityDiscrepancy;
    private String discrepancyNotes;
    private String notes;

    // Getters and Setters

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

    public BigDecimal getQuantityOrdered() { return quantityOrdered; }
    public void setQuantityOrdered(BigDecimal quantityOrdered) { this.quantityOrdered = quantityOrdered; }

    public BigDecimal getQuantityReceived() { return quantityReceived; }
    public void setQuantityReceived(BigDecimal quantityReceived) { this.quantityReceived = quantityReceived; }

    public BigDecimal getQuantityInvoiced() { return quantityInvoiced; }
    public void setQuantityInvoiced(BigDecimal quantityInvoiced) { this.quantityInvoiced = quantityInvoiced; }

    public BigDecimal getQuantityDiscrepancy() { return quantityDiscrepancy; }
    public void setQuantityDiscrepancy(BigDecimal quantityDiscrepancy) { this.quantityDiscrepancy = quantityDiscrepancy; }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }

    public BigDecimal getLineTotal() { return lineTotal; }
    public void setLineTotal(BigDecimal lineTotal) { this.lineTotal = lineTotal; }

    public BigDecimal getTaxAmount() { return taxAmount; }
    public void setTaxAmount(BigDecimal taxAmount) { this.taxAmount = taxAmount; }

    public BigDecimal getLineTotalWithTax() { return lineTotalWithTax; }
    public void setLineTotalWithTax(BigDecimal lineTotalWithTax) { this.lineTotalWithTax = lineTotalWithTax; }

    public String getCurrencyCode() { return currencyCode; }
    public void setCurrencyCode(String currencyCode) { this.currencyCode = currencyCode; }

    public boolean isHasQuantityDiscrepancy() { return hasQuantityDiscrepancy; }
    public void setHasQuantityDiscrepancy(boolean hasQuantityDiscrepancy) { this.hasQuantityDiscrepancy = hasQuantityDiscrepancy; }

    public String getDiscrepancyNotes() { return discrepancyNotes; }
    public void setDiscrepancyNotes(String discrepancyNotes) { this.discrepancyNotes = discrepancyNotes; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
