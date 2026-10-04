import { describe, expect, it } from "vitest";
import type { GoodsReceipt, GoodsReceiptLine } from "../../../goodsReceipts/types/goodsReceipt.types";
import type { ReturnStatus, ReturnToVendor } from "../../types/returnToVendor.types";
import { heldByReturns, isReturnableReceipt, lineError, toReturnDrafts, totalQuantity } from "../returnLine";

const receiptLine = (id: string, rejected: number, reason: string | null = "Scratched"): GoodsReceiptLine => ({
  id,
  lineNumber: 1,
  materialCode: `MAT-${id}`,
  quantityReceived: 10,
  quantityRejected: rejected,
  rejectionReason: reason,
});

const receipt = (overrides: Partial<GoodsReceipt> = {}): GoodsReceipt => ({
  id: "gr-1",
  receiptCode: "GR-2026-0001",
  purchaseOrderId: "po-1",
  status: "COMPLETED",
  receivedBy: "r-1",
  hasDiscrepancy: false,
  totalQuantityRejected: 5,
  lines: [receiptLine("l1", 4), receiptLine("l2", 0), receiptLine("l3", 1, null)],
  ...overrides,
});

const aReturn = (id: string, status: ReturnStatus, lines: [string, number][]): ReturnToVendor => ({
  id,
  returnCode: id,
  goodsReceiptId: "gr-1",
  supplierId: "s-1",
  supplierName: "Acme",
  status,
  returnReason: "r",
  lines: lines.map(([goodsReceiptLineId, quantityToReturn], i) => ({
    id: `${id}-${i}`,
    lineNumber: i + 1,
    goodsReceiptLineId,
    materialCode: "MAT",
    materialName: "Bolts",
    quantityToReturn,
    rejectionReason: "r",
    replaced: false,
    creditNote: false,
  })),
});

describe("return lines", () => {
  it("rule: only a completed receipt that rejected something is returnable", () => {
    expect(isReturnableReceipt(receipt())).toBe(true);
    expect(isReturnableReceipt(receipt({ status: "PARTIAL" }))).toBe(true);
    expect(isReturnableReceipt(receipt({ status: "DRAFT" }))).toBe(false);
    expect(isReturnableReceipt(receipt({ totalQuantityRejected: 0 }))).toBe(false);
    expect(isReturnableReceipt(receipt({ totalQuantityRejected: null }))).toBe(false);
  });

  it("rule: draft, shipped and resolved returns hold their quantity; cancelled ones and the excluded return do not", () => {
    const returns = [
      aReturn("a", "DRAFT", [["l1", 1]]),
      aReturn("b", "RESOLVED", [
        ["l1", 2],
        ["l3", 1],
      ]),
      aReturn("c", "CANCELLED", [["l1", 4]]),
    ];
    expect(heldByReturns(returns).get("l1")).toBe(3);
    expect(heldByReturns(returns).get("l3")).toBe(1);
    expect(heldByReturns(returns, "b").get("l1")).toBe(1);
  });

  it("rule: one row per rejected line, preselected for what is still returnable, with the receipt reason", () => {
    const drafts = toReturnDrafts(receipt(), [
      aReturn("a", "PENDING", [
        ["l1", 3],
        ["l3", 1],
      ]),
    ]);

    expect(drafts.map((d) => d.receiptLine.id)).toEqual(["l1", "l3"]);
    expect(drafts[0]).toMatchObject({ rejected: 4, held: 3, available: 1, selected: true, quantity: 1, reason: "Scratched" });
    expect(drafts[1]).toMatchObject({ available: 0, selected: false, quantity: 0, reason: "" });
  });

  it("rule: a selected row needs a positive whole quantity within what is available", () => {
    const row = toReturnDrafts(receipt(), [])[0];
    expect(lineError(row)).toBeNull();
    expect(lineError({ ...row, selected: false, quantity: 99 })).toBeNull();
    expect(lineError({ ...row, quantity: 0 })).toMatch(/positive/);
    expect(lineError({ ...row, quantity: 1.5 })).toMatch(/whole/);
    expect(lineError({ ...row, quantity: 5 })).toMatch(/Only 4/);
  });

  it("rule: the total counts selected rows only", () => {
    const [a, b] = toReturnDrafts(receipt(), []);
    expect(totalQuantity([a, b])).toBe(5);
    expect(totalQuantity([a, { ...b, selected: false }])).toBe(4);
  });
});
