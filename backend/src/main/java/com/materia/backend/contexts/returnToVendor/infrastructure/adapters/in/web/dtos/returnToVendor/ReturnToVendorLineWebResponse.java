package com.materia.backend.contexts.returnToVendor.infrastructure.adapters.in.web.dtos.returnToVendor;

import java.math.BigDecimal;
import java.util.UUID;

public record ReturnToVendorLineWebResponse(
        UUID id,
        Integer lineNumber,
        String goodsReceiptLineId,
        String purchaseOrderLineId,
        String materialId,
        String materialCode,
        String materialName,
        String unitOfMeasure,
        Integer rejectedQuantity,
        Integer quantityToReturn,
        Integer quantityAlreadyReturned,
        Integer remainingQuantity,
        BigDecimal unitPrice,
        BigDecimal lineValue,
        String rejectionReason,
        String qualityNotes,
        String defectDescription,
        boolean replaced,
        boolean creditNote,
        String notes) {
}
