package com.materia.backend.common.attachment.domain.exceptions;

/** The file store could not write, read or remove a file: an infrastructure failure, answered with 500. */
public class AttachmentStorageException extends RuntimeException {
    public AttachmentStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
