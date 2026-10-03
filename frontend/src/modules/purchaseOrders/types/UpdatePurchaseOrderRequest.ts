import type { PurchaseOrderLineRequest } from './PurchaseOrderLine';

/**
 * Payload for updating an existing purchase order.
 * Matches backend UpdatePurchaseOrderWebRequest.java.
 */
export interface UpdatePurchaseOrderRequest {
  requisitionId?: string;
  requisitionCode?: string;
  supplierId?: string;
  supplierName?: string;
  supplierCode?: string;
  orderDate?: string; // Format: YYYY-MM-DD
  expectedDeliveryDate?: string; // Format: YYYY-MM-DD
  paymentTerms?: string;
  paymentDelayDays?: number;
  deliveryTerms?: string;
  incoterm?: string;
  currencyCode?: string;
  taxAmount?: number | string;
  shippingCost?: number | string;
  orderedBy?: string;
  orderedByName?: string;
  approvedBy?: string;
  approvedByName?: string;
  notes?: string;
  internalNotes?: string;
  lines?: PurchaseOrderLineRequest[];
  updatedBy: string;
}

// Backward-compatible backend alias
export type UpdatePurchaseOrderWebRequest = UpdatePurchaseOrderRequest;
