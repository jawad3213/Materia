/**
 * Line item input for creating or updating a purchase order.
 * Matches backend PurchaseOrderLineWebRequest.java.
 */
export interface PurchaseOrderLineRequest {
  id?: string;
  lineNumber?: number;
  requisitionLineId?: string;
  materialCode: string;
  materialId?: string;
  materialName?: string;
  materialDescription?: string;
  unitOfMeasure?: string;
  quantity: number;
  unitPrice: number | string;
  /** VAT rate in percent; omitted, the backend applies its default rate. */
  taxRate?: number;
  currencyCode?: string;
  supplierId?: string;
  supplierName?: string;
  expectedDeliveryDate?: string; // Format: YYYY-MM-DD
  notes?: string;
}

// Backward-compatible backend alias
export type PurchaseOrderLineWebRequest = PurchaseOrderLineRequest;

/**
 * Purchase order line response model.
 * Matches backend PurchaseOrderLineWebResponse.java.
 */
export interface PurchaseOrderLine {
  id: string;
  lineNumber: number;
  requisitionLineId?: string | null;
  materialCode: string;
  materialId?: string | null;
  materialName?: string | null;
  materialDescription?: string | null;
  unitOfMeasure?: string | null;
  quantity: number;
  unitPrice: string | number;
  lineTotal: string | number;
  /** VAT rate in percent; null on orders created before VAT per line. */
  taxRate?: number | null;
  taxAmount?: string | number | null;
  currencyCode: string;
  supplierId?: string | null;
  supplierName?: string | null;
  expectedDeliveryDate?: string | null; // Format: YYYY-MM-DD
  notes?: string | null;
}

// Backward-compatible backend alias
export type PurchaseOrderLineWebResponse = PurchaseOrderLine;
