import React, { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { Modal } from "../../../shared/components/ui/modal";
import Badge from "../../../shared/components/ui/badge/Badge";
import Button from "../../../shared/components/ui/button/Button";
import { materialApi } from "../services/materialApi";
import type { MaterialListItem } from "../types/MaterialListItem";
import type { Material } from "../types/Material";

interface MaterialInfoModalProps {
  isOpen: boolean;
  onClose: () => void;
  material: MaterialListItem | any | null;
}

const colorClasses: Record<string, string> = {
  red: "bg-red-50 text-red-500 dark:bg-red-500/15 dark:text-red-500",
  orange: "bg-orange-50 text-orange-500 dark:bg-orange-500/15 dark:text-orange-500",
  purple: "bg-purple-50 text-purple-500 dark:bg-purple-500/15 dark:text-purple-500",
  green: "bg-green-50 text-green-500 dark:bg-green-500/15 dark:text-green-500",
  blue: "bg-brand-50 text-brand-500 dark:bg-brand-500/15 dark:text-brand-500",
};

const colors = ["red", "orange", "purple", "green", "blue"];

function getColorForName(name: string) {
  if (!name) return "blue";
  const sum = name.split("").reduce((acc, char) => acc + char.charCodeAt(0), 0);
  return colors[sum % colors.length];
}

function getInitials(name: string) {
  if (!name) return "M";
  return name.substring(0, 2).toUpperCase();
}

function formatType(type: string): string {
  if (!type) return "—";
  return type.replace(/_/g, " ").replace(/\b\w/g, (l) => l.toUpperCase());
}

function formatStatus(status: string): string {
  if (!status) return "—";
  return status.replace(/_/g, " ").replace(/\b\w/g, (l) => l.toUpperCase());
}

function formatDate(dateStr?: string) {
  if (!dateStr) return "—";
  try {
    return new Date(dateStr).toLocaleString();
  } catch {
    return dateStr;
  }
}

export default function MaterialInfoModal({
  isOpen,
  onClose,
  material,
}: MaterialInfoModalProps) {
  const [fullMaterial, setFullMaterial] = useState<Material | null>(null);

  useEffect(() => {
    if (isOpen && material?.id) {
      materialApi
        .getById(material.id)
        .then((res) => {
          setFullMaterial(res.data);
        })
        .catch(() => {
          setFullMaterial(null);
        });
    } else {
      setFullMaterial(null);
    }
  }, [isOpen, material?.id]);

  if (!material) return null;

  const color = getColorForName(material.name);
  const initials = getInitials(material.name);
  const current = fullMaterial || material;

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
              {current.name}
            </h3>
            <span className="font-mono text-xs px-2 py-0.5 rounded bg-gray-100 dark:bg-gray-800 text-gray-600 dark:text-gray-300 font-semibold">
              {current.code}
            </span>
            <Badge
              size="sm"
              variant="light"
              color={current.status === "ACTIVE" ? "success" : current.status === "DISCONTINUED" ? "error" : "light"}
            >
              {formatStatus(current.status || "ACTIVE")}
            </Badge>
            <Badge size="sm" variant="light" color="primary">
              {formatType(current.materialType)}
            </Badge>
          </div>
          {(current.shortDescription || current.description) && (
            <p className="text-xs text-gray-500 dark:text-gray-400 line-clamp-2">
              {current.shortDescription || current.description}
            </p>
          )}
        </div>
      </div>

      {/* Content Body */}
      <div className="py-5 space-y-5 max-h-[60vh] overflow-y-auto pr-1">
        {/* Section 1: Stock & Inventory */}
        <div>
          <h4 className="text-xs font-semibold uppercase tracking-wider text-gray-400 dark:text-gray-500 mb-2">
            Stock & Inventory
          </h4>
          <div className="grid grid-cols-1 sm:grid-cols-3 gap-3 bg-gray-50 dark:bg-white/[0.02] p-3.5 rounded-xl border border-gray-100 dark:border-white/[0.04]">
            <div>
              <span className="block text-[11px] text-gray-400 dark:text-gray-500">Current Stock</span>
              <span className={`text-sm font-semibold block ${
                (current.currentStock ?? 0) <= 0
                  ? "text-error-600 dark:text-error-400 font-bold"
                  : "text-gray-800 dark:text-white/90"
              }`}>
                {current.currentStock ?? 0} {current.unitOfMeasure || ""}
              </span>
            </div>
            <div>
              <span className="block text-[11px] text-gray-400 dark:text-gray-500">Available Stock</span>
              <span className="text-sm font-medium text-gray-800 dark:text-white/90 block">
                {current.availableStock !== undefined ? `${current.availableStock} ${current.unitOfMeasure || ""}` : `${current.currentStock ?? 0} ${current.unitOfMeasure || ""}`}
              </span>
            </div>
            <div>
              <span className="block text-[11px] text-gray-400 dark:text-gray-500">Stock Status</span>
              {current.isOutOfStock || (current.currentStock ?? 0) <= 0 ? (
                <Badge size="sm" variant="light" color="error">
                  Out of Stock
                </Badge>
              ) : current.isBelowMinimumStock ? (
                <Badge size="sm" variant="light" color="warning">
                  Low Stock
                </Badge>
              ) : (
                <Badge size="sm" variant="light" color="success">
                  In Stock
                </Badge>
              )}
            </div>
          </div>
        </div>

        {/* Section 2: Pricing & Commercials */}
        <div>
          <h4 className="text-xs font-semibold uppercase tracking-wider text-gray-400 dark:text-gray-500 mb-2">
            Pricing & Commercials
          </h4>
          <div className="grid grid-cols-1 sm:grid-cols-3 gap-3 bg-gray-50 dark:bg-white/[0.02] p-3.5 rounded-xl border border-gray-100 dark:border-white/[0.04]">
            <div>
              <span className="block text-[11px] text-gray-400 dark:text-gray-500">Standard Price</span>
              <span className="text-sm font-semibold text-gray-800 dark:text-white/90 block">
                {current.standardPrice !== undefined && current.standardPrice !== null
                  ? `${current.standardPrice} ${current.standardPriceCurrency || current.currencyCode || "MAD"}`
                  : "—"}
              </span>
            </div>
            <div>
              <span className="block text-[11px] text-gray-400 dark:text-gray-500">Currency</span>
              <Badge size="sm" variant="light" color="success">
                {current.standardPriceCurrency || current.currencyCode || "MAD"}
              </Badge>
            </div>
            <div>
              <span className="block text-[11px] text-gray-400 dark:text-gray-500">Unit of Measure</span>
              <span className="text-sm font-medium text-gray-800 dark:text-white/90 block">
                {current.unitOfMeasure || "—"}
              </span>
            </div>
          </div>
        </div>

        {/* Section 3: Classification & Details */}
        <div>
          <h4 className="text-xs font-semibold uppercase tracking-wider text-gray-400 dark:text-gray-500 mb-2">
            Classification & Identifiers
          </h4>
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 bg-gray-50 dark:bg-white/[0.02] p-3.5 rounded-xl border border-gray-100 dark:border-white/[0.04]">
            <div>
              <span className="block text-[11px] text-gray-400 dark:text-gray-500">Category</span>
              <span className="text-sm font-medium text-gray-800 dark:text-white/90 block">
                {current.categoryName || current.categoryId || "—"}
              </span>
            </div>
            <div>
              <span className="block text-[11px] text-gray-400 dark:text-gray-500">Alternative Name</span>
              <span className="text-sm font-medium text-gray-800 dark:text-white/90 block truncate">
                {current.alternativeName || "—"}
              </span>
            </div>
            {current.searchKeywords && (
              <div className="col-span-1 sm:col-span-2">
                <span className="block text-[11px] text-gray-400 dark:text-gray-500 mb-1">Keywords</span>
                <span className="text-xs font-medium text-gray-600 dark:text-gray-300 bg-gray-100 dark:bg-gray-800 px-2 py-1 rounded inline-block">
                  {current.searchKeywords}
                </span>
              </div>
            )}
          </div>
        </div>

        {/* Description Section (Full) */}
        {current.description && (
          <div>
            <h4 className="text-xs font-semibold uppercase tracking-wider text-gray-400 dark:text-gray-500 mb-1">
              Full Description
            </h4>
            <p className="text-xs text-gray-600 dark:text-gray-300 leading-relaxed bg-gray-50 dark:bg-white/[0.02] p-3 rounded-lg border border-gray-100 dark:border-white/[0.04]">
              {current.description}
            </p>
          </div>
        )}

        {/* Audit Metadata */}
        {(current.createdAt || current.createdBy) && (
          <div className="flex flex-wrap items-center justify-between text-[11px] text-gray-400 dark:text-gray-500 pt-2 border-t border-gray-100 dark:border-white/[0.05]">
            <span>Created: {formatDate(current.createdAt)} {current.createdBy ? `by ${current.createdBy}` : ""}</span>
            {current.updatedAt && (
              <span>Updated: {formatDate(current.updatedAt)}</span>
            )}
          </div>
        )}
      </div>

      {/* Footer */}
      <div className="flex items-center justify-between pt-4 border-t border-gray-100 dark:border-white/[0.06]">
        <Button variant="outline" size="sm" onClick={onClose}>
          Close
        </Button>
        <div className="flex items-center gap-2">
          <Link to={`/materials/edit-material/${current.id}`}>
            <Button variant="outline" size="sm">
              <span className="flex items-center gap-1.5">
                <svg className="size-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
                </svg>
                Edit
              </span>
            </Button>
          </Link>
          <Link to={`/materials/view/${current.id}`}>
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
