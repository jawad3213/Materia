package com.materia.backend.contexts.purchaseRequisition.domain.exceptions;

import com.materia.backend.common.application.exceptions.NotFoundException;

/**
 * Raised when a requisition line references a material that does not exist.
 */
public class RequisitionMaterialNotFoundException extends NotFoundException {

    public RequisitionMaterialNotFoundException(String message) {
        super(message);
    }
}
