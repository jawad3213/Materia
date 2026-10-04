import { useEffect, useState } from "react";
import { Link, useNavigate, useSearchParams } from "react-router-dom";
import goodsReceiptService from "../services/goodsReceiptService";
import purchaseOrderService from "../../purchaseOrders/services/purchaseOrderService";
import { RECEIVABLE_STATUSES } from "../../purchaseOrders/hooks/usePurchaseOrder";
import type { PurchaseOrder } from "../../purchaseOrders/types";
import GoodsReceiptLine from "./GoodsReceiptLine";
import { deriveQualityStatus, lineError, toDrafts, type ReceiptLineDraft } from "../utils/receiptLine";
import Button from "../../../shared/components/ui/button/Button";
import useAuth from "../../auth/hooks/useAuth";
import { getApiErrorMessage } from "../../../shared/utils/apiError";
import Label from "../../../shared/components/form/Label";
import TextArea from "../../../shared/components/form/input/TextArea";
import { FloatingToast, FormCard, FormSection } from "../../../shared/components/page/DetailParts";
import { SELECT_CLASS } from "../../../shared/components/page/pageStyles";

export default function GoodsReceiptForm() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const { user } = useAuth();
  const initialOrderId = searchParams.get("purchaseOrderId") || "";

  const [assignedOrders, setAssignedOrders] = useState<PurchaseOrder[]>([]);
  const [selectedOrderId, setSelectedOrderId] = useState(initialOrderId);
  const [order, setOrder] = useState<PurchaseOrder | null>(null);
  const [lines, setLines] = useState<ReceiptLineDraft[]>([]);
  const [notes, setNotes] = useState("");
  const [loadingOrder, setLoadingOrder] = useState(!!initialOrderId);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // Orders the current user is assigned to receive.
  useEffect(() => {
    let cancelled = false;
    purchaseOrderService
      .getAll()
      .then((res) => {
        if (cancelled) return;
        setAssignedOrders(
          res.data.filter((o) => o.assignedTo === user?.id && RECEIVABLE_STATUSES.includes(o.status))
        );
      })
      .catch((err) => console.error("Failed to load assigned purchase orders:", err));
    return () => {
      cancelled = true;
    };
  }, [user?.id]);

  useEffect(() => {
    if (!selectedOrderId) return;
    let cancelled = false;
    Promise.all([
      purchaseOrderService.getById(selectedOrderId),
      goodsReceiptService.getByPurchaseOrderId(selectedOrderId),
    ])
      .then(([orderRes, receiptsRes]) => {
        if (cancelled) return;
        const loaded = orderRes.data;
        if (!RECEIVABLE_STATUSES.includes(loaded.status)) {
          setError("This order is not ready for receipt.");
          return;
        }
        if (loaded.assignedTo !== user?.id) {
          setError("You are not assigned to receive this order.");
          return;
        }
        setOrder(loaded);
        setLines(toDrafts(loaded, receiptsRes.data));
      })
      .catch((err) => {
        if (!cancelled) setError(getApiErrorMessage(err, "Could not load the purchase order."));
      })
      .finally(() => {
        if (!cancelled) setLoadingOrder(false);
      });
    return () => {
      cancelled = true;
    };
  }, [selectedOrderId, user?.id]);

  const handleSelectOrder = (orderId: string) => {
    setError(null);
    setOrder(null);
    setLines([]);
    setLoadingOrder(!!orderId);
    setSelectedOrderId(orderId);
  };

  const updateLine = (index: number, line: ReceiptLineDraft) =>
    setLines((prev) => prev.map((l, i) => (i === index ? line : l)));

  const submit = async (validate: boolean) => {
    if (!order) return;
    setError(null);

    const received = lines.filter((l) => l.received > 0);
    if (received.length === 0) {
      setError("Enter at least one received quantity.");
      return;
    }
    if (lines.some((l) => lineError(l))) {
      setError("Fix the highlighted lines before saving.");
      return;
    }

    try {
      setSubmitting(true);
      const created = await goodsReceiptService.create({
        purchaseOrderId: order.id,
        notes: notes.trim() || undefined,
        lines: received.map((l) => ({
          purchaseOrderLineId: l.purchaseOrderLineId,
          materialCode: l.materialCode,
          quantityReceived: l.received,
          quantityRejected: l.rejected,
          qualityStatus: deriveQualityStatus(l),
          rejectionReason: l.rejected > 0 ? l.rejectionReason.trim() : undefined,
          batchNumber: l.batchNumber.trim() || undefined,
          storageLocation: l.storageLocation.trim() || undefined,
        })),
      });
      if (validate) {
        try {
          await goodsReceiptService.complete(created.data.id);
        } catch (completeErr) {
          // The draft exists; send the user to it so validation can be retried there.
          navigate(`/goods-receipts/${created.data.id}`, {
            state: {
              error: getApiErrorMessage(
                completeErr,
                "The receipt was saved as a draft but could not be validated."
              ),
            },
          });
          return;
        }
      }
      navigate(`/goods-receipts/${created.data.id}`);
    } catch (err) {
      console.error("Failed to save goods receipt:", err);
      setError(getApiErrorMessage(err, "Saving the goods receipt failed."));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <>
      <FloatingToast feedback={error ? { type: "error", text: error } : null} onClose={() => setError(null)} />

      <FormCard title="Create Goods Receipt">
        <FormSection
          title="Purchase Order"
          aside={
            <Link to={order ? `/purchase-orders/${order.id}` : "/goods-receipts"}>
              <Button variant="outline" size="sm">
                Cancel
              </Button>
            </Link>
          }
        >
          <Label>Order to receive *</Label>
          <select value={selectedOrderId} onChange={(e) => handleSelectOrder(e.target.value)} className={SELECT_CLASS}>
            <option value="">Select an order assigned to you</option>
            {assignedOrders.map((o) => (
              <option key={o.id} value={o.id}>
                {o.orderCode} — {o.supplierName}
              </option>
            ))}
            {selectedOrderId && !assignedOrders.some((o) => o.id === selectedOrderId) && order && (
              <option value={order.id}>
                {order.orderCode} — {order.supplierName}
              </option>
            )}
          </select>
          <p className="mt-1.5 text-theme-xs text-gray-500 dark:text-gray-400">
            Enter received and rejected quantities. Validating updates stock and the purchase order.
          </p>
          {loadingOrder && <p className="mt-3 text-sm text-gray-500">Loading purchase order...</p>}
          {order && lines.length === 0 && (
            <p className="mt-3 text-sm text-gray-500">Every quantity on this order has already been received.</p>
          )}
        </FormSection>

        {order && lines.length > 0 && (
          <>
            <FormSection title="Received Lines">
              <div className="space-y-4">
                {lines.map((line, index) => (
                  <GoodsReceiptLine
                    key={line.purchaseOrderLineId}
                    line={line}
                    onChange={(updated) => updateLine(index, updated)}
                  />
                ))}
              </div>
            </FormSection>

            <FormSection title="Notes">
              <TextArea rows={3} value={notes} onChange={setNotes} placeholder="Remarks about the delivery" />
            </FormSection>

            <div className="flex justify-end gap-3 border-t border-gray-100 pt-6 dark:border-gray-800">
              <Button variant="outline" onClick={() => submit(false)} disabled={submitting}>
                Save as Draft
              </Button>
              <Button onClick={() => submit(true)} disabled={submitting}>
                {submitting ? "Saving..." : "Validate Receipt"}
              </Button>
            </div>
          </>
        )}
      </FormCard>
    </>
  );
}
