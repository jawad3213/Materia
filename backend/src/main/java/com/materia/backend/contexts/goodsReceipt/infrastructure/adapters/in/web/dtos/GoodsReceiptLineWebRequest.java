package com.materia.backend.contexts.goodsReceipt.infrastructure.adapters.in.web.dtos;

import com.materia.backend.common.application.BaseInput;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/** Validated web request for a goods receipt line. */
public class GoodsReceiptLineWebRequest extends BaseInput {

    private UUID id;
    private Integer lineNumber;
    private String purchaseOrderLineId;
    @NotBlank(message = "Material code is mandatory") @Size(max = 100)
    private String materialCode;
    private UUID materialId;
    @Size(max = 255) private String materialName;
    @Size(max = 50) private String unitOfMeasure;
    @PositiveOrZero(message = "Ordered quantity cannot be negative") private Integer quantityOrdered;
    @NotNull(message = "Received quantity is mandatory") @PositiveOrZero(message = "Received quantity cannot be negative")
    private Integer quantityReceived;
    @NotNull(message = "Rejected quantity is mandatory") @PositiveOrZero(message = "Rejected quantity cannot be negative")
    private Integer quantityRejected;
    @Pattern(regexp = "^(ACCEPTED|REJECTED|UNDER_REVIEW|PARTIAL)$", message = "Invalid quality status")
    private String qualityStatus;
    @Size(max = 1000) private String qualityNotes;
    @Size(max = 1000) private String rejectionReason;
    @PositiveOrZero private Integer stockBefore;
    @PositiveOrZero private Integer stockAfter;
    @PositiveOrZero private BigDecimal unitPrice;
    @Pattern(regexp = "^(MAD|EUR|USD)$", message = "Currency must be MAD, EUR, or USD")
    private String currencyCode;
    @Size(max = 100) private String supplierId;
    @Size(max = 255) private String supplierName;
    @Size(max = 100) private String batchNumber;
    private LocalDate expiryDate;
    @Size(max = 100) private String storageLocation;
    @Size(max = 1000) private String notes;

    @AssertTrue(message = "Rejected quantity cannot exceed received quantity")
    public boolean isQuantityConsistent() {
        return quantityReceived == null || quantityRejected == null
                || quantityRejected <= quantityReceived;
    }

    @AssertTrue(message = "A rejection reason is mandatory when quantity is rejected")
    public boolean isRejectionReasonProvided() {
        return quantityRejected == null || quantityRejected == 0
                || (rejectionReason != null && !rejectionReason.isBlank());
    }

    public UUID getId() { return id; } public void setId(UUID id) { this.id = id; }
    public Integer getLineNumber() { return lineNumber; } public void setLineNumber(Integer lineNumber) { this.lineNumber = lineNumber; }
    public String getPurchaseOrderLineId() { return purchaseOrderLineId; } public void setPurchaseOrderLineId(String value) { this.purchaseOrderLineId = value; }
    public String getMaterialCode() { return materialCode; } public void setMaterialCode(String value) { this.materialCode = value; }
    public UUID getMaterialId() { return materialId; } public void setMaterialId(UUID value) { this.materialId = value; }
    public String getMaterialName() { return materialName; } public void setMaterialName(String value) { this.materialName = value; }
    public String getUnitOfMeasure() { return unitOfMeasure; } public void setUnitOfMeasure(String value) { this.unitOfMeasure = value; }
    public Integer getQuantityOrdered() { return quantityOrdered; } public void setQuantityOrdered(Integer value) { this.quantityOrdered = value; }
    public Integer getQuantityReceived() { return quantityReceived; } public void setQuantityReceived(Integer value) { this.quantityReceived = value; }
    public Integer getQuantityRejected() { return quantityRejected; } public void setQuantityRejected(Integer value) { this.quantityRejected = value; }
    public String getQualityStatus() { return qualityStatus; } public void setQualityStatus(String value) { this.qualityStatus = value; }
    public String getQualityNotes() { return qualityNotes; } public void setQualityNotes(String value) { this.qualityNotes = value; }
    public String getRejectionReason() { return rejectionReason; } public void setRejectionReason(String value) { this.rejectionReason = value; }
    public Integer getStockBefore() { return stockBefore; } public void setStockBefore(Integer value) { this.stockBefore = value; }
    public Integer getStockAfter() { return stockAfter; } public void setStockAfter(Integer value) { this.stockAfter = value; }
    public BigDecimal getUnitPrice() { return unitPrice; } public void setUnitPrice(BigDecimal value) { this.unitPrice = value; }
    public String getCurrencyCode() { return currencyCode; } public void setCurrencyCode(String value) { this.currencyCode = value; }
    public String getSupplierId() { return supplierId; } public void setSupplierId(String value) { this.supplierId = value; }
    public String getSupplierName() { return supplierName; } public void setSupplierName(String value) { this.supplierName = value; }
    public String getBatchNumber() { return batchNumber; } public void setBatchNumber(String value) { this.batchNumber = value; }
    public LocalDate getExpiryDate() { return expiryDate; } public void setExpiryDate(LocalDate value) { this.expiryDate = value; }
    public String getStorageLocation() { return storageLocation; } public void setStorageLocation(String value) { this.storageLocation = value; }
    public String getNotes() { return notes; } public void setNotes(String value) { this.notes = value; }
}
