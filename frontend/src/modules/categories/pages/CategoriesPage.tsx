import React from "react";
import { useSearchParams } from "react-router-dom";
import PageBreadcrumb from "../../../shared/components/common/PageBreadCrumb";
import PageMeta from "../../../shared/components/common/PageMeta";
import CategoryList from "../components/CategoryList";
import CategoryHierarchy from "../components/CategoryHierarchy";

export default function CategoriesPage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const currentView = (searchParams.get("view") as "table" | "hierarchy") || "hierarchy";

  const setViewMode = (mode: "table" | "hierarchy") => {
    setSearchParams((prev) => {
      const next = new URLSearchParams(prev);
      next.set("view", mode);
      return next;
    });
  };

  return (
    <>
      <PageMeta
        title="Categories | Materia Admin"
        description="Category master data and taxonomy explorer"
      />
      
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-6">
        <PageBreadcrumb pageTitle="Categories" />

        {/* Dual-View Switcher Toggle */}
        <div className="inline-flex items-center gap-1 p-1 bg-gray-100 dark:bg-gray-800/80 rounded-xl border border-gray-200 dark:border-white/[0.05] shadow-xs self-start sm:self-center -mt-3 sm:mt-0">
          <button
            type="button"
            onClick={() => setViewMode("hierarchy")}
            className={`flex items-center gap-2 px-3.5 py-1.5 rounded-lg text-xs font-semibold transition-all ${
              currentView === "hierarchy"
                ? "bg-white dark:bg-gray-900 text-brand-600 dark:text-brand-400 shadow-xs"
                : "text-gray-600 dark:text-gray-400 hover:text-gray-900 dark:hover:text-white"
            }`}
          >
            <svg className="size-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M4 6a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2H6a2 2 0 01-2-2V6zM14 6a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2h-2a2 2 0 01-2-2V6zM4 16a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2H6a2 2 0 01-2-2v-2zM14 16a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2h-2a2 2 0 01-2-2v-2z"
              />
            </svg>
            Hierarchy Explorer
          </button>
          <button
            type="button"
            onClick={() => setViewMode("table")}
            className={`flex items-center gap-2 px-3.5 py-1.5 rounded-lg text-xs font-semibold transition-all ${
              currentView === "table"
                ? "bg-white dark:bg-gray-900 text-brand-600 dark:text-brand-400 shadow-xs"
                : "text-gray-600 dark:text-gray-400 hover:text-gray-900 dark:hover:text-white"
            }`}
          >
            <svg className="size-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M3 10h18M3 14h18m-9-4v8m-7 0h14a2 2 0 002-2V6a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z"
              />
            </svg>
            Table View
          </button>
        </div>
      </div>

      <div className="space-y-6">
        {currentView === "hierarchy" ? <CategoryHierarchy /> : <CategoryList />}
      </div>
    </>
  );
}