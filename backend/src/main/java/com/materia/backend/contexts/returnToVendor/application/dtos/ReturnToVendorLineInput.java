package com.materia.backend.contexts.returnToVendor.application.dtos;

/** One goods-receipt line to send back, and how much of its rejected quantity. */
public class ReturnToVendorLineInput {

    private String goodsReceiptLineId;
    private Integer quantityToReturn;
    /** Defaults to the rejection reason recorded on the goods receipt line. */
    private String rejectionReason;
    private String defectDescription;
    private String qualityNotes;
    private String notes;

    public ReturnToVendorLineInput() {
    }

    public ReturnToVendorLineInput(String goodsReceiptLineId, Integer quantityToReturn, String rejectionReason) {
        this.goodsReceiptLineId = goodsReceiptLineId;
        this.quantityToReturn = quantityToReturn;
        this.rejectionReason = rejectionReason;
    }

    public String getGoodsReceiptLineId() { return goodsReceiptLineId; }
    public void setGoodsReceiptLineId(String goodsReceiptLineId) { this.goodsReceiptLineId = goodsReceiptLineId; }

    public Integer getQuantityToReturn() { return quantityToReturn; }
    public void setQuantityToReturn(Integer quantityToReturn) { this.quantityToReturn = quantityToReturn; }

    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }

    public String getDefectDescription() { return defectDescription; }
    public void setDefectDescription(String defectDescription) { this.defectDescription = defectDescription; }

    public String getQualityNotes() { return qualityNotes; }
    public void setQualityNotes(String qualityNotes) { this.qualityNotes = qualityNotes; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
