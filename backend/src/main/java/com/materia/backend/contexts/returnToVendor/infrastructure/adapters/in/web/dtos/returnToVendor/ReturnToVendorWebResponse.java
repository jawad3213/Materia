package com.materia.backend.contexts.returnToVendor.infrastructure.adapters.in.web.dtos.returnToVendor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ReturnToVendorWebResponse(
        UUID id,
        String returnCode,
        String goodsReceiptId,
        String goodsReceiptCode,
        String purchaseOrderId,
        String purchaseOrderCode,
        String supplierId,
        String supplierName,
        String supplierCode,
        String currencyCode,
        BigDecimal totalValue,
        Integer totalQuantity,
        String status,
        String resolutionType,
        LocalDate returnDate,
        LocalDate resolutionDate,
        String returnReason,
        String supplierResponse,
        String rejectionSummary,
        String creditNoteReference,
        String creditNoteAmount,
        String replacementReference,
        String notes,
        String internalNotes,
        String createdBy,
        LocalDateTime createdAt,
        String updatedBy,
        LocalDateTime updatedAt,
        List<ReturnToVendorLineWebResponse> lines) {
}
