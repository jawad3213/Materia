import { RECEIPT_STATUS_LABELS, type ReceiptStatus } from "../types/goodsReceipt.types";

export interface GoodsReceiptFilterValues {
  keyword: string;
  status: ReceiptStatus | "";
}

interface GoodsReceiptFiltersProps {
  value: GoodsReceiptFilterValues;
  onChange: (value: GoodsReceiptFilterValues) => void;
}

const INPUT =
  "rounded-xl border border-gray-200 dark:border-white/[0.1] bg-white dark:bg-gray-800 px-3 py-2 text-xs text-gray-800 dark:text-white focus:border-brand-500 focus:outline-none";

export default function GoodsReceiptFilters({ value, onChange }: GoodsReceiptFiltersProps) {
  return (
    <div className="flex flex-col sm:flex-row gap-3">
      <input
        type="search"
        placeholder="Rechercher (réception, commande, fournisseur)..."
        value={value.keyword}
        onChange={(e) => onChange({ ...value, keyword: e.target.value })}
        className={`${INPUT} flex-1`}
      />
      <select
        value={value.status}
        onChange={(e) => onChange({ ...value, status: e.target.value as ReceiptStatus | "" })}
        className={INPUT}
      >
        <option value="">Tous les statuts</option>
        {(Object.keys(RECEIPT_STATUS_LABELS) as ReceiptStatus[]).map((status) => (
          <option key={status} value={status}>
            {RECEIPT_STATUS_LABELS[status]}
          </option>
        ))}
      </select>
    </div>
  );
}
