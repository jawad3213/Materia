import FilterPanel, { FilterSelect } from "../../../shared/components/page/FilterPanel";
import { PAYMENT_METHOD_LABELS, PAYMENT_STATUS_LABELS, type PaymentMethod, type PaymentStatus } from "../types/payment.types";

export interface PaymentFilterValues {
  status: PaymentStatus | "";
  method: PaymentMethod | "";
}

const STATUS_OPTIONS = [
  { value: "", label: "All Statuses" },
  ...(Object.keys(PAYMENT_STATUS_LABELS) as PaymentStatus[]).map((s) => ({ value: s, label: PAYMENT_STATUS_LABELS[s] })),
];

const METHOD_OPTIONS = [
  { value: "", label: "All Methods" },
  ...(Object.keys(PAYMENT_METHOD_LABELS) as PaymentMethod[]).map((m) => ({ value: m, label: PAYMENT_METHOD_LABELS[m] })),
];

export default function PaymentFilters({
  isOpen,
  onClose,
  value,
  onChange,
  onApply,
  onClear,
}: {
  isOpen: boolean;
  onClose: () => void;
  value: PaymentFilterValues;
  onChange: (value: PaymentFilterValues) => void;
  onApply: () => void;
  onClear: () => void;
}) {
  return (
    <FilterPanel
      title="Filter Payments"
      isOpen={isOpen}
      onClose={onClose}
      activeCount={[value.status, value.method].filter(Boolean).length}
      onApply={onApply}
      onClear={onClear}
    >
      <FilterSelect
        label="Status"
        value={value.status}
        onChange={(status) => onChange({ ...value, status: status as PaymentStatus | "" })}
        options={STATUS_OPTIONS}
      />
      <FilterSelect
        label="Method"
        value={value.method}
        onChange={(method) => onChange({ ...value, method: method as PaymentMethod | "" })}
        options={METHOD_OPTIONS}
      />
    </FilterPanel>
  );
}
