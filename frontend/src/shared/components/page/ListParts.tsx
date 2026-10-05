import type { ReactNode } from "react";
import { Link } from "react-router-dom";
import Pagination from "../ui/Pagination";
import { TableCell, TableRow } from "../ui/table";
import { SUB_TEXT } from "./pageStyles";



/** "Showing x to y of z results" and the pager, as under the suppliers table. */
export function ListFooter({
  page,
  size,
  total,
  totalPages,
  onPageChange,
}: {
  page: number;
  size: number;
  total: number;
  totalPages: number;
  onPageChange: (page: number) => void;
}) {
  return (
    <div className="flex flex-col items-center justify-between gap-4 border-t border-gray-100 px-5 py-4 dark:border-white/[0.05] sm:flex-row">
      <div className="text-sm text-gray-500 dark:text-gray-400">
        Showing <span className="font-medium text-gray-800 dark:text-white/90">{total === 0 ? 0 : page * size + 1}</span> to{" "}
        <span className="font-medium text-gray-800 dark:text-white/90">{Math.min((page + 1) * size, total)}</span> of{" "}
        <span className="font-medium text-gray-800 dark:text-white/90">{total}</span> results
      </div>
      <Pagination currentPage={page} totalPages={totalPages} onPageChange={onPageChange} />
    </div>
  );
}

/** Loading and empty rows of the reference tables. */
export function TableStateRow({ colSpan, loading, message }: { colSpan: number; loading?: boolean; message: string }) {
  return (
    <TableRow>
      <TableCell colSpan={colSpan} className="py-16 text-center">
        {loading ? (
          <div className="flex flex-col items-center justify-center gap-3">
            <div className="h-10 w-10 animate-spin rounded-full border-4 border-brand-500 border-t-transparent" />
            <span className="text-sm font-medium text-gray-500 dark:text-gray-400">{message}</span>
          </div>
        ) : (
          <span className="text-sm text-gray-500 dark:text-gray-400">{message}</span>
        )}
      </TableCell>
    </TableRow>
  );
}

const ACTION_BUTTON =
  "flex items-center justify-center rounded-lg p-2 text-gray-400 transition-colors hover:bg-brand-50 hover:text-brand-500 dark:hover:bg-brand-500/10 dark:hover:text-brand-500";

/** The eye icon that opens a record, as in the suppliers table. */
export function ViewAction({ to, title }: { to: string; title: string }) {
  return (
    <Link to={to} className={ACTION_BUTTON} title={title} aria-label={title}>
      <svg className="size-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
        <path
          strokeLinecap="round"
          strokeLinejoin="round"
          strokeWidth={2}
          d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"
        />
      </svg>
    </Link>
  );
}

const ROW_ACTION_TONES = {
  default: "",
  brand: "hover:text-brand-500",
  warning: "hover:text-warning-500",
  danger: "hover:text-error-500",
};

/** A quick row action (submit, confirm, cancel...) styled like the eye icon. */
export function RowIconButton({
  title,
  onClick,
  tone = "default",
  disabled,
  children,
}: {
  title: string;
  onClick: () => void;
  tone?: keyof typeof ROW_ACTION_TONES;
  disabled?: boolean;
  children: ReactNode;
}) {
  return (
    <button
      type="button"
      onClick={onClick}
      disabled={disabled}
      title={title}
      aria-label={title}
      className={`${ACTION_BUTTON} ${ROW_ACTION_TONES[tone]} disabled:opacity-40`}
    >
      {children}
    </button>
  );
}

/** The coloured initials avatar shown beside names in the suppliers table. */
const AVATAR_COLORS = [
  "bg-red-50 text-red-500 dark:bg-red-500/15 dark:text-red-500",
  "bg-orange-50 text-orange-500 dark:bg-orange-500/15 dark:text-orange-500",
  "bg-purple-50 text-purple-500 dark:bg-purple-500/15 dark:text-purple-500",
  "bg-green-50 text-green-500 dark:bg-green-500/15 dark:text-green-500",
  "bg-brand-50 text-brand-500 dark:bg-brand-500/15 dark:text-brand-500",
];

export function InitialsAvatar({ name, size = "md" }: { name?: string | null; size?: "md" | "lg" }) {
  const label = (name || "?").trim();
  const sum = label.split("").reduce((acc, c) => acc + c.charCodeAt(0), 0);
  const dims = size === "lg" ? "h-16 w-16 rounded-2xl text-2xl font-bold" : "h-10 w-10 rounded-full font-medium";
  return (
    <div className={`flex shrink-0 items-center justify-center ${dims} ${AVATAR_COLORS[sum % AVATAR_COLORS.length]}`}>
      {label.substring(0, 2).toUpperCase()}
    </div>
  );
}

/** Two-line cell: a main value and a muted secondary line. */
export function StackedCell({ main, sub }: { main: ReactNode; sub?: ReactNode }) {
  return (
    <>
      <span className="block font-medium text-gray-800 text-theme-sm dark:text-white/90">{main}</span>
      {sub != null && sub !== "" && <span className={SUB_TEXT}>{sub}</span>}
    </>
  );
}
