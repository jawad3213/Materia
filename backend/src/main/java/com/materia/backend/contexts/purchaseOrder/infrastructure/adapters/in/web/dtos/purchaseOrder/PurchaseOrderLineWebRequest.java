package com.materia.backend.contexts.purchaseOrder.infrastructure.adapters.in.web.dtos.purchaseOrder;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Web request DTO for a purchase order line.
 */
public class PurchaseOrderLineWebRequest {

    private UUID id;

    private Integer lineNumber;

    private UUID requisitionLineId;

    @NotBlank(message = "Material code is mandatory")
    @Size(max = 50, message = "Material code must not exceed 50 characters")
    private String materialCode;

    private UUID materialId;

    @Size(max = 255, message = "Material name must not exceed 255 characters")
    private String materialName;

    @Size(max = 1000, message = "Material description must not exceed 1000 characters")
    private String materialDescription;

    @Size(max = 50, message = "Unit of measure must not exceed 50 characters")
    private String unitOfMeasure;

    @NotNull(message = "Quantity is mandatory")
    @Positive(message = "Quantity must be greater than zero")
    private Integer quantity;

    @NotNull(message = "Unit price is mandatory")
    @DecimalMin(value = "0.00", inclusive = true, message = "Unit price cannot be negative")
    private BigDecimal unitPrice;

    @Size(min = 3, max = 3, message = "Currency code must be exactly 3 characters if provided")
    private String currencyCode;

    private UUID supplierId;

    @Size(max = 255, message = "Supplier name must not exceed 255 characters")
    private String supplierName;

    private LocalDate expectedDeliveryDate;

    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    private String notes;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Integer getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(Integer lineNumber) {
        this.lineNumber = lineNumber;
    }

    public UUID getRequisitionLineId() {
        return requisitionLineId;
    }

    public void setRequisitionLineId(UUID requisitionLineId) {
        this.requisitionLineId = requisitionLineId;
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

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
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

    public LocalDate getExpectedDeliveryDate() {
        return expectedDeliveryDate;
    }

    public void setExpectedDeliveryDate(LocalDate expectedDeliveryDate) {
        this.expectedDeliveryDate = expectedDeliveryDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
