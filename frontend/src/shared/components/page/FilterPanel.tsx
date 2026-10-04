import type { ReactNode } from "react";
import { Dropdown } from "../ui/dropdown/Dropdown";
import Button from "../ui/button/Button";

/** The filter dropdown of the suppliers and materials lists: title, reset, fields, Clear / Apply. */
interface FilterPanelProps {
  title: string;
  isOpen: boolean;
  onClose: () => void;
  activeCount: number;
  onApply: () => void;
  onClear: () => void;
  children: ReactNode;
}

export default function FilterPanel({ title, isOpen, onClose, activeCount, onApply, onClear, children }: FilterPanelProps) {
  return (
    <Dropdown isOpen={isOpen} onClose={onClose} className="right-0 top-full z-50 mt-2 w-[320px] p-5 sm:w-[480px]">
      <div className="mb-4 flex items-center justify-between border-b border-gray-100 pb-3 dark:border-white/[0.05]">
        <h4 className="text-sm font-semibold text-gray-800 dark:text-white/90">{title}</h4>
        {activeCount > 0 && (
          <button onClick={onClear} className="text-xs font-medium text-brand-500 transition-colors hover:text-brand-600">
            Reset all
          </button>
        )}
      </div>
      <div className="mb-5 grid grid-cols-1 gap-4 sm:grid-cols-2">{children}</div>
      <div className="flex items-center justify-end gap-2 border-t border-gray-100 pt-3 dark:border-white/[0.05]">
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

/** A select field styled like the supplier filters. */
export function FilterSelect({
  label,
  value,
  onChange,
  options,
  className = "",
}: {
  label: string;
  value: string;
  onChange: (value: string) => void;
  options: { value: string; label: string }[];
  className?: string;
}) {
  return (
    <div className={className}>
      <label className="mb-1.5 block text-xs font-medium text-gray-700 dark:text-gray-300">{label}</label>
      <div className="relative">
        <select
          value={value}
          onChange={(e) => onChange(e.target.value)}
          className="h-10 w-full appearance-none rounded-lg border border-gray-200 bg-transparent px-3 py-2 text-sm text-gray-800 outline-none focus:border-brand-500 dark:border-gray-800 dark:bg-gray-900 dark:text-gray-200"
        >
          {options.map((opt) => (
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
  );
}
