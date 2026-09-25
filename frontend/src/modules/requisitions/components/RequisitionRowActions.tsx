import React from "react";
import { Link } from "react-router-dom";
import type { Requisition } from "../types";

export interface RequisitionRowActionsProps {
  requisition: Requisition;
  canConvert: boolean;
  canSubmit: boolean;
  canCancel: boolean;
  canEdit: boolean;
  canDelete: boolean;
  isDraft: boolean;
  onQuickSubmit: (req: Requisition) => void;
  onConvertToPo: (req: Requisition) => void;
  onCancel: (req: Requisition) => void;
  onDelete: (req: Requisition) => void;
}

export default function RequisitionRowActions({
  requisition,
  canConvert,
  canSubmit,
  canCancel,
  canEdit,
  canDelete,
  isDraft,
  onQuickSubmit,
  onConvertToPo,
  onCancel,
  onDelete,
}: RequisitionRowActionsProps) {
  return (
    <div className="flex items-center justify-end gap-1">
      {/* If Approved: Convert to Purchase Order Button */}
      {canConvert && (
        <button
          type="button"
          onClick={() => onConvertToPo(requisition)}
          className="inline-flex items-center gap-1 px-2.5 py-1 text-[11px] font-semibold rounded-lg bg-purple-50 text-purple-700 hover:bg-purple-100 dark:bg-purple-500/15 dark:text-purple-300 dark:hover:bg-purple-500/25 transition-colors"
          title="Convert to Purchase Order"
        >
          <svg className="size-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M16 11V7a4 4 0 00-8 0v4M5 9h14l1 12H4L5 9z" />
          </svg>
          Convert to PO
        </button>
      )}

      {/* If Draft: Quick Submit Button */}
      {canSubmit && (
        <button
          type="button"
          onClick={() => onQuickSubmit(requisition)}
          className="inline-flex items-center gap-1.5 px-2.5 py-1 text-[11px] font-semibold rounded-lg bg-blue-50 text-blue-700 hover:bg-blue-100 dark:bg-blue-500/15 dark:text-blue-300 dark:hover:bg-blue-500/25 transition-colors"
          title="Submit for Approval"
        >
          <svg className="size-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 19l9 2-9-18-9 18 9-2zm0 0v-8" />
          </svg>
          Submit
        </button>
      )}

      {/* Cancel Requisition Action: Active (Submitted, Approved) or Disabled (Draft, others) */}
      {canCancel ? (
        <button
          type="button"
          onClick={() => onCancel(requisition)}
          className="p-1.5 rounded-lg text-gray-400 hover:text-amber-600 hover:bg-amber-50 dark:text-gray-400 dark:hover:text-amber-400 dark:hover:bg-amber-500/15 transition-colors cursor-pointer"
          title="Cancel Requisition"
        >
          <svg className="size-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M18.364 18.364A9 9 0 005.636 5.636m12.728 12.728A9 9 0 015.636 5.636m12.728 12.728L5.636 5.636" />
          </svg>
        </button>
      ) : (
        <button
          type="button"
          disabled
          className="p-1.5 rounded-lg text-gray-300 dark:text-gray-600 cursor-not-allowed opacity-35 inline-flex items-center"
          title={
            isDraft
              ? "Draft requisitions cannot be cancelled (delete directly instead)"
              : `Cannot cancel: Requisition in ${requisition.status} status cannot be cancelled`
          }
        >
          <svg className="size-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M18.364 18.364A9 9 0 005.636 5.636m12.728 12.728A9 9 0 015.636 5.636m12.728 12.728L5.636 5.636" />
          </svg>
        </button>
      )}

      {/* View Details Link */}
      <Link
        to={`/requisitions/view/${requisition.id}`}
        className="p-1.5 rounded-lg text-gray-500 hover:text-brand-600 hover:bg-gray-100 dark:text-gray-400 dark:hover:text-brand-400 dark:hover:bg-gray-800 transition-colors"
        title="View Details"
      >
        <svg className="size-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
        </svg>
      </Link>

      {/* Edit Requisition Link (Draft & Submitted) or Locked Icon */}
      {canEdit ? (
        <Link
          to={`/requisitions/edit/${requisition.id}`}
          className="p-1.5 rounded-lg text-gray-500 hover:text-brand-600 hover:bg-brand-50 dark:text-gray-400 dark:hover:text-brand-400 dark:hover:bg-brand-500/15 transition-colors"
          title="Edit Requisition"
        >
          <svg className="size-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
          </svg>
        </Link>
      ) : (
        <span
          className="p-1.5 rounded-lg text-gray-300 dark:text-gray-600 cursor-not-allowed inline-flex items-center"
          title={`Locked: Requisition is in ${requisition.status} status and cannot be modified`}
        >
          <svg className="size-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" />
          </svg>
        </span>
      )}

      {/* Delete Requisition Action: Active (Draft, Rejected, Cancelled) or Disabled */}
      {canDelete ? (
        <button
          type="button"
          onClick={() => onDelete(requisition)}
          className="p-1.5 rounded-lg text-gray-500 hover:text-red-600 hover:bg-red-50 dark:text-gray-400 dark:hover:text-red-400 dark:hover:bg-red-500/15 transition-colors cursor-pointer"
          title="Delete Requisition"
        >
          <svg className="size-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
          </svg>
        </button>
      ) : (
        <button
          type="button"
          disabled
          className="p-1.5 rounded-lg text-gray-300 dark:text-gray-600 cursor-not-allowed opacity-40 inline-flex items-center"
          title={`Cannot delete: Requisition in ${requisition.status} status cannot be deleted`}
        >
          <svg className="size-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
          </svg>
        </button>
      )}
    </div>
  );
}
