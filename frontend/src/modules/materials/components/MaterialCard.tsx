import React from "react";
import Badge from "../../../shared/components/ui/badge/Badge";

interface MaterialCardItem {
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
}

interface MaterialCardProps {
  material: MaterialCardItem;
}

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
  const sum = name.split("").reduce((acc, char) => acc + char.charCodeAt(0), 0);
  return colors[sum % colors.length];
}

function truncate(text: string | undefined, maxLen: number): string {
  if (!text) return "—";
  return text.length > maxLen ? text.substring(0, maxLen) + "..." : text;
}

function formatType(type: string): string {
  return type.replace(/_/g, " ").replace(/\b\w/g, (l) => l.toUpperCase());
}

function formatStatus(status: string): string {
  return status.replace(/_/g, " ").replace(/\b\w/g, (l) => l.toUpperCase());
}

const HighlightText = ({ text, highlight }: { text: string | React.ReactNode, highlight?: string }) => {
  if (!text || typeof text !== 'string') return <>{text}</>;
  if (!highlight || highlight.trim() === "") return <>{text}</>;

  const parts = text.split(new RegExp(`(${highlight})`, 'gi'));
  return (
    <>
      {parts.map((part, i) =>
        part.toLowerCase() === highlight.toLowerCase() ? (
          <mark key={i} className="bg-yellow-200 text-yellow-900 rounded-sm px-0.5 dark:bg-yellow-500/30 dark:text-yellow-200">{part}</mark>
        ) : (
          part
        )
      )}
    </>
  );
};

const MaterialCard: React.FC<MaterialCardProps & { highlightKeyword?: string }> = ({ material, highlightKeyword }) => {
  const avatarColor = getColorForName(material.name);
  const initials = material.name ? material.name.substring(0, 2).toUpperCase() : "MA";

  return (
    <div className="w-full rounded-xl border border-gray-200 bg-white p-5 transition-all duration-200 hover:shadow-lg hover:border-brand-300 dark:border-white/[0.08] dark:bg-white/[0.03] dark:hover:border-brand-500/40 dark:hover:shadow-brand-500/5">
      {/* Row 1: Avatar + Name/Code + Status */}
      <div className="flex items-start justify-between mb-4">
        <div className="flex items-center gap-3">
          <div
            className={`flex h-11 w-11 shrink-0 items-center justify-center rounded-full text-sm font-semibold ${colorClasses[avatarColor]}`}
          >
            {initials}
          </div>
          <div>
            <h4 className="text-sm font-semibold text-gray-900 dark:text-white leading-tight mb-0.5">
              <HighlightText text={material.name} highlight={highlightKeyword} />
            </h4>
            <span className="text-xs text-gray-400 dark:text-gray-500 italic block">
              Alt: <HighlightText text={material.alternativeName ? truncate(material.alternativeName, 40) : "—"} highlight={highlightKeyword} />
            </span>
          </div>
        </div>
        <Badge
          variant="light"
          size="sm"
          color={statusColorMap[material.status] || "light"}
        >
          {formatStatus(material.status)}
        </Badge>
      </div>

      {/* Row 2: Grid for Attributes */}
      <div className="grid grid-cols-2 gap-x-6 gap-y-3 mb-3">
        {/* Code */}
        <div>
          <span className="block text-[10px] font-medium uppercase tracking-wider text-gray-400 dark:text-gray-500 mb-0.5">
            Code
          </span>
          <span className="text-xs font-mono font-medium text-gray-700 dark:text-gray-300">
            <HighlightText text={material.code} highlight={highlightKeyword} />
          </span>
        </div>

        {/* Material Type */}
        <div>
          <span className="block text-[10px] font-medium uppercase tracking-wider text-gray-400 dark:text-gray-500 mb-0.5">
            Type
          </span>
          <Badge
            variant="light"
            size="sm"
            color={typeColorMap[material.materialType] || "primary"}
          >
            {formatType(material.materialType)}
          </Badge>
        </div>

        {/* Category Name */}
        <div>
          <span className="block text-[10px] font-medium uppercase tracking-wider text-gray-400 dark:text-gray-500 mb-0.5">
            Category
          </span>
          <span className="text-xs text-gray-700 dark:text-gray-300">
            {material.categoryName ? truncate(material.categoryName, 20) : (material.categoryId ? truncate(material.categoryId, 20) : "—")}
          </span>
        </div>

        {/* Search Keywords */}
        <div>
          <span className="block text-[10px] font-medium uppercase tracking-wider text-gray-400 dark:text-gray-500 mb-0.5">
            Keywords
          </span>
          <span className="text-xs text-gray-600 dark:text-gray-400">
            <HighlightText text={truncate(material.searchKeywords, 30)} highlight={highlightKeyword} />
          </span>
        </div>
      </div>

      {/* Row 3: Short Description (Long line under attributes) */}
      <div className="mb-3">
         <span className="block text-[10px] font-medium uppercase tracking-wider text-gray-400 dark:text-gray-500 mb-0.5">
            Short Desc.
          </span>
          <p className="text-xs text-gray-600 dark:text-gray-400 truncate">
            <HighlightText text={material.shortDescription || "—"} highlight={highlightKeyword} />
          </p>
      </div>

      {/* Row 4: Full Description (Max 4 lines with ellipsis) */}
      <div className="pt-3 border-t border-gray-100 dark:border-white/[0.06]">
        <span className="block text-[10px] font-medium uppercase tracking-wider text-gray-400 dark:text-gray-500 mb-0.5">
          Description
        </span>
        <p className="text-xs text-gray-500 dark:text-gray-400 leading-relaxed line-clamp-4">
          <HighlightText text={material.description || "—"} highlight={highlightKeyword} />
        </p>
      </div>
    </div>
  );
};

export default MaterialCard;
