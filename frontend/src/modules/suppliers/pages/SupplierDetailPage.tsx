import React, { useEffect, useState } from "react";
import { useParams, Link } from "react-router-dom";
import PageBreadcrumb from "../../../shared/components/common/PageBreadCrumb";
import PageMeta from "../../../shared/components/common/PageMeta";
import { supplierApi } from "../services/supplierApi";
import type { Supplier } from "../types/Supplier";
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

export default function SupplierDetailPage() {
  const { id } = useParams<{ id: string }>();
  const [supplier, setSupplier] = useState<Supplier | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchSupplier = async () => {
      if (!id) return;
      try {
        setLoading(true);
        const response = await supplierApi.getById(id);
        setSupplier(response.data);
      } catch (err: any) {
        console.error("Failed to load supplier details:", err);
        setError(err.response?.data?.message || "Failed to load supplier details");
      } finally {
        setLoading(false);
      }
    };
    fetchSupplier();
  }, [id]);

  if (loading) {
    return (
      <div className="flex h-[calc(100vh-200px)] items-center justify-center">
        <div className="flex flex-col items-center gap-3">
          <svg className="animate-spin h-8 w-8 text-brand-500" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
            <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
            <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
          </svg>
          <p className="text-sm text-gray-500">Loading supplier details...</p>
        </div>
      </div>
    );
  }

  if (error || !supplier) {
    return (
      <div className="flex h-[calc(100vh-200px)] flex-col items-center justify-center gap-4">
        <div className="flex items-center justify-center w-16 h-16 rounded-full bg-error-50 dark:bg-error-500/10 text-error-500">
          <svg className="w-8 h-8" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
          </svg>
        </div>
        <h3 className="text-xl font-medium text-gray-900 dark:text-white">Supplier Not Found</h3>
        <p className="text-gray-500 dark:text-gray-400">{error || "The supplier you requested does not exist."}</p>
        <Link to="/suppliers" className="mt-4 text-brand-500 hover:underline">
          &larr; Back to Suppliers
        </Link>
      </div>
    );
  }

  return (
    <>
      <PageMeta
        title={`Supplier: ${supplier.name} | Materia Admin`}
        description="View supplier details"
      />
      
      <div className="flex items-center justify-between mb-6">
        <PageBreadcrumb 
          pageTitle={supplier.name} 
          parentName="Suppliers" 
          parentUrl="/suppliers" 
        />
        <div className="flex gap-3">
          <Link to="/suppliers">
            <button className="rounded-lg border border-gray-200 bg-white px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-50 dark:border-white/[0.05] dark:bg-white/[0.03] dark:text-gray-300 dark:hover:bg-white/[0.05]">
              Back to List
            </button>
          </Link>
          <Link to={`/suppliers/edit/${supplier.id}`}>
            <button className="flex items-center gap-2 rounded-lg bg-brand-500 px-4 py-2 text-sm font-medium text-white hover:bg-brand-600">
              <svg className="size-4" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
              </svg>
              Edit Supplier
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
                {supplier.name.substring(0, 2).toUpperCase()}
              </div>
              <Badge variant="light" color={statusColorMap[supplier.status] || "info"}>
                {formatStatus(supplier.status)}
              </Badge>
            </div>
            
            <h3 className="text-xl font-bold text-gray-900 dark:text-white mb-1">
              {supplier.name}
            </h3>
            <p className="text-sm font-medium text-gray-500 dark:text-gray-400 mb-6">
              ID: {supplier.code}
            </p>
            
            {supplier.description && (
              <p className="text-sm text-gray-600 dark:text-gray-300 mb-6 pb-6 border-b border-gray-100 dark:border-gray-800">
                {supplier.description}
              </p>
            )}

            <div className="space-y-4">
              <div className="flex flex-col">
                <span className="text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider mb-1">Contact Person</span>
                <span className="text-sm font-medium text-gray-900 dark:text-white flex items-center gap-2">
                  <svg className="w-4 h-4 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                  </svg>
                  {supplier.contactPerson || "—"}
                </span>
              </div>
              
              <div className="flex flex-col">
                <span className="text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider mb-1">Email</span>
                <span className="text-sm font-medium text-gray-900 dark:text-white flex items-center gap-2">
                  <svg className="w-4 h-4 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z" />
                  </svg>
                  <a href={`mailto:${supplier.contactEmail}`} className="text-brand-500 hover:underline">{supplier.contactEmail || "—"}</a>
                </span>
              </div>
              
              <div className="flex flex-col">
                <span className="text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider mb-1">Phone</span>
                <span className="text-sm font-medium text-gray-900 dark:text-white flex items-center gap-2">
                  <svg className="w-4 h-4 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 5a2 2 0 012-2h3.28a1 1 0 01.948.684l1.498 4.493a1 1 0 01-.502 1.21l-2.257 1.13a11.042 11.042 0 005.516 5.516l1.13-2.257a1 1 0 011.21-.502l4.493 1.498a1 1 0 01.684.949V19a2 2 0 01-2 2h-1C9.716 21 3 14.284 3 6V5z" />
                  </svg>
                  {supplier.contactPhone || "—"}
                </span>
              </div>
            </div>
          </div>
          
          <div className="rounded-2xl border border-gray-200 bg-white p-6 dark:border-white/[0.05] dark:bg-white/[0.03]">
            <h4 className="text-base font-semibold text-gray-900 dark:text-white mb-4">Location Details</h4>
            <div className="space-y-4">
              <div className="flex items-start gap-3">
                <div className="mt-0.5 text-brand-500">
                  <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z" />
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 11a3 3 0 11-6 0 3 3 0 016 0z" />
                  </svg>
                </div>
                <div>
                  <p className="text-sm font-medium text-gray-900 dark:text-white">{supplier.fullAddress || supplier.address || "—"}</p>
                  <p className="text-sm text-gray-500 dark:text-gray-400 mt-1">{supplier.city}, {supplier.country} {supplier.postalCode}</p>
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* Right Column: Other Details */}
        <div className="lg:col-span-2 flex flex-col gap-6">
          
          {/* Financial & Payment Terms */}
          <div className="rounded-2xl border border-gray-200 bg-white p-6 dark:border-white/[0.05] dark:bg-white/[0.03]">
            <div className="flex items-center gap-2 mb-6 pb-4 border-b border-gray-100 dark:border-gray-800">
              <div className="p-2 rounded-lg bg-success-50 text-success-600 dark:bg-success-500/10">
                <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
              </div>
              <h4 className="text-base font-semibold text-gray-900 dark:text-white">Financial & Payment</h4>
            </div>
            
            <div className="grid grid-cols-1 md:grid-cols-2 gap-y-6 gap-x-8">
              <div className="flex flex-col">
                <span className="text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider mb-1">Currency</span>
                <span className="text-sm font-medium text-gray-900 dark:text-white flex items-center">
                  <Badge variant="light" color="primary">{supplier.currencyCode || "—"}</Badge>
                </span>
              </div>
              <div className="flex flex-col">
                <span className="text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider mb-1">Payment Delay (Days)</span>
                <span className="text-sm font-medium text-gray-900 dark:text-white">{supplier.paymentDelay != null ? `${supplier.paymentDelay} days` : "—"}</span>
              </div>
              <div className="flex flex-col md:col-span-2">
                <span className="text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider mb-2">Payment Terms</span>
                <div className="flex flex-wrap gap-2">
                  {supplier.paymentTerms && supplier.paymentTerms.length > 0 ? (
                    supplier.paymentTerms.map((term, index) => (
                      <Badge key={index} variant="light" color="dark">
                        {term}
                      </Badge>
                    ))
                  ) : (
                    <span className="text-sm font-medium text-gray-900 dark:text-white">—</span>
                  )}
                </div>
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
                <span className="text-sm font-medium text-gray-900 dark:text-white">{supplier.createdBy || "—"}</span>
              </div>
              <div className="flex flex-col">
                <span className="text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider mb-1">Created At</span>
                <span className="text-sm font-medium text-gray-900 dark:text-white">{formatDate(supplier.createdAt)}</span>
              </div>
              <div className="flex flex-col">
                <span className="text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider mb-1">Last Updated By</span>
                <span className="text-sm font-medium text-gray-900 dark:text-white">{supplier.updatedBy || "—"}</span>
              </div>
              <div className="flex flex-col">
                <span className="text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider mb-1">Last Updated At</span>
                <span className="text-sm font-medium text-gray-900 dark:text-white">{formatDate(supplier.updatedAt)}</span>
              </div>
            </div>
          </div>
          
        </div>
      </div>
    </>
  );
}
