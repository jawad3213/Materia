package com.materia.backend.contexts.invoice.infrastructure.adapters.out.persistence.entities;

import com.materia.backend.common.infrastructure.persistence.BaseJpaEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "invoice_lines")
public class InvoiceLineJpaEntity extends BaseJpaEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "invoice_id", nullable = false)
    private InvoiceJpaEntity invoice;

    @Column(name = "line_number", nullable = false)
    private Integer lineNumber;

    @Column(name = "purchase_order_line_id", length = 100)
    private String purchaseOrderLineId;

    @Column(name = "goods_receipt_line_id", length = 100)
    private String goodsReceiptLineId;

    @Column(name = "material_code", length = 50)
    private String materialCode;

    @Column(name = "material_name", nullable = false, length = 255)
    private String materialName;

    @Column(name = "unit_of_measure", length = 50)
    private String unitOfMeasure;

    @Column(name = "quantity_ordered")
    private Integer quantityOrdered;

    @Column(name = "quantity_received")
    private Integer quantityReceived;

    @Column(name = "quantity_invoiced", nullable = false)
    private Integer quantityInvoiced;

    @Column(name = "quantity_discrepancy")
    private Integer quantityDiscrepancy;

    @Column(name = "unit_price", nullable = false, precision = 19, scale = 4)
    private BigDecimal unitPrice;

    @Column(name = "line_total", nullable = false, precision = 19, scale = 4)
    private BigDecimal lineTotal;

    @Column(name = "tax_amount", precision = 19, scale = 4)
    private BigDecimal taxAmount;

    @Column(name = "line_total_with_tax", precision = 19, scale = 4)
    private BigDecimal lineTotalWithTax;

    @Column(name = "currency_code", nullable = false, length = 3)
    private String currencyCode;

    @Column(name = "has_quantity_discrepancy", nullable = false)
    private boolean hasQuantityDiscrepancy;

    @Column(name = "discrepancy_notes", length = 1000)
    private String discrepancyNotes;

    @Column(name = "notes", length = 1000)
    private String notes;

    // Getters and Setters

    public InvoiceJpaEntity getInvoice() { return invoice; }
    public void setInvoice(InvoiceJpaEntity invoice) { this.invoice = invoice; }

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
