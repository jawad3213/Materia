import React from "react";
import type { RequisitionStatus } from "../types/requisition.types";

interface RequisitionStatusBadgeProps {
  status: RequisitionStatus | string;
  size?: "sm" | "md";
}

const statusConfig: Record<
  string,
  { label: string; badgeClass: string; dotClass: string }
> = {
  DRAFT: {
    label: "Draft",
    badgeClass: "bg-gray-100 text-gray-700 dark:bg-gray-800 dark:text-gray-300 border border-gray-200 dark:border-gray-700",
    dotClass: "bg-gray-400 dark:bg-gray-500",
  },
  SUBMITTED: {
    label: "Submitted",
    badgeClass: "bg-blue-50 text-blue-700 dark:bg-blue-500/15 dark:text-blue-400 border border-blue-200/60 dark:border-blue-500/20",
    dotClass: "bg-blue-500 animate-pulse",
  },
  UNDER_REVIEW: {
    label: "Under Review",
    badgeClass: "bg-sky-50 text-sky-700 dark:bg-sky-500/15 dark:text-sky-400 border border-sky-200/60 dark:border-sky-500/20",
    dotClass: "bg-sky-500 animate-pulse",
  },
  APPROVED: {
    label: "Approved",
    badgeClass: "bg-emerald-50 text-emerald-700 dark:bg-emerald-500/15 dark:text-emerald-400 border border-emerald-200/60 dark:border-emerald-500/20",
    dotClass: "bg-emerald-500",
  },
  REJECTED: {
    label: "Rejected",
    badgeClass: "bg-red-50 text-red-700 dark:bg-red-500/15 dark:text-red-400 border border-red-200/60 dark:border-red-500/20",
    dotClass: "bg-red-500",
  },
  CANCELLED: {
    label: "Cancelled",
    badgeClass: "bg-neutral-100 text-neutral-600 dark:bg-neutral-800 dark:text-neutral-400 border border-neutral-200 dark:border-neutral-700",
    dotClass: "bg-neutral-400",
  },
  CONVERTED: {
    label: "Converted (PO)",
    badgeClass: "bg-purple-50 text-purple-700 dark:bg-purple-500/15 dark:text-purple-400 border border-purple-200/60 dark:border-purple-500/20",
    dotClass: "bg-purple-500",
  },
};

export default function RequisitionStatusBadge({
  status,
  size = "md",
}: RequisitionStatusBadgeProps) {
  const normalizedKey = (status || "").toUpperCase();
  const config = statusConfig[normalizedKey] || {
    label: status || "Unknown",
    badgeClass: "bg-gray-100 text-gray-700 dark:bg-gray-800 dark:text-gray-300 border border-gray-200 dark:border-gray-700",
    dotClass: "bg-gray-400",
  };

  const sizeClasses =
    size === "sm"
      ? "px-2 py-0.5 text-xs font-medium"
      : "px-2.5 py-1 text-xs font-semibold";

  return (
    <span
      className={`inline-flex items-center gap-1.5 rounded-full transition-colors ${sizeClasses} ${config.badgeClass}`}
    >
      <span className={`h-1.5 w-1.5 rounded-full ${config.dotClass}`} />
      <span>{config.label}</span>
    </span>
  );
}
