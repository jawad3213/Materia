/**
 * Invoice types matching backend InvoiceWebResponse.java / CreateInvoiceWebRequest.java.
 */
export type InvoiceStatus = "DRAFT" | "SUBMITTED" | "VERIFIED" | "PAID" | "CANCELLED";

export type InvoiceType = "STANDARD" | "CREDIT_NOTE";

export const INVOICE_STATUS_LABELS: Record<InvoiceStatus, string> = {
  DRAFT: "Draft",
  SUBMITTED: "Submitted",
  VERIFIED: "Verified",
  PAID: "Paid",
  CANCELLED: "Cancelled",
};

export const INVOICE_TYPE_LABELS: Record<InvoiceType, string> = {
  STANDARD: "Invoice",
  CREDIT_NOTE: "Credit Note",
};

/** Invoices that can still be edited (backend InvoiceStatus.isModifiable). */
export const MODIFIABLE_INVOICE_STATUSES: InvoiceStatus[] = ["DRAFT", "SUBMITTED"];

/** Invoices that no longer change (backend InvoiceStatus.isClosed). */
export const CLOSED_INVOICE_STATUSES: InvoiceStatus[] = ["PAID", "CANCELLED"];

export interface InvoiceLine {
  id: string;
  lineNumber: number;
  purchaseOrderLineId?: string | null;
  goodsReceiptLineId?: string | null;
  materialCode?: string | null;
  materialName: string;
  unitOfMeasure?: string | null;
  quantityOrdered?: number | null;
  quantityReceived?: number | null;
  quantityInvoiced: number;
  quantityDiscrepancy?: number | null;
  unitPrice: number;
  lineTotal?: number | null;
  taxAmount?: number | null;
  lineTotalWithTax?: number | null;
  currencyCode?: string | null;
  hasQuantityDiscrepancy: boolean;
  discrepancyNotes?: string | null;
  /** Unit price of the matched purchase-order line. */
  orderUnitPrice?: number | null;
  /** Invoiced price against the order price, in percent (positive when billed higher). */
  priceVariancePercent?: number | null;
  /** The variance is beyond the tolerance: the invoice cannot be verified. */
  hasPriceDiscrepancy?: boolean;
  notes?: string | null;
}

export interface Invoice {
  id: string;
  invoiceCode: string;
  purchaseOrderId?: string | null;
  purchaseOrderCode?: string | null;
  goodsReceiptId?: string | null;
  goodsReceiptCode?: string | null;
  supplierId: string;
  supplierName: string;
  supplierCode?: string | null;
  invoiceType: InvoiceType;
  status: InvoiceStatus;
  externalReference?: string | null;
  invoiceDate: string;
  dueDate?: string | null;
  receivedDate?: string | null;
  paymentDate?: string | null;
  totalAmount?: number | null;
  totalTaxAmount?: number | null;
  totalAmountWithTax?: number | null;
  currencyCode: string;
  isVerified: boolean;
  hasDiscrepancy: boolean;
  discrepancySummary?: string | null;
  verificationDate?: string | null;
  verifiedBy?: string | null;
  verifiedByName?: string | null;
  paidAmount?: number | null;
  paidAt?: string | null;
  paidBy?: string | null;
  paidByName?: string | null;
  notes?: string | null;
  internalNotes?: string | null;
  lines: InvoiceLine[];
}

export interface InvoiceLineRequest {
  id?: string;
  lineNumber?: number;
  purchaseOrderLineId?: string;
  goodsReceiptLineId?: string;
  materialCode?: string;
  materialName: string;
  unitOfMeasure?: string;
  quantityInvoiced: number;
  unitPrice: number;
  taxAmount?: number;
  notes?: string;
}

export interface CreateInvoiceRequest {
  purchaseOrderId?: string;
  purchaseOrderCode?: string;
  goodsReceiptId?: string;
  goodsReceiptCode?: string;
  supplierId: string;
  supplierName: string;
  supplierCode?: string;
  invoiceType: InvoiceType;
  externalReference?: string;
  invoiceDate: string;
  dueDate?: string;
  currencyCode: string;
  notes?: string;
  internalNotes?: string;
  lines: InvoiceLineRequest[];
}
