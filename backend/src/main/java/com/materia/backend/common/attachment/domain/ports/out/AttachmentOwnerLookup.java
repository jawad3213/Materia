package com.materia.backend.common.attachment.domain.ports.out;

import com.materia.backend.common.attachment.domain.enums.AttachmentOwnerType;

import java.util.UUID;

/**
 * Provided by the context that owns a kind of document: whether one of its documents exists. Each answers from its
 * own data, so the attachment code names no other context's table.
 */
public interface AttachmentOwnerLookup {

    AttachmentOwnerType ownerType();

    boolean exists(UUID ownerId);
}
