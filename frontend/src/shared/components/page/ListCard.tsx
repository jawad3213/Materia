import type { ReactNode } from "react";
import { Link } from "react-router-dom";
import Button from "../ui/button/Button";

/**
 * The list layout used by the materials, suppliers and categories pages: one bordered card with a header
 * (title, search, filter button, primary action), the active-filter pills, the content and a footer.
 */

export interface FilterPill {
  label: string;
  onRemove: () => void;
}

interface ListCardProps {
  title: string;
  subtitle?: string;
  search?: { value: string; onChange: (value: string) => void; placeholder: string };
  filter?: { activeCount: number; isOpen: boolean; onToggle: () => void; panel: ReactNode };
  action?: { label: string; to: string };
  pills?: FilterPill[];
  onClearPills?: () => void;
  footer?: ReactNode;
  children: ReactNode;
}

export default function ListCard({
  title,
  subtitle,
  search,
  filter,
  action,
  pills = [],
  onClearPills,
  footer,
  children,
}: ListCardProps) {
  return (
    <div className="rounded-xl border border-gray-200 bg-white dark:border-white/[0.05] dark:bg-white/[0.03]">
      <div className="flex flex-col gap-4 border-b border-gray-100 px-5 py-4 dark:border-white/[0.05] sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h3 className="text-lg font-semibold text-gray-800 dark:text-white/90">{title}</h3>
          {subtitle && <p className="text-theme-xs text-gray-500 dark:text-gray-400">{subtitle}</p>}
        </div>
        <div className="flex flex-wrap items-center justify-end gap-3">
          {search && (
            <div className="relative w-full sm:w-auto">
              <svg
                className="absolute left-3 top-1/2 size-4 -translate-y-1/2 text-gray-400"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
              </svg>
              <input
                type="text"
                placeholder={search.placeholder}
                value={search.value}
                onChange={(e) => search.onChange(e.target.value)}
                className="w-full rounded-lg border border-gray-200 bg-transparent py-2 pl-9 pr-8 text-sm text-gray-700 outline-none focus:border-brand-500 dark:border-gray-800 dark:text-gray-300 sm:w-64"
              />
              {search.value && (
                <button
                  type="button"
                  aria-label="Clear search"
                  onClick={() => search.onChange("")}
                  className="absolute right-2.5 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600 dark:hover:text-gray-200"
                >
                  <svg className="size-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                  </svg>
                </button>
              )}
            </div>
          )}

          {filter && (
            <div className="relative">
              <Button variant="outline" size="sm" onClick={filter.onToggle}>
                <span className="flex items-center gap-2">
                  <svg className="size-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      strokeWidth={2}
                      d="M3 4a1 1 0 011-1h16a1 1 0 011 1v2.586a1 1 0 01-.293.707l-6.414 6.414a1 1 0 00-.293.707V17l-4 4v-6.586a1 1 0 00-.293-.707L3.293 7.293A1 1 0 013 6.586V4z"
                    />
                  </svg>
                  Filter
                </span>
              </Button>
              {filter.activeCount > 0 && (
                <span className="absolute -right-2 -top-2 flex h-5 w-5 items-center justify-center rounded-full bg-brand-500 text-[10px] font-bold text-white shadow-sm">
                  {filter.activeCount}
                </span>
              )}
              {filter.panel}
            </div>
          )}

          {action && (
            <Link to={action.to}>
              <Button size="sm">
                <span className="flex items-center gap-2">
                  <svg className="size-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v16m8-8H4" />
                  </svg>
                  {action.label}
                </span>
              </Button>
            </Link>
          )}
        </div>
      </div>

      {pills.length > 0 && (
        <div className="flex flex-wrap items-center gap-2 border-b border-gray-100 bg-gray-50/75 px-5 py-2.5 dark:border-white/[0.05] dark:bg-gray-900/40">
          <span className="text-xs font-medium text-gray-500 dark:text-gray-400">Active filters:</span>
          {pills.map((pill) => (
            <span
              key={pill.label}
              className="inline-flex items-center gap-1.5 rounded-full bg-brand-50 px-2.5 py-1 text-xs font-medium text-brand-700 dark:bg-brand-500/15 dark:text-brand-300"
            >
              {pill.label}
              <button
                type="button"
                aria-label={`Remove ${pill.label}`}
                onClick={pill.onRemove}
                className="hover:text-brand-900 dark:hover:text-white"
              >
                <svg className="size-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                </svg>
              </button>
            </span>
          ))}
          {onClearPills && (
            <button
              type="button"
              onClick={onClearPills}
              className="ml-2 text-xs font-semibold text-gray-500 transition-colors hover:text-error-500"
            >
              Clear all
            </button>
          )}
        </div>
      )}

      {children}

      {footer}
    </div>
  );
}
