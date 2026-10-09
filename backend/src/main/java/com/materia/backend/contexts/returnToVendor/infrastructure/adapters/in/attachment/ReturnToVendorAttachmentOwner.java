package com.materia.backend.contexts.returnToVendor.infrastructure.adapters.in.attachment;

import com.materia.backend.common.attachment.domain.enums.AttachmentOwnerType;
import com.materia.backend.common.attachment.domain.ports.out.AttachmentOwnerLookup;
import com.materia.backend.contexts.returnToVendor.domain.ports.out.ReturnToVendorRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Lets files be attached to this context's documents: answers whether one exists, from this context's own data. */
@Component
public class ReturnToVendorAttachmentOwner implements AttachmentOwnerLookup {

    private final ReturnToVendorRepository repository;

    public ReturnToVendorAttachmentOwner(ReturnToVendorRepository repository) {
        this.repository = repository;
    }

    @Override
    public AttachmentOwnerType ownerType() {
        return AttachmentOwnerType.RETURN_TO_VENDOR;
    }

    @Override
    public boolean exists(UUID ownerId) {
        return repository.existsById(ownerId);
    }
}
