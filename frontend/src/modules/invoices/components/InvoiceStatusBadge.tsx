import {
  INVOICE_STATUS_LABELS,
  INVOICE_TYPE_LABELS,
  type InvoiceStatus,
  type InvoiceType,
} from "../types/invoice.types";

const BASE = "inline-flex items-center gap-1.5 rounded-full px-2.5 py-0.5 text-[11px] font-semibold border";

const STATUS_CLASSES: Record<InvoiceStatus, string> = {
  DRAFT: "bg-gray-50 text-gray-700 border-gray-200/60 dark:bg-white/[0.05] dark:text-gray-300 dark:border-white/[0.1]",
  SUBMITTED: "bg-sky-50 text-sky-700 border-sky-200/60 dark:bg-sky-500/15 dark:text-sky-400 dark:border-sky-500/20",
  VERIFIED: "bg-indigo-50 text-indigo-700 border-indigo-200/60 dark:bg-indigo-500/15 dark:text-indigo-400 dark:border-indigo-500/20",
  PAID: "bg-emerald-50 text-emerald-700 border-emerald-200/60 dark:bg-emerald-500/15 dark:text-emerald-400 dark:border-emerald-500/20",
  CANCELLED: "bg-rose-50 text-rose-700 border-rose-200/60 dark:bg-rose-500/15 dark:text-rose-400 dark:border-rose-500/20",
};

export default function InvoiceStatusBadge({ status }: { status: InvoiceStatus }) {
  return <span className={`${BASE} ${STATUS_CLASSES[status]}`}>{INVOICE_STATUS_LABELS[status]}</span>;
}

export function InvoiceTypeBadge({ type }: { type: InvoiceType }) {
  const classes =
    type === "CREDIT_NOTE"
      ? "bg-amber-50 text-amber-700 border-amber-200/60 dark:bg-amber-500/15 dark:text-amber-400 dark:border-amber-500/20"
      : "bg-white text-gray-600 border-gray-200 dark:bg-transparent dark:text-gray-400 dark:border-white/[0.1]";
  return <span className={`${BASE} ${classes}`}>{INVOICE_TYPE_LABELS[type]}</span>;
}
