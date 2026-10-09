package com.materia.backend.common.attachment.application.services;

import com.materia.backend.common.attachment.application.dtos.UploadAttachmentInput;
import com.materia.backend.common.attachment.domain.entities.Attachment;
import com.materia.backend.common.attachment.domain.enums.AttachmentCategory;
import com.materia.backend.common.attachment.domain.enums.AttachmentFileType;
import com.materia.backend.common.attachment.domain.enums.AttachmentOwnerType;
import com.materia.backend.common.attachment.domain.exceptions.AttachmentAccessDeniedException;
import com.materia.backend.common.attachment.domain.exceptions.AttachmentNotFoundException;
import com.materia.backend.common.attachment.domain.exceptions.AttachmentValidationException;
import com.materia.backend.common.attachment.domain.ports.out.AttachmentOwnerDirectory;
import com.materia.backend.common.attachment.domain.ports.out.AttachmentRepository;
import com.materia.backend.common.attachment.domain.ports.out.AttachmentStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AttachmentServiceTest {

    private static final byte[] PDF = "%PDF-1.7\n1 0 obj".getBytes(StandardCharsets.US_ASCII);
    private static final byte[] PNG = {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0, 0};
    private static final byte[] JPEG = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0};
    private static final byte[] WEBP = "RIFF\0\0\0\0WEBPVP8 ".getBytes(StandardCharsets.US_ASCII);

    private final Map<UUID, Attachment> rows = new HashMap<>();
    private final Map<String, byte[]> files = new HashMap<>();
    private final UUID orderId = UUID.randomUUID();
    private boolean failNextSave;
    private AttachmentService service;

    @BeforeEach
    void setUp() {
        AttachmentRepository repository = new AttachmentRepository() {
            public Attachment save(Attachment a) {
                if (failNextSave) throw new IllegalStateException("database down");
                rows.put(a.id(), a);
                return a;
            }
            public Optional<Attachment> findById(UUID id) { return Optional.ofNullable(rows.get(id)); }
            public List<Attachment> findByOwner(AttachmentOwnerType type, UUID ownerId) {
                return rows.values().stream()
                        .filter(a -> a.ownerType() == type && a.ownerId().equals(ownerId))
                        .sorted(Comparator.comparing(Attachment::uploadedAt).reversed())
                        .toList();
            }
            public void deleteById(UUID id) { rows.remove(id); }
        };
        AttachmentStorage storage = new AttachmentStorage() {
            public void store(String key, byte[] content) { files.put(key, content); }
            public byte[] read(String key) { return files.get(key); }
            public void delete(String key) { files.remove(key); }
        };
        AttachmentOwnerDirectory directory = new AttachmentOwnerDirectory() {
            public boolean exists(AttachmentOwnerType type, UUID id) { return type == AttachmentOwnerType.PURCHASE_ORDER && id.equals(orderId); }
            public Optional<String> userDisplayName(String userId) { return Optional.of("Name of " + userId); }
        };
        service = new AttachmentService(repository, storage, directory, 1024);
    }

    private Attachment upload(byte[] content, String fileName, String actor) {
        return service.upload(new UploadAttachmentInput(AttachmentOwnerType.PURCHASE_ORDER, orderId,
                AttachmentCategory.DELIVERY_NOTE, fileName, content, actor));
    }

    @Test
    @DisplayName("upload: a PDF is stored with its detected type, size, uploader name and a server-generated key")
    void upload_storesFileAndMetadata() {
        Attachment a = upload(PDF, "delivery-note.pdf", "user-1");

        assertEquals("application/pdf", a.contentType());
        assertEquals(PDF.length, a.sizeBytes());
        assertEquals("delivery-note.pdf", a.fileName());
        assertEquals(AttachmentCategory.DELIVERY_NOTE, a.category());
        assertEquals("Name of user-1", a.uploadedByName());
        assertEquals("purchase_order/" + orderId + "/" + a.id() + ".pdf", a.storageKey());
        assertArrayEquals(PDF, files.get(a.storageKey()));
        assertArrayEquals(PDF, service.content(a.id()).bytes());
    }

    @Test
    @DisplayName("upload: the type comes from the file's bytes, so PNG, JPEG and WebP are recognised whatever their name")
    void upload_detectsImageTypesFromContent() {
        assertEquals("image/png", upload(PNG, "scan.pdf", "u").contentType());
        assertEquals("image/jpeg", upload(JPEG, "photo", "u").contentType());
        assertEquals("image/webp", upload(WEBP, "photo.webp", "u").contentType());
    }

    @Test
    @DisplayName("upload: a file that is not a PDF or a supported image is refused, even when named .pdf")
    void upload_refusesUnsupportedContent() {
        byte[] html = "<html><script>alert(1)</script>".getBytes(StandardCharsets.UTF_8);

        assertThrows(AttachmentValidationException.class, () -> upload(html, "invoice.pdf", "u"));
        assertTrue(files.isEmpty());
    }

    @Test
    @DisplayName("upload: an empty file and a file over the size limit are refused")
    void upload_refusesEmptyAndOversizedFiles() {
        byte[] big = Arrays.copyOf(PDF, 1025);

        assertThrows(AttachmentValidationException.class, () -> upload(new byte[0], "a.pdf", "u"));
        assertThrows(AttachmentValidationException.class, () -> upload(big, "a.pdf", "u"));
    }

    @Test
    @DisplayName("upload: a file cannot be attached to a document that does not exist")
    void upload_refusesUnknownDocument() {
        var input = new UploadAttachmentInput(AttachmentOwnerType.INVOICE, UUID.randomUUID(), null, "a.pdf", PDF, "u");

        assertThrows(AttachmentNotFoundException.class, () -> service.upload(input));
        assertTrue(files.isEmpty());
    }

    @Test
    @DisplayName("upload: when the metadata cannot be saved, the stored file is removed again")
    void upload_removesFileWhenSaveFails() {
        failNextSave = true;

        assertThrows(IllegalStateException.class, () -> upload(PDF, "a.pdf", "u"));
        assertTrue(files.isEmpty());
    }

    @Test
    @DisplayName("upload: folder parts and control characters are dropped from the name; a blank name gets a default")
    void upload_cleansFileName() {
        assertEquals("evil.pdf", AttachmentService.cleanFileName("../../etc/evil.pdf", AttachmentFileType.PDF));
        assertEquals("scan.pdf", AttachmentService.cleanFileName("C:\\Users\\me\\scan.pdf", AttachmentFileType.PDF));
        assertEquals("ab.png", AttachmentService.cleanFileName("a\"\r\nb.png", AttachmentFileType.PNG));
        assertEquals("attachment.jpg", AttachmentService.cleanFileName("  ", AttachmentFileType.JPEG));
        assertEquals("attachment.pdf", AttachmentService.cleanFileName(null, AttachmentFileType.PDF));
    }

    @Test
    @DisplayName("delete: the uploader can remove their file, which also removes it from storage")
    void delete_byUploader() {
        Attachment a = upload(PDF, "a.pdf", "user-1");

        service.delete(a.id(), "user-1", false);

        assertFalse(rows.containsKey(a.id()));
        assertFalse(files.containsKey(a.storageKey()));
    }

    @Test
    @DisplayName("delete: someone else cannot remove the file, but an administrator can")
    void delete_byOtherUserIsRefusedUnlessAdmin() {
        Attachment a = upload(PDF, "a.pdf", "user-1");

        assertThrows(AttachmentAccessDeniedException.class, () -> service.delete(a.id(), "user-2", false));
        assertTrue(rows.containsKey(a.id()));

        service.delete(a.id(), "admin", true);
        assertFalse(rows.containsKey(a.id()));
    }

    @Test
    @DisplayName("list: only the document's own attachments are returned")
    void list_returnsOnlyTheDocumentsFiles() {
        Attachment a = upload(PDF, "a.pdf", "u");

        assertEquals(List.of(a), service.list(AttachmentOwnerType.PURCHASE_ORDER, orderId));
        assertTrue(service.list(AttachmentOwnerType.PURCHASE_ORDER, UUID.randomUUID()).isEmpty());
    }
}
