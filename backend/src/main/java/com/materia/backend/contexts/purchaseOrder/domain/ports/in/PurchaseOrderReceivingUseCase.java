package com.materia.backend.contexts.purchaseOrder.domain.ports.in;

import com.materia.backend.contexts.purchaseOrder.application.dtos.OrderForReceiptOutput;

import java.util.Optional;
import java.util.UUID;

/** What goods receiving reads from and records on purchase orders. */
public interface PurchaseOrderReceivingUseCase {

    Optional<OrderForReceiptOutput> findForReceipt(UUID purchaseOrderId);

    /**
     * Records a validated receipt on the order: partially received, or received in full, and publishes the new
     * delivery status.
     */
    void recordReceipt(UUID purchaseOrderId, boolean fullyReceived, String userId);
}
