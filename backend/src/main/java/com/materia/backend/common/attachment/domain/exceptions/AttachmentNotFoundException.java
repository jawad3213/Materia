package com.materia.backend.common.attachment.domain.exceptions;

import com.materia.backend.common.application.exceptions.NotFoundException;

public class AttachmentNotFoundException extends NotFoundException {
    public AttachmentNotFoundException(String message) {
        super(message, "ATTACHMENT_NOT_FOUND");
    }
}
