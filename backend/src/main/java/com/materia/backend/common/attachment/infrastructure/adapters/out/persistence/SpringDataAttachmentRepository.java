package com.materia.backend.common.attachment.infrastructure.adapters.out.persistence;

import com.materia.backend.common.attachment.domain.enums.AttachmentOwnerType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SpringDataAttachmentRepository extends JpaRepository<AttachmentJpaEntity, UUID> {

    List<AttachmentJpaEntity> findByOwnerTypeAndOwnerIdOrderByUploadedAtDesc(AttachmentOwnerType ownerType, UUID ownerId);
}
