package com.materia.backend.common.attachment.domain.enums;

/**
 * The documents a file can be attached to. Reading and adding attachments reuse the document's own permissions
 * from Role.java, so whoever may see or change the document may see or change its files.
 */
public enum AttachmentOwnerType {
    PURCHASE_ORDER("order:read", "order:write"),
    GOODS_RECEIPT("receipt:read", "receipt:write"),
    INVOICE("invoice:read", "invoice:write"),
    RETURN_TO_VENDOR("return:read", "return:write");

    private final String readPermission;
    private final String writePermission;

    AttachmentOwnerType(String readPermission, String writePermission) {
        this.readPermission = readPermission;
        this.writePermission = writePermission;
    }

    public String getReadPermission() { return readPermission; }
    public String getWritePermission() { return writePermission; }
}
