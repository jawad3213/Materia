package com.materia.backend.contexts.purchaseOrder.domain.ports.in;

import com.materia.backend.contexts.purchaseOrder.application.dtos.PurchaseOrderView;

import java.util.Optional;
import java.util.UUID;

/** Read access to purchase orders for the other contexts. */
public interface PurchaseOrderLookupUseCase {

    Optional<PurchaseOrderView> findOrder(UUID purchaseOrderId);
}
