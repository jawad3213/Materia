package com.materia.backend.common.attachment.domain.exceptions;

import com.materia.backend.common.application.exceptions.ValidationException;

public class AttachmentValidationException extends ValidationException {
    public AttachmentValidationException(String message) {
        super(message);
    }
}
