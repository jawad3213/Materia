import type { PurchaseOrder } from "../../purchaseOrders/types";
import type { GoodsReceipt } from "../../goodsReceipts/types/goodsReceipt.types";
import type { Invoice } from "../types/invoice.types";
import { parseAmount } from "../../../shared/utils/moneyUtils";

/** One editable invoice line, pre-filled from the purchase order and its receipts. */
export interface InvoiceLineDraft {
  purchaseOrderLineId: string;
  materialCode: string;
  materialName: string;
  unitOfMeasure: string;
  ordered: number;
  /** Accepted on validated receipts (completed or with discrepancies). */
  received: number;
  /** Already billed on other invoices that are not cancelled; credit notes reduce it. */
  alreadyInvoiced: number;
  orderPrice: number;
  invoiced: number;
  unitPrice: number;
  taxAmount: number;
}

const VALIDATED_RECEIPT_STATUSES = ["COMPLETED", "PARTIAL"];

/** Purchase orders that have received goods and so can be invoiced. */
export const INVOICEABLE_ORDER_STATUSES = ["PARTIALLY_RECEIVED", "RECEIVED", "COMPLETED"];

function acceptedOn(receipts: GoodsReceipt[], purchaseOrderLineId: string): number {
  return receipts
    .filter((r) => VALIDATED_RECEIPT_STATUSES.includes(r.status))
    .flatMap((r) => r.lines)
    .filter((l) => l.purchaseOrderLineId === purchaseOrderLineId)
    .reduce((sum, l) => sum + (l.quantityAccepted ?? (l.quantityReceived ?? 0) - (l.quantityRejected ?? 0)), 0);
}

function invoicedOn(invoices: Invoice[], purchaseOrderLineId: string): number {
  return invoices
    .filter((inv) => inv.status !== "CANCELLED")
    .reduce((sum, inv) => {
      const sign = inv.invoiceType === "CREDIT_NOTE" ? -1 : 1;
      const qty = inv.lines
        .filter((l) => l.purchaseOrderLineId === purchaseOrderLineId)
        .reduce((s, l) => s + (l.quantityInvoiced ?? 0), 0);
      return sum + sign * qty;
    }, 0);
}

/**
 * Builds one draft per order line. The invoiced quantity starts at what was accepted but not yet
 * billed, and the unit price at the order price, so a matching invoice needs no typing.
 */
export function toInvoiceDrafts(order: PurchaseOrder, receipts: GoodsReceipt[], invoices: Invoice[]): InvoiceLineDraft[] {
  return order.lines.map((line) => {
    const received = acceptedOn(receipts, line.id);
    const alreadyInvoiced = invoicedOn(invoices, line.id);
    const orderPrice = parseAmount(line.unitPrice);
    return {
      purchaseOrderLineId: line.id,
      materialCode: line.materialCode,
      materialName: line.materialName || line.materialCode,
      unitOfMeasure: line.unitOfMeasure || "",
      ordered: line.quantity,
      received,
      alreadyInvoiced,
      orderPrice,
      invoiced: Math.max(0, received - alreadyInvoiced),
      unitPrice: orderPrice,
      taxAmount: 0,
    };
  });
}

const round2 = (value: number) => Math.round(value * 100) / 100;

export function lineTotal(line: Pick<InvoiceLineDraft, "invoiced" | "unitPrice">): number {
  return round2(line.invoiced * line.unitPrice);
}

export function invoiceTotals(lines: InvoiceLineDraft[]) {
  const totalAmount = round2(lines.reduce((sum, l) => sum + lineTotal(l), 0));
  const totalTaxAmount = round2(lines.reduce((sum, l) => sum + l.taxAmount, 0));
  return { totalAmount, totalTaxAmount, totalAmountWithTax: round2(totalAmount + totalTaxAmount) };
}

/** A value that blocks saving the line. */
export function lineError(line: InvoiceLineDraft): string | null {
  if (!Number.isFinite(line.invoiced) || line.invoiced < 0) return "The invoiced quantity cannot be negative.";
  if (!Number.isInteger(line.invoiced)) return "The invoiced quantity must be a whole number.";
  if (!Number.isFinite(line.unitPrice) || line.unitPrice < 0) return "The unit price cannot be negative.";
  if (!Number.isFinite(line.taxAmount) || line.taxAmount < 0) return "The tax amount cannot be negative.";
  return null;
}

/**
 * Three-way match warnings: the invoice may still be saved, but the verifier will see why it
 * does not match the order and the receipts.
 */
export function lineWarnings(line: InvoiceLineDraft): string[] {
  const warnings: string[] = [];
  const billable = Math.max(0, line.received - line.alreadyInvoiced);
  if (line.invoiced > billable) {
    warnings.push(
      `Invoiced quantity (${line.invoiced}) is more than received and not yet invoiced (${billable}).`
    );
  }
  if (line.invoiced > 0 && round2(line.unitPrice) !== round2(line.orderPrice)) {
    warnings.push(`Unit price differs from the purchase order (${line.orderPrice}).`);
  }
  return warnings;
}

/** The due date implied by the order's payment delay, as YYYY-MM-DD. */
export function defaultDueDate(invoiceDate: string, paymentDelayDays?: number | null): string {
  if (!invoiceDate) return "";
  const date = new Date(`${invoiceDate}T00:00:00Z`);
  date.setUTCDate(date.getUTCDate() + (paymentDelayDays ?? 30));
  return date.toISOString().slice(0, 10);
}

export function formatAmount(value: number | string | null | undefined, currency?: string | null): string {
  const amount = parseAmount(value ?? 0);
  const formatted = amount.toLocaleString("en-US", { minimumFractionDigits: 2, maximumFractionDigits: 2 });
  return currency ? `${formatted} ${currency}` : formatted;
}

/** What remains to be paid: total with tax minus payments recorded, never below zero (backend getOutstandingAmount). */
export function outstandingAmount(invoice: Pick<Invoice, "totalAmountWithTax" | "paidAmount">): number {
  return Math.max(0, round2(parseAmount(invoice.totalAmountWithTax ?? 0) - parseAmount(invoice.paidAmount ?? 0)));
}

/** A verified invoice that has received some, but not all, of its payment (backend isPartiallyPaid). */
export function isPartiallyPaid(invoice: Pick<Invoice, "status" | "paidAmount">): boolean {
  return invoice.status === "VERIFIED" && parseAmount(invoice.paidAmount ?? 0) > 0;
}

/** An invoice is overdue once its due date has passed and it is not yet paid or cancelled. */
export function isOverdue(invoice: Pick<Invoice, "dueDate" | "status">, today = new Date().toISOString().slice(0, 10)): boolean {
  return !!invoice.dueDate && invoice.dueDate < today && invoice.status !== "PAID" && invoice.status !== "CANCELLED";
}
