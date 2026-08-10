package com.materia.backend.contexts.returnToVendor.infrastructure.adapters.out.persistence.entities;

import com.materia.backend.common.infrastructure.persistence.BaseJpaEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "return_to_vendor_lines")
public class ReturnToVendorLineJpaEntity extends BaseJpaEntity {

    @ManyToOne
    @JoinColumn(name = "return_to_vendor_id", nullable = false)
    private ReturnToVendorJpaEntity returnToVendor;

    @Column(name = "line_number")
    private Integer lineNumber;

    @Column(name = "goods_receipt_line_id", length = 100)
    private String goodsReceiptLineId;

    @Column(name = "material_code", length = 100)
    private String materialCode;

    @Column(name = "material_name", length = 200)
    private String materialName;

    @Column(name = "unit_of_measure", length = 50)
    private String unitOfMeasure;

    @Column(name = "rejected_quantity")
    private Integer rejectedQuantity;

    @Column(name = "quantity_to_return")
    private Integer quantityToReturn;

    @Column(name = "quantity_already_returned")
    private Integer quantityAlreadyReturned;

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    @Column(name = "quality_notes", length = 500)
    private String qualityNotes;

    @Column(name = "defect_description", length = 500)
    private String defectDescription;

    @Column(name = "is_replaced")
    private boolean isReplaced;

    @Column(name = "is_credit_note")
    private boolean isCreditNote;

    @Column(name = "notes", length = 500)
    private String notes;

    public ReturnToVendorJpaEntity getReturnToVendor() { return returnToVendor; }
    public void setReturnToVendor(ReturnToVendorJpaEntity returnToVendor) { this.returnToVendor = returnToVendor; }

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

    public boolean isReplaced() { return isReplaced; }
    public void setReplaced(boolean replaced) { isReplaced = replaced; }

    public boolean isCreditNote() { return isCreditNote; }
    public void setCreditNote(boolean creditNote) { isCreditNote = creditNote; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
