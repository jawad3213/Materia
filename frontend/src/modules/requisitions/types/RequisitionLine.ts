/**
 * Line item input for requisition creation/updates.
 */
export interface RequisitionLineRequest {
  id?: string;
  materialId?: string;
  materialCode?: string;
  quantity: number;
  requiredDate?: string;
  supplierId?: string;
  supplierCode?: string;
  notes?: string;
  deliveryTerms?: string;
  storageLocation?: string;
  batchNumber?: string;
  expiryDate?: string;
}

// Backward-compatible alias
export type RequisitionLineWebRequest = RequisitionLineRequest;

/**
 * Line item response from the API.
 */
export interface RequisitionLine {
  id: string;
  lineNumber: number;
  materialId?: string;
  materialCode: string;
  materialName: string;
  materialDescription?: string;
  unitOfMeasure: string;
  standardPrice?: string;
  unitPrice: string;
  currencyCode: string;
  quantity: number;
  quantityReceived?: number;
  quantityRejected?: number;
  requiredDate: string;
  lineTotal: string;
  currencyCodeLine?: string;
  supplierId?: string;
  supplierName?: string;
  supplierCode?: string;
  notes?: string;
  deliveryTerms?: string;
  storageLocation?: string;
  batchNumber?: string;
  expiryDate?: string;
}

// Backward-compatible alias
export type RequisitionLineWebResponse = RequisitionLine;
