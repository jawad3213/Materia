import React from "react";
import { Link } from "react-router-dom";
import Badge from "../../../shared/components/ui/badge/Badge";
import type { MaterialListItem } from "../types/MaterialListItem";

export interface MaterialCardItem extends Partial<MaterialListItem> {
  id: string;
  code: string;
  name: string;
  alternativeName?: string;
  shortDescription?: string;
  description?: string;
  searchKeywords?: string;
  materialType: string;
  status: string;
  categoryId?: string;
  categoryName?: string;
  unitOfMeasure?: string;
  currentStock?: number;
  standardPrice?: number | string;
  standardPriceCurrency?: string;
  currencyCode?: string;
}

export interface MaterialCardProps {
  material: MaterialCardItem;
  highlightKeyword?: string;
  onCardClick?: (material: MaterialCardItem) => void;
}

const statusColorMap: Record<string, "success" | "warning" | "error" | "info" | "light"> = {
  ACTIVE: "success",
  INACTIVE: "light",
  DISCONTINUED: "error",
  PENDING: "warning",
  OUT_OF_STOCK: "info",
};

const colors = ["blue", "purple", "green", "orange", "pink", "cyan"] as const;
const colorClasses: Record<string, string> = {
  blue: "bg-blue-100 text-blue-700 dark:bg-blue-500/20 dark:text-blue-400",
  purple: "bg-purple-100 text-purple-700 dark:bg-purple-500/20 dark:text-purple-400",
  green: "bg-green-100 text-green-700 dark:bg-green-500/20 dark:text-green-400",
  orange: "bg-orange-100 text-orange-700 dark:bg-orange-500/20 dark:text-orange-400",
  pink: "bg-pink-100 text-pink-700 dark:bg-pink-500/20 dark:text-pink-400",
  cyan: "bg-cyan-100 text-cyan-700 dark:bg-cyan-500/20 dark:text-cyan-400",
};

function getColorForName(name: string) {
  const sum = (name || "").split("").reduce((acc, char) => acc + char.charCodeAt(0), 0);
  return colors[sum % colors.length];
}

function truncate(text: string | undefined, maxLen: number): string {
  if (!text) return "—";
  return text.length > maxLen ? text.substring(0, maxLen) + "..." : text;
}

function formatType(type: string): string {
  if (!type) return "—";
  return type.replace(/_/g, " ").replace(/\b\w/g, (l) => l.toUpperCase());
}

function formatStatus(status: string): string {
  if (!status) return "—";
  return status.replace(/_/g, " ").replace(/\b\w/g, (l) => l.toUpperCase());
}

const HighlightText = ({ text, highlight }: { text: string | React.ReactNode; highlight?: string }) => {
  if (!text || typeof text !== "string") return <>{text}</>;
  if (!highlight || highlight.trim() === "") return <>{text}</>;

  const parts = text.split(new RegExp(`(${highlight})`, "gi"));
  return (
    <>
      {parts.map((part, i) =>
        part.toLowerCase() === highlight.toLowerCase() ? (
          <mark
            key={i}
            className="bg-yellow-200 text-yellow-900 rounded-sm px-0.5 dark:bg-yellow-500/30 dark:text-yellow-200"
          >
            {part}
          </mark>
        ) : (
          part
        )
      )}
    </>
  );
};

export default function MaterialCard({ material, highlightKeyword, onCardClick }: MaterialCardProps) {
  const avatarColor = getColorForName(material.name);
  const initials = material.name ? material.name.substring(0, 2).toUpperCase() : "MA";

  return (
    <div
      onClick={() => onCardClick?.(material)}
      className="group relative w-full cursor-pointer rounded-xl border border-gray-200 bg-white p-5 transition-all duration-200 hover:shadow-lg hover:border-brand-400 hover:ring-2 hover:ring-brand-400/20 dark:border-white/[0.08] dark:bg-white/[0.03] dark:hover:border-brand-500/50 dark:hover:ring-brand-500/20"
    >
      {/* Row 1: Avatar + Name/Code + Status + Quick View */}
      <div className="flex items-start justify-between mb-4">
        <div className="flex items-center gap-3 min-w-0">
          <div
            className={`flex h-11 w-11 shrink-0 items-center justify-center rounded-full text-sm font-semibold ${colorClasses[avatarColor]}`}
          >
            {initials}
          </div>
          <div className="min-w-0">
            <h4 className="text-sm font-semibold text-gray-900 dark:text-white leading-tight mb-0.5 truncate group-hover:text-brand-600 dark:group-hover:text-brand-400 transition-colors">
              <HighlightText text={material.name} highlight={highlightKeyword} />
            </h4>
            <div className="flex items-center gap-2">
              <span className="text-xs font-mono font-medium text-gray-500 dark:text-gray-400">
                <HighlightText text={material.code} highlight={highlightKeyword} />
              </span>
              {(material.standardPriceCurrency || material.currencyCode) && (
                <span className="inline-flex items-center px-1.5 py-0.2 rounded text-[10px] font-bold bg-gray-100 text-gray-600 dark:bg-gray-800 dark:text-gray-300">
                  {material.standardPriceCurrency || material.currencyCode}
                </span>
              )}
            </div>
          </div>
        </div>

        <div className="flex items-center gap-1.5 shrink-0 ml-2" onClick={(e) => e.stopPropagation()}>
          <Badge
            variant="light"
            size="sm"
            color={statusColorMap[material.status] || "light"}
          >
            {formatStatus(material.status || "ACTIVE")}
          </Badge>
          <button
            type="button"
            onClick={() => onCardClick?.(material)}
            className="flex items-center justify-center p-1.5 rounded-lg text-gray-400 hover:text-brand-500 hover:bg-brand-50 dark:hover:bg-brand-500/10 dark:hover:text-brand-500 transition-colors"
            title="Quick view info"
          >
            <svg className="size-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
          </button>
          <Link to={`/materials/view/${material.id}`} title="View full material details">
            <button className="flex items-center justify-center p-1.5 rounded-lg text-gray-400 hover:text-brand-500 hover:bg-brand-50 dark:hover:bg-brand-500/10 dark:hover:text-brand-500 transition-colors">
              <svg className="size-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M10 6H6a2 2 0 00-2 2v10a2 2 0 002 2h10a2 2 0 002-2v-4M14 4h6m0 0v6m0-6L10 14" />
              </svg>
            </button>
          </Link>
        </div>
      </div>

      {/* Row 2: Grid for Attributes (styled container matching SupplierCard) */}
      <div className="grid grid-cols-2 gap-x-4 gap-y-2.5 mb-3 bg-gray-50/75 dark:bg-white/[0.02] p-3 rounded-lg border border-gray-100 dark:border-white/[0.04]">
        {/* Type */}
        <div>
          <span className="block text-[10px] font-medium uppercase tracking-wider text-gray-400 dark:text-gray-500 mb-0.5">
            Type
          </span>
          <span className="text-xs font-medium text-gray-800 dark:text-gray-200 truncate block">
            {formatType(material.materialType)}
          </span>
        </div>

        {/* Category */}
        <div>
          <span className="block text-[10px] font-medium uppercase tracking-wider text-gray-400 dark:text-gray-500 mb-0.5">
            Category
          </span>
          <span className="text-xs font-medium text-gray-800 dark:text-gray-200 truncate block">
            {material.categoryName ? truncate(material.categoryName, 22) : (material.categoryId ? truncate(material.categoryId, 22) : "—")}
          </span>
        </div>

        {/* Current Stock */}
        <div>
          <span className="block text-[10px] font-medium uppercase tracking-wider text-gray-400 dark:text-gray-500 mb-0.5">
            Current Stock
          </span>
          <span className={`text-xs font-semibold truncate block ${
            (material.currentStock ?? 0) <= 0 
              ? "text-error-600 dark:text-error-400 font-bold" 
              : "text-gray-800 dark:text-gray-200"
          }`}>
            {material.currentStock ?? 0} {material.unitOfMeasure || ""}
          </span>
        </div>

        {/* Standard Price */}
        <div>
          <span className="block text-[10px] font-medium uppercase tracking-wider text-gray-400 dark:text-gray-500 mb-0.5">
            Price
          </span>
          <span className="text-xs font-semibold text-gray-800 dark:text-gray-200 truncate block">
            {material.standardPrice !== undefined && material.standardPrice !== null
              ? `${material.standardPrice} ${material.standardPriceCurrency || material.currencyCode || "MAD"}`
              : "—"}
          </span>
        </div>
      </div>

      {/* Row 3: Tags / Extra info */}
      {(material.alternativeName || material.searchKeywords) && (
        <div className="mb-3 flex items-center gap-2 flex-wrap text-xs">
          {material.alternativeName && (
            <span className="text-xs text-gray-500 dark:text-gray-400 truncate max-w-full">
              <span className="text-[10px] uppercase font-semibold text-gray-400 dark:text-gray-500 mr-1">Alt:</span>
              <HighlightText text={truncate(material.alternativeName, 40)} highlight={highlightKeyword} />
            </span>
          )}
          {material.searchKeywords && (
            <span className="px-2 py-0.5 rounded text-[11px] font-medium bg-gray-100 text-gray-600 dark:bg-gray-800 dark:text-gray-300">
              <HighlightText text={truncate(material.searchKeywords, 30)} highlight={highlightKeyword} />
            </span>
          )}
        </div>
      )}

      {/* Row 4: Description (clamped to 2 lines with highlight) */}
      {(material.description || material.shortDescription) && (
        <div className="pt-2.5 border-t border-gray-100 dark:border-white/[0.06]">
          <p className="text-xs text-gray-500 dark:text-gray-400 leading-relaxed line-clamp-2">
            <HighlightText text={material.description || material.shortDescription} highlight={highlightKeyword} />
          </p>
        </div>
      )}

      {/* Tap hint footer */}
      <div className="mt-2.5 flex items-center justify-between text-[11px] text-gray-400 dark:text-gray-500 pt-2 border-t border-gray-50 dark:border-white/[0.02]">
        <span className="flex items-center gap-1 text-brand-500/80 group-hover:text-brand-500 transition-colors">
          <svg className="size-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 15l-2 5L9 9l11 4-5 2zm0 0l5 5M7.188 2.239l.777 2.897M5.136 7.965l-2.898-.777M13.95 4.05l-2.122 2.122m-5.657 5.656l-2.12 2.122" />
          </svg>
          Tap card for details
        </span>
        <span>{formatType(material.materialType || "")}</span>
      </div>
    </div>
  );
}
