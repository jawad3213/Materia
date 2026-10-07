package com.materia.backend.contexts.masterData.domain.ports.out;

import com.materia.backend.contexts.masterData.domain.entities.Material;

import java.util.List;
import java.util.UUID;

/** Raises the purchase requisitions the replenishment asks for. */
public interface RequisitionRequests {

    /** A requisition raised by the automatic replenishment: the requester is the system. */
    String createRequisitionFromReorder(Material material, int quantity, String reason, boolean isUrgent);

    /** A replenishment requisition; a user who asked for it (one-click reorder) is its requester. */
    String createRequisitionFromReorder(Material material, int quantity, String reason, boolean isUrgent,
                                        String requesterId);

    /** One requisition for several materials of the same supplier. */
    String createGroupedRequisition(String supplierId, List<ReorderLine> lines);

    /** Whether a requisition still in progress already asks for this material. */
    boolean hasOpenRequisitionFor(UUID materialId, String materialCode);

    record ReorderLine(String materialCode, int quantity) {
    }
}
