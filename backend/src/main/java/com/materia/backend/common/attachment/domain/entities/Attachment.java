package com.materia.backend.common.attachment.domain.entities;

import com.materia.backend.common.attachment.domain.enums.AttachmentCategory;
import com.materia.backend.common.attachment.domain.enums.AttachmentOwnerType;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * A file attached to a document. The bytes live in AttachmentStorage under {@code storageKey}; this record is the
 * metadata a list shows. {@code fileName} is the uploader's name for the file, kept for display and download only.
 */
public record Attachment(
        UUID id,
        AttachmentOwnerType ownerType,
        UUID ownerId,
        AttachmentCategory category,
        String fileName,
        String contentType,
        long sizeBytes,
        String storageKey,
        String uploadedBy,
        String uploadedByName,
        LocalDateTime uploadedAt
) {
}
