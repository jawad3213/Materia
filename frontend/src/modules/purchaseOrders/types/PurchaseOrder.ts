import type { OrderStatus } from './OrderStatus';
import type { DeliveryStatus } from './DeliveryStatus';
import type { PurchaseOrderLine } from './PurchaseOrderLine';

/**
 * Purchase order response model.
 * Matches backend PurchaseOrderWebResponse.java.
 */
export interface PurchaseOrder {
  id: string;
  orderCode: string;
  requisitionId?: string | null;
  requisitionCode?: string | null;
  status: OrderStatus;
  deliveryStatus: DeliveryStatus;
  supplierId: string;
  supplierName: string;
  supplierCode?: string | null;
  orderDate: string; // Format: YYYY-MM-DD
  expectedDeliveryDate?: string | null; // Format: YYYY-MM-DD
  confirmedDeliveryDate?: string | null; // Format: YYYY-MM-DD
  receivedDate?: string | null; // Format: YYYY-MM-DD
  paymentTerms?: string | null;
  paymentDelayDays?: number | null;
  deliveryTerms?: string | null;
  incoterm?: string | null;
  currencyCode: string;
  totalAmount: string | number;
  taxAmount?: string | number | null;
  shippingCost?: string | number | null;
  grandTotal: string | number;
  orderedBy: string;
  orderedByName?: string | null;
  approvedBy?: string | null;
  approvedByName?: string | null;
  assignedTo?: string | null;
  assignedToName?: string | null;
  assignedAt?: string | null; // ISO DateTime
  assignedBy?: string | null;
  assignedByName?: string | null;
  notes?: string | null;
  internalNotes?: string | null;
  createdBy: string;
  createdAt: string; // ISO DateTime
  updatedBy?: string | null;
  updatedAt?: string | null; // ISO DateTime
  lines: PurchaseOrderLine[];
}

// Backward-compatible backend alias
export type PurchaseOrderWebResponse = PurchaseOrder;
