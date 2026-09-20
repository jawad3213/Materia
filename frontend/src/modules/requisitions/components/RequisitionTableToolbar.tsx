import React from "react";
import { Link } from "react-router-dom";
import Button from "../../../shared/components/ui/button/Button";
import RequisitionFilters from "./RequisitionFilters";

export interface RequisitionTableToolbarProps {
  searchKeyword: string;
  onSearchKeywordChange: (val: string) => void;
  onSearchSubmit: () => void;
  isFilterOpen: boolean;
  onToggleFilter: () => void;
  activeFiltersCount: number;
  onRefresh: () => void;
  loading: boolean;
  pendingReviewCount: number;

  // Active filter chips
  activeStatFilter: string;
  onClearStatFilter: () => void;
  filterStatus: string;
  onClearFilterStatus: () => void;
  filterRequester: string;
  onClearFilterRequester: () => void;
  onClearFilterDateFrom?: () => void;
  onClearFilterDateTo?: () => void;

  // Filter Drawer props
  filterDateFrom: string;
  setFilterDateFrom: (val: string) => void;
  filterDateTo: string;
  setFilterDateTo: (val: string) => void;
  setFilterStatus: (val: string) => void;
  setFilterRequester: (val: string) => void;
  onApplyFilters: () => void;
  onClearFilters: () => void;
}

export default function RequisitionTableToolbar({
  searchKeyword,
  onSearchKeywordChange,
  onSearchSubmit,
  isFilterOpen,
  onToggleFilter,
  activeFiltersCount,
  onRefresh,
  loading,
  pendingReviewCount,
  activeStatFilter,
  onClearStatFilter,
  filterStatus,
  onClearFilterStatus,
  filterRequester,
  onClearFilterRequester,
  onClearFilterDateFrom,
  onClearFilterDateTo,
  filterDateFrom,
  setFilterDateFrom,
  filterDateTo,
  setFilterDateTo,
  setFilterStatus,
  setFilterRequester,
  onApplyFilters,
  onClearFilters,
}: RequisitionTableToolbarProps) {
  const hasActiveChips =
    activeFiltersCount > 0 ||
    activeStatFilter !== "ALL" ||
    Boolean(filterStatus) ||
    Boolean(filterRequester) ||
    Boolean(filterDateFrom) ||
    Boolean(filterDateTo);

  return (
    <>
      {/* Table Controls Header */}
      <div className="flex flex-col gap-3 p-5 sm:flex-row sm:items-center sm:justify-between border-b border-gray-100 dark:border-white/[0.07]">
        {/* Left: Search Bar */}
        <div className="relative w-full sm:w-80">
          <input
            type="text"
            placeholder="Search code, title, requester..."
            value={searchKeyword}
            onChange={(e) => onSearchKeywordChange(e.target.value)}
            onKeyDown={(e) => {
              if (e.key === "Enter") {
                onSearchSubmit();
              }
            }}
            className="w-full rounded-xl border border-gray-200 bg-gray-50/50 py-2.5 pl-10 pr-10 text-xs text-gray-900 transition-colors focus:border-brand-500 focus:bg-white focus:outline-none focus:ring-1 focus:ring-brand-500 dark:border-white/[0.1] dark:bg-white/[0.03] dark:text-white dark:focus:bg-transparent"
          />
          <svg
            className="absolute left-3.5 top-3 size-4 text-gray-400"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"
            />
          </svg>
          {searchKeyword && (
            <button
              type="button"
              onClick={() => {
                onSearchKeywordChange("");
                onSearchSubmit();
              }}
              className="absolute right-3 top-3 text-gray-400 hover:text-gray-600 dark:hover:text-gray-200"
              title="Clear search"
            >
              <svg className="size-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
              </svg>
            </button>
          )}
        </div>

        {/* Right: Actions & Filter Toggle */}
        <div className="flex items-center gap-2 flex-wrap">
          {/* Filter Popover Button */}
          <div className="relative">
            <Button
              variant="outline"
              size="sm"
              onClick={onToggleFilter}
            >
              <span className="flex items-center gap-2">
                <svg
                  className="size-4"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M3 4a1 1 0 011-1h16a1 1 0 011 1v2.586a1 1 0 01-.293.707l-6.414 6.414a1 1 0 00-.293.707V17l-4 4v-6.586a1 1 0 00-.293-.707L3.293 7.293A1 1 0 013 6.586V4z"
                  />
                </svg>
                Filter
              </span>
            </Button>
            {activeFiltersCount > 0 && (
              <span className="absolute -top-2 -right-2 flex h-5 w-5 items-center justify-center rounded-full bg-brand-500 text-[10px] font-bold text-white shadow-sm">
                {activeFiltersCount}
              </span>
            )}

            <RequisitionFilters
              isOpen={isFilterOpen}
              onClose={onToggleFilter}
              filterStatus={filterStatus}
              setFilterStatus={setFilterStatus}
              filterRequester={filterRequester}
              setFilterRequester={setFilterRequester}
              filterDateFrom={filterDateFrom}
              setFilterDateFrom={setFilterDateFrom}
              filterDateTo={filterDateTo}
              setFilterDateTo={setFilterDateTo}
              onApply={onApplyFilters}
              onClear={onClearFilters}
            />
          </div>

          {/* Refresh Button */}
          <Button
            variant="outline"
            size="sm"
            onClick={onRefresh}
            title="Refresh"
          >
            <svg
              className={`size-4 ${loading ? "animate-spin text-brand-500" : ""}`}
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"
              />
            </svg>
          </Button>

          {/* Approvals Portal Link (Dedicated Page for Manager Approvals) */}
          <Link to="/requisitions/approvals">
            <Button
              variant="outline"
              size="sm"
              className="border-amber-300 text-amber-700 bg-amber-50/60 hover:bg-amber-100 dark:border-amber-500/30 dark:bg-amber-500/10 dark:text-amber-300"
            >
              <span className="flex items-center gap-1.5">
                <svg
                  className="size-4 text-amber-600 dark:text-amber-400"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"
                  />
                </svg>
                Approvals Portal
                {pendingReviewCount > 0 && (
                  <span className="ml-1 px-1.5 py-0.2 rounded-full bg-amber-500 text-white text-[10px] font-bold">
                    {pendingReviewCount}
                  </span>
                )}
              </span>
            </Button>
          </Link>

          {/* Create New Requisition */}
          <Link to="/requisitions/create">
            <Button size="sm">
              <span className="flex items-center gap-1.5">
                <svg
                  className="size-4"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M12 4v16m8-8H4"
                  />
                </svg>
                New Requisition
              </span>
            </Button>
          </Link>
        </div>
      </div>

      {/* Active Filters Pill Bar */}
      {hasActiveChips && (
        <div className="flex flex-wrap items-center gap-2 px-5 py-2.5 bg-gray-50/75 border-b border-gray-100 dark:bg-gray-900/40 dark:border-white/[0.05]">
          <span className="text-xs text-gray-500 dark:text-gray-400 font-medium">
            Active filters:
          </span>
          {activeStatFilter !== "ALL" && (
            <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-medium bg-brand-50 text-brand-700 dark:bg-brand-500/15 dark:text-brand-300">
              View: {activeStatFilter}
              <button
                type="button"
                onClick={onClearStatFilter}
                className="hover:text-brand-900 dark:hover:text-white"
              >
                <svg className="size-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                </svg>
              </button>
            </span>
          )}
          {filterStatus && (
            <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-medium bg-brand-50 text-brand-700 dark:bg-brand-500/15 dark:text-brand-300">
              Status: {filterStatus}
              <button
                type="button"
                onClick={onClearFilterStatus}
                className="hover:text-brand-900 dark:hover:text-white"
              >
                <svg className="size-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                </svg>
              </button>
            </span>
          )}
          {filterRequester && (
            <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-medium bg-brand-50 text-brand-700 dark:bg-brand-500/15 dark:text-brand-300">
              Requester: {filterRequester}
              <button
                type="button"
                onClick={onClearFilterRequester}
                className="hover:text-brand-900 dark:hover:text-white"
              >
                <svg className="size-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                </svg>
              </button>
            </span>
          )}
          {filterDateFrom && (
            <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-medium bg-brand-50 text-brand-700 dark:bg-brand-500/15 dark:text-brand-300">
              From: {filterDateFrom}
              <button
                type="button"
                onClick={() => {
                  if (onClearFilterDateFrom) {
                    onClearFilterDateFrom();
                  } else {
                    setFilterDateFrom("");
                    onRefresh();
                  }
                }}
                className="hover:text-brand-900 dark:hover:text-white"
              >
                <svg className="size-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                </svg>
              </button>
            </span>
          )}
          {filterDateTo && (
            <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-medium bg-brand-50 text-brand-700 dark:bg-brand-500/15 dark:text-brand-300">
              To: {filterDateTo}
              <button
                type="button"
                onClick={() => {
                  if (onClearFilterDateTo) {
                    onClearFilterDateTo();
                  } else {
                    setFilterDateTo("");
                    onRefresh();
                  }
                }}
                className="hover:text-brand-900 dark:hover:text-white"
              >
                <svg className="size-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                </svg>
              </button>
            </span>
          )}
        </div>
      )}
    </>
  );
}
