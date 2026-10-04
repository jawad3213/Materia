import type { GoodsReceipt, GoodsReceiptLine } from "../../goodsReceipts/types/goodsReceipt.types";
import { HOLDING_RETURN_STATUSES, RETURNABLE_RECEIPT_STATUSES, type ReturnToVendor } from "../types/returnToVendor.types";

/** A rejected receipt line the return may send back, with what is still returnable on it. */
export interface ReturnLineDraft {
  receiptLine: GoodsReceiptLine;
  rejected: number;
  /** Held by the receipt's other returns that are not cancelled. */
  held: number;
  /** What this return may send back: rejected minus held, never below zero. */
  available: number;
  selected: boolean;
  quantity: number;
  reason: string;
}

/** A completed receipt that rejected something can have its rejected goods returned. */
export function isReturnableReceipt(receipt: GoodsReceipt): boolean {
  return RETURNABLE_RECEIPT_STATUSES.includes(receipt.status) && (receipt.totalQuantityRejected ?? 0) > 0;
}

/** Quantity per receipt line held by returns other than `excludeReturnId`, mirroring the backend. */
export function heldByReturns(returns: ReturnToVendor[], excludeReturnId?: string): Map<string, number> {
  const held = new Map<string, number>();
  for (const rtv of returns) {
    if (rtv.id === excludeReturnId || !HOLDING_RETURN_STATUSES.includes(rtv.status)) continue;
    for (const line of rtv.lines) {
      held.set(line.goodsReceiptLineId, (held.get(line.goodsReceiptLineId) ?? 0) + line.quantityToReturn);
    }
  }
  return held;
}

/** Form rows: every rejected line of the receipt, selected for what is still returnable. */
export function toReturnDrafts(receipt: GoodsReceipt, returns: ReturnToVendor[]): ReturnLineDraft[] {
  const held = heldByReturns(returns);
  return receipt.lines
    .filter((line) => (line.quantityRejected ?? 0) > 0)
    .map((receiptLine) => {
      const rejected = receiptLine.quantityRejected ?? 0;
      const taken = held.get(receiptLine.id) ?? 0;
      const available = Math.max(0, rejected - taken);
      return {
        receiptLine,
        rejected,
        held: taken,
        available,
        selected: available > 0,
        quantity: available,
        reason: receiptLine.rejectionReason ?? "",
      };
    });
}

/** A value that blocks saving the row. */
export function lineError(line: ReturnLineDraft): string | null {
  if (!line.selected) return null;
  if (!Number.isInteger(line.quantity) || line.quantity <= 0) return "The quantity must be a positive whole number.";
  if (line.quantity > line.available) return `Only ${line.available} can still be returned.`;
  return null;
}

export function totalQuantity(lines: ReturnLineDraft[]): number {
  return lines.filter((l) => l.selected).reduce((sum, l) => sum + l.quantity, 0);
}
