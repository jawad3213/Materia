package com.materia.backend.contexts.goodsReceipt.infrastructure.adapters.in.attachment;

import com.materia.backend.common.attachment.domain.enums.AttachmentOwnerType;
import com.materia.backend.common.attachment.domain.ports.out.AttachmentOwnerLookup;
import com.materia.backend.contexts.goodsReceipt.domain.ports.out.GoodsReceiptRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Lets files be attached to this context's documents: answers whether one exists, from this context's own data. */
@Component
public class GoodsReceiptAttachmentOwner implements AttachmentOwnerLookup {

    private final GoodsReceiptRepository repository;

    public GoodsReceiptAttachmentOwner(GoodsReceiptRepository repository) {
        this.repository = repository;
    }

    @Override
    public AttachmentOwnerType ownerType() {
        return AttachmentOwnerType.GOODS_RECEIPT;
    }

    @Override
    public boolean exists(UUID ownerId) {
        return repository.existsById(ownerId);
    }
}
