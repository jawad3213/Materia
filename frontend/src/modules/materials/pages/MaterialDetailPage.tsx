import React, { useEffect, useState } from "react";
import { useParams, Link } from "react-router-dom";
import PageBreadcrumb from "../../../shared/components/common/PageBreadCrumb";
import PageMeta from "../../../shared/components/common/PageMeta";
import { materialApi } from "../services/materialApi";
import type { Material } from "../types/Material";
import Badge from "../../../shared/components/ui/badge/Badge";
import MaterialStockDashboard from "../components/MaterialStockDashboard";
import ReorderRecommendationModal from "../components/ReorderRecommendationModal";

const statusColorMap: Record<string, "success" | "warning" | "error" | "info" | "light"> = {
  ACTIVE: "success",
  INACTIVE: "light",
  DISCONTINUED: "error",
  PENDING: "warning",
  OUT_OF_STOCK: "info",
};

const typeColorMap: Record<string, "primary" | "info" | "warning" | "dark"> = {
  RAW_MATERIAL: "primary",
  SEMI_FINISHED: "info",
  FINISHED_PRODUCT: "warning",
  CONSUMABLE: "dark",
};

function formatType(type: string): string {
  if (!type) return "—";
  return type.replace(/_/g, " ").replace(/\b\w/g, (l) => l.toUpperCase());
}

function formatStatus(status: string): string {
  if (!status) return "—";
  return status.replace(/_/g, " ").replace(/\b\w/g, (l) => l.toUpperCase());
}

function formatDate(dateStr: string | undefined): string {
  if (!dateStr) return "—";
  return new Date(dateStr).toLocaleString();
}

export default function MaterialDetailPage() {
  const { id } = useParams<{ id: string }>();
  const [material, setMaterial] = useState<Material | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [isReorderModalOpen, setIsReorderModalOpen] = useState(false);

  const fetchMaterial = async () => {
    if (!id) return;
    try {
      setLoading(true);
      const response = await materialApi.getById(id);
      setMaterial(response.data);
    } catch (err: any) {
      console.error("Failed to load material details:", err);
      setError(err.response?.data?.message || "Failed to load material details");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchMaterial();
  }, [id]);

  if (loading) {
    return (
      <div className="flex h-[calc(100vh-200px)] items-center justify-center">
        <div className="flex flex-col items-center gap-3">
          <svg className="animate-spin h-8 w-8 text-brand-500" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
            <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
            <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
          </svg>
          <p className="text-sm text-gray-500">Loading material details...</p>
        </div>
      </div>
    );
  }

  if (error || !material) {
    return (
      <div className="flex h-[calc(100vh-200px)] flex-col items-center justify-center gap-4">
        <div className="flex items-center justify-center w-16 h-16 rounded-full bg-error-50 dark:bg-error-500/10 text-error-500">
          <svg className="w-8 h-8" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
          </svg>
        </div>
        <h3 className="text-xl font-medium text-gray-900 dark:text-white">Material Not Found</h3>
        <p className="text-gray-500 dark:text-gray-400">{error || "The material you requested does not exist."}</p>
        <Link to="/materials" className="mt-4 text-brand-500 hover:underline">
          &larr; Back to Materials
        </Link>
      </div>
    );
  }

  return (
    <>
      <PageMeta
        title={`Material: ${material.name} | Materia Admin`}
        description="View material details"
      />
      
      <div className="flex items-center justify-between mb-6">
        <PageBreadcrumb 
          pageTitle="Material Details" 
          parentName="Materials" 
          parentUrl="/materials" 
        />
        <div className="flex gap-3">
          <Link to="/materials">
            <button className="rounded-lg border border-gray-200 bg-white px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-50 dark:border-white/[0.05] dark:bg-white/[0.03] dark:text-gray-300 dark:hover:bg-white/[0.05]">
              Back to List
            </button>
          </Link>
          <Link to={`/materials/edit/${material.id}`}>
            <button className="flex items-center gap-2 rounded-lg bg-brand-500 px-4 py-2 text-sm font-medium text-white hover:bg-brand-600">
              <svg className="size-4" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
              </svg>
              Edit Material
            </button>
          </Link>
        </div>
      </div>

      <div className="grid grid-cols-1 xl:grid-cols-3 gap-6">
        {/* Left Column: Main info */}
        <div className="xl:col-span-2 flex flex-col gap-6">
          
          {/* Header Card */}
          <div className="rounded-xl border border-gray-200 bg-white p-6 dark:border-white/[0.05] dark:bg-white/[0.03]">
            <div className="flex flex-col sm:flex-row sm:items-start justify-between gap-4">
              <div>
                <div className="flex items-center gap-3 mb-2">
                  <h1 className="text-2xl font-bold text-gray-900 dark:text-white">{material.name}</h1>
                  <Badge variant="light" size="sm" color={statusColorMap[material.status] || "light"}>
                    {formatStatus(material.status)}
                  </Badge>
                </div>
                <div className="flex items-center gap-4 text-sm text-gray-500 dark:text-gray-400 font-mono">
                  <span>{material.code}</span>
                  {material.alternativeName && (
                    <>
                      <span className="w-1 h-1 rounded-full bg-gray-300 dark:bg-gray-700"></span>
                      <span>Alt: {material.alternativeName}</span>
                    </>
                  )}
                </div>
              </div>
              <Badge variant="light" size="sm" color={typeColorMap[material.materialType] || "primary"}>
                {formatType(material.materialType)}
              </Badge>
            </div>
            
            {(material.shortDescription || material.description) && (
              <div className="mt-6 pt-6 border-t border-gray-100 dark:border-white/[0.05]">
                <h3 className="text-sm font-semibold text-gray-900 dark:text-white mb-2">Description</h3>
                {material.shortDescription && (
                  <p className="text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                    {material.shortDescription}
                  </p>
                )}
                {material.description && (
                  <p className="text-sm text-gray-500 dark:text-gray-400 whitespace-pre-wrap">
                    {material.description}
                  </p>
                )}
              </div>
            )}
          </div>

          {/* Inventory & Stock Dashboard (Option 3) */}
          <MaterialStockDashboard
            material={material}
            onOpenReorder={() => setIsReorderModalOpen(true)}
          />
        </div>

        {/* Right Column: Sidebar info */}
        <div className="flex flex-col gap-6">
          
          {/* Classification & Pricing */}
          <div className="rounded-xl border border-gray-200 bg-white p-6 dark:border-white/[0.05] dark:bg-white/[0.03]">
            <h3 className="text-base font-semibold text-gray-900 dark:text-white mb-4">Classification & Pricing</h3>
            
            <div className="flex flex-col gap-4">
              <div>
                <span className="block text-[11px] font-medium uppercase tracking-wider text-gray-500 mb-1">Category</span>
                <span className="text-sm font-medium text-gray-900 dark:text-gray-200">{material.categoryName || material.categoryId || "—"}</span>
              </div>
              <div>
                <span className="block text-[11px] font-medium uppercase tracking-wider text-gray-500 mb-1">Supplier</span>
                <span className="text-sm font-medium text-gray-900 dark:text-gray-200">{material.supplierName || material.supplierId || "—"}</span>
              </div>
              <div className="pt-4 border-t border-gray-100 dark:border-white/[0.05]">
                <span className="block text-[11px] font-medium uppercase tracking-wider text-gray-500 mb-1">Standard Price</span>
                <span className="text-xl font-semibold text-gray-900 dark:text-white">{material.standardPrice} <span className="text-sm text-gray-500">{material.currencyCode}</span></span>
              </div>
              {material.costPrice && (
                <div>
                  <span className="block text-[11px] font-medium uppercase tracking-wider text-gray-500 mb-1">Cost Price</span>
                  <span className="text-sm font-medium text-gray-700 dark:text-gray-300">{material.costPrice} <span className="text-xs text-gray-500">{material.currencyCode}</span></span>
                </div>
              )}
              <div>
                <span className="block text-[11px] font-medium uppercase tracking-wider text-gray-500 mb-1">Economic Order Quantity (EOQ)</span>
                <span className="text-sm font-medium text-gray-700 dark:text-gray-300">{material.economicOrderQuantity}</span>
              </div>
            </div>
          </div>

          {/* System Info */}
          <div className="rounded-xl border border-gray-200 bg-white p-6 dark:border-white/[0.05] dark:bg-white/[0.03]">
            <h3 className="text-base font-semibold text-gray-900 dark:text-white mb-4">System Information</h3>
            
            <div className="flex flex-col gap-3">
              <div className="flex justify-between items-center">
                <span className="text-xs text-gray-500">Search Keywords</span>
                <span className="text-xs font-medium text-gray-900 dark:text-gray-200 text-right">{material.searchKeywords || "—"}</span>
              </div>
              <div className="flex justify-between items-center">
                <span className="text-xs text-gray-500">Created At</span>
                <span className="text-xs font-medium text-gray-900 dark:text-gray-200">{formatDate(material.createdAt)}</span>
              </div>
              <div className="flex justify-between items-center">
                <span className="text-xs text-gray-500">Created By</span>
                <span className="text-xs font-medium text-gray-900 dark:text-gray-200">{material.createdBy || "—"}</span>
              </div>
              {material.updatedAt && (
                <div className="flex justify-between items-center">
                  <span className="text-xs text-gray-500">Last Updated</span>
                  <span className="text-xs font-medium text-gray-900 dark:text-gray-200">{formatDate(material.updatedAt)}</span>
                </div>
              )}
              {material.updatedBy && (
                <div className="flex justify-between items-center">
                  <span className="text-xs text-gray-500">Updated By</span>
                  <span className="text-xs font-medium text-gray-900 dark:text-gray-200">{material.updatedBy}</span>
                </div>
              )}
              
              {material.status === 'DISCONTINUED' && material.obsoletedAt && (
                <div className="mt-2 pt-3 border-t border-error-100 dark:border-error-500/20">
                  <div className="flex justify-between items-center mb-1">
                    <span className="text-xs text-error-500">Obsoleted At</span>
                    <span className="text-xs font-medium text-error-600 dark:text-error-400">{formatDate(material.obsoletedAt)}</span>
                  </div>
                  {material.obsoletedBy && (
                    <div className="flex justify-between items-center mb-1">
                      <span className="text-xs text-error-500">Obsoleted By</span>
                      <span className="text-xs font-medium text-error-600 dark:text-error-400">{material.obsoletedBy}</span>
                    </div>
                  )}
                  {material.obsoletedReason && (
                    <div className="flex flex-col mt-1">
                      <span className="text-[10px] text-error-400 uppercase tracking-wider mb-0.5">Reason</span>
                      <span className="text-xs text-error-600 dark:text-error-400">{material.obsoletedReason}</span>
                    </div>
                  )}
                </div>
              )}
            </div>
          </div>

        </div>
      </div>

      {/* 1-Click Reorder Modal */}
      {material && (
        <ReorderRecommendationModal
          materialId={material.id}
          isOpen={isReorderModalOpen}
          onClose={() => setIsReorderModalOpen(false)}
          onReorderSuccess={() => {
            fetchMaterial();
          }}
        />
      )}
    </>
  );
}
