package com.materia.backend.contexts.goodsReceipt.infrastructure.adapters.out.persistence.entities;

import com.materia.backend.common.infrastructure.persistence.BaseJpaEntity;
import com.materia.backend.contexts.goodsReceipt.domain.enums.QualityStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "goods_receipt_lines", indexes = {
        @Index(name = "idx_grl_goods_receipt_id", columnList = "goods_receipt_id"),
        @Index(name = "idx_grl_purchase_order_line_id", columnList = "purchase_order_line_id"),
        @Index(name = "idx_grl_material_code", columnList = "material_code"),
        @Index(name = "idx_grl_line_number", columnList = "line_number")
})
public class GoodsReceiptLineJpaEntity extends BaseJpaEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "goods_receipt_id", nullable = false)
    private GoodsReceiptJpaEntity goodsReceipt;

    @Column(name = "line_number")
    private Integer lineNumber;

    @Column(name = "purchase_order_line_id", length = 100)
    private String purchaseOrderLineId;

    @Column(name = "material_code", nullable = false, length = 50)
    private String materialCode;

    @Column(name = "material_id")
    private UUID materialId;

    @Column(name = "material_name", length = 255)
    private String materialName;

    @Column(name = "unit_of_measure", length = 20)
    private String unitOfMeasure;

    @Column(name = "quantity_ordered")
    private Integer quantityOrdered;

    @Column(name = "quantity_received")
    private Integer quantityReceived;

    @Column(name = "quantity_rejected")
    private Integer quantityRejected;

    @Column(name = "quantity_accepted")
    private Integer quantityAccepted;

    @Column(name = "quantity_pending")
    private Integer quantityPending;

    @Enumerated(EnumType.STRING)
    @Column(name = "quality_status", length = 30)
    private QualityStatus qualityStatus;

    @Column(name = "quality_notes", length = 1000)
    private String qualityNotes;

    @Column(name = "rejection_reason", length = 1000)
    private String rejectionReason;

    @Column(name = "stock_before")
    private Integer stockBefore;

    @Column(name = "stock_after")
    private Integer stockAfter;

    @Column(name = "unit_price", precision = 19, scale = 4)
    private BigDecimal unitPrice;

    @Column(name = "line_total", precision = 19, scale = 4)
    private BigDecimal lineTotal;

    @Column(name = "currency_code", length = 10)
    private String currencyCode;

    @Column(name = "supplier_id", length = 100)
    private String supplierId;

    @Column(name = "supplier_name", length = 255)
    private String supplierName;

    @Column(name = "batch_number", length = 100)
    private String batchNumber;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "storage_location", length = 100)
    private String storageLocation;

    @Column(name = "notes", length = 1000)
    private String notes;

    public GoodsReceiptJpaEntity getGoodsReceipt() {
        return goodsReceipt;
    }

    public void setGoodsReceipt(GoodsReceiptJpaEntity goodsReceipt) {
        this.goodsReceipt = goodsReceipt;
    }

    public Integer getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(Integer lineNumber) {
        this.lineNumber = lineNumber;
    }

    public String getPurchaseOrderLineId() {
        return purchaseOrderLineId;
    }

    public void setPurchaseOrderLineId(String purchaseOrderLineId) {
        this.purchaseOrderLineId = purchaseOrderLineId;
    }

    public String getMaterialCode() {
        return materialCode;
    }

    public void setMaterialCode(String materialCode) {
        this.materialCode = materialCode;
    }

    public UUID getMaterialId() {
        return materialId;
    }

    public void setMaterialId(UUID materialId) {
        this.materialId = materialId;
    }

    public String getMaterialName() {
        return materialName;
    }

    public void setMaterialName(String materialName) {
        this.materialName = materialName;
    }

    public String getUnitOfMeasure() {
        return unitOfMeasure;
    }

    public void setUnitOfMeasure(String unitOfMeasure) {
        this.unitOfMeasure = unitOfMeasure;
    }

    public Integer getQuantityOrdered() {
        return quantityOrdered;
    }

    public void setQuantityOrdered(Integer quantityOrdered) {
        this.quantityOrdered = quantityOrdered;
    }

    public Integer getQuantityReceived() {
        return quantityReceived;
    }

    public void setQuantityReceived(Integer quantityReceived) {
        this.quantityReceived = quantityReceived;
    }

    public Integer getQuantityRejected() {
        return quantityRejected;
    }

    public void setQuantityRejected(Integer quantityRejected) {
        this.quantityRejected = quantityRejected;
    }

    public Integer getQuantityAccepted() {
        return quantityAccepted;
    }

    public void setQuantityAccepted(Integer quantityAccepted) {
        this.quantityAccepted = quantityAccepted;
    }

    public Integer getQuantityPending() {
        return quantityPending;
    }

    public void setQuantityPending(Integer quantityPending) {
        this.quantityPending = quantityPending;
    }

    public QualityStatus getQualityStatus() {
        return qualityStatus;
    }

    public void setQualityStatus(QualityStatus qualityStatus) {
        this.qualityStatus = qualityStatus;
    }

    public String getQualityNotes() {
        return qualityNotes;
    }

    public void setQualityNotes(String qualityNotes) {
        this.qualityNotes = qualityNotes;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public Integer getStockBefore() {
        return stockBefore;
    }

    public void setStockBefore(Integer stockBefore) {
        this.stockBefore = stockBefore;
    }

    public Integer getStockAfter() {
        return stockAfter;
    }

    public void setStockAfter(Integer stockAfter) {
        this.stockAfter = stockAfter;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public BigDecimal getLineTotal() {
        return lineTotal;
    }

    public void setLineTotal(BigDecimal lineTotal) {
        this.lineTotal = lineTotal;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public String getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(String supplierId) {
        this.supplierId = supplierId;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    public String getBatchNumber() {
        return batchNumber;
    }

    public void setBatchNumber(String batchNumber) {
        this.batchNumber = batchNumber;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getStorageLocation() {
        return storageLocation;
    }

    public void setStorageLocation(String storageLocation) {
        this.storageLocation = storageLocation;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
