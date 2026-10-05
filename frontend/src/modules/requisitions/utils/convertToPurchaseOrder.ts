import type { CreatePurchaseOrderRequest } from "../../purchaseOrders/types";
import type { Requisition } from "../types";

export interface OrderSupplier {
  id: string;
  name: string;
  code?: string;
}

/** The supplier already chosen on one of the requisition lines, if any. */
export function supplierFromLines(requisition: Requisition): OrderSupplier | null {
  const line = requisition.lines?.find((l) => l.supplierId && l.supplierName);
  return line?.supplierId ? { id: line.supplierId, name: line.supplierName || "", code: line.supplierCode || undefined } : null;
}

/**
 * The purchase order created from an approved requisition: every line with its quantity and estimated price,
 * delivered by the date the requester needs. The backend converts the requisition in the same transaction.
 */
export function toPurchaseOrderRequest(
  requisition: Requisition,
  supplier: OrderSupplier,
  buyer: { id?: string; name?: string; email?: string } | null
): CreatePurchaseOrderRequest {
  const currency = requisition.currencyCode || "MAD";
  return {
    requisitionId: requisition.id,
    requisitionCode: requisition.requisitionCode,
    supplierId: supplier.id,
    supplierName: supplier.name,
    supplierCode: supplier.code || undefined,
    orderDate: new Date().toISOString().split("T")[0],
    expectedDeliveryDate: requisition.requiredDate || undefined,
    paymentTerms: "Bank transfer, 30 days",
    paymentDelayDays: 30,
    deliveryTerms: "Delivered on site",
    incoterm: "DAP",
    currencyCode: currency,
    taxAmount: 0,
    shippingCost: 0,
    orderedBy: buyer?.id || "",
    orderedByName: buyer?.name || buyer?.email || "",
    notes: requisition.title + (requisition.description ? ` - ${requisition.description}` : ""),
    lines: (requisition.lines || []).map((l, idx) => ({
      lineNumber: idx + 1,
      requisitionLineId: l.id,
      materialCode: l.materialCode,
      materialId: l.materialId || undefined,
      materialName: l.materialName || undefined,
      unitOfMeasure: l.unitOfMeasure || undefined,
      quantity: Number(l.quantity) || 1,
      unitPrice: Number(l.unitPrice) || 0,
      currencyCode: l.currencyCode || currency,
      expectedDeliveryDate: l.requiredDate || requisition.requiredDate || undefined,
      notes: l.notes || undefined,
    })),
    createdBy: buyer?.email || "system",
  };
}
