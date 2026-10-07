package com.materia.backend.contexts.purchaseRequisition.domain.ports.in;

import com.materia.backend.contexts.purchaseRequisition.application.dtos.ReplenishmentLineInput;

import java.util.List;
import java.util.UUID;

/** Requisitions raised by the stock replenishment of the master data context. */
public interface ReplenishmentUseCase {

    /**
     * Raises a draft requisition for one material.
     *
     * @param requesterId the user who asked for it (one-click reorder); null when the system raises it
     * @return the requisition code
     */
    String raiseReplenishment(UUID materialId, int quantity, String reason, boolean urgent, String requesterId);

    /** Raises one draft requisition for several materials of a supplier; returns its code. */
    String raiseGroupedReplenishment(String supplierId, List<ReplenishmentLineInput> lines);

    /** Whether a draft, submitted or approved requisition already asks for this material. */
    boolean hasOpenRequisitionFor(UUID materialId, String materialCode);
}
