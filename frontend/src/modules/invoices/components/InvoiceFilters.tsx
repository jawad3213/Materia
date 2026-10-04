import FilterPanel, { FilterSelect } from "../../../shared/components/page/FilterPanel";
import { INVOICE_STATUS_LABELS, INVOICE_TYPE_LABELS, type InvoiceStatus, type InvoiceType } from "../types/invoice.types";

export interface InvoiceFilterValues {
  status: InvoiceStatus | "";
  type: InvoiceType | "";
}

const STATUS_OPTIONS = [
  { value: "", label: "All Statuses" },
  ...(Object.keys(INVOICE_STATUS_LABELS) as InvoiceStatus[]).map((s) => ({ value: s, label: INVOICE_STATUS_LABELS[s] })),
];

const TYPE_OPTIONS = [
  { value: "", label: "All Types" },
  ...(Object.keys(INVOICE_TYPE_LABELS) as InvoiceType[]).map((t) => ({ value: t, label: INVOICE_TYPE_LABELS[t] })),
];

export default function InvoiceFilters({
  isOpen,
  onClose,
  value,
  onChange,
  onApply,
  onClear,
}: {
  isOpen: boolean;
  onClose: () => void;
  value: InvoiceFilterValues;
  onChange: (value: InvoiceFilterValues) => void;
  onApply: () => void;
  onClear: () => void;
}) {
  return (
    <FilterPanel
      title="Filter Invoices"
      isOpen={isOpen}
      onClose={onClose}
      activeCount={[value.status, value.type].filter(Boolean).length}
      onApply={onApply}
      onClear={onClear}
    >
      <FilterSelect
        label="Status"
        value={value.status}
        onChange={(status) => onChange({ ...value, status: status as InvoiceStatus | "" })}
        options={STATUS_OPTIONS}
      />
      <FilterSelect
        label="Type"
        value={value.type}
        onChange={(type) => onChange({ ...value, type: type as InvoiceType | "" })}
        options={TYPE_OPTIONS}
      />
    </FilterPanel>
  );
}
