package com.materia.backend.common.attachment.application.dtos;

import com.materia.backend.common.attachment.domain.enums.AttachmentCategory;
import com.materia.backend.common.attachment.domain.enums.AttachmentOwnerType;

import java.util.UUID;

public record UploadAttachmentInput(
        AttachmentOwnerType ownerType,
        UUID ownerId,
        AttachmentCategory category,
        String fileName,
        byte[] content,
        String actorId
) {
}
