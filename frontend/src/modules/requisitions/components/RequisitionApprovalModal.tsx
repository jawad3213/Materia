import React, { useState } from "react";
import { Modal } from "../../../shared/components/ui/modal";
import Button from "../../../shared/components/ui/button/Button";
import Label from "../../../shared/components/form/Label";
import TextArea from "../../../shared/components/form/input/TextArea";
import type { Requisition } from "../types";
import { formatAmount } from "../../invoices/utils/invoiceLine";
import { getApiErrorMessage } from "../../../shared/utils/apiError";

interface RequisitionApprovalModalProps {
  isOpen: boolean;
  onClose: () => void;
  requisition: Requisition | null;
  mode: "APPROVE" | "REJECT";
  onConfirm: (notesOrReason: string) => Promise<void>;
  isLoading?: boolean;
}

/** Approve (optional notes) or reject (reason required) a submitted requisition. */
export default function RequisitionApprovalModal({ isOpen, onClose, requisition, mode, onConfirm, isLoading = false }: RequisitionApprovalModalProps) {
  const [comment, setComment] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState("");
  const busy = isLoading || submitting;

  if (!requisition) return null;
  const approve = mode === "APPROVE";

  const close = () => {
    setComment("");
    setError("");
    onClose();
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!approve && !comment.trim()) {
      setError("Give the reason for the rejection.");
      return;
    }
    try {
      setSubmitting(true);
      setError("");
      await onConfirm(comment.trim());
      setComment("");
      onClose();
    } catch (err) {
      setError(getApiErrorMessage(err, "The decision could not be saved."));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <Modal isOpen={isOpen} onClose={close} className="max-w-md p-6">
      <form onSubmit={handleSubmit} className="space-y-4">
        <div>
          <h3 className="text-lg font-semibold text-gray-800 dark:text-white/90">{approve ? "Approve Requisition" : "Reject Requisition"}</h3>
          <p className="mt-1 text-sm text-gray-500 dark:text-gray-400">
            {approve ? "The requisition can then be turned into a purchase order." : "The requester will see the reason."}
          </p>
        </div>

        <dl className="grid grid-cols-2 gap-x-4 gap-y-3 rounded-xl border border-gray-200 bg-gray-50/60 p-4 text-sm dark:border-gray-800 dark:bg-white/[0.02]">
          <div>
            <dt className="text-theme-xs text-gray-500 dark:text-gray-400">Requisition</dt>
            <dd className="font-mono font-medium text-gray-800 dark:text-white/90">{requisition.requisitionCode}</dd>
          </div>
          <div>
            <dt className="text-theme-xs text-gray-500 dark:text-gray-400">Estimated Total</dt>
            <dd className="font-medium text-gray-800 dark:text-white/90">{formatAmount(requisition.totalAmount, requisition.currencyCode)}</dd>
          </div>
          <div className="col-span-2">
            <dt className="text-theme-xs text-gray-500 dark:text-gray-400">Title</dt>
            <dd className="font-medium text-gray-800 dark:text-white/90">{requisition.title}</dd>
          </div>
          <div>
            <dt className="text-theme-xs text-gray-500 dark:text-gray-400">Requester</dt>
            <dd className="text-gray-800 dark:text-white/90">{requisition.requesterName}</dd>
          </div>
          <div>
            <dt className="text-theme-xs text-gray-500 dark:text-gray-400">Lines</dt>
            <dd className="text-gray-800 dark:text-white/90">{requisition.lines?.length || 0}</dd>
          </div>
          {requisition.justification && (
            <div className="col-span-2">
              <dt className="text-theme-xs text-gray-500 dark:text-gray-400">Justification</dt>
              <dd className="text-gray-700 dark:text-gray-300">{requisition.justification}</dd>
            </div>
          )}
        </dl>

        <div>
          <Label>{approve ? "Approval Notes" : "Rejection Reason *"}</Label>
          <TextArea
            rows={3}
            value={comment}
            onChange={(value) => {
              setComment(value);
              if (error) setError("");
            }}
            placeholder={approve ? "Optional" : "e.g. Over the quarterly budget; reduce the quantities"}
            error={!!error}
            hint={error || undefined}
          />
        </div>

        <div className="flex justify-end gap-2 border-t border-gray-100 pt-3 dark:border-white/[0.05]">
          <Button variant="outline" size="sm" onClick={close} disabled={busy}>
            Back
          </Button>
          <Button size="sm" type="submit" disabled={busy}>
            {busy ? "Saving..." : approve ? "Confirm Approval" : "Confirm Rejection"}
          </Button>
        </div>
      </form>
    </Modal>
  );
}
