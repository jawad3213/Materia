import { INVOICE_STATUS_LABELS, type InvoiceStatus } from "../types/invoice.types";

export interface InvoiceFilterValues {
  keyword: string;
  status: InvoiceStatus | "";
  overdueOnly: boolean;
}

const FIELD =
  "rounded-xl border border-gray-200 dark:border-white/[0.1] bg-white dark:bg-gray-800 px-3 py-2 text-xs text-gray-800 dark:text-white focus:border-brand-500 focus:outline-none";

export default function InvoiceFilters({
  value,
  onChange,
}: {
  value: InvoiceFilterValues;
  onChange: (value: InvoiceFilterValues) => void;
}) {
  return (
    <div className="flex flex-col sm:flex-row gap-3 bg-white dark:bg-gray-900 p-4 rounded-2xl border border-gray-200 dark:border-white/[0.07] shadow-sm">
      <input
        type="search"
        placeholder="Rechercher par facture, référence fournisseur, commande ou fournisseur..."
        value={value.keyword}
        onChange={(e) => onChange({ ...value, keyword: e.target.value })}
        className={`${FIELD} flex-1`}
      />
      <select
        value={value.status}
        onChange={(e) => onChange({ ...value, status: e.target.value as InvoiceStatus | "" })}
        className={FIELD}
        aria-label="Statut"
      >
        <option value="">Tous les statuts</option>
        {(Object.keys(INVOICE_STATUS_LABELS) as InvoiceStatus[]).map((status) => (
          <option key={status} value={status}>
            {INVOICE_STATUS_LABELS[status]}
          </option>
        ))}
      </select>
      <label className="flex items-center gap-2 text-xs text-gray-700 dark:text-gray-300 whitespace-nowrap">
        <input
          type="checkbox"
          checked={value.overdueOnly}
          onChange={(e) => onChange({ ...value, overdueOnly: e.target.checked })}
        />
        En retard uniquement
      </label>
    </div>
  );
}
