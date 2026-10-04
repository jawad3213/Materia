import { describe, expect, it } from "vitest";
import {
  completionError,
  lineError,
  lineOutcome,
  payableInvoices,
  paymentTotal,
  reservedByOpenPayments,
  suppliersWithVerifiedInvoices,
  toPaymentDrafts,
} from "../paymentLine";
import type { Invoice } from "../../../invoices/types/invoice.types";
import type { Payment, PaymentStatus } from "../../types/payment.types";

function invoice(id: string, overrides: Partial<Invoice> = {}): Invoice {
  return {
    id,
    invoiceCode: `INV-${id}`,
    supplierId: "sup-1",
    supplierName: "Acme",
    invoiceType: "STANDARD",
    status: "VERIFIED",
    invoiceDate: "2026-10-01",
    dueDate: "2026-10-31",
    currencyCode: "MAD",
    totalAmountWithTax: 100,
    paidAmount: null,
    isVerified: true,
    hasDiscrepancy: false,
    lines: [],
    ...overrides,
  } as Invoice;
}

function payment(id: string, status: PaymentStatus, lines: Array<[string, number]>): Payment {
  return {
    id,
    paymentCode: `PAY-${id}`,
    supplierId: "sup-1",
    supplierName: "Acme",
    status,
    totalAmount: lines.reduce((s, [, a]) => s + a, 0),
    currencyCode: "MAD",
    lines: lines.map(([invoiceId, amount], i) => ({ id: `${id}-${i}`, lineNumber: i + 1, invoiceId, amount, isPaid: false })),
  };
}

describe("reservedByOpenPayments", () => {
  it("rule: draft and prepared payments set their amounts aside; completed and cancelled ones do not", () => {
    const reserved = reservedByOpenPayments([
      payment("p1", "DRAFT", [["a", 10], ["b", 5]]),
      payment("p2", "PENDING", [["a", 2.5]]),
      payment("p3", "COMPLETED", [["a", 40]]),
      payment("p4", "CANCELLED", [["a", 40]]),
    ]);

    expect(reserved.get("a")).toBe(12.5);
    expect(reserved.get("b")).toBe(5);
  });

  it("rule: the payment being edited does not reserve against itself", () => {
    expect(reservedByOpenPayments([payment("p1", "DRAFT", [["a", 10]])], "p1").size).toBe(0);
  });
});

describe("payableInvoices", () => {
  it("rule: only the supplier's verified standard invoices with something left are payable, oldest due first", () => {
    const invoices = [
      invoice("late", { dueDate: "2026-09-01" }),
      invoice("partly", { paidAmount: 30, dueDate: "2026-12-01" }),
      invoice("settled", { paidAmount: 100 }),
      invoice("submitted", { status: "SUBMITTED" }),
      invoice("credit", { invoiceType: "CREDIT_NOTE" }),
      invoice("other", { supplierId: "sup-2" }),
      invoice("reserved"),
    ];

    const payable = payableInvoices(invoices, [payment("p1", "PENDING", [["reserved", 100], ["partly", 20]])], "sup-1");

    expect(payable.map((p) => p.invoice.id)).toEqual(["late", "partly"]);
    expect(payable[1]).toMatchObject({ outstanding: 70, reserved: 20, available: 50 });
  });
});

describe("drafts and totals", () => {
  const payable = payableInvoices([invoice("a"), invoice("b", { paidAmount: 60 })], [], "sup-1");

  it("rule: the invoice the user came from starts selected for its full available amount", () => {
    const drafts = toPaymentDrafts(payable, "b");

    expect(drafts.find((d) => d.invoice.id === "b")).toMatchObject({ selected: true, amount: 40 });
    expect(drafts.find((d) => d.invoice.id === "a")).toMatchObject({ selected: false, amount: 100 });
  });

  it("rule: the total adds only the selected rows, to the cent", () => {
    const drafts = toPaymentDrafts(payable, null).map((d) => ({ ...d, selected: true, amount: d.invoice.id === "a" ? 0.1 : 0.2 }));
    expect(paymentTotal(drafts)).toBe(0.3);
    expect(paymentTotal(toPaymentDrafts(payable, null))).toBe(0);
  });

  it("rule: a selected row needs a positive amount within what is available; unselected rows are never in error", () => {
    const [row] = toPaymentDrafts(payable, "a").filter((d) => d.invoice.id === "a");

    expect(lineError(row)).toBeNull();
    expect(lineError({ ...row, amount: 0 })).toMatch(/positive/);
    expect(lineError({ ...row, amount: Number.NaN })).toMatch(/positive/);
    expect(lineError({ ...row, amount: 100.01 })).toMatch(/exceeds/);
    expect(lineError({ ...row, selected: false, amount: -5 })).toBeNull();
  });

  it("rule: paying the whole outstanding settles the invoice, otherwise the remainder is shown", () => {
    const [row] = toPaymentDrafts(payable, "a").filter((d) => d.invoice.id === "a");

    expect(lineOutcome(row)).toEqual({ settles: true, remaining: 0 });
    expect(lineOutcome({ ...row, amount: 25 })).toEqual({ settles: false, remaining: 75 });
  });
});

describe("completion and suppliers", () => {
  it("rule: a method is required; transfers and cheques also need a bank reference", () => {
    expect(completionError("", "")).toMatch(/method/);
    expect(completionError("BANK_TRANSFER", " ")).toMatch(/reference/);
    expect(completionError("CHECK", "")).toMatch(/reference/);
    expect(completionError("CHECK", "CHQ-1")).toBeNull();
    expect(completionError("CASH", "")).toBeNull();
    expect(completionError("CARD", "")).toBeNull();
  });

  it("rule: suppliers offered are those with a verified invoice still to pay, by name", () => {
    const suppliers = suppliersWithVerifiedInvoices([
      invoice("a", { supplierId: "s-z", supplierName: "Zeta" }),
      invoice("b", { supplierId: "s-a", supplierName: "Alpha" }),
      invoice("c", { supplierId: "s-a", supplierName: "Alpha" }),
      invoice("d", { supplierId: "s-paid", supplierName: "Paid", paidAmount: 100 }),
      invoice("e", { supplierId: "s-draft", supplierName: "Draft", status: "DRAFT" }),
    ]);

    expect(suppliers).toEqual([{ id: "s-a", name: "Alpha" }, { id: "s-z", name: "Zeta" }]);
  });
});
