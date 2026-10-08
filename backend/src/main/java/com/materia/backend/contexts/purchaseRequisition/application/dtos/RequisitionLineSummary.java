package com.materia.backend.contexts.purchaseRequisition.application.dtos;

import java.util.UUID;

/** What other contexts may know about a requisition line: what was asked for, how much, and from whom. */
public record RequisitionLineSummary(UUID id, String materialCode, Integer quantity, UUID supplierId) {
}
