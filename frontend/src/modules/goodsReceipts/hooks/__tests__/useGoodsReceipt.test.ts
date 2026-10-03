import { afterEach, describe, expect, it, vi } from "vitest";
import { renderHook } from "@testing-library/react";
import useGoodsReceiptPermissions from "../useGoodsReceipt";
import { isAllowed, roles, statuses } from "../../../../test/actionMatrix";
import { signInAs } from "../../../../test/mockAuth";
import type { GoodsReceipt, ReceiptStatus } from "../../types/goodsReceipt.types";

/**
 * [T057] The goods receipt screens offer validate, cancel and delete only to the receipt's own
 * receiver while it is open, exactly as the shared matrix says (US7-3, SC-002a).
 */
const HOOK_FOR_ACTION = { complete: "canComplete", cancel: "canCancel", delete: "canDelete" } as const;

function receipt(status: string, receivedBy: string): GoodsReceipt {
  return {
    id: "gr-1",
    receiptCode: "GR-2026-0001",
    purchaseOrderId: "po-1",
    status: status as ReceiptStatus,
    receivedBy,
    hasDiscrepancy: false,
    lines: [],
  };
}

describe("useGoodsReceiptPermissions agrees with the action matrix", () => {
  afterEach(() => vi.restoreAllMocks());

  for (const role of roles) {
    for (const status of statuses("goodsReceipt")) {
      for (const [action, hook] of Object.entries(HOOK_FOR_ACTION)) {
        for (const own of [true, false]) {
          it(`rule: ${role} ${own ? "on own" : "on someone else's"} ${status} receipt is ${
            isAllowed("goodsReceipt", role, status, action, own) ? "offered" : "not offered"
          } ${action}`, () => {
            const userId = signInAs(role);

            const { result } = renderHook(() => useGoodsReceiptPermissions());

            expect(result.current[hook](receipt(status, own ? userId : "someone-else"))).toBe(
              isAllowed("goodsReceipt", role, status, action, own)
            );
          });
        }
      }
    }
  }

  it("rule: only roles with receipt:write may record a new receipt", () => {
    for (const role of roles) {
      signInAs(role);
      const { result } = renderHook(() => useGoodsReceiptPermissions());
      expect(result.current.canRecord).toBe(isAllowed("goodsReceipt", role, "DRAFT", "record", true));
      vi.restoreAllMocks();
    }
  });
});
