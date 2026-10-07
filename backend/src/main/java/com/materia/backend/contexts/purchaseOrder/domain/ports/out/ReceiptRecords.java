package com.materia.backend.contexts.purchaseOrder.domain.ports.out;

import java.util.Map;

/** What the goods receipts of an order delivered. */
public interface ReceiptRecords {

    /** Quantity received per purchase-order line id, summed over the validated (completed or partial) receipts. */
    Map<String, Integer> receivedQuantitiesByOrderLine(String purchaseOrderId);

    /** Receives everything still outstanding on the order through one goods receipt. */
    void receiveRemaining(String purchaseOrderId, String receiverId, String receiverName);
}
