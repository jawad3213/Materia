package com.materia.backend.contexts.masterData.infrastructure.adapters.out.requisition;

import com.materia.backend.contexts.masterData.domain.entities.Material;
import com.materia.backend.contexts.masterData.domain.ports.out.RequisitionRequests;
import com.materia.backend.contexts.purchaseRequisition.application.dtos.ReplenishmentLineInput;
import com.materia.backend.contexts.purchaseRequisition.domain.ports.in.ReplenishmentUseCase;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Raises replenishment requisitions through the public use case of the requisition context. */
@Component
public class ReplenishmentRequisitionAdapter implements RequisitionRequests {

    private final ReplenishmentUseCase replenishment;

    public ReplenishmentRequisitionAdapter(ReplenishmentUseCase replenishment) {
        this.replenishment = replenishment;
    }

    @Override
    public String createRequisitionFromReorder(Material material, int quantity, String reason, boolean isUrgent) {
        return createRequisitionFromReorder(material, quantity, reason, isUrgent, null);
    }

    @Override
    public String createRequisitionFromReorder(Material material, int quantity, String reason, boolean isUrgent,
                                               String requesterId) {
        return replenishment.raiseReplenishment(material.getId(), quantity, reason, isUrgent, requesterId);
    }

    @Override
    public String createGroupedRequisition(String supplierId, List<ReorderLine> lines) {
        return replenishment.raiseGroupedReplenishment(supplierId, lines.stream()
                .map(line -> new ReplenishmentLineInput(line.materialCode(), line.quantity()))
                .toList());
    }

    @Override
    public boolean hasOpenRequisitionFor(UUID materialId, String materialCode) {
        return replenishment.hasOpenRequisitionFor(materialId, materialCode);
    }
}
