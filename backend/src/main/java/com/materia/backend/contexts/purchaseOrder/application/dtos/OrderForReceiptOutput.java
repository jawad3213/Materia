package com.materia.backend.contexts.purchaseOrder.application.dtos;

import com.materia.backend.common.domain.valueObjects.Money;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * A purchase order as goods receiving needs it.
 *
 * @param receivable whether goods can be received against it now (ready for receipt or partially received)
 * @param assignedTo the id of the receiver assigned to it
 */
public record OrderForReceiptOutput(UUID id, String orderCode, boolean receivable, String assignedTo,
                                    String assignedToName, UUID supplierId, String supplierName,
                                    LocalDate expectedDeliveryDate, UUID requisitionId, List<Line> lines) {

    public record Line(UUID id, Integer lineNumber, UUID materialId, String materialCode, String materialName,
                       String unitOfMeasure, Money unitPrice, Integer quantity, UUID requisitionLineId) {
    }
}
