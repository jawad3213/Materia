package com.materia.backend.contexts.purchaseOrder.infrastructure.adapters.out.requisition;

import com.materia.backend.contexts.purchaseOrder.domain.ports.out.RequisitionSource;
import com.materia.backend.contexts.purchaseRequisition.application.dtos.RequisitionOutput;
import com.materia.backend.contexts.purchaseRequisition.domain.ports.in.RequisitionUseCase;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Reads and converts requisitions through the public use case of the requisition context. */
@Component
public class RequisitionSourceAdapter implements RequisitionSource {

    private final RequisitionUseCase requisitions;

    public RequisitionSourceAdapter(RequisitionUseCase requisitions) {
        this.requisitions = requisitions;
    }

    @Override
    public RequisitionToOrder get(UUID requisitionId) {
        RequisitionOutput requisition = requisitions.getById(requisitionId);
        List<RequestedLine> lines = requisition.getLineSummaries().stream()
                .map(line -> new RequestedLine(line.id(), line.materialCode(), line.quantity(), line.supplierId()))
                .toList();
        return new RequisitionToOrder(requisition.getRequisitionCode(), requisition.getCurrencyCode(),
                requisition.getStatus(), requisition.getPurchaseOrderId(), lines);
    }

    @Override
    public void markConverted(UUID requisitionId, String purchaseOrderId, String purchaseOrderCode, String userId) {
        requisitions.convert(requisitionId, purchaseOrderId, purchaseOrderCode, userId);
    }

    @Override
    public void revertConversion(UUID requisitionId, String purchaseOrderId, String userId) {
        requisitions.revertConversion(requisitionId, purchaseOrderId, userId);
    }
}
