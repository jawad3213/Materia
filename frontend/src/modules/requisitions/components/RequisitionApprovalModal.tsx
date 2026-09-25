import React, { useState } from "react";
import { Modal } from "../../../shared/components/ui/modal";
import Button from "../../../shared/components/ui/button/Button";
import type { Requisition } from "../types/requisition.types";

interface RequisitionApprovalModalProps {
  isOpen: boolean;
  onClose: () => void;
  requisition: Requisition | null;
  mode: "APPROVE" | "REJECT";
  onConfirm: (notesOrReason: string) => Promise<void>;
  isLoading?: boolean;
}

export default function RequisitionApprovalModal({
  isOpen,
  onClose,
  requisition,
  mode,
  onConfirm,
  isLoading = false,
}: RequisitionApprovalModalProps) {
  const [comment, setComment] = useState("");
  const [internalSubmitting, setInternalSubmitting] = useState(false);
  const isSubmitting = isLoading || internalSubmitting;
  const [error, setError] = useState("");

  if (!requisition) return null;

  const isApprove = mode === "APPROVE";

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!isApprove && !comment.trim()) {
      setError("Please provide a reason for rejecting this requisition.");
      return;
    }

    try {
      setInternalSubmitting(true);
      setError("");
      await onConfirm(comment.trim());
      setComment("");
      onClose();
    } catch (err: any) {
      setError(err?.response?.data?.message || err?.message || "Operation failed.");
    } finally {
      setInternalSubmitting(false);
    }
  };

  return (
    <Modal isOpen={isOpen} onClose={onClose} className="max-w-lg p-6">
      <div className="space-y-4">
        {/* Header */}
        <div className="flex items-start gap-3">
          <div
            className={`p-3 rounded-xl flex-shrink-0 ${
              isApprove
                ? "bg-emerald-50 text-emerald-600 dark:bg-emerald-500/15 dark:text-emerald-400"
                : "bg-red-50 text-red-600 dark:bg-red-500/15 dark:text-red-400"
            }`}
          >
            {isApprove ? (
              <svg className="size-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
              </svg>
            ) : (
              <svg className="size-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
              </svg>
            )}
          </div>
          <div>
            <h3 className="text-lg font-bold text-gray-900 dark:text-white">
              {isApprove ? "Approve Requisition" : "Reject Requisition"}
            </h3>
            <p className="text-xs text-gray-500 dark:text-gray-400 mt-0.5">
              {isApprove
                ? "Authorize this requisition to proceed to Purchase Order conversion."
                : "Reject this requisition. The requester will be notified of the reason."}
            </p>
          </div>
        </div>

        {/* Requisition Summary Box */}
        <div className="p-3.5 rounded-xl bg-gray-50 dark:bg-gray-800/50 border border-gray-200/80 dark:border-white/[0.07] text-xs space-y-1.5">
          <div className="flex justify-between items-center">
            <span className="font-semibold text-gray-800 dark:text-white">
              {requisition.requisitionCode}
            </span>
            <span className="font-bold text-gray-900 dark:text-white">
              {requisition.totalAmount} {requisition.currencyCode || "MAD"}
            </span>
          </div>
          <div className="text-gray-600 dark:text-gray-300 font-medium">
            {requisition.title}
          </div>
          <div className="text-gray-400 dark:text-gray-500 flex justify-between text-[11px]">
            <span>Requester: {requisition.requesterName}</span>
            <span>Items: {requisition.lines?.length || 0}</span>
          </div>
          {requisition.justification && (
            <div className="pt-2 border-t border-gray-200/60 dark:border-white/[0.05]">
              <span className="font-bold text-[10px] uppercase tracking-wider text-brand-600 dark:text-brand-400 block mb-0.5">
                Business Justification:
              </span>
              <p className="text-[11px] text-gray-700 dark:text-gray-300 italic">
                "{requisition.justification}"
              </p>
            </div>
          )}
        </div>

        {/* Form */}
        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-xs font-semibold text-gray-700 dark:text-gray-300 mb-1.5">
              {isApprove ? "Approval Notes (Optional)" : "Rejection Reason *"}
            </label>
            <textarea
              rows={3}
              value={comment}
              onChange={(e) => {
                setComment(e.target.value);
                if (error) setError("");
              }}
              placeholder={
                isApprove
                  ? "e.g. Approved for procurement. Preferred vendor: Acme Industrial."
                  : "e.g. Budget exceeded for Q3, please review line item quantities."
              }
              className="w-full rounded-xl border border-gray-200 dark:border-white/[0.1] bg-white dark:bg-gray-800 p-3 text-xs text-gray-900 dark:text-white placeholder-gray-400 focus:border-brand-500 focus:outline-none focus:ring-1 focus:ring-brand-500"
            />
            {error && <p className="text-xs text-red-500 mt-1">{error}</p>}
          </div>

          <div className="flex items-center justify-end gap-2 pt-2 border-t border-gray-100 dark:border-white/[0.07]">
            <Button
              type="button"
              variant="outline"
              size="sm"
              onClick={() => {
                setComment("");
                setError("");
                onClose();
              }}
              disabled={isSubmitting}
            >
              Cancel
            </Button>
            <Button
              type="submit"
              size="sm"
              disabled={isSubmitting}
              className={
                isApprove
                  ? "bg-emerald-600 hover:bg-emerald-700 text-white"
                  : "bg-red-600 hover:bg-red-700 text-white"
              }
            >
              {isSubmitting
                ? "Processing..."
                : isApprove
                ? "Confirm Approval"
                : "Confirm Rejection"}
            </Button>
          </div>
        </form>
      </div>
    </Modal>
  );
}
