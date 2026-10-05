import React, { useState } from "react";
import { Modal } from "../../../shared/components/ui/modal";
import Button from "../../../shared/components/ui/button/Button";
import Label from "../../../shared/components/form/Label";
import TextArea from "../../../shared/components/form/input/TextArea";
import type { PurchaseOrder } from "../types/PurchaseOrder";
import type { PurchaseOrderListItem } from "../types/PurchaseOrderListItem";
import { getApiErrorMessage } from "../../../shared/utils/apiError";

interface PurchaseOrderCancelModalProps {
  isOpen: boolean;
  onClose: () => void;
  order: PurchaseOrder | PurchaseOrderListItem | null;
  onConfirm: (reason: string) => Promise<void>;
  isLoading?: boolean;
}

export default function PurchaseOrderCancelModal({ isOpen, onClose, order, onConfirm, isLoading = false }: PurchaseOrderCancelModalProps) {
  const [reason, setReason] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState("");
  const busy = isLoading || submitting;

  if (!order) return null;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!reason.trim()) {
      setError("A cancellation reason is required.");
      return;
    }
    try {
      setSubmitting(true);
      setError("");
      await onConfirm(reason.trim());
      setReason("");
      onClose();
    } catch (err) {
      setError(getApiErrorMessage(err, "Cancelling the order failed."));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <Modal isOpen={isOpen} onClose={onClose} className="max-w-md p-6">
      <form onSubmit={handleSubmit} className="space-y-4">
        <div>
          <h3 className="text-lg font-semibold text-gray-800 dark:text-white/90">Cancel Order</h3>
          <p className="mt-1 text-sm text-gray-500 dark:text-gray-400">
            <span className="font-mono font-medium text-gray-700 dark:text-gray-300">{order.orderCode}</span> for {order.supplierName}{" "}
            will be cancelled. This cannot be undone.
          </p>
        </div>
        <div>
          <Label>Reason *</Label>
          <TextArea rows={3} value={reason} onChange={setReason} placeholder="Why is this order cancelled?" error={!!error} hint={error || undefined} />
        </div>
        <div className="flex justify-end gap-2 border-t border-gray-100 pt-3 dark:border-white/[0.05]">
          <Button variant="outline" size="sm" onClick={onClose} disabled={busy}>
            Back
          </Button>
          <Button size="sm" type="submit" disabled={busy || !reason.trim()}>
            {busy ? "Cancelling..." : "Confirm Cancellation"}
          </Button>
        </div>
      </form>
    </Modal>
  );
}
