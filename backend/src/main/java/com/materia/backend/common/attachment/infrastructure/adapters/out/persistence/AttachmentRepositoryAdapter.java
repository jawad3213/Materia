package com.materia.backend.common.attachment.infrastructure.adapters.out.persistence;

import com.materia.backend.common.attachment.domain.entities.Attachment;
import com.materia.backend.common.attachment.domain.enums.AttachmentOwnerType;
import com.materia.backend.common.attachment.domain.ports.out.AttachmentRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class AttachmentRepositoryAdapter implements AttachmentRepository {

    private final SpringDataAttachmentRepository jpa;

    public AttachmentRepositoryAdapter(SpringDataAttachmentRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Attachment save(Attachment attachment) {
        return jpa.save(AttachmentJpaEntity.fromDomain(attachment)).toDomain();
    }

    @Override
    public Optional<Attachment> findById(UUID id) {
        return jpa.findById(id).map(AttachmentJpaEntity::toDomain);
    }

    @Override
    public List<Attachment> findByOwner(AttachmentOwnerType ownerType, UUID ownerId) {
        return jpa.findByOwnerTypeAndOwnerIdOrderByUploadedAtDesc(ownerType, ownerId).stream()
                .map(AttachmentJpaEntity::toDomain)
                .toList();
    }

    @Override
    public void deleteById(UUID id) {
        jpa.deleteById(id);
    }
}
