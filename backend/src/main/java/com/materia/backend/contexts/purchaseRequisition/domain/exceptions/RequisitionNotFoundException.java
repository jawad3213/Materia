package com.materia.backend.contexts.purchaseRequisition.domain.exceptions;

import com.materia.backend.common.application.exceptions.NotFoundException;

import java.util.UUID;

/**
 * Raised when a purchase requisition cannot be found.
 */
public class RequisitionNotFoundException extends NotFoundException {

    public RequisitionNotFoundException(UUID id) {
        super("Purchase requisition not found: " + id);
    }

    public RequisitionNotFoundException(String code) {
        super("Purchase requisition not found: " + code);
    }
}
