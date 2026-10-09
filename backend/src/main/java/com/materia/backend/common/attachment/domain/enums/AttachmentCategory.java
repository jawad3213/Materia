package com.materia.backend.common.attachment.domain.enums;

/** What an attached file is, so a reader finds the supplier's invoice scan without opening every file. */
public enum AttachmentCategory {
    SUPPLIER_INVOICE,
    DELIVERY_NOTE,
    QUALITY_PHOTO,
    OTHER
}
