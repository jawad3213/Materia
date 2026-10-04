/**
 * Payment types matching backend PaymentWebResponse.java / CreatePaymentWebRequest.java.
 */
export type PaymentStatus = "DRAFT" | "PENDING" | "COMPLETED" | "CANCELLED";

export type PaymentMethod = "BANK_TRANSFER" | "CHECK" | "CASH" | "CARD";

export const PAYMENT_STATUS_LABELS: Record<PaymentStatus, string> = {
  DRAFT: "Draft",
  PENDING: "Prepared",
  COMPLETED: "Paid",
  CANCELLED: "Cancelled",
};

export const PAYMENT_METHOD_LABELS: Record<PaymentMethod, string> = {
  BANK_TRANSFER: "Bank Transfer",
  CHECK: "Cheque",
  CASH: "Cash",
  CARD: "Card",
};

/** Methods for which the backend requires a bank reference (Payment.markAsCompleted). */
export const METHODS_NEEDING_REFERENCE: PaymentMethod[] = ["BANK_TRANSFER", "CHECK"];

/** Payments that still set money aside on their invoices (backend: draft or prepared). */
export const OPEN_PAYMENT_STATUSES: PaymentStatus[] = ["DRAFT", "PENDING"];

export interface PaymentLine {
  id: string;
  lineNumber: number;
  invoiceId: string;
  invoiceCode?: string | null;
  supplierId?: string | null;
  supplierName?: string | null;
  amount: number;
  paidAmount?: number | null;
  currencyCode?: string | null;
  isPaid: boolean;
  notes?: string | null;
}

export interface Payment {
  id: string;
  paymentCode: string;
  supplierId: string;
  supplierName: string;
  supplierCode?: string | null;
  status: PaymentStatus;
  totalAmount: number;
  paidAmount?: number | null;
  currencyCode: string;
  paymentDate?: string | null;
  confirmedDate?: string | null;
  bankReference?: string | null;
  transactionId?: string | null;
  paymentMethod?: PaymentMethod | null;
  notes?: string | null;
  internalNotes?: string | null;
  createdAt?: string | null;
  createdBy?: string | null;
  updatedAt?: string | null;
  updatedBy?: string | null;
  lines: PaymentLine[];
}

/** The server derives the total, the supplier name and each invoice's code and supplier. */
export interface CreatePaymentRequest {
  supplierId: string;
  currencyCode: string;
  notes?: string;
  lines: { invoiceId: string; amount: number }[];
}

export interface CompletePaymentRequest {
  paymentMethod: PaymentMethod;
  bankReference?: string;
  transactionId?: string;
}
