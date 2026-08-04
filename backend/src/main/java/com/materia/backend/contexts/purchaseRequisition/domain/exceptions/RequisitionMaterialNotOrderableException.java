package com.materia.backend.contexts.purchaseRequisition.domain.exceptions;

import com.materia.backend.common.application.exceptions.ValidationException;

import java.util.Map;

public class RequisitionMaterialNotOrderableException extends ValidationException {

    public RequisitionMaterialNotOrderableException(int lineIndex) {
        super(
                "Material is not available for requisition",
                Map.of("lines[" + lineIndex + "].material", "Only active and orderable materials can be requested")
        );
    }
}
