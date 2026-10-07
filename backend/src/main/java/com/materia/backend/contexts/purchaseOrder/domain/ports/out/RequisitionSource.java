package com.materia.backend.contexts.purchaseOrder.domain.ports.out;

import java.util.List;
import java.util.UUID;

/** The requisitions purchase orders are created from. */
public interface RequisitionSource {

    /** The requisition with this id; fails as the requisition context does when there is none. */
    RequisitionToOrder get(UUID requisitionId);

    /** Records that an order now covers the requisition; refused unless the requisition is approved. */
    void markConverted(UUID requisitionId, String purchaseOrderId, String purchaseOrderCode, String userId);

    /** Hands the requisition back so it can be ordered again. */
    void revertConversion(UUID requisitionId, String purchaseOrderId, String userId);

    /**
     * @param status          the requisition status code, e.g. APPROVED or CONVERTED
     * @param purchaseOrderId the order it was converted to, if any
     */
    record RequisitionToOrder(String code, String currencyCode, String status, String purchaseOrderId,
                              List<RequestedLine> lines) {
    }

    record RequestedLine(UUID id, String materialCode, Integer quantity, UUID supplierId) {
    }
}
