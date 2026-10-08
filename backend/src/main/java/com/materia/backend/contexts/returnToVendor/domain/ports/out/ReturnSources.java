package com.materia.backend.contexts.returnToVendor.domain.ports.out;

import com.materia.backend.common.domain.valueObjects.Money;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** The goods receipts and purchase orders a return to vendor comes from, and what a replacement changes on them. */
public interface ReturnSources {

    Optional<ReceiptToReturn> findReceipt(UUID goodsReceiptId);

    Optional<OrderToReplace> findOrder(UUID purchaseOrderId);

    /** The supplier replaces the goods: the order is reopened for the replacement delivery. */
    void reopenOrderForReplacement(UUID purchaseOrderId, String userId);

    /** The replaced quantity of a material is on order again. */
    void putBackOnOrder(UUID materialId, String materialCode, int quantity, String userId);

    /** @param validated whether the receipt was validated (completed or partial) */
    record ReceiptToReturn(UUID id, String receiptCode, boolean validated, String purchaseOrderId,
                           String purchaseOrderCode, String supplierId, String supplierName,
                           List<ReceivedLine> lines) {
    }

    record ReceivedLine(UUID id, String purchaseOrderLineId, UUID materialId, String materialCode, String materialName,
                        String unitOfMeasure, Money unitPrice, Integer quantityRejected, String rejectionReason,
                        String qualityNotes) {
    }

    /**
     * @param status        the order status code
     * @param goodsReceived whether goods were received on it (partially received, received or completed)
     */
    record OrderToReplace(UUID id, String status, boolean goodsReceived, String supplierCode, String currencyCode) {
    }
}
