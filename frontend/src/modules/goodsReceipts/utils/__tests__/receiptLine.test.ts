import { describe, expect, it } from "vitest";
import { deriveQualityStatus, lineError, receivedByLine, toDrafts, type ReceiptLineDraft } from "../receiptLine";
import type { GoodsReceipt, ReceiptStatus } from "../../types/goodsReceipt.types";
import type { PurchaseOrder } from "../../../purchaseOrders/types";

/** [T058] The receipt entry form refuses what the backend would refuse, and derives what the backend derives (US7-4 to US7-8). */

function draft(overrides: Partial<ReceiptLineDraft> = {}): ReceiptLineDraft {
  return {
    purchaseOrderLineId: "pol-1",
    materialCode: "MAT-1",
    materialName: "Bolts",
    unitOfMeasure: "PCE",
    ordered: 10,
    remaining: 6,
    received: 6,
    rejected: 0,
    rejectionReason: "",
    batchNumber: "",
    storageLocation: "",
    ...overrides,
  };
}

function receipt(status: ReceiptStatus, lines: Array<[string, number]>): GoodsReceipt {
  return {
    id: `gr-${status}`,
    receiptCode: "GR-2026-0001",
    purchaseOrderId: "po-1",
    status,
    receivedBy: "r",
    hasDiscrepancy: false,
    lines: lines.map(([purchaseOrderLineId, quantityReceived], i) => ({
      id: `l-${i}`,
      lineNumber: i + 1,
      purchaseOrderLineId,
      materialCode: "MAT",
      quantityReceived,
    })),
  };
}

describe("lineError", () => {
  it("rule: a valid line has no error", () => {
    expect(lineError(draft())).toBeNull();
  });

  it("rule: receiving more than remains is refused (US7-4)", () => {
    expect(lineError(draft({ received: 7 }))).toMatch(/6/);
  });

  it("rule: rejecting more than was received is refused (US7-5)", () => {
    expect(lineError(draft({ received: 3, rejected: 4 }))).not.toBeNull();
  });

  it("rule: a rejection without a reason is refused; with one it is accepted (US7-6)", () => {
    expect(lineError(draft({ rejected: 2 }))).not.toBeNull();
    expect(lineError(draft({ rejected: 2, rejectionReason: "  " }))).not.toBeNull();
    expect(lineError(draft({ rejected: 2, rejectionReason: "Dented" }))).toBeNull();
  });

  it("rule: negative quantities are refused", () => {
    expect(lineError(draft({ received: -1 }))).not.toBeNull();
    expect(lineError(draft({ rejected: -1 }))).not.toBeNull();
  });
});

describe("deriveQualityStatus", () => {
  it("rule: nothing rejected is accepted, everything rejected is rejected, otherwise partial (US7-7)", () => {
    expect(deriveQualityStatus({ received: 5, rejected: 0 })).toBe("ACCEPTED");
    expect(deriveQualityStatus({ received: 5, rejected: 5 })).toBe("REJECTED");
    expect(deriveQualityStatus({ received: 5, rejected: 2 })).toBe("PARTIAL");
  });
});

describe("receivedByLine", () => {
  it("rule: only completed receipts, with or without discrepancies, count towards what was received (US7-8)", () => {
    const totals = receivedByLine([
      receipt("COMPLETED", [["pol-1", 3]]),
      receipt("PARTIAL", [["pol-1", 2], ["pol-2", 1]]),
      receipt("DRAFT", [["pol-1", 9]]),
      receipt("IN_PROGRESS", [["pol-1", 9]]),
      receipt("CANCELLED", [["pol-1", 9]]),
    ]);

    expect(totals.get("pol-1")).toBe(5);
    expect(totals.get("pol-2")).toBe(1);
  });

  it("rule: lines without an order-line reference are ignored", () => {
    const r = receipt("COMPLETED", [["pol-1", 3]]);
    r.lines[0].purchaseOrderLineId = null;

    expect(receivedByLine([r]).size).toBe(0);
  });
});

describe("toDrafts", () => {
  const order = {
    id: "po-1",
    lines: [
      { id: "pol-1", materialCode: "MAT-1", materialName: "Bolts", unitOfMeasure: "PCE", quantity: 10 },
      { id: "pol-2", materialCode: "MAT-2", materialName: null, unitOfMeasure: null, quantity: 4 },
    ],
  } as unknown as PurchaseOrder;

  it("rule: each draft line starts at its outstanding quantity; fully received lines are dropped", () => {
    const drafts = toDrafts(order, [receipt("COMPLETED", [["pol-1", 4], ["pol-2", 4]])]);

    expect(drafts).toHaveLength(1);
    expect(drafts[0]).toMatchObject({ purchaseOrderLineId: "pol-1", ordered: 10, remaining: 6, received: 6, rejected: 0 });
  });

  it("rule: with no earlier receipts every line is fully outstanding, with display defaults", () => {
    const drafts = toDrafts(order, []);

    expect(drafts.map((d) => d.remaining)).toEqual([10, 4]);
    expect(drafts[1]).toMatchObject({ materialName: "", unitOfMeasure: "U" });
  });
});
