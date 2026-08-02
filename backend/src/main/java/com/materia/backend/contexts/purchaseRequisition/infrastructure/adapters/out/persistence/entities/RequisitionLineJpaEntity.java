package com.materia.backend.contexts.purchaseRequisition.infrastructure.adapters.out.persistence.entities;

import com.materia.backend.common.infrastructure.persistence.BaseJpaEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * JPA entity for requisition lines.
 */
@Entity
@Table(name = "purchase_requisition_lines", indexes = {
        @Index(name = "idx_pr_line_requisition_id", columnList = "requisition_id"),
        @Index(name = "idx_pr_line_material_code", columnList = "material_code"),
        @Index(name = "idx_pr_line_line_number", columnList = "line_number")
})
public class RequisitionLineJpaEntity extends BaseJpaEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requisition_id", nullable = false)
    private RequisitionJpaEntity requisition;

    @Column(name = "line_number")
    private Integer lineNumber;

    @Column(name = "material_code", length = 50)
    private String materialCode;

    @Column(name = "material_id")
    private UUID materialId;

    @Column(name = "material_name", length = 255)
    private String materialName;

    @Column(name = "material_description", length = 1000)
    private String materialDescription;

    @Column(name = "unit_of_measure", length = 20)
    private String unitOfMeasure;

    @Column(name = "standard_price", precision = 19, scale = 4)
    private BigDecimal standardPrice;

    @Column(name = "unit_price", precision = 19, scale = 4)
    private BigDecimal unitPrice;

    @Column(name = "currency_code", length = 10)
    private String currencyCode;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "quantity_received")
    private Integer quantityReceived;

    @Column(name = "quantity_rejected")
    private Integer quantityRejected;

    @Column(name = "required_date")
    private LocalDate requiredDate;

    @Column(name = "line_total", precision = 19, scale = 4)
    private BigDecimal lineTotal;

    @Column(name = "currency_code_line", length = 10)
    private String currencyCodeLine;

    @Column(name = "supplier_id")
    private UUID supplierId;

    @Column(name = "supplier_name", length = 255)
    private String supplierName;

    @Column(name = "supplier_code", length = 50)
    private String supplierCode;

    @Column(name = "notes", length = 1000)
    private String notes;

    @Column(name = "delivery_terms", length = 255)
    private String deliveryTerms;

    @Column(name = "storage_location", length = 100)
    private String storageLocation;

    @Column(name = "batch_number", length = 100)
    private String batchNumber;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    public RequisitionJpaEntity getRequisition() {
        return requisition;
    }

    public void setRequisition(RequisitionJpaEntity requisition) {
        this.requisition = requisition;
    }

    public Integer getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(Integer lineNumber) {
        this.lineNumber = lineNumber;
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

    public String getMaterialDescription() {
        return materialDescription;
    }

    public void setMaterialDescription(String materialDescription) {
        this.materialDescription = materialDescription;
    }

    public String getUnitOfMeasure() {
        return unitOfMeasure;
    }

    public void setUnitOfMeasure(String unitOfMeasure) {
        this.unitOfMeasure = unitOfMeasure;
    }

    public BigDecimal getStandardPrice() {
        return standardPrice;
    }

    public void setStandardPrice(BigDecimal standardPrice) {
        this.standardPrice = standardPrice;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
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

    public LocalDate getRequiredDate() {
        return requiredDate;
    }

    public void setRequiredDate(LocalDate requiredDate) {
        this.requiredDate = requiredDate;
    }

    public BigDecimal getLineTotal() {
        return lineTotal;
    }

    public void setLineTotal(BigDecimal lineTotal) {
        this.lineTotal = lineTotal;
    }

    public String getCurrencyCodeLine() {
        return currencyCodeLine;
    }

    public void setCurrencyCodeLine(String currencyCodeLine) {
        this.currencyCodeLine = currencyCodeLine;
    }

    public UUID getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(UUID supplierId) {
        this.supplierId = supplierId;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    public String getSupplierCode() {
        return supplierCode;
    }

    public void setSupplierCode(String supplierCode) {
        this.supplierCode = supplierCode;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getDeliveryTerms() {
        return deliveryTerms;
    }

    public void setDeliveryTerms(String deliveryTerms) {
        this.deliveryTerms = deliveryTerms;
    }

    public String getStorageLocation() {
        return storageLocation;
    }

    public void setStorageLocation(String storageLocation) {
        this.storageLocation = storageLocation;
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
}
