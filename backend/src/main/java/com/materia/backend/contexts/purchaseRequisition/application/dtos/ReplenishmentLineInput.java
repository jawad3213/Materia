package com.materia.backend.contexts.purchaseRequisition.application.dtos;

/** One material of a grouped replenishment requisition. */
public record ReplenishmentLineInput(String materialCode, int quantity) {
}
