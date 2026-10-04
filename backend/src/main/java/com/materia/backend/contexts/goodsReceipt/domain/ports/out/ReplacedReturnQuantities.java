package com.materia.backend.contexts.goodsReceipt.domain.ports.out;

import java.util.Map;

/**
 * Goods rejected at receipt, sent back to the supplier and being replaced by them. The order counted them as
 * received; the replacement is expected again, so receipts must not count them as delivered any more.
 */
@FunctionalInterface
public interface ReplacedReturnQuantities {

    /** No replacement is pending (used when the returns context is not available). */
    ReplacedReturnQuantities NONE = purchaseOrderId -> Map.of();

    /** Quantity being replaced, per purchase-order line id, for the given purchase order. */
    Map<String, Integer> byPurchaseOrderLine(String purchaseOrderId);
}
