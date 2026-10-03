import {
  QUALITY_STATUS_LABELS,
  RECEIPT_STATUS_LABELS,
  type QualityStatus,
  type ReceiptStatus,
} from "../types/goodsReceipt.types";

const BASE = "inline-flex items-center gap-1.5 rounded-full px-2.5 py-0.5 text-[11px] font-semibold border";

const QUALITY_CLASSES: Record<QualityStatus, string> = {
  ACCEPTED: "bg-emerald-50 text-emerald-700 border-emerald-200/60 dark:bg-emerald-500/15 dark:text-emerald-400 dark:border-emerald-500/20",
  REJECTED: "bg-rose-50 text-rose-700 border-rose-200/60 dark:bg-rose-500/15 dark:text-rose-400 dark:border-rose-500/20",
  UNDER_REVIEW: "bg-amber-50 text-amber-700 border-amber-200/60 dark:bg-amber-500/15 dark:text-amber-400 dark:border-amber-500/20",
  PARTIAL: "bg-yellow-50 text-yellow-800 border-yellow-200/60 dark:bg-yellow-500/15 dark:text-yellow-400 dark:border-yellow-500/20",
};

const RECEIPT_CLASSES: Record<ReceiptStatus, string> = {
  DRAFT: "bg-gray-50 text-gray-700 border-gray-200/60 dark:bg-white/[0.05] dark:text-gray-300 dark:border-white/[0.1]",
  IN_PROGRESS: "bg-sky-50 text-sky-700 border-sky-200/60 dark:bg-sky-500/15 dark:text-sky-400 dark:border-sky-500/20",
  COMPLETED: "bg-emerald-50 text-emerald-700 border-emerald-200/60 dark:bg-emerald-500/15 dark:text-emerald-400 dark:border-emerald-500/20",
  PARTIAL: "bg-amber-50 text-amber-700 border-amber-200/60 dark:bg-amber-500/15 dark:text-amber-400 dark:border-amber-500/20",
  CANCELLED: "bg-rose-50 text-rose-700 border-rose-200/60 dark:bg-rose-500/15 dark:text-rose-400 dark:border-rose-500/20",
};

export default function QualityStatusBadge({ status }: { status?: QualityStatus | null }) {
  if (!status) return <span className="text-xs text-gray-400">-</span>;
  return <span className={`${BASE} ${QUALITY_CLASSES[status]}`}>{QUALITY_STATUS_LABELS[status]}</span>;
}

export function ReceiptStatusBadge({ status }: { status: ReceiptStatus }) {
  return <span className={`${BASE} ${RECEIPT_CLASSES[status]}`}>{RECEIPT_STATUS_LABELS[status]}</span>;
}
