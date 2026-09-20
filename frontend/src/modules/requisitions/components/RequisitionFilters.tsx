import React from "react";
import { Dropdown } from "../../../shared/components/ui/dropdown/Dropdown";
import Button from "../../../shared/components/ui/button/Button";

interface RequisitionFiltersProps {
  isOpen: boolean;
  onClose: () => void;
  filterStatus: string;
  setFilterStatus: (val: string) => void;
  filterRequester: string;
  setFilterRequester: (val: string) => void;
  filterDateFrom: string;
  setFilterDateFrom: (val: string) => void;
  filterDateTo: string;
  setFilterDateTo: (val: string) => void;
  onApply: () => void;
  onClear: () => void;
}

const statusOptions = [
  { value: "", label: "All Statuses" },
  { value: "DRAFT", label: "Draft" },
  { value: "SUBMITTED", label: "Submitted" },
  { value: "UNDER_REVIEW", label: "Under Review" },
  { value: "APPROVED", label: "Approved" },
  { value: "CONVERTED", label: "Converted to PO" },
  { value: "REJECTED", label: "Rejected" },
  { value: "CANCELLED", label: "Cancelled" },
];

export default function RequisitionFilters({
  isOpen,
  onClose,
  filterStatus,
  setFilterStatus,
  filterRequester,
  setFilterRequester,
  filterDateFrom,
  setFilterDateFrom,
  filterDateTo,
  setFilterDateTo,
  onApply,
  onClear,
}: RequisitionFiltersProps) {
  const activeCount = [
    filterStatus,
    filterRequester,
    filterDateFrom,
    filterDateTo,
  ].filter(Boolean).length;

  return (
    <Dropdown
      isOpen={isOpen}
      onClose={onClose}
      className="w-[320px] sm:w-[460px] p-5 top-full right-0 mt-2 z-50 shadow-xl border border-gray-200 dark:border-white/[0.1] bg-white dark:bg-gray-900 rounded-2xl"
    >
      <div className="flex items-center justify-between pb-3 mb-4 border-b border-gray-100 dark:border-white/[0.05]">
        <div className="flex items-center gap-2">
          <svg
            className="size-4 text-brand-500"
            fill="none"
            viewBox="0 0 24 24"
            stroke="currentColor"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M3 4a1 1 0 011-1h16a1 1 0 011 1v2.586a1 1 0 01-.293.707l-6.414 6.414a1 1 0 00-.293.707V17l-4 4v-6.586a1 1 0 00-.293-.707L3.293 7.293A1 1 0 013 6.586V4z"
            />
          </svg>
          <h4 className="text-sm font-semibold text-gray-800 dark:text-white/90">
            Filter Requisitions
          </h4>
        </div>
        {activeCount > 0 && (
          <button
            type="button"
            onClick={onClear}
            className="text-xs font-medium text-brand-500 hover:text-brand-600 dark:text-brand-400 transition-colors"
          >
            Reset all ({activeCount})
          </button>
        )}
      </div>

      <div className="space-y-4 mb-5">
        <div>
          <label className="block mb-1.5 text-xs font-medium text-gray-700 dark:text-gray-300">
            Workflow Status
          </label>
          <select
            value={filterStatus}
            onChange={(e) => setFilterStatus(e.target.value)}
            className="w-full rounded-xl border border-gray-200 dark:border-white/[0.1] bg-white dark:bg-gray-800 px-3 py-2 text-xs text-gray-800 dark:text-white focus:border-brand-500 focus:outline-none focus:ring-1 focus:ring-brand-500"
          >
            {statusOptions.map((opt) => (
              <option key={opt.value} value={opt.value}>
                {opt.label}
              </option>
            ))}
          </select>
        </div>

        <div>
          <label className="block mb-1.5 text-xs font-medium text-gray-700 dark:text-gray-300">
            Requester ID / Name
          </label>
          <input
            type="text"
            value={filterRequester}
            onChange={(e) => setFilterRequester(e.target.value)}
            placeholder="e.g. John Doe or REQ-001"
            className="w-full rounded-xl border border-gray-200 dark:border-white/[0.1] bg-white dark:bg-gray-800 px-3 py-2 text-xs text-gray-800 dark:text-white placeholder-gray-400 focus:border-brand-500 focus:outline-none focus:ring-1 focus:ring-brand-500"
          />
        </div>

        <div>
          <label className="block mb-1.5 text-xs font-medium text-gray-700 dark:text-gray-300">
            Need-By Date Range
          </label>
          <div className="grid grid-cols-2 gap-2">
            <div>
              <input
                type="date"
                value={filterDateFrom}
                onChange={(e) => setFilterDateFrom(e.target.value)}
                className="w-full rounded-xl border border-gray-200 dark:border-white/[0.1] bg-white dark:bg-gray-800 px-3 py-2 text-xs text-gray-800 dark:text-white focus:border-brand-500 focus:outline-none focus:ring-1 focus:ring-brand-500"
              />
              <span className="text-[10px] text-gray-400 mt-0.5 block">From</span>
            </div>
            <div>
              <input
                type="date"
                value={filterDateTo}
                onChange={(e) => setFilterDateTo(e.target.value)}
                className="w-full rounded-xl border border-gray-200 dark:border-white/[0.1] bg-white dark:bg-gray-800 px-3 py-2 text-xs text-gray-800 dark:text-white focus:border-brand-500 focus:outline-none focus:ring-1 focus:ring-brand-500"
              />
              <span className="text-[10px] text-gray-400 mt-0.5 block">To</span>
            </div>
          </div>
        </div>
      </div>

      <div className="flex items-center justify-end gap-2 pt-3 border-t border-gray-100 dark:border-white/[0.05]">
        <Button variant="outline" size="sm" onClick={onClose}>
          Cancel
        </Button>
        <Button size="sm" onClick={onApply}>
          Apply Filters
        </Button>
      </div>
    </Dropdown>
  );
}
