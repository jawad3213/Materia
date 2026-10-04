import { lineError, lineTotal, lineWarnings, formatAmount, type InvoiceLineDraft } from "../utils/invoiceLine";
import Label from "../../../shared/components/form/Label";
import Input from "../../../shared/components/form/input/InputField";

const toNumber = (value: string) => (value === "" ? 0 : Number(value));

/** One editable invoice line, showing ordered, received and already-invoiced quantities beside it. */
export default function InvoiceLine({
  line,
  currency,
  onChange,
}: {
  line: InvoiceLineDraft;
  currency: string;
  onChange: (line: InvoiceLineDraft) => void;
}) {
  const error = lineError(line);
  const warnings = lineWarnings(line);

  return (
    <div className="space-y-4 rounded-xl border border-gray-200 p-4 dark:border-white/[0.05]">
      <div className="flex flex-wrap items-baseline justify-between gap-2">
        <p className="text-sm font-medium text-gray-800 dark:text-white/90">
          <span className="font-mono text-brand-500">{line.materialCode}</span>
          <span className="ml-2 text-gray-500 dark:text-gray-400">{line.materialName}</span>
        </p>
        <p className="text-theme-xs text-gray-500 dark:text-gray-400">
          Ordered: {line.ordered} • Accepted: {line.received} • Already invoiced: {line.alreadyInvoiced} {line.unitOfMeasure}
        </p>
      </div>

      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <div>
          <Label>Quantity Invoiced</Label>
          <Input type="number" min="0" step={1} value={line.invoiced} onChange={(e) => onChange({ ...line, invoiced: toNumber(e.target.value) })} />
        </div>
        <div>
          <Label>Unit Price excl. Tax</Label>
          <Input type="number" min="0" step={0.01} value={line.unitPrice} onChange={(e) => onChange({ ...line, unitPrice: toNumber(e.target.value) })} />
        </div>
        <div>
          <Label>Tax Amount</Label>
          <Input type="number" min="0" step={0.01} value={line.taxAmount} onChange={(e) => onChange({ ...line, taxAmount: toNumber(e.target.value) })} />
        </div>
        <div>
          <Label>Line Total excl. Tax</Label>
          <p className="flex h-11 items-center text-sm font-semibold text-gray-800 dark:text-white/90">{formatAmount(lineTotal(line), currency)}</p>
        </div>
      </div>

      {error && <p className="text-theme-xs text-error-500">{error}</p>}
      {warnings.map((w) => (
        <p key={w} className="text-theme-xs text-warning-600 dark:text-orange-400">
          ⚠ {w}
        </p>
      ))}
    </div>
  );
}
