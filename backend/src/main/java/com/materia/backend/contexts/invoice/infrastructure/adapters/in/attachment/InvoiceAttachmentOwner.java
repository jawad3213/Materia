package com.materia.backend.contexts.invoice.infrastructure.adapters.in.attachment;

import com.materia.backend.common.attachment.domain.enums.AttachmentOwnerType;
import com.materia.backend.common.attachment.domain.ports.out.AttachmentOwnerLookup;
import com.materia.backend.contexts.invoice.domain.ports.out.InvoiceRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Lets files be attached to this context's documents: answers whether one exists, from this context's own data. */
@Component
public class InvoiceAttachmentOwner implements AttachmentOwnerLookup {

    private final InvoiceRepository repository;

    public InvoiceAttachmentOwner(InvoiceRepository repository) {
        this.repository = repository;
    }

    @Override
    public AttachmentOwnerType ownerType() {
        return AttachmentOwnerType.INVOICE;
    }

    @Override
    public boolean exists(UUID ownerId) {
        return repository.existsById(ownerId);
    }
}
