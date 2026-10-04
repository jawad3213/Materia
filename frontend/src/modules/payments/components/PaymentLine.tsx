import { formatAmount, isOverdue } from "../../invoices/utils/invoiceLine";
import { lineError, lineOutcome, type PaymentLineDraft } from "../utils/paymentLine";
import Label from "../../../shared/components/form/Label";
import Input from "../../../shared/components/form/input/InputField";
import Checkbox from "../../../shared/components/form/input/Checkbox";

/** One invoice the payment may cover: tick it, then adjust the amount (a partial payment is allowed). */
export default function PaymentLine({ line, onChange }: { line: PaymentLineDraft; onChange: (line: PaymentLineDraft) => void }) {
  const { invoice } = line;
  const error = lineError(line);
  const outcome = lineOutcome(line);
  const overdue = isOverdue(invoice);

  return (
    <div
      className={`space-y-4 rounded-xl border p-4 ${
        line.selected ? "border-brand-300 bg-brand-25 dark:border-brand-500/40 dark:bg-brand-500/[0.04]" : "border-gray-200 dark:border-white/[0.05]"
      }`}
    >
      <div className="flex flex-wrap items-start justify-between gap-3">
        <div className="flex items-start gap-3">
          <Checkbox checked={line.selected} onChange={(checked) => onChange({ ...line, selected: checked })} />
          <div>
            <p className="text-sm font-medium text-gray-800 dark:text-white/90">
              <span className="font-mono text-brand-500">{invoice.invoiceCode}</span>
              {invoice.externalReference && <span className="ml-2 text-gray-500 dark:text-gray-400">Ref. {invoice.externalReference}</span>}
            </p>
            <p className={`text-theme-xs ${overdue ? "font-semibold text-error-500" : "text-gray-500 dark:text-gray-400"}`}>
              {invoice.dueDate ? `Due ${invoice.dueDate}` : "No due date"}
              {overdue && " — overdue"}
            </p>
          </div>
        </div>
        <div className="text-right text-theme-xs text-gray-500 dark:text-gray-400">
          <p>Left to pay: {formatAmount(line.outstanding, invoice.currencyCode)}</p>
          {line.reserved > 0 && (
            <p className="text-warning-600 dark:text-orange-400">
              Set aside by another payment: {formatAmount(line.reserved, invoice.currencyCode)}
            </p>
          )}
        </div>
      </div>

      <div className="grid grid-cols-1 items-end gap-4 sm:grid-cols-2">
        <div>
          <Label>Amount to Pay</Label>
          <Input
            type="number"
            min="0.01"
            max={String(line.available)}
            step={0.01}
            disabled={!line.selected}
            value={line.amount}
            onChange={(e) => onChange({ ...line, amount: e.target.value === "" ? 0 : Number(e.target.value) })}
          />
        </div>
        {line.selected && !error && (
          <p className="pb-3 text-theme-xs text-gray-600 dark:text-gray-400">
            {outcome.settles
              ? "The invoice will be fully settled."
              : `Partial payment: ${formatAmount(outcome.remaining, invoice.currencyCode)} will remain.`}
          </p>
        )}
      </div>
      {error && <p className="text-theme-xs text-error-500">{error}</p>}
    </div>
  );
}
