package com.materia.backend.contexts.returnToVendor.application.dtos;

import com.materia.backend.common.application.BaseInput;

import java.util.UUID;

public class ReturnToVendorLineInput extends BaseInput {

    private UUID id;
    private Integer lineNumber;
    private String goodsReceiptLineId;
    private String materialCode;
    private String materialName;
    private String unitOfMeasure;
    private Integer rejectedQuantity;
    private Integer quantityToReturn;
    private Integer quantityAlreadyReturned;
    private String rejectionReason;
    private String qualityNotes;
    private String defectDescription;
    private boolean replaced;
    private boolean creditNote;
    private String notes;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public Integer getLineNumber() { return lineNumber; }
    public void setLineNumber(Integer lineNumber) { this.lineNumber = lineNumber; }
    public String getGoodsReceiptLineId() { return goodsReceiptLineId; }
    public void setGoodsReceiptLineId(String goodsReceiptLineId) { this.goodsReceiptLineId = goodsReceiptLineId; }
    public String getMaterialCode() { return materialCode; }
    public void setMaterialCode(String materialCode) { this.materialCode = materialCode; }
    public String getMaterialName() { return materialName; }
    public void setMaterialName(String materialName) { this.materialName = materialName; }
    public String getUnitOfMeasure() { return unitOfMeasure; }
    public void setUnitOfMeasure(String unitOfMeasure) { this.unitOfMeasure = unitOfMeasure; }
    public Integer getRejectedQuantity() { return rejectedQuantity; }
    public void setRejectedQuantity(Integer rejectedQuantity) { this.rejectedQuantity = rejectedQuantity; }
    public Integer getQuantityToReturn() { return quantityToReturn; }
    public void setQuantityToReturn(Integer quantityToReturn) { this.quantityToReturn = quantityToReturn; }
    public Integer getQuantityAlreadyReturned() { return quantityAlreadyReturned; }
    public void setQuantityAlreadyReturned(Integer quantityAlreadyReturned) { this.quantityAlreadyReturned = quantityAlreadyReturned; }
    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
    public String getQualityNotes() { return qualityNotes; }
    public void setQualityNotes(String qualityNotes) { this.qualityNotes = qualityNotes; }
    public String getDefectDescription() { return defectDescription; }
    public void setDefectDescription(String defectDescription) { this.defectDescription = defectDescription; }
    public boolean isReplaced() { return replaced; }
    public void setReplaced(boolean replaced) { this.replaced = replaced; }
    public boolean isCreditNote() { return creditNote; }
    public void setCreditNote(boolean creditNote) { this.creditNote = creditNote; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
