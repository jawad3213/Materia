package com.materia.backend.contexts.goodsReceipt.infrastructure.adapters.out.requisition;

import com.materia.backend.contexts.goodsReceipt.domain.ports.out.RequisitionDeliveries;
import com.materia.backend.contexts.purchaseRequisition.domain.ports.in.RequisitionUseCase;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Records deliveries on requisitions through the public use case of the requisition context. */
@Component
public class RequisitionDeliveriesAdapter implements RequisitionDeliveries {

    private final RequisitionUseCase requisitions;

    public RequisitionDeliveriesAdapter(RequisitionUseCase requisitions) {
        this.requisitions = requisitions;
    }

    @Override
    public void recordReceipt(UUID requisitionId, UUID requisitionLineId, int accepted, int rejected) {
        requisitions.recordReceipt(requisitionId, requisitionLineId, accepted, rejected);
    }
}
