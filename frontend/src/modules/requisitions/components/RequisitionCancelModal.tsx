import React, { useState } from "react";
import { Modal } from "../../../shared/components/ui/modal";
import Button from "../../../shared/components/ui/button/Button";
import type { Requisition } from "../types/requisition.types";
import type { RequisitionListItem } from "../types/RequisitionListItem";

interface RequisitionCancelModalProps {
  isOpen: boolean;
  onClose: () => void;
  requisition: Requisition | RequisitionListItem | null;
  onConfirm: (reason: string) => Promise<void>;
  isLoading?: boolean;
}

export default function RequisitionCancelModal({
  isOpen,
  onClose,
  requisition,
  onConfirm,
  isLoading = false,
}: RequisitionCancelModalProps) {
  const [reason, setReason] = useState("");
  const [internalSubmitting, setInternalSubmitting] = useState(false);
  const isSubmitting = isLoading || internalSubmitting;
  const [error, setError] = useState("");

  if (!requisition) return null;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      setInternalSubmitting(true);
      setError("");
      await onConfirm(reason.trim());
      setReason("");
      onClose();
    } catch (err: any) {
      setError(err?.response?.data?.message || err?.message || "Cancellation failed.");
    } finally {
      setInternalSubmitting(false);
    }
  };

  return (
    <Modal isOpen={isOpen} onClose={onClose} className="max-w-lg p-6">
      <div className="space-y-4">
        {/* Header */}
        <div className="flex items-start gap-3">
          <div className="p-3 rounded-xl flex-shrink-0 bg-amber-50 text-amber-600 dark:bg-amber-500/15 dark:text-amber-400">
            <svg className="size-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
            </svg>
          </div>
          <div>
            <h3 className="text-base font-bold text-gray-900 dark:text-white">
              Cancel Purchase Requisition
            </h3>
            <p className="text-xs text-gray-500 dark:text-gray-400 mt-1">
              Are you sure you want to cancel requisition{" "}
              <span className="font-semibold text-brand-600 dark:text-brand-400">
                {requisition.requisitionCode}
              </span>
              ? This action will mark it as cancelled.
            </p>
          </div>
        </div>

        {/* Error Alert */}
        {error && (
          <div className="p-3 rounded-xl text-xs font-medium bg-red-50 text-red-700 border border-red-200 dark:bg-red-500/10 dark:text-red-400 dark:border-red-500/20">
            {error}
          </div>
        )}

        {/* Form */}
        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-xs font-semibold text-gray-700 dark:text-gray-300 mb-1">
              Cancellation Reason (Optional)
            </label>
            <textarea
              value={reason}
              onChange={(e) => setReason(e.target.value)}
              placeholder="Provide context on why this requisition is being cancelled..."
              rows={3}
              className="w-full text-xs rounded-xl border border-gray-200 bg-gray-50/50 p-3 text-gray-900 focus:border-brand-500 focus:bg-white focus:outline-none dark:border-white/[0.08] dark:bg-white/[0.03] dark:text-white"
            />
          </div>

          <div className="flex items-center justify-end gap-2 pt-2">
            <Button
              type="button"
              variant="outline"
              size="sm"
              onClick={onClose}
              disabled={isSubmitting}
            >
              Back
            </Button>
            <Button
              type="submit"
              size="sm"
              disabled={isSubmitting}
              className="bg-amber-600 hover:bg-amber-700 text-white"
            >
              {isSubmitting ? "Cancelling..." : "Confirm Cancellation"}
            </Button>
          </div>
        </form>
      </div>
    </Modal>
  );
}
