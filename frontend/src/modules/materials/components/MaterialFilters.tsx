import React from "react";
import { Dropdown } from "../../../shared/components/ui/dropdown/Dropdown";
import Button from "../../../shared/components/ui/button/Button";
import CustomSelect from "./CustomSelect";
import CategoryTreeSelect from "../../categories/components/CategoryTreeSelect";
import { MaterialStatus, MaterialType } from "../enums";

interface MaterialFiltersProps {
  isOpen: boolean;
  onClose: () => void;
  filterCategoryId: string;
  setFilterCategoryId: (val: string) => void;
  filterMaterialType: string;
  setFilterMaterialType: (val: string) => void;
  filterStatus: string;
  setFilterStatus: (val: string) => void;
  onApply: () => void;
  onClear: () => void;
}

export default function MaterialFilters({
  isOpen,
  onClose,
  filterCategoryId,
  setFilterCategoryId,
  filterMaterialType,
  setFilterMaterialType,
  filterStatus,
  setFilterStatus,
  onApply,
  onClear,
}: MaterialFiltersProps) {
  const activeCount = [filterCategoryId, filterMaterialType, filterStatus].filter(Boolean).length;

  return (
    <Dropdown isOpen={isOpen} onClose={onClose} className="w-[320px] sm:w-[500px] p-5 top-full right-0 mt-2 z-50">
      <div className="flex items-center justify-between pb-3 mb-4 border-b border-gray-100 dark:border-white/[0.05]">
        <h4 className="text-sm font-semibold text-gray-800 dark:text-white/90">
          Filter Materials
        </h4>
        {activeCount > 0 && (
          <button
            type="button"
            onClick={onClear}
            className="text-xs font-medium text-brand-500 hover:text-brand-600 transition-colors"
          >
            Reset all
          </button>
        )}
      </div>

      <div className="space-y-4 mb-5">
        <div>
          <label className="block mb-1.5 text-xs font-medium text-gray-700 dark:text-gray-300">
            Category
          </label>
          <CategoryTreeSelect
            value={filterCategoryId}
            onChange={setFilterCategoryId}
            placeholder="Select Category"
            maxHeightClass="max-h-[200px]"
          />
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <div>
            <label className="block mb-1.5 text-xs font-medium text-gray-700 dark:text-gray-300">
              Material Type
            </label>
            <CustomSelect
              value={filterMaterialType}
              onChange={setFilterMaterialType}
              placeholder="All Types"
              options={[
                { value: "", label: "All Types" },
                ...Object.entries(MaterialType).map(([key, val]) => ({
                  value: val,
                  label: key.replace(/_/g, " "),
                })),
              ]}
            />
          </div>

          <div>
            <label className="block mb-1.5 text-xs font-medium text-gray-700 dark:text-gray-300">
              Status
            </label>
            <CustomSelect
              value={filterStatus}
              onChange={setFilterStatus}
              placeholder="All Statuses"
              options={[
                { value: "", label: "All Statuses" },
                ...Object.entries(MaterialStatus).map(([key, val]) => ({
                  value: val,
                  label: key.replace(/_/g, " "),
                })),
              ]}
            />
          </div>
        </div>
      </div>

      <div className="flex justify-end gap-2 pt-4 border-t border-gray-100 dark:border-white/[0.05]">
        <Button variant="outline" size="sm" onClick={onClear}>
          Clear
        </Button>
        <Button variant="primary" size="sm" onClick={onApply}>
          Apply Filters
        </Button>
      </div>
    </Dropdown>
  );
}
