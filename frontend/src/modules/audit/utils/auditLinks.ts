import type { AuditEntry } from "../types";

/** Detail page of each kind of record, by its id. User accounts have no page of their own (staff pages use employees). */
const ROUTES: Record<string, (id: string) => string> = {
  PurchaseOrder: (id) => `/purchase-orders/view/${id}`,
  Requisition: (id) => `/requisitions/view/${id}`,
  GoodsReceipt: (id) => `/goods-receipts/${id}`,
  Invoice: (id) => `/invoices/${id}`,
  Payment: (id) => `/payments/${id}`,
  ReturnToVendor: (id) => `/returns/${id}`,
  Supplier: (id) => `/suppliers/view/${id}`,
  Material: (id) => `/materials/view/${id}`,
  Category: (id) => `/categories/view/${id}`,
  Employee: (id) => `/users/view/${id}`,
  Setting: () => "/settings",
};

/** Where to open the record a change belongs to: its own page, or its document's page for a line or attachment. */
export function recordLink(entry: Pick<AuditEntry, "entityType" | "entityId" | "parentType" | "parentId">): string | null {
  const own = ROUTES[entry.entityType];
  if (own) return own(entry.entityId);
  const parent = entry.parentType ? ROUTES[entry.parentType] : undefined;
  return parent && entry.parentId ? parent(entry.parentId) : null;
}
