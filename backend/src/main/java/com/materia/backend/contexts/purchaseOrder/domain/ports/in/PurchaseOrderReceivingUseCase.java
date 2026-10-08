package com.materia.backend.contexts.purchaseOrder.domain.ports.in;

import java.util.UUID;

/** What goods receiving and vendor returns record on purchase orders. */
public interface PurchaseOrderReceivingUseCase {

    /**
     * Records a validated receipt on the order: partially received, or received in full, and publishes the new
     * delivery status.
     */
    void recordReceipt(UUID purchaseOrderId, boolean fullyReceived, String userId);

    /** Goods sent back will be replaced: a received or closed order is reopened for the replacement delivery. */
    void reopenForReplacement(UUID purchaseOrderId, String userId);
}
