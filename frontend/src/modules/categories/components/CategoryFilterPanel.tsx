import React from "react";
import { CategoryType } from "../enums/CategoryType";
import { CategoryStatus } from "../enums/CategoryStatus";

export interface CategoryFilters {
  categoryType: string;
  status: string;
  parentCode: string;
  level: string;
  isRoot: string;
}

const EMPTY_FILTERS: CategoryFilters = {
  categoryType: "",
  status: "",
  parentCode: "",
  level: "",
  isRoot: "",
};

// Human-readable labels for category types
const categoryTypeLabels: Record<string, string> = {
  MAT: "Material",
  PRD: "Product",
  SRV: "Service",
  RMC: "Raw Material",
  CMP: "Component",
  PKG: "Packaging",
  SPR: "Spare Part",
  CNS: "Consumable",
  TOL: "Tool",
  CHM: "Chemical",
  ELC: "Electronic",
  FAM: "Family",
  BRD: "Brand",
  DEP: "Department",
  PRJ: "Project",
  GEO: "Geographic",
  SEA: "Seasonal",
};

interface Props {
  filters: CategoryFilters;
  onChange: (filters: CategoryFilters) => void;
  parentCodes: string[];
  levels: number[];
  isOpen: boolean;
  onToggle: () => void;
}

export { EMPTY_FILTERS };

export default function CategoryFilterPanel({
  filters,
  onChange,
  parentCodes,
  levels,
  isOpen,
  onToggle,
}: Props) {
  const handleChange = (key: keyof CategoryFilters, value: string) => {
    onChange({ ...filters, [key]: value });
  };

  const handleReset = () => {
    onChange(EMPTY_FILTERS);
  };

  const activeCount = Object.values(filters).filter(Boolean).length;

  if (!isOpen) return null;

  return (
    <div className="mb-4 rounded-xl border border-gray-200 bg-white p-5 dark:border-white/[0.05] dark:bg-white/[0.03] animate-in slide-in-from-top-2">
      <div className="flex items-center justify-between mb-4">
        <div className="flex items-center gap-2">
          <svg className="w-5 h-5 text-brand-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 4a1 1 0 011-1h16a1 1 0 011 1v2.586a1 1 0 01-.293.707l-6.414 6.414a1 1 0 00-.293.707V17l-4 4v-6.586a1 1 0 00-.293-.707L3.293 7.293A1 1 0 013 6.586V4z" />
          </svg>
          <h4 className="text-sm font-semibold text-gray-800 dark:text-white/90">
            Filters
          </h4>
          {activeCount > 0 && (
            <span className="inline-flex items-center justify-center h-5 min-w-[20px] px-1.5 rounded-full bg-brand-500 text-[10px] font-bold text-white">
              {activeCount}
            </span>
          )}
        </div>
        <button
          onClick={handleReset}
          className="text-xs text-gray-500 hover:text-error-500 dark:text-gray-400 dark:hover:text-error-400 transition-colors"
        >
          Reset All
        </button>
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-5 gap-4">
        {/* Filter 1: Category Type */}
        <div className="flex flex-col gap-1.5">
          <label className="text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
            Type
          </label>
          <select
            value={filters.categoryType}
            onChange={(e) => handleChange("categoryType", e.target.value)}
            className="h-9 rounded-lg border border-gray-200 bg-white px-3 text-sm text-gray-700 focus:border-brand-300 focus:ring-2 focus:ring-brand-500/20 dark:border-white/[0.1] dark:bg-white/[0.03] dark:text-gray-200 outline-none transition-all"
          >
            <option value="">All Types</option>
            {Object.entries(CategoryType).map(([key, value]) => (
              <option key={key} value={value}>
                {categoryTypeLabels[value] || key}
              </option>
            ))}
          </select>
        </div>

        {/* Filter 2: Status */}
        <div className="flex flex-col gap-1.5">
          <label className="text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
            Status
          </label>
          <select
            value={filters.status}
            onChange={(e) => handleChange("status", e.target.value)}
            className="h-9 rounded-lg border border-gray-200 bg-white px-3 text-sm text-gray-700 focus:border-brand-300 focus:ring-2 focus:ring-brand-500/20 dark:border-white/[0.1] dark:bg-white/[0.03] dark:text-gray-200 outline-none transition-all"
          >
            <option value="">All Statuses</option>
            {Object.entries(CategoryStatus).map(([key, value]) => (
              <option key={key} value={value}>
                {key.charAt(0) + key.slice(1).toLowerCase()}
              </option>
            ))}
          </select>
        </div>

        {/* Filter 3: Parent */}
        <div className="flex flex-col gap-1.5">
          <label className="text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
            Parent
          </label>
          <select
            value={filters.parentCode}
            onChange={(e) => handleChange("parentCode", e.target.value)}
            className="h-9 rounded-lg border border-gray-200 bg-white px-3 text-sm text-gray-700 focus:border-brand-300 focus:ring-2 focus:ring-brand-500/20 dark:border-white/[0.1] dark:bg-white/[0.03] dark:text-gray-200 outline-none transition-all"
          >
            <option value="">All Parents</option>
            {parentCodes.map((code) => (
              <option key={code} value={code}>
                {code}
              </option>
            ))}
          </select>
        </div>

        {/* Filter 4: Level */}
        <div className="flex flex-col gap-1.5">
          <label className="text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
            Level
          </label>
          <select
            value={filters.level}
            onChange={(e) => handleChange("level", e.target.value)}
            className="h-9 rounded-lg border border-gray-200 bg-white px-3 text-sm text-gray-700 focus:border-brand-300 focus:ring-2 focus:ring-brand-500/20 dark:border-white/[0.1] dark:bg-white/[0.03] dark:text-gray-200 outline-none transition-all"
          >
            <option value="">All Levels</option>
            {levels.map((lvl) => (
              <option key={lvl} value={lvl}>
                Level {lvl}
              </option>
            ))}
          </select>
        </div>

        {/* Filter 5: Root Only */}
        <div className="flex flex-col gap-1.5">
          <label className="text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
            Root Only
          </label>
          <select
            value={filters.isRoot}
            onChange={(e) => handleChange("isRoot", e.target.value)}
            className="h-9 rounded-lg border border-gray-200 bg-white px-3 text-sm text-gray-700 focus:border-brand-300 focus:ring-2 focus:ring-brand-500/20 dark:border-white/[0.1] dark:bg-white/[0.03] dark:text-gray-200 outline-none transition-all"
          >
            <option value="">All</option>
            <option value="true">Root Categories Only</option>
            <option value="false">Sub-Categories Only</option>
          </select>
        </div>
      </div>
    </div>
  );
}
