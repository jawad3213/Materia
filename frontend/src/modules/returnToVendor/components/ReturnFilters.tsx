import FilterPanel, { FilterSelect } from "../../../shared/components/page/FilterPanel";
import {
  RESOLUTION_TYPE_LABELS,
  RETURN_STATUS_LABELS,
  type ResolutionType,
  type ReturnStatus,
} from "../types/returnToVendor.types";

export interface ReturnFilterValues {
  status: ReturnStatus | "";
  resolution: ResolutionType | "";
}

const STATUS_OPTIONS = [
  { value: "", label: "All Statuses" },
  ...(Object.keys(RETURN_STATUS_LABELS) as ReturnStatus[]).map((s) => ({ value: s, label: RETURN_STATUS_LABELS[s] })),
];

const RESOLUTION_OPTIONS = [
  { value: "", label: "All Resolutions" },
  ...(Object.keys(RESOLUTION_TYPE_LABELS) as ResolutionType[]).map((r) => ({ value: r, label: RESOLUTION_TYPE_LABELS[r] })),
];

export default function ReturnFilters({
  isOpen,
  onClose,
  value,
  onChange,
  onApply,
  onClear,
}: {
  isOpen: boolean;
  onClose: () => void;
  value: ReturnFilterValues;
  onChange: (value: ReturnFilterValues) => void;
  onApply: () => void;
  onClear: () => void;
}) {
  return (
    <FilterPanel
      title="Filter Returns"
      isOpen={isOpen}
      onClose={onClose}
      activeCount={[value.status, value.resolution].filter(Boolean).length}
      onApply={onApply}
      onClear={onClear}
    >
      <FilterSelect
        label="Status"
        value={value.status}
        onChange={(status) => onChange({ ...value, status: status as ReturnStatus | "" })}
        options={STATUS_OPTIONS}
      />
      <FilterSelect
        label="Resolution"
        value={value.resolution}
        onChange={(resolution) => onChange({ ...value, resolution: resolution as ResolutionType | "" })}
        options={RESOLUTION_OPTIONS}
      />
    </FilterPanel>
  );
}
