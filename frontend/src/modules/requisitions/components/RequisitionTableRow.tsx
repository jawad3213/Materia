import React from "react";
import { Link } from "react-router-dom";
import { TableRow, TableCell } from "../../../shared/components/ui/table";
import Checkbox from "../../../shared/components/form/input/Checkbox";
import RequisitionStatusBadge from "./RequisitionStatusBadge";
import RequisitionRowActions from "./RequisitionRowActions";
import type { Requisition } from "../types";

export interface RequisitionTableRowProps {
  requisition: Requisition;
  isExpanded: boolean;
  isSelected: boolean;
  onToggleExpand: (id: string) => void;
  onSelect: (id: string) => void;
  formatAmount: (amt: string | number, curr?: string) => string;
  getUrgencyBadge: (requiredDateStr?: string) => React.ReactNode;
  onQuickSubmit: (req: Requisition) => void;
  onConvertToPo: (req: Requisition) => void;
  onCancel: (req: Requisition) => void;
  onDelete: (req: Requisition) => void;
}

export default function RequisitionTableRow({
  requisition,
  isExpanded,
  isSelected,
  onToggleExpand,
  onSelect,
  formatAmount,
  getUrgencyBadge,
  onQuickSubmit,
  onConvertToPo,
  onCancel,
  onDelete,
}: RequisitionTableRowProps) {
  const linesCount = requisition.lines?.length || 0;
  const isPending =
    requisition.status === "SUBMITTED" || requisition.status === "UNDER_REVIEW";
  const isApproved = requisition.status === "APPROVED";
  const isConverted = requisition.status === "CONVERTED";
  const isDraft = requisition.status === "DRAFT";
  const isRejected = requisition.status === "REJECTED";
  const isCancelled = requisition.status === "CANCELLED";

  // Status Lifecycle Rules:
  // Modifier: DRAFT ✅ OUI, SUBMITTED ✅ OUI, others ❌ NON
  const canEdit = isDraft || isPending;
  // Supprimer: DRAFT ✅ OUI, REJECTED ✅ OUI, CANCELLED ✅ OUI, others ❌ NON
  const canDelete = isDraft || isRejected || isCancelled;
  // Soumettre: DRAFT ✅ OUI
  const canSubmit = isDraft;
  // Convertir: APPROVED ✅ OUI
  const canConvert = isApproved;
  // Annuler: SUBMITTED ✅ OUI, APPROVED ✅ OUI, DRAFT ❌ NON (delete directly instead)
  const canCancel = isPending || isApproved;

  const requesterInitials = (requisition.requesterName || "U")
    .split(" ")
    .map((n) => n[0])
    .join("")
    .slice(0, 2)
    .toUpperCase();

  return (
    <TableRow
      className={`border-b border-gray-100 transition-colors hover:bg-gray-50/60 dark:border-white/[0.05] dark:hover:bg-white/[0.02] ${
        isSelected ? "bg-brand-50/30 dark:bg-brand-500/[0.06]" : ""
      }`}
    >
      {/* Expand Row Toggle */}
      <TableCell className="px-4 py-3.5">
        <button
          type="button"
          onClick={() => onToggleExpand(requisition.id)}
          className="p-1 rounded-md text-gray-400 hover:text-gray-700 hover:bg-gray-100 dark:hover:text-gray-200 dark:hover:bg-gray-800 transition-transform"
          title={isExpanded ? "Collapse lines" : "Expand lines"}
        >
          <svg
            className={`size-4 transition-transform duration-200 ${
              isExpanded ? "rotate-90 text-brand-500" : ""
            }`}
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
          </svg>
        </button>
      </TableCell>

      {/* Select Checkbox */}
      <TableCell className="px-4 py-3.5">
        <Checkbox
          checked={isSelected}
          onChange={() => onSelect(requisition.id)}
        />
      </TableCell>

      {/* PR Code, Title & Item Count */}
      <TableCell className="px-4 py-3.5">
        <div className="flex flex-col gap-0.5">
          <div className="flex items-center gap-2">
            <Link
              to={`/requisitions/view/${requisition.id}`}
              className="font-bold text-xs text-brand-600 hover:underline dark:text-brand-400"
            >
              {requisition.requisitionCode}
            </Link>
            <span className="inline-flex items-center px-1.5 py-0.2 text-[10px] font-medium rounded bg-gray-100 text-gray-600 dark:bg-gray-800 dark:text-gray-400">
              {linesCount} {linesCount === 1 ? "item" : "items"}
            </span>
          </div>
          <span className="text-xs font-medium text-gray-800 dark:text-gray-200 truncate max-w-xs">
            {requisition.title}
          </span>
        </div>
      </TableCell>

      {/* Requester */}
      <TableCell className="px-4 py-3.5">
        <div className="flex items-center gap-2">
          <div className="size-7 rounded-full bg-brand-100 text-brand-700 dark:bg-brand-500/20 dark:text-brand-300 flex items-center justify-center font-bold text-[10px]">
            {requesterInitials}
          </div>
          <div className="flex flex-col">
            <span className="text-xs font-medium text-gray-800 dark:text-gray-200">
              {requisition.requesterName}
            </span>
            <span className="text-[10px] text-gray-400 dark:text-gray-500">
              ID: {requisition.requesterId}
            </span>
          </div>
        </div>
      </TableCell>

      {/* Need-By Date & Urgency Indicator */}
      <TableCell className="px-4 py-3.5">
        <div className="flex flex-col gap-1">
          {getUrgencyBadge(requisition.requiredDate)}
        </div>
      </TableCell>

      {/* Total Estimated Value */}
      <TableCell className="px-4 py-3.5">
        <span className="text-xs font-semibold text-gray-900 dark:text-white">
          {formatAmount(requisition.totalAmount, requisition.currencyCode)}
        </span>
      </TableCell>

      {/* Status Badge */}
      <TableCell className="px-4 py-3.5">
        <RequisitionStatusBadge status={requisition.status} size="sm" />
      </TableCell>

      {/* PO Reference */}
      <TableCell className="px-4 py-3.5">
        {isConverted && requisition.purchaseOrderCode ? (
          <span className="inline-flex items-center gap-1 text-xs font-semibold text-purple-600 dark:text-purple-400 hover:underline">
            <svg className="size-3" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M10 6H6a2 2 0 00-2 2v10a2 2 0 002 2h10a2 2 0 002-2v-4M14 4h6m0 0v6m0-6L10 14" />
            </svg>
            {requisition.purchaseOrderCode}
          </span>
        ) : (
          <span className="text-xs text-gray-400 dark:text-gray-600">—</span>
        )}
      </TableCell>

      {/* Contextual Quick Actions */}
      <TableCell className="px-4 py-3.5 text-right">
        <RequisitionRowActions
          requisition={requisition}
          canConvert={canConvert}
          canSubmit={canSubmit}
          canCancel={canCancel}
          canEdit={canEdit}
          canDelete={canDelete}
          isDraft={isDraft}
          onQuickSubmit={onQuickSubmit}
          onConvertToPo={onConvertToPo}
          onCancel={onCancel}
          onDelete={onDelete}
        />
      </TableCell>
    </TableRow>
  );
}
