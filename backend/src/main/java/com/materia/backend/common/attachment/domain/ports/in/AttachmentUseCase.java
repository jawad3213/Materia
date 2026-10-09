package com.materia.backend.common.attachment.domain.ports.in;

import com.materia.backend.common.attachment.application.dtos.AttachmentContent;
import com.materia.backend.common.attachment.application.dtos.UploadAttachmentInput;
import com.materia.backend.common.attachment.domain.entities.Attachment;
import com.materia.backend.common.attachment.domain.enums.AttachmentOwnerType;

import java.util.List;
import java.util.UUID;

public interface AttachmentUseCase {
    Attachment upload(UploadAttachmentInput input);
    List<Attachment> list(AttachmentOwnerType ownerType, UUID ownerId);
    Attachment get(UUID id);
    AttachmentContent content(UUID id);
    /** The uploader may remove their own file; an administrator may remove any. */
    void delete(UUID id, String actorId, boolean actorIsAdmin);
}
