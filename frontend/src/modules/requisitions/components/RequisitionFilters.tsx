import FilterPanel, { FilterSelect } from "../../../shared/components/page/FilterPanel";
import Label from "../../../shared/components/form/Label";
import Input from "../../../shared/components/form/input/InputField";
import { REQUISITION_STATUS_INFO, type RequisitionStatus } from "../types/RequisitionStatus";

export interface RequisitionFilterValues {
  status: RequisitionStatus | "";
  requester: string;
  neededFrom: string;
  neededTo: string;
}

const STATUS_OPTIONS = [
  { value: "", label: "All Statuses" },
  ...(Object.keys(REQUISITION_STATUS_INFO) as RequisitionStatus[]).map((s) => ({ value: s, label: REQUISITION_STATUS_INFO[s].label })),
];

export default function RequisitionFilters({
  isOpen,
  onClose,
  value,
  onChange,
  onApply,
  onClear,
}: {
  isOpen: boolean;
  onClose: () => void;
  value: RequisitionFilterValues;
  onChange: (value: RequisitionFilterValues) => void;
  onApply: () => void;
  onClear: () => void;
}) {
  return (
    <FilterPanel
      title="Filter Requisitions"
      isOpen={isOpen}
      onClose={onClose}
      activeCount={[value.status, value.requester, value.neededFrom, value.neededTo].filter(Boolean).length}
      onApply={onApply}
      onClear={onClear}
    >
      <FilterSelect
        label="Status"
        value={value.status}
        onChange={(status) => onChange({ ...value, status: status as RequisitionStatus | "" })}
        options={STATUS_OPTIONS}
      />
      <div>
        <Label>Requester</Label>
        <Input value={value.requester} placeholder="Name" onChange={(e) => onChange({ ...value, requester: e.target.value })} />
      </div>
      <div className="grid grid-cols-2 gap-3">
        <div>
          <Label>Needed From</Label>
          <Input type="date" value={value.neededFrom} onChange={(e) => onChange({ ...value, neededFrom: e.target.value })} />
        </div>
        <div>
          <Label>Needed To</Label>
          <Input type="date" value={value.neededTo} min={value.neededFrom} onChange={(e) => onChange({ ...value, neededTo: e.target.value })} />
        </div>
      </div>
    </FilterPanel>
  );
}
