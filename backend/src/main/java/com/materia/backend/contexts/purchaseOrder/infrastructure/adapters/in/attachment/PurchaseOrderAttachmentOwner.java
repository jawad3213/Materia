package com.materia.backend.contexts.purchaseOrder.infrastructure.adapters.in.attachment;

import com.materia.backend.common.attachment.domain.enums.AttachmentOwnerType;
import com.materia.backend.common.attachment.domain.ports.out.AttachmentOwnerLookup;
import com.materia.backend.contexts.purchaseOrder.domain.ports.out.PurchaseOrderRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Lets files be attached to this context's documents: answers whether one exists, from this context's own data. */
@Component
public class PurchaseOrderAttachmentOwner implements AttachmentOwnerLookup {

    private final PurchaseOrderRepository repository;

    public PurchaseOrderAttachmentOwner(PurchaseOrderRepository repository) {
        this.repository = repository;
    }

    @Override
    public AttachmentOwnerType ownerType() {
        return AttachmentOwnerType.PURCHASE_ORDER;
    }

    @Override
    public boolean exists(UUID ownerId) {
        return repository.existsById(ownerId);
    }
}
