import React, { useState, useRef, useEffect } from "react";

export interface Option {
  value: string;
  label: string;
}

export interface SelectProps {
  id?: string;
  options: Option[];
  placeholder?: string;
  onChange: (value: string) => void;
  className?: string;
  defaultValue?: string;
  value?: string;
  disabled?: boolean;
  error?: boolean;
}

const Select: React.FC<SelectProps> = ({
  id,
  options,
  placeholder = "Select an option",
  onChange,
  className = "",
  defaultValue = "",
  value,
  disabled = false,
  error = false,
}) => {
  const [isOpen, setIsOpen] = useState(false);
  const [internalValue, setInternalValue] = useState<string>(defaultValue);
  const selectRef = useRef<HTMLDivElement>(null);

  const currentVal = value !== undefined ? value : internalValue;
  const selectedOption = options.find((opt) => opt.value === currentVal);

  // Click outside and Escape key to collapse
  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (selectRef.current && !selectRef.current.contains(event.target as Node)) {
        setIsOpen(false);
      }
    };

    const handleKeyDown = (event: KeyboardEvent) => {
      if (event.key === "Escape") {
        setIsOpen(false);
      }
    };

    if (isOpen) {
      document.addEventListener("mousedown", handleClickOutside);
      document.addEventListener("keydown", handleKeyDown);
    }

    return () => {
      document.removeEventListener("mousedown", handleClickOutside);
      document.removeEventListener("keydown", handleKeyDown);
    };
  }, [isOpen]);

  const handleSelect = (optionValue: string) => {
    if (disabled) return;
    if (value === undefined) {
      setInternalValue(optionValue);
    }
    onChange(optionValue);
    setIsOpen(false);
  };

  const toggleDropdown = () => {
    if (!disabled) {
      setIsOpen((prev) => !prev);
    }
  };

  return (
    <div className={`relative w-full ${className}`} ref={selectRef}>
      {/* Hidden input for form/accessibility support */}
      {id && <input type="hidden" id={id} name={id} value={currentVal} />}

      {/* Trigger Button */}
      <button
        type="button"
        disabled={disabled}
        onClick={toggleDropdown}
        aria-haspopup="listbox"
        aria-expanded={isOpen}
        className={`flex h-11 w-full items-center justify-between rounded-lg border px-4 py-2.5 text-left text-sm shadow-theme-xs transition-all focus:outline-hidden ${
          disabled
            ? "cursor-not-allowed border-gray-200 bg-gray-100 opacity-60 dark:border-gray-800 dark:bg-gray-800/60 text-gray-400 dark:text-gray-500"
            : error
            ? "cursor-pointer border-error-500 bg-transparent text-gray-800 focus:ring-3 focus:ring-error-500/20 dark:border-error-500 dark:text-white"
            : isOpen
            ? "cursor-pointer border-brand-500 bg-white ring-3 ring-brand-500/10 dark:border-brand-500 dark:bg-gray-900 text-gray-800 dark:text-white"
            : "cursor-pointer border-gray-300 bg-transparent hover:border-gray-400 focus:border-brand-300 focus:ring-3 focus:ring-brand-500/10 dark:border-gray-700 dark:bg-gray-900 dark:text-white/90 dark:hover:border-gray-600"
        }`}
      >
        <span
          className={`block truncate ${
            selectedOption
              ? "text-gray-800 dark:text-white/90 font-medium"
              : "text-gray-400 dark:text-gray-500"
          }`}
        >
          {selectedOption ? selectedOption.label : placeholder}
        </span>

        {/* Visible Chevron Arrow */}
        <span
          className={`pointer-events-none ml-2 flex shrink-0 items-center text-gray-400 transition-transform duration-200 dark:text-gray-500 ${
            isOpen ? "rotate-180 text-brand-500 dark:text-brand-400" : ""
          }`}
        >
          <svg
            className="h-4 w-4"
            fill="none"
            viewBox="0 0 24 24"
            stroke="currentColor"
            strokeWidth={2}
          >
            <path strokeLinecap="round" strokeLinejoin="round" d="M19 9l-7 7-7-7" />
          </svg>
        </span>
      </button>

      {/* Collapsible / Expandable Menu */}
      {isOpen && (
        <div
          role="listbox"
          className="absolute left-0 top-full z-50 mt-1.5 max-h-60 w-full overflow-auto rounded-xl border border-gray-200 bg-white p-1.5 shadow-xl transition-all dark:border-gray-800 dark:bg-gray-900"
        >
          {options.length === 0 ? (
            <div className="px-3 py-2 text-center text-xs text-gray-400">
              No options available
            </div>
          ) : (
            options.map((option) => {
              const isSelected = option.value === currentVal;
              return (
                <div
                  key={option.value}
                  role="option"
                  aria-selected={isSelected}
                  onClick={() => handleSelect(option.value)}
                  className={`flex cursor-pointer items-center justify-between rounded-lg px-3 py-2.5 text-sm transition-colors ${
                    isSelected
                      ? "bg-brand-50 font-semibold text-brand-600 dark:bg-brand-500/15 dark:text-brand-400"
                      : "text-gray-700 hover:bg-gray-100/80 dark:text-gray-300 dark:hover:bg-white/[0.05]"
                  }`}
                >
                  <span className="truncate">{option.label}</span>
                  {isSelected && (
                    <svg
                      className="ml-2 h-4 w-4 shrink-0 text-brand-600 dark:text-brand-400"
                      fill="none"
                      viewBox="0 0 24 24"
                      stroke="currentColor"
                      strokeWidth={2.5}
                    >
                      <path strokeLinecap="round" strokeLinejoin="round" d="M5 13l4 4L19 7" />
                    </svg>
                  )}
                </div>
              );
            })
          )}
        </div>
      )}
    </div>
  );
};

export default Select;
