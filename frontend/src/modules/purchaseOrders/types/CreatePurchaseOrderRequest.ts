import type { PurchaseOrderLineRequest } from './PurchaseOrderLine';

/**
 * Payload for creating a purchase order.
 * Matches backend CreatePurchaseOrderWebRequest.java.
 */
export interface CreatePurchaseOrderRequest {
  requisitionId?: string;
  requisitionCode?: string;
  supplierId: string;
  supplierName: string;
  supplierCode?: string;
  orderDate?: string; // Format: YYYY-MM-DD
  expectedDeliveryDate?: string; // Format: YYYY-MM-DD
  paymentTerms?: string;
  paymentDelayDays?: number;
  deliveryTerms?: string;
  incoterm?: string;
  currencyCode: string;
  taxAmount?: number | string;
  shippingCost?: number | string;
  orderedBy: string;
  orderedByName?: string;
  approvedBy?: string;
  approvedByName?: string;
  notes?: string;
  internalNotes?: string;
  lines: PurchaseOrderLineRequest[];
  createdBy: string;
}

// Backward-compatible backend alias
export type CreatePurchaseOrderWebRequest = CreatePurchaseOrderRequest;
