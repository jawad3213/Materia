package com.materia.backend.contexts.goodsReceipt.domain.ports.out;

import java.util.UUID;

/** Tells a requisition what arrived for it, so the requester can follow the delivery of what they asked for. */
public interface RequisitionDeliveries {

    void recordReceipt(UUID requisitionId, UUID requisitionLineId, int accepted, int rejected);

    /** For callers that do not follow requisitions (unit tests of unrelated rules). */
    RequisitionDeliveries NONE = (requisitionId, requisitionLineId, accepted, rejected) -> {
    };
}
