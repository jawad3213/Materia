package com.materia.backend.contexts.goodsReceipt.domain.exceptions;

/** Raised when a receipt is completed before every line has a quality result. */
public class GoodsReceiptQualityInspectionRequiredException extends GoodsReceiptBusinessException {

    public GoodsReceiptQualityInspectionRequiredException(String message) {
        super(message, "GR_QUALITY_INSPECTION_REQUIRED");
    }
}
