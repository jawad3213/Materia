package com.materia.backend.contexts.goodsReceipt.domain.ports.out;

import com.materia.backend.common.domain.valueObjects.Money;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** The purchase orders goods are received against. */
public interface OrderedGoods {

    Optional<OrderToReceive> find(UUID purchaseOrderId);

    /** Records a validated receipt on the order: received in full, or partially. */
    void recordDelivery(UUID purchaseOrderId, boolean fullyReceived, String userId);

    /**
     * @param receivable whether goods can be received against the order now
     * @param assignedTo the id of the receiver assigned to the order
     */
    record OrderToReceive(UUID id, String orderCode, boolean receivable, String assignedTo, String assignedToName,
                          UUID supplierId, String supplierName, LocalDate expectedDeliveryDate, UUID requisitionId,
                          List<OrderedLine> lines) {
    }

    record OrderedLine(UUID id, Integer lineNumber, UUID materialId, String materialCode, String materialName,
                       String unitOfMeasure, Money unitPrice, Integer quantity, UUID requisitionLineId) {
    }
}
