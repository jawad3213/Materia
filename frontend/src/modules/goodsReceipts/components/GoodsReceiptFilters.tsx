import FilterPanel, { FilterSelect } from "../../../shared/components/page/FilterPanel";
import { RECEIPT_STATUS_LABELS, type ReceiptStatus } from "../types/goodsReceipt.types";

export interface GoodsReceiptFilterValues {
  status: ReceiptStatus | "";
}

const STATUS_OPTIONS = [
  { value: "", label: "All Statuses" },
  ...(Object.keys(RECEIPT_STATUS_LABELS) as ReceiptStatus[]).map((s) => ({ value: s, label: RECEIPT_STATUS_LABELS[s] })),
];

export default function GoodsReceiptFilters({
  isOpen,
  onClose,
  value,
  onChange,
  onApply,
  onClear,
}: {
  isOpen: boolean;
  onClose: () => void;
  value: GoodsReceiptFilterValues;
  onChange: (value: GoodsReceiptFilterValues) => void;
  onApply: () => void;
  onClear: () => void;
}) {
  return (
    <FilterPanel
      title="Filter Goods Receipts"
      isOpen={isOpen}
      onClose={onClose}
      activeCount={value.status ? 1 : 0}
      onApply={onApply}
      onClear={onClear}
    >
      <FilterSelect
        label="Status"
        value={value.status}
        onChange={(status) => onChange({ ...value, status: status as ReceiptStatus | "" })}
        options={STATUS_OPTIONS}
        className="sm:col-span-2"
      />
    </FilterPanel>
  );
}
