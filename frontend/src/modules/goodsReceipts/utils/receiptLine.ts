import type { PurchaseOrder } from "../../purchaseOrders/types";
import type { GoodsReceipt, QualityStatus } from "../types/goodsReceipt.types";

export interface ReceiptLineDraft {
  purchaseOrderLineId: string;
  materialCode: string;
  materialName: string;
  unitOfMeasure: string;
  ordered: number;
  remaining: number;
  received: number;
  rejected: number;
  rejectionReason: string;
  batchNumber: string;
  storageLocation: string;
}

/** Quality outcome implied by the quantities, so a completed receipt never carries a pending inspection. */
export function deriveQualityStatus(line: Pick<ReceiptLineDraft, "received" | "rejected">): QualityStatus {
  if (line.rejected <= 0) return "ACCEPTED";
  if (line.rejected >= line.received) return "REJECTED";
  return "PARTIAL";
}

export function lineError(line: ReceiptLineDraft): string | null {
  if (line.received < 0 || line.rejected < 0) return "Les quantités ne peuvent pas être négatives.";
  if (line.received > line.remaining) return `Maximum ${line.remaining} restant à recevoir.`;
  if (line.rejected > line.received) return "Le rejeté ne peut pas dépasser le reçu.";
  if (line.rejected > 0 && !line.rejectionReason.trim()) return "Motif de rejet obligatoire.";
  return null;
}

/** Mirrors the backend: only completed or partial receipts count towards what has been received. */
export function receivedByLine(receipts: GoodsReceipt[]): Map<string, number> {
  const totals = new Map<string, number>();
  receipts
    .filter((r) => r.status === "COMPLETED" || r.status === "PARTIAL")
    .flatMap((r) => r.lines)
    .forEach((l) => {
      if (!l.purchaseOrderLineId) return;
      totals.set(l.purchaseOrderLineId, (totals.get(l.purchaseOrderLineId) ?? 0) + (l.quantityReceived ?? 0));
    });
  return totals;
}

export function toDrafts(order: PurchaseOrder, receipts: GoodsReceipt[]): ReceiptLineDraft[] {
  const received = receivedByLine(receipts);
  return order.lines
    .map((l) => {
      const remaining = Math.max(0, Number(l.quantity) - (received.get(l.id) ?? 0));
      return {
        purchaseOrderLineId: l.id,
        materialCode: l.materialCode,
        materialName: l.materialName || "",
        unitOfMeasure: l.unitOfMeasure || "U",
        ordered: Number(l.quantity),
        remaining,
        received: remaining,
        rejected: 0,
        rejectionReason: "",
        batchNumber: "",
        storageLocation: "",
      };
    })
    .filter((l) => l.remaining > 0);
}
