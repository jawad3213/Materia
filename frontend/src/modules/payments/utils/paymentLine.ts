import type { Invoice } from "../../invoices/types/invoice.types";
import { outstandingAmount } from "../../invoices/utils/invoiceLine";
import { parseAmount } from "../../../shared/utils/moneyUtils";
import { METHODS_NEEDING_REFERENCE, OPEN_PAYMENT_STATUSES, type Payment, type PaymentMethod } from "../types/payment.types";

/** One invoice the payment may cover, with what can still be paid on it. */
export interface PayableInvoice {
  invoice: Invoice;
  /** Total with tax minus what was paid. */
  outstanding: number;
  /** Set aside by the supplier's other open (draft or prepared) payments. */
  reserved: number;
  /** What this payment may pay: outstanding minus reserved, never below zero. */
  available: number;
}

/** A row of the payment form: the invoice, whether it is selected, and the amount to pay. */
export interface PaymentLineDraft extends PayableInvoice {
  selected: boolean;
  amount: number;
}

const round2 = (value: number) => Math.round(value * 100) / 100;

/** Amount per invoice set aside by open payments, other than the one being edited. */
export function reservedByOpenPayments(payments: Payment[], excludePaymentId?: string): Map<string, number> {
  const reserved = new Map<string, number>();
  for (const payment of payments) {
    if (payment.id === excludePaymentId || !OPEN_PAYMENT_STATUSES.includes(payment.status)) continue;
    for (const line of payment.lines) {
      reserved.set(line.invoiceId, round2((reserved.get(line.invoiceId) ?? 0) + parseAmount(line.amount)));
    }
  }
  return reserved;
}

/**
 * The supplier's invoices this payment can cover, mirroring the backend checks: verified standard
 * invoices (no credit notes) in the payment currency, with something left once open payments are counted.
 * Sorted by due date, oldest first, so overdue invoices come first.
 */
export function payableInvoices(invoices: Invoice[], openPayments: Payment[], supplierId: string): PayableInvoice[] {
  const reserved = reservedByOpenPayments(openPayments);
  return invoices
    .filter((inv) => inv.supplierId === supplierId && inv.status === "VERIFIED" && inv.invoiceType === "STANDARD")
    .map((invoice) => {
      const outstanding = outstandingAmount(invoice);
      const held = reserved.get(invoice.id) ?? 0;
      return { invoice, outstanding, reserved: held, available: Math.max(0, round2(outstanding - held)) };
    })
    .filter((p) => p.available > 0)
    .sort((a, b) => String(a.invoice.dueDate ?? "9999").localeCompare(String(b.invoice.dueDate ?? "9999")));
}

/** Form rows; the invoice given in `preselectId` (coming from an invoice page) starts selected for its full amount. */
export function toPaymentDrafts(payable: PayableInvoice[], preselectId?: string | null): PaymentLineDraft[] {
  return payable.map((p) => ({ ...p, selected: p.invoice.id === preselectId, amount: p.available }));
}

export function paymentTotal(lines: PaymentLineDraft[]): number {
  return round2(lines.filter((l) => l.selected).reduce((sum, l) => sum + l.amount, 0));
}

/** A value that blocks saving the row. */
export function lineError(line: PaymentLineDraft): string | null {
  if (!line.selected) return null;
  if (!Number.isFinite(line.amount) || line.amount <= 0) return "The amount must be positive.";
  if (round2(line.amount) > line.available) return `The amount exceeds what is left to pay (${line.available}).`;
  return null;
}

/** What paying this row does to the invoice: settled, or the balance that remains. */
export function lineOutcome(line: PaymentLineDraft): { settles: boolean; remaining: number } {
  const remaining = Math.max(0, round2(line.outstanding - line.amount));
  return { settles: remaining === 0, remaining };
}

/** The completion form needs a bank reference for transfers and cheques (backend Payment.markAsCompleted). */
export function completionError(method: PaymentMethod | "", bankReference: string): string | null {
  if (!method) return "Choose a payment method.";
  if (METHODS_NEEDING_REFERENCE.includes(method) && !bankReference.trim()) {
    return "A bank reference is required for a bank transfer or a cheque.";
  }
  return null;
}

/** Suppliers that have at least one invoice waiting for payment, sorted by name. */
export function suppliersWithVerifiedInvoices(invoices: Invoice[]): { id: string; name: string }[] {
  const byId = new Map<string, string>();
  for (const inv of invoices) {
    if (inv.status === "VERIFIED" && inv.invoiceType === "STANDARD" && outstandingAmount(inv) > 0) {
      byId.set(inv.supplierId, inv.supplierName);
    }
  }
  return [...byId.entries()].map(([id, name]) => ({ id, name })).sort((a, b) => a.name.localeCompare(b.name));
}
