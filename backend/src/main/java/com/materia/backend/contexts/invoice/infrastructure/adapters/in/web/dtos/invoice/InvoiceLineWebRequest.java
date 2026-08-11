package com.materia.backend.contexts.invoice.infrastructure.adapters.in.web.dtos.invoice;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class InvoiceLineWebRequest {

    private String id;

    private Integer lineNumber;

    private String purchaseOrderLineId;

    private String goodsReceiptLineId;

    @Size(max = 50, message = "Material code must not exceed 50 characters")
    private String materialCode;

    @NotBlank(message = "Material name is mandatory")
    @Size(max = 255, message = "Material name must not exceed 255 characters")
    private String materialName;

    @Size(max = 50, message = "Unit of measure must not exceed 50 characters")
    private String unitOfMeasure;

    @NotNull(message = "Quantity invoiced is mandatory")
    @PositiveOrZero(message = "Quantity invoiced cannot be negative")
    private BigDecimal quantityInvoiced;

    @NotNull(message = "Unit price is mandatory")
    @DecimalMin(value = "0.00", inclusive = true, message = "Unit price cannot be negative")
    private BigDecimal unitPrice;

    @DecimalMin(value = "0.00", inclusive = true, message = "Tax amount cannot be negative")
    private BigDecimal taxAmount;

    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    private String notes;

    // Getters and Setters

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

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

    public BigDecimal getQuantityInvoiced() { return quantityInvoiced; }
    public void setQuantityInvoiced(BigDecimal quantityInvoiced) { this.quantityInvoiced = quantityInvoiced; }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }

    public BigDecimal getTaxAmount() { return taxAmount; }
    public void setTaxAmount(BigDecimal taxAmount) { this.taxAmount = taxAmount; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
