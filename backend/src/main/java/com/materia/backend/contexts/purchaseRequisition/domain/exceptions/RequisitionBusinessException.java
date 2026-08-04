package com.materia.backend.contexts.purchaseRequisition.domain.exceptions;

import com.materia.backend.common.application.exceptions.BusinessException;

public class RequisitionBusinessException extends BusinessException {

    public RequisitionBusinessException(String message, String errorCode) {
        super(message, errorCode);
    }
}
