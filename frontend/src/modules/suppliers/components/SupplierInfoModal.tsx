import React from "react";
import { Link } from "react-router-dom";
import { Modal } from "../../../shared/components/ui/modal";
import Badge from "../../../shared/components/ui/badge/Badge";
import Button from "../../../shared/components/ui/button/Button";
import type { Supplier } from "../types/Supplier";

interface SupplierInfoModalProps {
  isOpen: boolean;
  onClose: () => void;
  supplier: Supplier | null;
}

const colorClasses: Record<string, string> = {
  red: "bg-red-50 text-red-500 dark:bg-red-500/15 dark:text-red-500",
  orange: "bg-orange-50 text-orange-500 dark:bg-orange-500/15 dark:text-orange-500",
  purple: "bg-purple-50 text-purple-500 dark:bg-purple-500/15 dark:text-purple-500",
  green: "bg-green-50 text-green-500 dark:bg-green-500/15 dark:text-green-500",
  blue: "bg-brand-50 text-brand-500 dark:bg-brand-500/15 dark:text-brand-500",
};

const colors = ["red", "orange", "purple", "green", "blue"];

function getColorForSupplier(name: string) {
  if (!name) return "blue";
  const sum = name.split("").reduce((acc, char) => acc + char.charCodeAt(0), 0);
  return colors[sum % colors.length];
}

function getInitials(name: string) {
  if (!name) return "S";
  return name.substring(0, 2).toUpperCase();
}

function formatDate(dateStr?: string) {
  if (!dateStr) return "—";
  try {
    return new Date(dateStr).toLocaleString();
  } catch {
    return dateStr;
  }
}

export default function SupplierInfoModal({
  isOpen,
  onClose,
  supplier,
}: SupplierInfoModalProps) {
  if (!supplier) return null;

  const color = getColorForSupplier(supplier.name);
  const initials = getInitials(supplier.name);

  return (
    <Modal isOpen={isOpen} onClose={onClose} className="max-w-2xl p-6 sm:p-8">
      {/* Header */}
      <div className="flex items-start gap-4 pb-5 border-b border-gray-100 dark:border-white/[0.06]">
        <div
          className={`flex h-14 w-14 shrink-0 items-center justify-center rounded-2xl text-lg font-bold ${colorClasses[color]}`}
        >
          {initials}
        </div>
        <div className="flex-1 min-w-0 pr-8">
          <div className="flex items-center gap-2 mb-1 flex-wrap">
            <h3 className="text-xl font-bold text-gray-900 dark:text-white truncate">
              {supplier.name}
            </h3>
            <span className="font-mono text-xs px-2 py-0.5 rounded bg-gray-100 dark:bg-gray-800 text-gray-600 dark:text-gray-300 font-semibold">
              {supplier.code}
            </span>
            <Badge
              size="sm"
              variant="light"
              color={supplier.status === "ACTIVE" ? "success" : "light"}
            >
              {supplier.status || "ACTIVE"}
            </Badge>
          </div>
          {supplier.description && (
            <p className="text-xs text-gray-500 dark:text-gray-400 line-clamp-2">
              {supplier.description}
            </p>
          )}
        </div>
      </div>

      {/* Content Grid */}
      <div className="py-5 space-y-5 max-h-[60vh] overflow-y-auto pr-1">
        {/* Contact Info */}
        <div>
          <h4 className="text-xs font-semibold uppercase tracking-wider text-gray-400 dark:text-gray-500 mb-2">
            Contact Information
          </h4>
          <div className="grid grid-cols-1 sm:grid-cols-3 gap-3 bg-gray-50 dark:bg-white/[0.02] p-3.5 rounded-xl border border-gray-100 dark:border-white/[0.04]">
            <div>
              <span className="block text-[11px] text-gray-400 dark:text-gray-500">Contact Person</span>
              <span className="text-sm font-medium text-gray-800 dark:text-white/90">
                {supplier.contactPerson || "—"}
              </span>
            </div>
            <div>
              <span className="block text-[11px] text-gray-400 dark:text-gray-500">Email</span>
              <a
                href={`mailto:${supplier.contactEmail}`}
                className="text-sm font-medium text-brand-500 hover:underline truncate block"
              >
                {supplier.contactEmail || "—"}
              </a>
            </div>
            <div>
              <span className="block text-[11px] text-gray-400 dark:text-gray-500">Phone</span>
              <a
                href={`tel:${supplier.contactPhone}`}
                className="text-sm font-medium text-gray-800 dark:text-white/90 hover:text-brand-500 truncate block"
              >
                {supplier.contactPhone || "—"}
              </a>
            </div>
          </div>
        </div>

        {/* Location & Address */}
        <div>
          <h4 className="text-xs font-semibold uppercase tracking-wider text-gray-400 dark:text-gray-500 mb-2">
            Location & Address
          </h4>
          <div className="grid grid-cols-1 sm:grid-cols-3 gap-3 bg-gray-50 dark:bg-white/[0.02] p-3.5 rounded-xl border border-gray-100 dark:border-white/[0.04]">
            <div>
              <span className="block text-[11px] text-gray-400 dark:text-gray-500">City / Country</span>
              <span className="text-sm font-medium text-gray-800 dark:text-white/90">
                {supplier.city ? `${supplier.city}, ${supplier.country}` : supplier.country || "—"}
              </span>
            </div>
            <div>
              <span className="block text-[11px] text-gray-400 dark:text-gray-500">Postal Code</span>
              <span className="text-sm font-medium text-gray-800 dark:text-white/90">
                {supplier.postalCode || "—"}
              </span>
            </div>
            <div>
              <span className="block text-[11px] text-gray-400 dark:text-gray-500">Street / Full Address</span>
              <span className="text-sm font-medium text-gray-800 dark:text-white/90 truncate block" title={supplier.fullAddress || supplier.address}>
                {supplier.fullAddress || supplier.address || "—"}
              </span>
            </div>
          </div>
        </div>

        {/* Commercial Terms */}
        <div>
          <h4 className="text-xs font-semibold uppercase tracking-wider text-gray-400 dark:text-gray-500 mb-2">
            Commercial Terms
          </h4>
          <div className="grid grid-cols-1 sm:grid-cols-3 gap-3 bg-gray-50 dark:bg-white/[0.02] p-3.5 rounded-xl border border-gray-100 dark:border-white/[0.04]">
            <div>
              <span className="block text-[11px] text-gray-400 dark:text-gray-500">Currency</span>
              <Badge size="sm" variant="light" color="success">
                {supplier.currencyCode || "MAD"}
              </Badge>
            </div>
            <div>
              <span className="block text-[11px] text-gray-400 dark:text-gray-500">Payment Delay</span>
              <span className="text-sm font-medium text-gray-800 dark:text-white/90">
                {supplier.paymentDelay != null ? `${supplier.paymentDelay} days` : "—"}
              </span>
            </div>
            <div>
              <span className="block text-[11px] text-gray-400 dark:text-gray-500 mb-1">Payment Terms</span>
              {supplier.paymentTerms && supplier.paymentTerms.length > 0 ? (
                <div className="flex flex-wrap gap-1">
                  {supplier.paymentTerms.map((term, i) => (
                    <span
                      key={i}
                      className="px-2 py-0.5 rounded text-[11px] font-medium bg-gray-200 dark:bg-gray-800 text-gray-700 dark:text-gray-300"
                    >
                      {term}
                    </span>
                  ))}
                </div>
              ) : (
                <span className="text-sm text-gray-500">—</span>
              )}
            </div>
          </div>
        </div>

        {/* Metadata */}
        <div className="flex flex-wrap items-center justify-between text-[11px] text-gray-400 dark:text-gray-500 pt-2 border-t border-gray-100 dark:border-white/[0.05]">
          <span>Created: {formatDate(supplier.createdAt)} by {supplier.createdBy || "System"}</span>
          {supplier.updatedAt && (
            <span>Updated: {formatDate(supplier.updatedAt)}</span>
          )}
        </div>
      </div>

      {/* Footer */}
      <div className="flex items-center justify-between pt-4 border-t border-gray-100 dark:border-white/[0.06]">
        <Button variant="outline" size="sm" onClick={onClose}>
          Close
        </Button>
        <div className="flex items-center gap-2">
          <Link to={`/suppliers/edit/${supplier.id}`}>
            <Button variant="outline" size="sm">
              <span className="flex items-center gap-1.5">
                <svg className="size-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
                </svg>
                Edit
              </span>
            </Button>
          </Link>
          <Link to={`/suppliers/view/${supplier.id}`}>
            <Button size="sm">
              <span className="flex items-center gap-1.5">
                <svg className="size-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
                </svg>
                View Full Page
              </span>
            </Button>
          </Link>
        </div>
      </div>
    </Modal>
  );
}
