package com.materia.backend.common.attachment.domain.ports.out;

import com.materia.backend.common.attachment.domain.enums.AttachmentOwnerType;

import java.util.Optional;
import java.util.UUID;

/** Facts the attachment context needs from the documents and users it does not own. */
public interface AttachmentOwnerDirectory {
    boolean exists(AttachmentOwnerType ownerType, UUID ownerId);
    Optional<String> userDisplayName(String userId);
}
