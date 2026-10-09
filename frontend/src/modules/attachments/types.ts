/** Matches backend AttachmentOwnerType.java. */
export type AttachmentOwnerType = "PURCHASE_ORDER" | "GOODS_RECEIPT" | "INVOICE" | "RETURN_TO_VENDOR";

/** Matches backend AttachmentCategory.java. */
export type AttachmentCategory = "SUPPLIER_INVOICE" | "DELIVERY_NOTE" | "QUALITY_PHOTO" | "OTHER";

/** Matches backend AttachmentWebResponse.java. */
export interface Attachment {
  id: string;
  ownerType: AttachmentOwnerType;
  ownerId: string;
  category: AttachmentCategory;
  fileName: string;
  contentType: string;
  sizeBytes: number;
  uploadedBy?: string | null;
  uploadedByName?: string | null;
  uploadedAt: string; // ISO DateTime
}

export const ATTACHMENT_CATEGORY_LABELS: Record<AttachmentCategory, string> = {
  SUPPLIER_INVOICE: "Supplier invoice",
  DELIVERY_NOTE: "Delivery note",
  QUALITY_PHOTO: "Quality photo",
  OTHER: "Other",
};

/** The document permissions the backend checks for each owner type (AttachmentOwnerType.java). */
export const ATTACHMENT_PERMISSIONS: Record<AttachmentOwnerType, { read: string; write: string }> = {
  PURCHASE_ORDER: { read: "order:read", write: "order:write" },
  GOODS_RECEIPT: { read: "receipt:read", write: "receipt:write" },
  INVOICE: { read: "invoice:read", write: "invoice:write" },
  RETURN_TO_VENDOR: { read: "return:read", write: "return:write" },
};

/** Same limits as the backend (app.attachments.max-file-size-bytes, AttachmentFileType.java). */
export const MAX_ATTACHMENT_BYTES = 10 * 1024 * 1024;
export const ACCEPTED_ATTACHMENT_TYPES: Record<string, string[]> = {
  "application/pdf": [".pdf"],
  "image/png": [".png"],
  "image/jpeg": [".jpg", ".jpeg"],
  "image/webp": [".webp"],
};
