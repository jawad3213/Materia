package com.materia.backend.common.attachment.infrastructure.adapters.in.web.dtos;

import com.materia.backend.common.attachment.domain.entities.Attachment;
import com.materia.backend.common.attachment.domain.enums.AttachmentCategory;
import com.materia.backend.common.attachment.domain.enums.AttachmentOwnerType;

import java.time.LocalDateTime;
import java.util.UUID;

/** Attachment metadata; the storage key stays on the server. */
public record AttachmentWebResponse(
        UUID id,
        AttachmentOwnerType ownerType,
        UUID ownerId,
        AttachmentCategory category,
        String fileName,
        String contentType,
        long sizeBytes,
        String uploadedBy,
        String uploadedByName,
        LocalDateTime uploadedAt
) {
    public static AttachmentWebResponse from(Attachment a) {
        return new AttachmentWebResponse(a.id(), a.ownerType(), a.ownerId(), a.category(), a.fileName(),
                a.contentType(), a.sizeBytes(), a.uploadedBy(), a.uploadedByName(), a.uploadedAt());
    }
}
