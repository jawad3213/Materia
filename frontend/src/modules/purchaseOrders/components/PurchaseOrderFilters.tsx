import FilterPanel, { FilterSelect } from "../../../shared/components/page/FilterPanel";
import Label from "../../../shared/components/form/Label";
import Input from "../../../shared/components/form/input/InputField";
import { ORDER_STATUS_INFO, type OrderStatus } from "../types/OrderStatus";
import { DELIVERY_STATUS_INFO, type DeliveryStatus } from "../types/DeliveryStatus";

export interface PurchaseOrderFilterValues {
  status: OrderStatus | "";
  deliveryStatus: DeliveryStatus | "";
  dateFrom: string;
  dateTo: string;
}

const STATUS_OPTIONS = [
  { value: "", label: "All Statuses" },
  ...(Object.keys(ORDER_STATUS_INFO) as OrderStatus[]).map((s) => ({ value: s, label: ORDER_STATUS_INFO[s].label })),
];

const DELIVERY_OPTIONS = [
  { value: "", label: "All Deliveries" },
  ...(Object.keys(DELIVERY_STATUS_INFO) as DeliveryStatus[]).map((s) => ({ value: s, label: DELIVERY_STATUS_INFO[s].label })),
];

export default function PurchaseOrderFilters({
  isOpen,
  onClose,
  value,
  onChange,
  onApply,
  onClear,
}: {
  isOpen: boolean;
  onClose: () => void;
  value: PurchaseOrderFilterValues;
  onChange: (value: PurchaseOrderFilterValues) => void;
  onApply: () => void;
  onClear: () => void;
}) {
  return (
    <FilterPanel
      title="Filter Orders"
      isOpen={isOpen}
      onClose={onClose}
      activeCount={[value.status, value.deliveryStatus, value.dateFrom, value.dateTo].filter(Boolean).length}
      onApply={onApply}
      onClear={onClear}
    >
      <FilterSelect
        label="Status"
        value={value.status}
        onChange={(status) => onChange({ ...value, status: status as OrderStatus | "" })}
        options={STATUS_OPTIONS}
      />
      <FilterSelect
        label="Delivery"
        value={value.deliveryStatus}
        onChange={(deliveryStatus) => onChange({ ...value, deliveryStatus: deliveryStatus as DeliveryStatus | "" })}
        options={DELIVERY_OPTIONS}
      />
      <div className="grid grid-cols-2 gap-3">
        <div>
          <Label>Ordered From</Label>
          <Input type="date" value={value.dateFrom} onChange={(e) => onChange({ ...value, dateFrom: e.target.value })} />
        </div>
        <div>
          <Label>Ordered To</Label>
          <Input type="date" value={value.dateTo} min={value.dateFrom} onChange={(e) => onChange({ ...value, dateTo: e.target.value })} />
        </div>
      </div>
    </FilterPanel>
  );
}
