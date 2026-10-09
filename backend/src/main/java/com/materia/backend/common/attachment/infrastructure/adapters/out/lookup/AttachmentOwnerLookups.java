package com.materia.backend.common.attachment.infrastructure.adapters.out.lookup;

import com.materia.backend.common.attachment.domain.enums.AttachmentOwnerType;
import com.materia.backend.common.attachment.domain.ports.out.AttachmentOwnerDirectory;
import com.materia.backend.common.attachment.domain.ports.out.AttachmentOwnerLookup;
import com.materia.backend.common.domain.ports.out.UserDisplayNames;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/** Asks the owning context whether a document exists, and the identity context for user names. */
@Component
public class AttachmentOwnerLookups implements AttachmentOwnerDirectory {

    private final Map<AttachmentOwnerType, AttachmentOwnerLookup> owners = new EnumMap<>(AttachmentOwnerType.class);
    private final ObjectProvider<UserDisplayNames> userNames;

    public AttachmentOwnerLookups(List<AttachmentOwnerLookup> lookups, ObjectProvider<UserDisplayNames> userNames) {
        lookups.forEach(lookup -> owners.put(lookup.ownerType(), lookup));
        this.userNames = userNames;
    }

    @Override
    public boolean exists(AttachmentOwnerType ownerType, UUID ownerId) {
        AttachmentOwnerLookup owner = owners.get(ownerType);
        return owner != null && ownerId != null && owner.exists(ownerId);
    }

    @Override
    public Optional<String> userDisplayName(String userId) {
        UserDisplayNames names = userNames.getIfAvailable();
        return names != null && userId != null ? names.displayName(userId) : Optional.empty();
    }
}
