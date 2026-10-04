import { lineError, lineTotal, lineWarnings, formatAmount, type InvoiceLineDraft } from "../utils/invoiceLine";

const INPUT =
  "w-24 rounded-lg border border-gray-200 dark:border-white/[0.1] bg-white dark:bg-gray-800 px-2 py-1.5 text-xs text-gray-800 dark:text-white focus:border-brand-500 focus:outline-none";

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
    <div className="p-4 rounded-xl border border-gray-200 dark:border-white/[0.07] space-y-3">
      <div className="flex flex-wrap items-baseline justify-between gap-2">
        <div>
          <span className="font-mono text-xs font-semibold text-gray-900 dark:text-white">{line.materialCode}</span>
          <span className="ml-2 text-xs text-gray-500">{line.materialName}</span>
        </div>
        <p className="text-[11px] text-gray-500">
          Commandé : {line.ordered} • Reçu accepté : {line.received} • Déjà facturé : {line.alreadyInvoiced}{" "}
          {line.unitOfMeasure}
        </p>
      </div>

      <div className="flex flex-wrap items-end gap-4">
        <label className="text-[11px] font-semibold text-gray-600 dark:text-gray-400 space-y-1">
          <span className="block">Quantité facturée</span>
          <input
            type="number"
            min={0}
            step={1}
            value={line.invoiced}
            onChange={(e) => onChange({ ...line, invoiced: toNumber(e.target.value) })}
            className={INPUT}
          />
        </label>
        <label className="text-[11px] font-semibold text-gray-600 dark:text-gray-400 space-y-1">
          <span className="block">Prix unitaire HT</span>
          <input
            type="number"
            min={0}
            step="0.01"
            value={line.unitPrice}
            onChange={(e) => onChange({ ...line, unitPrice: toNumber(e.target.value) })}
            className={INPUT}
          />
        </label>
        <label className="text-[11px] font-semibold text-gray-600 dark:text-gray-400 space-y-1">
          <span className="block">Montant taxe</span>
          <input
            type="number"
            min={0}
            step="0.01"
            value={line.taxAmount}
            onChange={(e) => onChange({ ...line, taxAmount: toNumber(e.target.value) })}
            className={INPUT}
          />
        </label>
        <div className="text-xs text-gray-700 dark:text-gray-300 pb-1.5">
          Total HT : <span className="font-semibold">{formatAmount(lineTotal(line), currency)}</span>
        </div>
      </div>

      {error && <p className="text-[11px] text-red-600 dark:text-red-400">{error}</p>}
      {warnings.map((w) => (
        <p key={w} className="text-[11px] text-amber-700 dark:text-amber-400">
          ⚠ {w}
        </p>
      ))}
    </div>
  );
}
