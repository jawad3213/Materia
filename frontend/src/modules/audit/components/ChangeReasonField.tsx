import { useId } from "react";

export const CHANGE_REASON_MAX = 500;

/**
 * Asks why a record is being changed. The text is sent as X-Change-Reason (withChangeReason) and shown in the
 * record's History and the audit log.
 */
export default function ChangeReasonField({
  value,
  onChange,
  className = "",
}: {
  value: string;
  onChange: (value: string) => void;
  className?: string;
}) {
  const id = useId();
  return (
    <div className={className}>
      <label htmlFor={id} className="mb-1.5 block text-sm font-medium text-gray-700 dark:text-gray-400">
        Reason for this change <span className="font-normal text-gray-400">(recorded in the history)</span>
      </label>
      <textarea
        id={id}
        rows={2}
        maxLength={CHANGE_REASON_MAX}
        value={value}
        onChange={(e) => onChange(e.target.value)}
        placeholder="e.g. Price updated after the supplier's new quote"
        className="w-full rounded-lg border border-gray-300 bg-transparent px-4 py-2.5 text-sm text-gray-800 placeholder:text-gray-400 focus:border-brand-300 focus:outline-none focus:ring-3 focus:ring-brand-500/10 dark:border-gray-700 dark:bg-gray-900 dark:text-white/90"
      />
      <p className="mt-1 text-right text-theme-xs text-gray-400">
        {value.length}/{CHANGE_REASON_MAX}
      </p>
    </div>
  );
}
