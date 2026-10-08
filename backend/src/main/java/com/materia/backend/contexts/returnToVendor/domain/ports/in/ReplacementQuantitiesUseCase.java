package com.materia.backend.contexts.returnToVendor.domain.ports.in;

import java.util.Map;

/** What the supplier is replacing, for goods receiving to expect again. */
public interface ReplacementQuantitiesUseCase {

    /** Quantity being replaced, per purchase-order line id, by the order's resolved replacement returns. */
    Map<String, Integer> replacedQuantitiesByPurchaseOrderLine(String purchaseOrderId);
}
