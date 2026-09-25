import React from "react";
import Button from "../../../shared/components/ui/button/Button";
import type { Requisition } from "../types";

export interface RequisitionConvertToPoModalProps {
  isOpen: boolean;
  onClose: () => void;
  onConfirm: () => void;
  requisition: Requisition | null;
  isConverting: boolean;
}

export default function RequisitionConvertToPoModal({
  isOpen,
  onClose,
  onConfirm,
  requisition,
  isConverting,
}: RequisitionConvertToPoModalProps) {
  if (!isOpen || !requisition) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center overflow-y-auto overflow-x-hidden bg-black/60 backdrop-blur-sm p-4">
      <div className="relative w-full max-w-md rounded-2xl bg-white p-6 shadow-2xl dark:bg-gray-900 border border-gray-100 dark:border-white/[0.08]">
        {/* Icon & Title */}
        <div className="flex items-center gap-3.5 mb-4">
          <div className="flex size-11 items-center justify-center rounded-xl bg-purple-50 text-purple-600 dark:bg-purple-500/15 dark:text-purple-300">
            <svg className="size-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M16 11V7a4 4 0 00-8 0v4M5 9h14l1 12H4L5 9z" />
            </svg>
          </div>
          <div>
            <h3 className="text-base font-bold text-gray-900 dark:text-white">
              Convert to Purchase Order
            </h3>
            <p className="text-xs text-gray-500 dark:text-gray-400">
              Transform approved requisition into an official PO
            </p>
          </div>
        </div>

        {/* Requisition Details Box */}
        <div className="mb-5 rounded-xl bg-purple-50/50 p-4 border border-purple-100 dark:bg-purple-500/10 dark:border-purple-500/20 text-xs text-gray-700 dark:text-gray-300 space-y-1.5">
          <div className="flex justify-between">
            <span className="text-gray-500 dark:text-gray-400">Requisition:</span>
            <span className="font-bold text-purple-700 dark:text-purple-300">
              {requisition.requisitionCode}
            </span>
          </div>
          <div className="flex justify-between">
            <span className="text-gray-500 dark:text-gray-400">Title:</span>
            <span className="font-medium text-gray-900 dark:text-white truncate max-w-[200px]">
              {requisition.title}
            </span>
          </div>
          <div className="flex justify-between">
            <span className="text-gray-500 dark:text-gray-400">Line Items:</span>
            <span className="font-semibold text-gray-900 dark:text-white">
              {requisition.lines?.length || 0} items
            </span>
          </div>
        </div>

        {/* Descriptive Notice */}
        <p className="text-xs text-gray-600 dark:text-gray-400 mb-6 leading-relaxed">
          Converting this requisition will automatically generate a new Purchase Order in draft state and update this requisition's status to <span className="font-semibold text-purple-600 dark:text-purple-400">CONVERTED</span>.
        </p>

        {/* Action Buttons */}
        <div className="flex items-center justify-end gap-3">
          <Button
            type="button"
            variant="outline"
            size="sm"
            onClick={onClose}
            disabled={isConverting}
          >
            Cancel
          </Button>
          <button
            type="button"
            onClick={onConfirm}
            disabled={isConverting}
            className="inline-flex items-center gap-1.5 px-4 py-2 text-xs font-semibold rounded-lg bg-purple-600 text-white hover:bg-purple-700 active:bg-purple-800 disabled:opacity-50 transition-colors shadow-sm"
          >
            {isConverting ? (
              <>
                <div className="size-3.5 animate-spin rounded-full border-2 border-white border-t-transparent" />
                Converting...
              </>
            ) : (
              <>
                <svg className="size-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                </svg>
                Confirm Conversion
              </>
            )}
          </button>
        </div>
      </div>
    </div>
  );
}
