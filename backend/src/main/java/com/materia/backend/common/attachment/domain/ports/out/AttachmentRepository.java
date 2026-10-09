package com.materia.backend.common.attachment.domain.ports.out;

import com.materia.backend.common.attachment.domain.entities.Attachment;
import com.materia.backend.common.attachment.domain.enums.AttachmentOwnerType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AttachmentRepository {
    Attachment save(Attachment attachment);
    Optional<Attachment> findById(UUID id);
    /** Newest first. */
    List<Attachment> findByOwner(AttachmentOwnerType ownerType, UUID ownerId);
    void deleteById(UUID id);
}
