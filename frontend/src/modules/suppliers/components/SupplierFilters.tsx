import React from "react";
import { Dropdown } from "../../../shared/components/ui/dropdown/Dropdown";
import Button from "../../../shared/components/ui/button/Button";
import { SupplierStatus } from "../enums/SupplierStatus";

interface SupplierFiltersProps {
  isOpen: boolean;
  onClose: () => void;
  filterStatus: string;
  setFilterStatus: (val: string) => void;
  filterCurrency: string;
  setFilterCurrency: (val: string) => void;
  filterCountry: string;
  setFilterCountry: (val: string) => void;
  onApply: () => void;
  onClear: () => void;
}

const currencyOptions = [
  { value: "", label: "All Currencies" },
  { value: "MAD", label: "MAD - Moroccan Dirham" },
  { value: "EUR", label: "EUR - Euro" },
  { value: "USD", label: "USD - US Dollar" },
];

const statusOptions = [
  { value: "", label: "All Statuses" },
  { value: SupplierStatus.ACTIVE, label: "Active" },
  { value: SupplierStatus.INACTIVE, label: "Inactive" },
];

export default function SupplierFilters({
  isOpen,
  onClose,
  filterStatus,
  setFilterStatus,
  filterCurrency,
  setFilterCurrency,
  filterCountry,
  setFilterCountry,
  onApply,
  onClear,
}: SupplierFiltersProps) {
  return (
    <Dropdown isOpen={isOpen} onClose={onClose} className="w-[320px] sm:w-[480px] p-5 top-full right-0 mt-2 z-50">
      <div className="flex items-center justify-between pb-3 mb-4 border-b border-gray-100 dark:border-white/[0.05]">
        <h4 className="text-sm font-semibold text-gray-800 dark:text-white/90">
          Filter Suppliers
        </h4>
        {[filterStatus, filterCurrency, filterCountry].filter(Boolean).length > 0 && (
          <button
            onClick={onClear}
            className="text-xs font-medium text-brand-500 hover:text-brand-600 transition-colors"
          >
            Reset all
          </button>
        )}
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 mb-5">
        {/* Status Filter */}
        <div>
          <label className="block mb-1.5 text-xs font-medium text-gray-700 dark:text-gray-300">
            Status
          </label>
          <div className="relative">
            <select
              value={filterStatus}
              onChange={(e) => setFilterStatus(e.target.value)}
              className="h-10 w-full appearance-none rounded-lg border border-gray-200 bg-transparent px-3 py-2 text-sm text-gray-800 outline-none focus:border-brand-500 dark:border-gray-800 dark:bg-gray-900 dark:text-gray-200"
            >
              {statusOptions.map((opt) => (
                <option key={opt.value} value={opt.value} className="dark:bg-gray-900">
                  {opt.label}
                </option>
              ))}
            </select>
            <div className="pointer-events-none absolute right-3 top-1/2 -translate-y-1/2 text-gray-400">
              <svg className="size-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7" />
              </svg>
            </div>
          </div>
        </div>

        {/* Currency Filter */}
        <div>
          <label className="block mb-1.5 text-xs font-medium text-gray-700 dark:text-gray-300">
            Currency
          </label>
          <div className="relative">
            <select
              value={filterCurrency}
              onChange={(e) => setFilterCurrency(e.target.value)}
              className="h-10 w-full appearance-none rounded-lg border border-gray-200 bg-transparent px-3 py-2 text-sm text-gray-800 outline-none focus:border-brand-500 dark:border-gray-800 dark:bg-gray-900 dark:text-gray-200"
            >
              {currencyOptions.map((opt) => (
                <option key={opt.value} value={opt.value} className="dark:bg-gray-900">
                  {opt.label}
                </option>
              ))}
            </select>
            <div className="pointer-events-none absolute right-3 top-1/2 -translate-y-1/2 text-gray-400">
              <svg className="size-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7" />
              </svg>
            </div>
          </div>
        </div>

        {/* Country Filter */}
        <div className="sm:col-span-2">
          <label className="block mb-1.5 text-xs font-medium text-gray-700 dark:text-gray-300">
            Country
          </label>
          <div className="relative">
            <input
              type="text"
              value={filterCountry}
              onChange={(e) => setFilterCountry(e.target.value)}
              placeholder="e.g. Morocco, France, Spain, USA..."
              className="h-10 w-full rounded-lg border border-gray-200 bg-transparent px-3 py-2 text-sm text-gray-800 placeholder:text-gray-400 outline-none focus:border-brand-500 dark:border-gray-800 dark:bg-gray-900 dark:text-gray-200 dark:placeholder:text-gray-500"
            />
            {filterCountry && (
              <button
                type="button"
                onClick={() => setFilterCountry("")}
                className="absolute right-2.5 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600 dark:hover:text-gray-300"
              >
                <svg className="size-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                </svg>
              </button>
            )}
          </div>
        </div>
      </div>

      {/* Footer Actions */}
      <div className="flex items-center justify-end gap-2 pt-3 border-t border-gray-100 dark:border-white/[0.05]">
        <Button variant="outline" size="sm" onClick={onClear}>
          Clear
        </Button>
        <Button size="sm" onClick={onApply}>
          Apply Filters
        </Button>
      </div>
    </Dropdown>
  );
}
