/**
 * Goods receipt types matching backend GoodsReceiptWebResponse.java / CreateGoodsReceiptWebRequest.java.
 */
export type ReceiptStatus = "DRAFT" | "IN_PROGRESS" | "COMPLETED" | "PARTIAL" | "CANCELLED";

export type QualityStatus = "ACCEPTED" | "REJECTED" | "UNDER_REVIEW" | "PARTIAL";

export const RECEIPT_STATUS_LABELS: Record<ReceiptStatus, string> = {
  DRAFT: "Draft",
  IN_PROGRESS: "In progress",
  COMPLETED: "Completed",
  PARTIAL: "With discrepancies",
  CANCELLED: "Cancelled",
};

export const QUALITY_STATUS_LABELS: Record<QualityStatus, string> = {
  ACCEPTED: "Accepted",
  REJECTED: "Rejected",
  UNDER_REVIEW: "Under review",
  PARTIAL: "Partly accepted",
};

/** Receipts that can still be edited, validated or cancelled (backend ReceiptStatus.isModifiable). */
export const OPEN_RECEIPT_STATUSES: ReceiptStatus[] = ["DRAFT", "IN_PROGRESS"];

export interface GoodsReceiptLine {
  id: string;
  lineNumber: number;
  purchaseOrderLineId?: string | null;
  materialCode: string;
  materialId?: string | null;
  materialName?: string | null;
  unitOfMeasure?: string | null;
  quantityOrdered?: number | null;
  quantityReceived?: number | null;
  quantityRejected?: number | null;
  quantityAccepted?: number | null;
  quantityPending?: number | null;
  qualityStatus?: QualityStatus | null;
  qualityNotes?: string | null;
  rejectionReason?: string | null;
  stockBefore?: number | null;
  stockAfter?: number | null;
  batchNumber?: string | null;
  expiryDate?: string | null;
  storageLocation?: string | null;
  notes?: string | null;
}

export interface GoodsReceipt {
  id: string;
  receiptCode: string;
  purchaseOrderId: string;
  purchaseOrderCode?: string | null;
  status: ReceiptStatus;
  receiptDate?: string | null;
  expectedDeliveryDate?: string | null;
  receivedBy: string;
  receivedByName?: string | null;
  notes?: string | null;
  supplierId?: string | null;
  supplierName?: string | null;
  totalQuantityOrdered?: number | null;
  totalQuantityReceived?: number | null;
  totalQuantityRejected?: number | null;
  totalQuantityAccepted?: number | null;
  hasDiscrepancy: boolean;
  discrepancyNotes?: string | null;
  createdBy?: string | null;
  createdAt?: string | null;
  updatedBy?: string | null;
  updatedAt?: string | null;
  lines: GoodsReceiptLine[];
}

export interface GoodsReceiptLineRequest {
  purchaseOrderLineId: string;
  materialCode: string;
  quantityReceived: number;
  quantityRejected: number;
  qualityStatus: QualityStatus;
  qualityNotes?: string;
  rejectionReason?: string;
  batchNumber?: string;
  expiryDate?: string;
  storageLocation?: string;
  notes?: string;
}

/** The server fills the receiver, supplier and line details from the purchase order. */
export interface CreateGoodsReceiptRequest {
  purchaseOrderId: string;
  notes?: string;
  discrepancyNotes?: string;
  lines: GoodsReceiptLineRequest[];
}
