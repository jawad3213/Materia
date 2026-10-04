import { formatAmount } from "../../invoices/utils/invoiceLine";
import { lineOutcome, paymentTotal, type PaymentLineDraft } from "../utils/paymentLine";

/** Preview of the payment before it is saved: the total and what each selected invoice becomes. */
export default function PaymentSimulation({ lines, currency }: { lines: PaymentLineDraft[]; currency: string }) {
  const selected = lines.filter((l) => l.selected);
  const settled = selected.filter((l) => lineOutcome(l).settles).length;

  return (
    <div className="space-y-3 text-sm">
      <div>
        <p className="text-theme-xs font-medium uppercase tracking-wider text-gray-500 dark:text-gray-400">Payment Total</p>
        <p className="text-2xl font-bold text-gray-900 dark:text-white">{formatAmount(paymentTotal(lines), currency)}</p>
      </div>
      {selected.length === 0 ? (
        <p className="text-gray-400">No invoice selected.</p>
      ) : (
        <>
          <p className="text-gray-600 dark:text-gray-400">
            {selected.length} invoice(s): {settled} settled, {selected.length - settled} partly paid.
          </p>
          <ul className="divide-y divide-gray-100 dark:divide-white/[0.05]">
            {selected.map((l) => {
              const outcome = lineOutcome(l);
              return (
                <li key={l.invoice.id} className="flex justify-between gap-2 py-2">
                  <span className="font-mono text-gray-800 dark:text-white/90">{l.invoice.invoiceCode}</span>
                  <span className="text-right">
                    <span className="block font-medium text-gray-800 dark:text-white/90">{formatAmount(l.amount, currency)}</span>
                    <span className="text-theme-xs text-gray-500 dark:text-gray-400">
                      {outcome.settles ? "Settled" : `${formatAmount(outcome.remaining, currency)} left`}
                    </span>
                  </span>
                </li>
              );
            })}
          </ul>
        </>
      )}
    </div>
  );
}
