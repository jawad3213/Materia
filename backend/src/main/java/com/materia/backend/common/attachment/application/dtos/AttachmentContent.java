package com.materia.backend.common.attachment.application.dtos;

import com.materia.backend.common.attachment.domain.entities.Attachment;

public record AttachmentContent(Attachment attachment, byte[] bytes) {
}
