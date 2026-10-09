package com.materia.backend.contexts.purchaseOrder.application.dtos;

import com.materia.backend.common.domain.valueObjects.Money;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * A purchase order as the other contexts (receiving, invoicing, returns) need to see it.
 *
 * @param status        the order status code, e.g. READY_FOR_RECEIPT
 * @param receivable    whether goods can be received against it now (ready for receipt or partially received)
 * @param goodsReceived whether goods were received on it (partially received, received or completed)
 * @param assignedTo    the id of the receiver assigned to it
 */
public record PurchaseOrderView(UUID id, String orderCode, String status, boolean receivable, boolean goodsReceived,
                                UUID supplierId, String supplierCode, String supplierName, String currencyCode,
                                String assignedTo, String assignedToName, LocalDate expectedDeliveryDate,
                                UUID requisitionId, List<Line> lines) {

    public record Line(UUID id, Integer lineNumber, UUID materialId, String materialCode, String materialName,
                       String unitOfMeasure, Money unitPrice, Integer quantity, UUID requisitionLineId,
                       java.math.BigDecimal taxRate) {
    }
}
