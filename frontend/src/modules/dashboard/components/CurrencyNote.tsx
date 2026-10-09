import type { CurrencyConversion } from "../types/dashboard.types";

function formatRate(rate: number) {
  return rate.toLocaleString("en-GB", { minimumFractionDigits: 2, maximumFractionDigits: 4 });
}

function formatDate(value?: string | null) {
  if (!value) return "";
  const date = new Date(value);
  return Number.isNaN(date.getTime()) ? value : date.toLocaleDateString("en-GB", { day: "numeric", month: "short", year: "numeric" });
}

/**
 * How amounts in other currencies were brought into the reporting currency: one line per currency with the latest
 * rate, plus a warning when fallback rates are in use or documents had to be left out.
 */
export default function CurrencyNote({ conversion }: { conversion: CurrencyConversion }) {
  const leftOut = conversion.currencies.some((c) => c.latestRate == null);
  const warn = conversion.fallbackRates || leftOut;

  return (
    <div
      role="note"
      className={`mb-6 rounded-xl border px-4 py-3 text-sm ${
        warn
          ? "border-warning-200 bg-warning-50 text-warning-800 dark:border-warning-500/30 dark:bg-warning-500/10 dark:text-warning-300"
          : "border-gray-200 bg-white text-gray-600 dark:border-gray-800 dark:bg-white/[0.03] dark:text-gray-300"
      }`}
    >
      <p>{conversion.note}</p>
      {conversion.currencies.length > 0 && (
        <ul className="mt-2 flex flex-wrap gap-2">
          {conversion.currencies.map((c) => (
            <li
              key={c.currency}
              className="rounded-lg bg-black/[0.04] px-2.5 py-1 text-xs dark:bg-white/[0.06]"
              title={`${c.valuedAtDocumentDate} of ${c.documents} document(s) carry the rate of their own date`}
            >
              <span className="font-semibold">{c.currency}</span>
              {c.latestRate != null ? (
                <>
                  {" "}
                  1 {c.currency} = {formatRate(c.latestRate)} {conversion.reportingCurrency}
                  {c.latestRateDate && ` · ${formatDate(c.latestRateDate)}`}
                  {c.latestRateSource === "FALLBACK" && " · fallback"}
                </>
              ) : (
                " · no rate"
              )}
              <span className="text-gray-400"> · {c.documents} doc(s)</span>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
