package com.materia.backend.common.attachment.infrastructure.adapters.out.persistence;

import com.materia.backend.common.audit.application.Audited;
import com.materia.backend.common.attachment.domain.entities.Attachment;
import com.materia.backend.common.attachment.domain.enums.AttachmentCategory;
import com.materia.backend.common.attachment.domain.enums.AttachmentOwnerType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "attachments", indexes = @Index(name = "idx_attachments_owner", columnList = "owner_type, owner_id"))
@Audited(type = "Attachment", code = "fileName")
public class AttachmentJpaEntity {

    @Id
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "owner_type", nullable = false, length = 30)
    private AttachmentOwnerType ownerType;

    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 30)
    private AttachmentCategory category;

    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @Column(name = "content_type", nullable = false, length = 100)
    private String contentType;

    @Column(name = "size_bytes", nullable = false)
    private long sizeBytes;

    @Column(name = "storage_key", nullable = false, length = 300)
    private String storageKey;

    @Column(name = "uploaded_by", length = 100)
    private String uploadedBy;

    @Column(name = "uploaded_by_name", length = 200)
    private String uploadedByName;

    @Column(name = "uploaded_at", nullable = false)
    private LocalDateTime uploadedAt;

    protected AttachmentJpaEntity() {
    }

    static AttachmentJpaEntity fromDomain(Attachment a) {
        AttachmentJpaEntity e = new AttachmentJpaEntity();
        e.id = a.id();
        e.ownerType = a.ownerType();
        e.ownerId = a.ownerId();
        e.category = a.category();
        e.fileName = a.fileName();
        e.contentType = a.contentType();
        e.sizeBytes = a.sizeBytes();
        e.storageKey = a.storageKey();
        e.uploadedBy = a.uploadedBy();
        e.uploadedByName = a.uploadedByName();
        e.uploadedAt = a.uploadedAt();
        return e;
    }

    Attachment toDomain() {
        return new Attachment(id, ownerType, ownerId, category, fileName, contentType, sizeBytes, storageKey,
                uploadedBy, uploadedByName, uploadedAt);
    }
}
