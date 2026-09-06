import React, { useEffect, useState } from "react";
import { useParams, Link } from "react-router-dom";
import PageBreadcrumb from "../../../shared/components/common/PageBreadCrumb";
import PageMeta from "../../../shared/components/common/PageMeta";
import { categoryApi } from "../services/categoryApi";
import type { Category } from "../types/Category";
import Badge from "../../../shared/components/ui/badge/Badge";

const statusColorMap: Record<string, "success" | "warning" | "error" | "info" | "light"> = {
  ACTIVE: "success",
  INACTIVE: "light",
  PENDING: "warning",
  SUSPENDED: "error",
};

function formatStatus(status: string): string {
  if (!status) return "—";
  return status.replace(/_/g, " ").replace(/\b\w/g, (l) => l.toUpperCase());
}

function formatDate(dateStr: string | undefined): string {
  if (!dateStr) return "—";
  return new Date(dateStr).toLocaleString();
}

export default function CategoryDetailPage() {
  const { id } = useParams<{ id: string }>();
  const [category, setCategory] = useState<Category | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchCategory = async () => {
      if (!id) return;
      try {
        setLoading(true);
        const response = await categoryApi.getById(id);
        setCategory(response.data);
      } catch (err: any) {
        console.error("Failed to load category details:", err);
        setError(err.response?.data?.message || "Failed to load category details");
      } finally {
        setLoading(false);
      }
    };
    fetchCategory();
  }, [id]);

  if (loading) {
    return (
      <div className="flex h-[calc(100vh-200px)] items-center justify-center">
        <div className="flex flex-col items-center gap-3">
          <svg className="animate-spin h-8 w-8 text-brand-500" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
            <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
            <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
          </svg>
          <p className="text-sm text-gray-500">Loading category details...</p>
        </div>
      </div>
    );
  }

  if (error || !category) {
    return (
      <div className="flex h-[calc(100vh-200px)] flex-col items-center justify-center gap-4">
        <div className="flex items-center justify-center w-16 h-16 rounded-full bg-error-50 dark:bg-error-500/10 text-error-500">
          <svg className="w-8 h-8" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
          </svg>
        </div>
        <h3 className="text-xl font-medium text-gray-900 dark:text-white">Category Not Found</h3>
        <p className="text-gray-500 dark:text-gray-400">{error || "The category you requested does not exist."}</p>
        <Link to="/categories" className="mt-4 text-brand-500 hover:underline">
          &larr; Back to Categories
        </Link>
      </div>
    );
  }

  return (
    <>
      <PageMeta
        title={`Category: ${category.name} | Materia Admin`}
        description="View category details"
      />
      
      <div className="flex items-center justify-between mb-6">
        <PageBreadcrumb 
          pageTitle={category.name} 
          parentName="Categories" 
          parentUrl="/categories" 
        />
        <div className="flex gap-3">
          <Link to="/categories">
            <button className="rounded-lg border border-gray-200 bg-white px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-50 dark:border-white/[0.05] dark:bg-white/[0.03] dark:text-gray-300 dark:hover:bg-white/[0.05]">
              Back to List
            </button>
          </Link>
          <Link to={`/categories/edit/${category.id}`}>
            <button className="flex items-center gap-2 rounded-lg bg-brand-500 px-4 py-2 text-sm font-medium text-white hover:bg-brand-600">
              <svg className="size-4" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
              </svg>
              Edit Category
            </button>
          </Link>
        </div>
      </div>

      <div className="grid grid-cols-1 gap-6 lg:grid-cols-3">
        {/* Left Column: Essential Info */}
        <div className="lg:col-span-1 flex flex-col gap-6">
          <div className="rounded-2xl border border-gray-200 bg-white p-6 dark:border-white/[0.05] dark:bg-white/[0.03]">
            <div className="flex items-center justify-between mb-6">
              <div className="flex h-16 w-16 items-center justify-center rounded-2xl bg-brand-50 text-2xl font-bold text-brand-500 dark:bg-brand-500/10">
                {category.name.substring(0, 2).toUpperCase()}
              </div>
              <Badge variant="light" color={statusColorMap[category.status] || "info"}>
                {formatStatus(category.status)}
              </Badge>
            </div>
            
            <h3 className="text-xl font-bold text-gray-900 dark:text-white mb-1">
              {category.name}
            </h3>
            <p className="text-sm font-medium text-gray-500 dark:text-gray-400 mb-6">
              Code: {category.code}
            </p>
            
            {category.shortDescription && (
              <p className="text-sm font-medium text-gray-600 dark:text-gray-300 mb-2">
                {category.shortDescription}
              </p>
            )}

            {category.description && (
              <p className="text-sm text-gray-600 dark:text-gray-300 mb-6 pb-6 border-b border-gray-100 dark:border-gray-800">
                {category.description}
              </p>
            )}

            <div className="space-y-4">
              <div className="flex flex-col">
                <span className="text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider mb-1">Category Type</span>
                <span className="text-sm font-medium text-gray-900 dark:text-white flex items-center gap-2 capitalize">
                  {category.categoryType?.toLowerCase().replace('_', ' ')}
                </span>
              </div>
              
              <div className="flex flex-col">
                <span className="text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider mb-1">Hierarchy Level & Path</span>
                <span className="text-sm font-medium text-gray-900 dark:text-white flex items-center gap-2">
                  <span className="bg-gray-100 text-gray-600 dark:bg-white/[0.05] dark:text-gray-300 px-2 py-0.5 rounded text-xs font-semibold">L{category.level}</span>
                  {category.path || "—"}
                </span>
              </div>
              
              <div className="flex flex-col">
                <span className="text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider mb-1">Parent Category</span>
                <span className="text-sm font-medium text-gray-900 dark:text-white flex flex-col gap-1">
                  <span>{category.parentCode || "Root Category (None)"}</span>
                  {category.parentId && (
                    <span className="text-xs text-gray-500">ID: {category.parentId}</span>
                  )}
                </span>
              </div>

              {category.childrenIds && category.childrenIds.length > 0 && (
                <div className="flex flex-col pt-2 border-t border-gray-100 dark:border-gray-800">
                  <span className="text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider mb-1">Child IDs ({category.childrenIds.length})</span>
                  <div className="flex flex-wrap gap-2 mt-1">
                    {category.childrenIds.slice(0, 5).map(id => (
                      <span key={id} className="text-[10px] bg-gray-50 border border-gray-200 text-gray-600 dark:bg-gray-800 dark:border-gray-700 dark:text-gray-400 px-1.5 py-0.5 rounded">
                        {id.substring(0, 8)}...
                      </span>
                    ))}
                    {category.childrenIds.length > 5 && (
                      <span className="text-[10px] text-gray-500 self-center">+{category.childrenIds.length - 5} more</span>
                    )}
                  </div>
                </div>
              )}
            </div>
          </div>
        </div>

        {/* Right Column: Other Details */}
        <div className="lg:col-span-2 flex flex-col gap-6">
          
          {/* Statistics */}
          <div className="rounded-2xl border border-gray-200 bg-white p-6 dark:border-white/[0.05] dark:bg-white/[0.03]">
            <div className="flex items-center gap-2 mb-6 pb-4 border-b border-gray-100 dark:border-gray-800">
              <div className="p-2 rounded-lg bg-success-50 text-success-600 dark:bg-success-500/10">
                <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M16 8v8m-4-5v5m-4-2v2m-2 4h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
                </svg>
              </div>
              <h4 className="text-base font-semibold text-gray-900 dark:text-white">Statistics</h4>
            </div>
            
            <div className="grid grid-cols-1 md:grid-cols-3 gap-y-6 gap-x-8">
              <div className="flex flex-col">
                <span className="text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider mb-1">Total Sub-categories</span>
                <span className="text-xl font-bold text-gray-900 dark:text-white">{category.subCategoryCount || 0}</span>
              </div>
              <div className="flex flex-col">
                <span className="text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider mb-1">Total Materials</span>
                <span className="text-xl font-bold text-gray-900 dark:text-white">{category.materialCount || 0}</span>
              </div>
              <div className="flex flex-col">
                <span className="text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider mb-1">Total Items</span>
                <span className="text-xl font-bold text-gray-900 dark:text-white">{category.totalItems || 0}</span>
              </div>
            </div>
          </div>

          {/* System Information */}
          <div className="rounded-2xl border border-gray-200 bg-white p-6 dark:border-white/[0.05] dark:bg-white/[0.03]">
            <div className="flex items-center gap-2 mb-6 pb-4 border-b border-gray-100 dark:border-gray-800">
              <div className="p-2 rounded-lg bg-gray-50 text-gray-600 dark:bg-white/[0.05] dark:text-gray-300">
                <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
              </div>
              <h4 className="text-base font-semibold text-gray-900 dark:text-white">System Information</h4>
            </div>
            
            <div className="grid grid-cols-1 md:grid-cols-2 gap-y-6 gap-x-8">
              <div className="flex flex-col">
                <span className="text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider mb-1">Created By</span>
                <span className="text-sm font-medium text-gray-900 dark:text-white">{category.createdBy || "—"}</span>
              </div>
              <div className="flex flex-col">
                <span className="text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider mb-1">Created At</span>
                <span className="text-sm font-medium text-gray-900 dark:text-white">{formatDate(category.createdAt)}</span>
              </div>
              <div className="flex flex-col">
                <span className="text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider mb-1">Last Updated By</span>
                <span className="text-sm font-medium text-gray-900 dark:text-white">{category.updatedBy || "—"}</span>
              </div>
              <div className="flex flex-col">
                <span className="text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider mb-1">Last Updated At</span>
                <span className="text-sm font-medium text-gray-900 dark:text-white">{formatDate(category.updatedAt)}</span>
              </div>
            </div>
          </div>
          
        </div>
      </div>
    </>
  );
}
