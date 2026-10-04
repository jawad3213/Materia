/**
 * Return-to-vendor types matching backend ReturnToVendorWebResponse.java / CreateReturnToVendorWebRequest.java.
 */
export type ReturnStatus = "DRAFT" | "PENDING" | "RESOLVED" | "CANCELLED";

export type ResolutionType = "REPLACEMENT" | "CREDIT_NOTE";

export const RETURN_STATUS_LABELS: Record<ReturnStatus, string> = {
  DRAFT: "Draft",
  PENDING: "Shipped",
  RESOLVED: "Resolved",
  CANCELLED: "Cancelled",
};

export const RESOLUTION_TYPE_LABELS: Record<ResolutionType, string> = {
  REPLACEMENT: "Replacement",
  CREDIT_NOTE: "Credit Note",
};

/** Returns that still hold their quantities on the goods receipt (backend: everything but cancelled). */
export const HOLDING_RETURN_STATUSES: ReturnStatus[] = ["DRAFT", "PENDING", "RESOLVED"];

/** Goods receipts whose rejected goods can be returned (backend RETURNABLE_RECEIPTS). */
export const RETURNABLE_RECEIPT_STATUSES = ["COMPLETED", "PARTIAL"];

export interface ReturnToVendorLine {
  id: string;
  lineNumber: number;
  goodsReceiptLineId: string;
  purchaseOrderLineId?: string | null;
  materialId?: string | null;
  materialCode: string;
  materialName: string;
  unitOfMeasure?: string | null;
  rejectedQuantity?: number | null;
  quantityToReturn: number;
  quantityAlreadyReturned?: number | null;
  remainingQuantity?: number | null;
  unitPrice?: number | null;
  lineValue?: number | null;
  rejectionReason: string;
  qualityNotes?: string | null;
  defectDescription?: string | null;
  replaced: boolean;
  creditNote: boolean;
  notes?: string | null;
}

export interface ReturnToVendor {
  id: string;
  returnCode: string;
  goodsReceiptId: string;
  goodsReceiptCode?: string | null;
  purchaseOrderId?: string | null;
  purchaseOrderCode?: string | null;
  supplierId: string;
  supplierName: string;
  supplierCode?: string | null;
  currencyCode?: string | null;
  totalValue?: number | null;
  totalQuantity?: number | null;
  status: ReturnStatus;
  resolutionType?: ResolutionType | null;
  returnDate?: string | null;
  resolutionDate?: string | null;
  returnReason: string;
  supplierResponse?: string | null;
  rejectionSummary?: string | null;
  creditNoteReference?: string | null;
  creditNoteAmount?: string | null;
  replacementReference?: string | null;
  notes?: string | null;
  internalNotes?: string | null;
  createdBy?: string | null;
  createdAt?: string | null;
  updatedBy?: string | null;
  updatedAt?: string | null;
  lines: ReturnToVendorLine[];
}

/** The server takes supplier, order, materials and prices from the goods receipt. */
export interface CreateReturnRequest {
  goodsReceiptId: string;
  returnDate?: string;
  returnReason: string;
  notes?: string;
  lines: {
    goodsReceiptLineId: string;
    quantityToReturn: number;
    rejectionReason?: string;
    defectDescription?: string;
  }[];
}

export interface ResolveReturnRequest {
  resolutionType: ResolutionType;
  reference: string;
  supplierResponse?: string;
}
