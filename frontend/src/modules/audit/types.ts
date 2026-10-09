/** Kinds of records with a change history (backend AuditController). */
export type AuditEntityType =
  | "PurchaseOrder"
  | "Requisition"
  | "GoodsReceipt"
  | "Invoice"
  | "Payment"
  | "ReturnToVendor"
  | "Supplier"
  | "Material"
  | "Category"
  | "User"
  | "Employee"
  | "Setting";

export type AuditAction = "CREATE" | "UPDATE" | "STATUS_CHANGE" | "DELETE";

export interface FieldChange {
  from: unknown;
  to: unknown;
}

/** One recorded change: who made it, when, to which fields, and why. */
export interface AuditEntry {
  id: string;
  occurredAt: string;
  actorId: string;
  actorName?: string | null;
  entityType: string;
  entityId: string;
  parentType?: string | null;
  parentId?: string | null;
  action: AuditAction;
  summary?: string | null;
  reason?: string | null;
  changes: Record<string, FieldChange>;
}

/** The permission that reads each kind of record, and therefore its history. */
export const AUDIT_READ_PERMISSIONS: Record<AuditEntityType, string> = {
  PurchaseOrder: "order:read",
  Requisition: "requisition:read",
  GoodsReceipt: "receipt:read",
  Invoice: "invoice:read",
  Payment: "payment:read",
  ReturnToVendor: "return:read",
  Supplier: "supplier:read",
  Material: "material:read",
  Category: "category:read",
  User: "user:read",
  Employee: "user:read",
  Setting: "setting:read",
};

/** How each kind of record is named in the change feed. */
export const ENTITY_LABELS: Record<string, string> = {
  PurchaseOrder: "Purchase order",
  Requisition: "Requisition",
  GoodsReceipt: "Goods receipt",
  Invoice: "Invoice",
  Payment: "Payment",
  ReturnToVendor: "Vendor return",
  Supplier: "Supplier",
  Material: "Material",
  Category: "Category",
  User: "User account",
  Employee: "Employee",
  Setting: "Setting",
  Attachment: "Attachment",
};

/** How the lines of a document are named in its history. */
export const LINE_LABELS: Record<string, string> = {
  PurchaseOrderLine: "Order line",
  RequisitionLine: "Requisition line",
  GoodsReceiptLine: "Receipt line",
  InvoiceLine: "Invoice line",
  PaymentLine: "Payment line",
  ReturnToVendorLine: "Return line",
  Attachment: "Attachment",
};
