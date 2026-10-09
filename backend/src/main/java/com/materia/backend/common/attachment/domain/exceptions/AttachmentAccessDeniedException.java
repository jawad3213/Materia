package com.materia.backend.common.attachment.domain.exceptions;

import com.materia.backend.common.application.exceptions.BusinessException;

/** Answered with 403 by GlobalExceptionHandler. */
public class AttachmentAccessDeniedException extends BusinessException {
    public AttachmentAccessDeniedException(String message) {
        super(message, "ATTACHMENT_ACCESS_DENIED");
    }
}
