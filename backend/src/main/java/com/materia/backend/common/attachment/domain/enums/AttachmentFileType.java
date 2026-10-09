package com.materia.backend.common.attachment.domain.enums;

import java.util.Optional;

/**
 * The file types accepted as attachments: scans and photos. The type is read from the file's first bytes, never
 * from its name or the browser's claim, so a renamed script or HTML page is refused.
 */
public enum AttachmentFileType {
    PDF("application/pdf", "pdf"),
    PNG("image/png", "png"),
    JPEG("image/jpeg", "jpg"),
    WEBP("image/webp", "webp");

    private final String contentType;
    private final String extension;

    AttachmentFileType(String contentType, String extension) {
        this.contentType = contentType;
        this.extension = extension;
    }

    public String getContentType() { return contentType; }
    public String getExtension() { return extension; }

    public static Optional<AttachmentFileType> detect(byte[] content) {
        if (content == null) return Optional.empty();
        if (startsWith(content, 0, 0x25, 0x50, 0x44, 0x46, 0x2D)) return Optional.of(PDF); // %PDF-
        if (startsWith(content, 0, 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A)) return Optional.of(PNG);
        if (startsWith(content, 0, 0xFF, 0xD8, 0xFF)) return Optional.of(JPEG);
        if (startsWith(content, 0, 0x52, 0x49, 0x46, 0x46) && startsWith(content, 8, 0x57, 0x45, 0x42, 0x50)) {
            return Optional.of(WEBP); // RIFF....WEBP
        }
        return Optional.empty();
    }

    private static boolean startsWith(byte[] content, int offset, int... signature) {
        if (content.length < offset + signature.length) return false;
        for (int i = 0; i < signature.length; i++) {
            if ((content[offset + i] & 0xFF) != signature[i]) return false;
        }
        return true;
    }
}
