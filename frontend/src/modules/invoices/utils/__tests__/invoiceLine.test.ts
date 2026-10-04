import { describe, expect, it } from "vitest";
import {
  defaultDueDate,
  formatAmount,
  invoiceTotals,
  isOverdue,
  isPartiallyPaid,
  outstandingAmount,
  lineError,
  lineTotal,
  lineWarnings,
  toInvoiceDrafts,
  type InvoiceLineDraft,
} from "../invoiceLine";
import type { PurchaseOrder } from "../../../purchaseOrders/types";
import type { GoodsReceipt } from "../../../goodsReceipts/types/goodsReceipt.types";
import type { Invoice } from "../../types/invoice.types";

const order = {
  id: "po-1",
  orderCode: "PO-2026-0001",
  currencyCode: "MAD",
  lines: [
    { id: "pol-1", lineNumber: 1, materialCode: "MAT-1", materialName: "Bolts", unitOfMeasure: "PCE", quantity: 10, unitPrice: "5.00", lineTotal: 50, currencyCode: "MAD" },
    { id: "pol-2", lineNumber: 2, materialCode: "MAT-2", materialName: null, quantity: 4, unitPrice: 2.5, lineTotal: 10, currencyCode: "MAD" },
  ],
} as unknown as PurchaseOrder;

function receipt(status: string, lines: Array<[string, number, number, number?]>): GoodsReceipt {
  return {
    id: `gr-${status}`,
    receiptCode: "GR-2026-0001",
    purchaseOrderId: "po-1",
    status,
    receivedBy: "r-1",
    hasDiscrepancy: false,
    lines: lines.map(([purchaseOrderLineId, received, rejected, accepted], i) => ({
      id: `grl-${i}`,
      lineNumber: i + 1,
      materialCode: "MAT",
      purchaseOrderLineId,
      quantityReceived: received,
      quantityRejected: rejected,
      quantityAccepted: accepted,
    })),
  } as GoodsReceipt;
}

function invoice(status: string, type: "STANDARD" | "CREDIT_NOTE", qty: number): Invoice {
  return {
    id: `inv-${status}-${type}`,
    invoiceCode: "INV-1",
    supplierId: "s-1",
    supplierName: "Acme",
    invoiceType: type,
    status,
    invoiceDate: "2026-10-01",
    currencyCode: "MAD",
    isVerified: false,
    hasDiscrepancy: false,
    lines: [{ id: "il-1", lineNumber: 1, purchaseOrderLineId: "pol-1", materialName: "Bolts", quantityInvoiced: qty, unitPrice: 5, hasQuantityDiscrepancy: false }],
  } as Invoice;
}

const draft = (overrides: Partial<InvoiceLineDraft> = {}): InvoiceLineDraft => ({
  purchaseOrderLineId: "pol-1",
  materialCode: "MAT-1",
  materialName: "Bolts",
  unitOfMeasure: "PCE",
  ordered: 10,
  received: 8,
  alreadyInvoiced: 0,
  orderPrice: 5,
  invoiced: 8,
  unitPrice: 5,
  taxAmount: 0,
  ...overrides,
});

describe("toInvoiceDrafts", () => {
  it("rule: the invoiced quantity starts at what validated receipts accepted, at the order price", () => {
    const drafts = toInvoiceDrafts(order, [receipt("COMPLETED", [["pol-1", 6, 1, 5]]), receipt("PARTIAL", [["pol-1", 3, 0, 3]])], []);

    expect(drafts[0]).toMatchObject({ received: 8, invoiced: 8, unitPrice: 5, orderPrice: 5, ordered: 10 });
    expect(drafts[1]).toMatchObject({ received: 0, invoiced: 0, unitPrice: 2.5, materialName: "MAT-2" });
  });

  it("rule: draft and cancelled receipts count for nothing; accepted falls back to received minus rejected", () => {
    const drafts = toInvoiceDrafts(
      order,
      [receipt("DRAFT", [["pol-1", 9, 0]]), receipt("CANCELLED", [["pol-1", 9, 0]]), receipt("COMPLETED", [["pol-1", 7, 2]])],
      []
    );

    expect(drafts[0].received).toBe(5);
  });

  it("rule: quantities already billed are deducted; credit notes give them back; cancelled invoices are ignored", () => {
    const drafts = toInvoiceDrafts(
      order,
      [receipt("COMPLETED", [["pol-1", 10, 0, 10]])],
      [invoice("PAID", "STANDARD", 6), invoice("VERIFIED", "CREDIT_NOTE", 2), invoice("CANCELLED", "STANDARD", 10)]
    );

    expect(drafts[0]).toMatchObject({ alreadyInvoiced: 4, invoiced: 6 });
  });

  it("rule: nothing is pre-filled when everything received is already billed", () => {
    const drafts = toInvoiceDrafts(order, [receipt("COMPLETED", [["pol-1", 5, 0, 5]])], [invoice("SUBMITTED", "STANDARD", 7)]);

    expect(drafts[0].invoiced).toBe(0);
  });
});

describe("totals", () => {
  it("rule: line total is quantity × unit price, rounded to cents; invoice totals add tax", () => {
    expect(lineTotal({ invoiced: 3, unitPrice: 0.1 })).toBe(0.3);
    expect(invoiceTotals([draft({ invoiced: 2, unitPrice: 5, taxAmount: 2 }), draft({ invoiced: 1, unitPrice: 2.5, taxAmount: 0.5 })])).toEqual({
      totalAmount: 12.5,
      totalTaxAmount: 2.5,
      totalAmountWithTax: 15,
    });
    expect(invoiceTotals([])).toEqual({ totalAmount: 0, totalTaxAmount: 0, totalAmountWithTax: 0 });
  });
});

describe("lineError", () => {
  it("rule: negative or non-numeric quantity, price or tax blocks saving", () => {
    expect(lineError(draft())).toBeNull();
    expect(lineError(draft({ invoiced: -1 }))).toMatch(/quantité/);
    expect(lineError(draft({ invoiced: Number.NaN }))).toMatch(/quantité/);
    expect(lineError(draft({ invoiced: 2.5 }))).toMatch(/entier/);
    expect(lineError(draft({ unitPrice: -0.01 }))).toMatch(/prix/);
    expect(lineError(draft({ taxAmount: -1 }))).toMatch(/taxe/);
  });
});

describe("lineWarnings (three-way match)", () => {
  it("rule: a line matching the receipts and the order price has no warning", () => {
    expect(lineWarnings(draft())).toEqual([]);
  });

  it("rule: billing more than was received and not yet billed is flagged", () => {
    expect(lineWarnings(draft({ invoiced: 9 }))).toHaveLength(1);
    expect(lineWarnings(draft({ invoiced: 5, alreadyInvoiced: 4 }))[0]).toMatch(/\(4\)/);
  });

  it("rule: a unit price different from the order is flagged, but not on a line billed at zero", () => {
    expect(lineWarnings(draft({ unitPrice: 5.5 }))[0]).toMatch(/Prix unitaire/);
    expect(lineWarnings(draft({ unitPrice: 5.5, invoiced: 0 }))).toEqual([]);
  });
});

describe("payments", () => {
  it("rule: the outstanding amount is the total with tax minus payments, never below zero", () => {
    expect(outstandingAmount({ totalAmountWithTax: 32, paidAmount: null })).toBe(32);
    expect(outstandingAmount({ totalAmountWithTax: 32.0, paidAmount: 20 })).toBe(12);
    expect(outstandingAmount({ totalAmountWithTax: 32, paidAmount: 40 })).toBe(0);
    expect(outstandingAmount({ totalAmountWithTax: 0.3, paidAmount: 0.1 })).toBe(0.2);
  });

  it("rule: an invoice is partly paid only while verified with a payment recorded", () => {
    expect(isPartiallyPaid({ status: "VERIFIED", paidAmount: 5 })).toBe(true);
    expect(isPartiallyPaid({ status: "VERIFIED", paidAmount: null })).toBe(false);
    expect(isPartiallyPaid({ status: "VERIFIED", paidAmount: 0 })).toBe(false);
    expect(isPartiallyPaid({ status: "PAID", paidAmount: 32 })).toBe(false);
  });
});

describe("dates and formatting", () => {
  it("rule: the due date adds the order's payment delay, 30 days when none, across month ends", () => {
    expect(defaultDueDate("2026-10-01", 60)).toBe("2026-11-30");
    expect(defaultDueDate("2026-01-31", null)).toBe("2026-03-02");
    expect(defaultDueDate("", 30)).toBe("");
  });

  it("rule: an invoice is overdue only past its due date while neither paid nor cancelled", () => {
    expect(isOverdue({ dueDate: "2026-10-01", status: "VERIFIED" }, "2026-10-02")).toBe(true);
    expect(isOverdue({ dueDate: "2026-10-02", status: "VERIFIED" }, "2026-10-02")).toBe(false);
    expect(isOverdue({ dueDate: "2026-10-01", status: "PAID" }, "2026-10-02")).toBe(false);
    expect(isOverdue({ dueDate: "2026-10-01", status: "CANCELLED" }, "2026-10-02")).toBe(false);
    expect(isOverdue({ dueDate: null, status: "SUBMITTED" }, "2026-10-02")).toBe(false);
  });

  it("rule: amounts show two decimals and the currency, accepting numbers or strings", () => {
    expect(formatAmount(1234.5, "MAD").replace(/\s/g, " ")).toBe("1 234,50 MAD");
    expect(formatAmount("12.3 MAD")).toBe("12,30");
    expect(formatAmount(null, "EUR")).toBe("0,00 EUR");
  });
});
