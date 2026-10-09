package com.materia.backend.common.attachment.application.services;

import com.materia.backend.common.attachment.application.dtos.AttachmentContent;
import com.materia.backend.common.attachment.application.dtos.UploadAttachmentInput;
import com.materia.backend.common.attachment.domain.entities.Attachment;
import com.materia.backend.common.attachment.domain.enums.AttachmentCategory;
import com.materia.backend.common.attachment.domain.enums.AttachmentFileType;
import com.materia.backend.common.attachment.domain.enums.AttachmentOwnerType;
import com.materia.backend.common.attachment.domain.exceptions.AttachmentAccessDeniedException;
import com.materia.backend.common.attachment.domain.exceptions.AttachmentNotFoundException;
import com.materia.backend.common.attachment.domain.exceptions.AttachmentValidationException;
import com.materia.backend.common.attachment.domain.ports.in.AttachmentUseCase;
import com.materia.backend.common.attachment.domain.ports.out.AttachmentOwnerDirectory;
import com.materia.backend.common.attachment.domain.ports.out.AttachmentRepository;
import com.materia.backend.common.attachment.domain.ports.out.AttachmentStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Attaches scans and photos to purchase orders, goods receipts, invoices and returns. The bytes are written to
 * the file store first and the metadata row second, so a row never points to a missing file; if the row cannot be
 * saved the file is removed again.
 */
@Service
public class AttachmentService implements AttachmentUseCase {

    private static final Logger log = LoggerFactory.getLogger(AttachmentService.class);
    private static final int MAX_FILE_NAME_LENGTH = 200;

    private final AttachmentRepository repository;
    private final AttachmentStorage storage;
    private final AttachmentOwnerDirectory directory;
    private final long maxFileSizeBytes;

    public AttachmentService(AttachmentRepository repository,
                             AttachmentStorage storage,
                             AttachmentOwnerDirectory directory,
                             @Value("${app.attachments.max-file-size-bytes:10485760}") long maxFileSizeBytes) {
        this.repository = repository;
        this.storage = storage;
        this.directory = directory;
        this.maxFileSizeBytes = maxFileSizeBytes;
    }

    @Override
    public Attachment upload(UploadAttachmentInput input) {
        if (input.ownerType() == null || input.ownerId() == null) {
            throw new AttachmentValidationException("Choose the document the file belongs to.");
        }
        byte[] content = input.content();
        if (content == null || content.length == 0) {
            throw new AttachmentValidationException("The file is empty.");
        }
        if (content.length > maxFileSizeBytes) {
            throw new AttachmentValidationException("The file is larger than " + (maxFileSizeBytes / (1024 * 1024)) + " MB.");
        }
        AttachmentFileType type = AttachmentFileType.detect(content).orElseThrow(() ->
                new AttachmentValidationException("Only PDF, PNG, JPEG and WebP files can be attached."));
        if (!directory.exists(input.ownerType(), input.ownerId())) {
            throw new AttachmentNotFoundException("The document to attach the file to does not exist.");
        }

        UUID id = UUID.randomUUID();
        String storageKey = input.ownerType().name().toLowerCase(Locale.ROOT) + "/" + input.ownerId() + "/" + id + "." + type.getExtension();
        Attachment attachment = new Attachment(
                id,
                input.ownerType(),
                input.ownerId(),
                input.category() != null ? input.category() : AttachmentCategory.OTHER,
                cleanFileName(input.fileName(), type),
                type.getContentType(),
                content.length,
                storageKey,
                input.actorId(),
                directory.userDisplayName(input.actorId()).orElse(null),
                LocalDateTime.now()
        );

        storage.store(storageKey, content);
        try {
            return repository.save(attachment);
        } catch (RuntimeException e) {
            removeQuietly(storageKey);
            throw e;
        }
    }

    @Override
    public List<Attachment> list(AttachmentOwnerType ownerType, UUID ownerId) {
        return repository.findByOwner(ownerType, ownerId);
    }

    @Override
    public Attachment get(UUID id) {
        return repository.findById(id).orElseThrow(() -> new AttachmentNotFoundException("The attachment does not exist."));
    }

    @Override
    public AttachmentContent content(UUID id) {
        Attachment attachment = get(id);
        return new AttachmentContent(attachment, storage.read(attachment.storageKey()));
    }

    @Override
    public void delete(UUID id, String actorId, boolean actorIsAdmin) {
        Attachment attachment = get(id);
        if (!actorIsAdmin && (actorId == null || !actorId.equals(attachment.uploadedBy()))) {
            throw new AttachmentAccessDeniedException("Only the person who added this file or an administrator can remove it.");
        }
        repository.deleteById(id);
        removeQuietly(attachment.storageKey());
    }

    /** Keeps the name readable but drops any folder part and characters that would break a download header. */
    static String cleanFileName(String original, AttachmentFileType type) {
        String name = original == null ? "" : original;
        name = name.substring(Math.max(name.lastIndexOf('/'), name.lastIndexOf('\\')) + 1);
        name = name.replaceAll("[\\p{Cntrl}\"]", "").trim();
        if (name.isEmpty() || name.equals(".") || name.equals("..")) {
            name = "attachment." + type.getExtension();
        }
        return name.length() > MAX_FILE_NAME_LENGTH ? name.substring(name.length() - MAX_FILE_NAME_LENGTH) : name;
    }

    /** A file left behind only wastes disk space, so a failed removal is logged rather than surfaced. */
    private void removeQuietly(String storageKey) {
        try {
            storage.delete(storageKey);
        } catch (RuntimeException e) {
            log.warn("Could not remove attachment file {}", storageKey, e);
        }
    }
}
