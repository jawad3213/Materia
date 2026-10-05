import { useState } from "react";

export interface ChartTabOption<T extends string> {
  value: T;
  label: string;
}

interface ChartTabProps<T extends string> {
  options?: ChartTabOption<T>[];
  /** Controlled value; the tab keeps its own selection when omitted. */
  value?: T;
  onChange?: (value: T) => void;
}

const DEFAULT_OPTIONS = [
  { value: "optionOne", label: "Monthly" },
  { value: "optionTwo", label: "Quarterly" },
  { value: "optionThree", label: "Annually" },
];

/** Segmented switch above a chart (TailAdmin "Chart Tab"). */
function ChartTab<T extends string>({ options, value, onChange }: ChartTabProps<T>) {
  const items = (options ?? DEFAULT_OPTIONS) as ChartTabOption<T>[];
  const [internal, setInternal] = useState<T>(items[0].value);
  const selected = value ?? internal;

  const select = (next: T) => {
    setInternal(next);
    onChange?.(next);
  };

  return (
    <div className="flex items-center gap-0.5 rounded-lg bg-gray-100 p-0.5 dark:bg-gray-900">
      {items.map((item) => (
        <button
          key={item.value}
          type="button"
          onClick={() => select(item.value)}
          className={`w-full rounded-md px-3 py-2 text-theme-sm font-medium hover:text-gray-900 dark:hover:text-white ${
            selected === item.value ? "bg-white text-gray-900 shadow-theme-xs dark:bg-gray-800 dark:text-white" : "text-gray-500 dark:text-gray-400"
          }`}
        >
          {item.label}
        </button>
      ))}
    </div>
  );
}

export default ChartTab;
