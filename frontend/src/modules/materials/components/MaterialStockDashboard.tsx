import React from "react";
import type { Material } from "../types/Material";

interface MaterialStockDashboardProps {
  material: Material;
  onOpenReorder?: () => void;
}

export default function MaterialStockDashboard({
  material,
  onOpenReorder,
}: MaterialStockDashboardProps) {
  const currentStock = material.currentStock ?? 0;
  const availableStock = material.availableStock ?? 0;
  const safetyStock = material.safetyStock ?? 0;
  const reorderPoint = material.reorderPoint ?? 0;
  const minStock = material.minimumStock ?? 0;
  const maxStock = material.maximumStock ?? 0;
  const onOrder = material.stockOnOrder ?? 0;
  const reservedStock = Math.max(0, currentStock - availableStock);
  const unit = material.unitOfMeasure || "Units";

  // Determine stock health status
  type HealthStatus = "OUT_OF_STOCK" | "CRITICAL" | "REORDER_NEEDED" | "OVERSTOCKED" | "OPTIMAL";
  
  let healthStatus: HealthStatus = "OPTIMAL";
  if (currentStock === 0 || material.isOutOfStock) {
    healthStatus = "OUT_OF_STOCK";
  } else if (material.stockStatus === "CRITICAL" || (safetyStock > 0 && currentStock <= safetyStock)) {
    healthStatus = "CRITICAL";
  } else if (material.isReorderNeeded || (reorderPoint > 0 && currentStock <= reorderPoint)) {
    healthStatus = "REORDER_NEEDED";
  } else if (maxStock > 0 && currentStock > maxStock) {
    healthStatus = "OVERSTOCKED";
  }

  // Configuration for status banner and colors
  const statusConfig = {
    OUT_OF_STOCK: {
      label: "Out of Stock",
      subtitle: "Inventory is completely depleted. Fulfillments are blocked.",
      badgeClass: "bg-red-100 text-red-700 border-red-200 dark:bg-red-500/20 dark:text-red-300 dark:border-red-500/30",
      accentColor: "#EF4444",
      accentBorder: "border-red-500",
      accentBg: "bg-red-50 dark:bg-red-500/10",
      icon: (
        <svg className="size-5 text-red-600 dark:text-red-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M18.364 18.364A9 9 0 005.636 5.636m12.728 12.728A9 9 0 015.636 5.636m12.728 12.728L5.636 5.636" />
        </svg>
      ),
    },
    CRITICAL: {
      label: "Critical Stock",
      subtitle: `Stock is below safety buffer (${safetyStock} ${unit}). High risk of stockout.`,
      badgeClass: "bg-orange-100 text-orange-800 border-orange-200 dark:bg-orange-500/20 dark:text-orange-300 dark:border-orange-500/30",
      accentColor: "#F97316",
      accentBorder: "border-orange-500",
      accentBg: "bg-orange-50 dark:bg-orange-500/10",
      icon: (
        <svg className="size-5 text-orange-600 dark:text-orange-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
        </svg>
      ),
    },
    REORDER_NEEDED: {
      label: "Reorder Required",
      subtitle: `Stock is at or below reorder threshold (${reorderPoint} ${unit}). Replenishment needed.`,
      badgeClass: "bg-amber-100 text-amber-800 border-amber-200 dark:bg-amber-500/20 dark:text-amber-300 dark:border-amber-500/30",
      accentColor: "#F59E0B",
      accentBorder: "border-amber-500",
      accentBg: "bg-amber-50 dark:bg-amber-500/10",
      icon: (
        <svg className="size-5 text-amber-600 dark:text-amber-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
        </svg>
      ),
    },
    OVERSTOCKED: {
      label: "Overstocked",
      subtitle: `Stock exceeds maximum warehouse storage limit (${maxStock} ${unit}).`,
      badgeClass: "bg-indigo-100 text-indigo-800 border-indigo-200 dark:bg-indigo-500/20 dark:text-indigo-300 dark:border-indigo-500/30",
      accentColor: "#6366F1",
      accentBorder: "border-indigo-500",
      accentBg: "bg-indigo-50 dark:bg-indigo-500/10",
      icon: (
        <svg className="size-5 text-indigo-600 dark:text-indigo-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 10l7-7m0 0l7 7m-7-7v18" />
        </svg>
      ),
    },
    OPTIMAL: {
      label: "Optimal Stock Level",
      subtitle: "Inventory is operating within healthy and safe warehouse limits.",
      badgeClass: "bg-emerald-100 text-emerald-800 border-emerald-200 dark:bg-emerald-500/20 dark:text-emerald-300 dark:border-emerald-500/30",
      accentColor: "#10B981",
      accentBorder: "border-emerald-500",
      accentBg: "bg-emerald-50 dark:bg-emerald-500/10",
      icon: (
        <svg className="size-5 text-emerald-600 dark:text-emerald-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
        </svg>
      ),
    },
  }[healthStatus];

  // Dynamic Scale for visualization
  // Max scale baseline ensures thresholds are proportionally spaced
  const effectiveMax = maxStock > 0 ? maxStock : Math.max(reorderPoint * 2, currentStock * 1.5, 100);
  const maxScale = Math.max(effectiveMax, currentStock * 1.15, 10);

  // Percentage calculations clamped between 0 and 100
  const toPercent = (val: number) => Math.min(100, Math.max(0, (val / maxScale) * 100));

  const safetyPct = toPercent(safetyStock);
  const reorderPct = toPercent(reorderPoint);
  const maxStockPct = maxStock > 0 ? toPercent(maxStock) : 100;
  const currentStockPct = toPercent(currentStock);

  // Capacity fill percentage for radial gauge
  const capacityDenominator = maxStock > 0 ? maxStock : Math.max(reorderPoint * 1.5, currentStock, 1);
  const capacityFillPercent = Math.min(100, Math.round((currentStock / capacityDenominator) * 100));

  // Radial Gauge SVG calculations (Semi-circle 180 deg)
  const radius = 70;
  const strokeWidth = 14;
  const circumference = Math.PI * radius; // 180 degrees arc length: pi * r
  const strokeDashoffset = circumference - (capacityFillPercent / 100) * circumference;

  return (
    <div className="rounded-2xl border border-gray-200 bg-white p-6 shadow-xs dark:border-white/[0.08] dark:bg-white/[0.03]">
      {/* Header & Status Banner */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-6 border-b border-gray-100 dark:border-white/[0.06]">
        <div className="flex items-center gap-3">
          <div className={`flex size-11 items-center justify-center rounded-xl ${statusConfig.accentBg} transition-colors`}>
            {statusConfig.icon}
          </div>
          <div>
            <div className="flex items-center gap-2.5">
              <h3 className="text-lg font-bold text-gray-900 dark:text-white">
                Inventory & Stock Analysis
              </h3>
              <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-semibold border ${statusConfig.badgeClass}`}>
                {statusConfig.label}
              </span>
            </div>
            <p className="text-xs text-gray-500 dark:text-gray-400 mt-0.5">
              {statusConfig.subtitle}
            </p>
          </div>
        </div>

        {/* Trigger reorder button if applicable */}
        {onOpenReorder && (healthStatus === "CRITICAL" || healthStatus === "REORDER_NEEDED" || healthStatus === "OUT_OF_STOCK") && (
          <button
            type="button"
            onClick={onOpenReorder}
            className="flex items-center gap-2 self-start sm:self-auto rounded-lg bg-brand-500 px-4 py-2 text-xs font-semibold text-white shadow-xs hover:bg-brand-600 transition-colors"
          >
            <svg className="size-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 10V3L4 14h7v7l9-11h-7z" />
            </svg>
            1-Click Reorder
          </button>
        )}
      </div>

      {/* Main Visual Dashboard Grid: Radial Gauge (Left) + Multi-Zone Bar (Right) */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6 py-6 border-b border-gray-100 dark:border-white/[0.06] items-center">
        
        {/* Left Dial: Semi-Circle Warehouse Capacity Gauge (4 Cols) */}
        <div className="lg:col-span-4 flex flex-col items-center justify-center p-4 rounded-xl bg-gray-50/60 dark:bg-white/[0.02] border border-gray-100 dark:border-white/[0.04]">
          <span className="text-xs font-semibold uppercase tracking-wider text-gray-400 dark:text-gray-500 mb-2">
            Capacity Utilization
          </span>

          <div className="relative flex items-center justify-center">
            <svg width="190" height="110" viewBox="0 0 190 110" className="overflow-visible">
              {/* Background Track */}
              <path
                d="M 15 100 A 70 70 0 0 1 175 100"
                fill="none"
                stroke="currentColor"
                strokeWidth={strokeWidth}
                strokeLinecap="round"
                className="text-gray-200 dark:text-gray-800"
              />
              {/* Filled Arc (only rendered if fill > 0 to prevent stray round cap at 0%) */}
              {capacityFillPercent > 0 && (
                <path
                  d="M 15 100 A 70 70 0 0 1 175 100"
                  fill="none"
                  stroke={statusConfig.accentColor}
                  strokeWidth={strokeWidth}
                  strokeLinecap="round"
                  strokeDasharray={circumference}
                  strokeDashoffset={strokeDashoffset}
                  className="transition-all duration-1000 ease-out"
                />
              )}
            </svg>

            {/* Inner Center Metrics */}
            <div className="absolute top-10 flex flex-col items-center">
              <span className="text-3xl font-extrabold tracking-tight text-gray-900 dark:text-white">
                {capacityFillPercent}%
              </span>
              <span className="text-[11px] font-medium text-gray-500 dark:text-gray-400">
                of max capacity
              </span>
            </div>
          </div>

          <div className="flex items-center justify-between w-full max-w-[200px] text-[11px] text-gray-400 dark:text-gray-500 mt-1 px-2 font-mono">
            <span>0 {unit}</span>
            <span>{maxStock > 0 ? `${maxStock} ${unit}` : "Unlimited"}</span>
          </div>
        </div>

        {/* Right Section: Multi-Zone Stock Level Spectrum Bar (8 Cols) */}
        <div className="lg:col-span-8 flex flex-col justify-center space-y-4">
          <div className="flex items-center justify-between">
            <div>
              <h4 className="text-sm font-semibold text-gray-900 dark:text-white">
                Stock Level Spectrum
              </h4>
              <p className="text-xs text-gray-500 dark:text-gray-400">
                Real-time position against configured safety and reorder thresholds
              </p>
            </div>
            <div className="text-right">
              <span className="text-sm font-bold text-gray-900 dark:text-white">
                {currentStock} <span className="text-xs font-normal text-gray-500">{unit}</span>
              </span>
              <span className="block text-[10px] text-gray-400">Current On Hand</span>
            </div>
          </div>

          {/* Continuous Multi-Zone Spectrum Bar */}
          <div className="relative pt-7 pb-2 select-none">
            {/* Pointer / Marker for Current Stock (clamped so it never clips or overflows) */}
            <div
              className="absolute top-0 -translate-x-1/2 transition-all duration-700 ease-out z-20 flex flex-col items-center pointer-events-none"
              style={{ left: `clamp(26px, ${currentStockPct}%, calc(100% - 26px))` }}
            >
              <div
                className="px-2.5 py-0.5 rounded-md text-[11px] font-bold text-white shadow-md flex items-center gap-1 whitespace-nowrap"
                style={{ backgroundColor: statusConfig.accentColor }}
              >
                <span>{currentStock} {unit}</span>
              </div>
              <div
                className="w-0 h-0 border-x-4 border-x-transparent border-t-4"
                style={{ borderTopColor: statusConfig.accentColor }}
              />
            </div>

            {/* Threshold Bar Track with clean zone segments and dividers */}
            <div className="relative h-5 w-full rounded-full overflow-hidden bg-gray-100 dark:bg-gray-800 flex shadow-inner">
              {/* Zone 1: Critical (0 -> Safety) */}
              <div
                style={{ width: `${Math.max(2, safetyPct)}%` }}
                className="h-full bg-gradient-to-r from-red-500 to-orange-500 relative"
                title={`Critical Zone: 0 to ${safetyStock} ${unit}`}
              />

              {/* Zone 2: Reorder (Safety -> Reorder Point) */}
              <div
                style={{ width: `${Math.max(2, Math.max(0, reorderPct - safetyPct))}%` }}
                className="h-full bg-gradient-to-r from-orange-500 to-amber-500 relative"
                title={`Reorder Zone: ${safetyStock} to ${reorderPoint} ${unit}`}
              />

              {/* Zone 3: Optimal (Reorder Point -> Max Stock) */}
              <div
                style={{ width: `${Math.max(2, Math.max(0, maxStockPct - reorderPct))}%` }}
                className="h-full bg-gradient-to-r from-emerald-500 to-teal-500 relative"
                title={`Optimal Operating Zone: ${reorderPoint} to ${maxStock || maxScale} ${unit}`}
              />

              {/* Zone 4: Overstock (Above Max Stock) */}
              {maxStockPct < 100 && (
                <div
                  style={{ width: `${100 - maxStockPct}%` }}
                  className="h-full bg-gradient-to-r from-indigo-500 to-purple-500 relative"
                  title={`Overstock Zone: > ${maxStock} ${unit}`}
                />
              )}

              {/* Subtle internal tick marks on the bar */}
              {safetyStock > 0 && (
                <div
                  className="absolute top-0 bottom-0 w-0.5 bg-white/70 shadow-xs z-10"
                  style={{ left: `${safetyPct}%` }}
                />
              )}
              {reorderPoint > 0 && (
                <div
                  className="absolute top-0 bottom-0 w-0.5 bg-white/70 shadow-xs z-10"
                  style={{ left: `${reorderPct}%` }}
                />
              )}
              {maxStock > 0 && maxStockPct < 100 && (
                <div
                  className="absolute top-0 bottom-0 w-0.5 bg-white/70 shadow-xs z-10"
                  style={{ left: `${maxStockPct}%` }}
                />
              )}
            </div>
          </div>

          {/* Structured Threshold & Zone Cards (Immune to overlapping/collisions) */}
          <div className="grid grid-cols-2 sm:grid-cols-4 gap-2 pt-1">
            {/* Critical Zone Card */}
            <div className="p-2.5 rounded-lg bg-red-50/60 dark:bg-red-500/10 border border-red-100 dark:border-red-500/20">
              <div className="flex items-center gap-1.5 text-[11px] font-semibold text-red-600 dark:text-red-400">
                <span className="size-2 rounded-full bg-red-500 shrink-0" />
                <span className="truncate">Critical Buffer</span>
              </div>
              <div className="text-xs font-bold text-gray-900 dark:text-white mt-1">
                &lt; {safetyStock} <span className="font-normal text-[10px] text-gray-500">{unit}</span>
              </div>
            </div>

            {/* Reorder Zone Card */}
            <div className="p-2.5 rounded-lg bg-amber-50/60 dark:bg-amber-500/10 border border-amber-100 dark:border-amber-500/20">
              <div className="flex items-center gap-1.5 text-[11px] font-semibold text-amber-600 dark:text-amber-400">
                <span className="size-2 rounded-full bg-amber-500 shrink-0" />
                <span className="truncate">Reorder Point</span>
              </div>
              <div className="text-xs font-bold text-gray-900 dark:text-white mt-1">
                {safetyStock} – {reorderPoint} <span className="font-normal text-[10px] text-gray-500">{unit}</span>
              </div>
            </div>

            {/* Optimal Zone Card */}
            <div className="p-2.5 rounded-lg bg-emerald-50/60 dark:bg-emerald-500/10 border border-emerald-100 dark:border-emerald-500/20">
              <div className="flex items-center gap-1.5 text-[11px] font-semibold text-emerald-600 dark:text-emerald-400">
                <span className="size-2 rounded-full bg-emerald-500 shrink-0" />
                <span className="truncate">Optimal Zone</span>
              </div>
              <div className="text-xs font-bold text-gray-900 dark:text-white mt-1">
                {reorderPoint} – {maxStock > 0 ? `${maxStock} ` : "∞ "}<span className="font-normal text-[10px] text-gray-500">{maxStock > 0 ? unit : ""}</span>
              </div>
            </div>

            {/* Max Limit Card */}
            <div className="p-2.5 rounded-lg bg-indigo-50/60 dark:bg-indigo-500/10 border border-indigo-100 dark:border-indigo-500/20">
              <div className="flex items-center gap-1.5 text-[11px] font-semibold text-indigo-600 dark:text-indigo-400">
                <span className="size-2 rounded-full bg-indigo-500 shrink-0" />
                <span className="truncate">Max Limit</span>
              </div>
              <div className="text-xs font-bold text-gray-900 dark:text-white mt-1">
                {maxStock > 0 ? `${maxStock} ` : "Unlimited "}<span className="font-normal text-[10px] text-gray-500">{maxStock > 0 ? unit : ""}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Stock Allocation & Composition Breakdown (4 Metric Cards) */}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-4 pt-6">
        {/* Physical On Hand */}
        <div className="p-4 rounded-xl border border-gray-100 bg-gray-50/70 dark:border-white/[0.04] dark:bg-white/[0.02]">
          <div className="flex items-center justify-between mb-1.5">
            <span className="text-[11px] font-semibold uppercase tracking-wider text-gray-500 dark:text-gray-400">
              Physical On Hand
            </span>
            <span className="size-2 rounded-full bg-gray-400" />
          </div>
          <div className="text-2xl font-bold text-gray-900 dark:text-white">
            {currentStock} <span className="text-xs font-normal text-gray-500">{unit}</span>
          </div>
          <p className="text-[11px] text-gray-500 dark:text-gray-400 mt-1">
            Total warehouse physical stock
          </p>
        </div>

        {/* Available to Promise */}
        <div className="p-4 rounded-xl border border-brand-100 bg-brand-50/40 dark:border-brand-500/20 dark:bg-brand-500/5">
          <div className="flex items-center justify-between mb-1.5">
            <span className="text-[11px] font-semibold uppercase tracking-wider text-brand-600 dark:text-brand-400">
              Available to Promise
            </span>
            <span className="size-2 rounded-full bg-brand-500" />
          </div>
          <div className="text-2xl font-bold text-brand-600 dark:text-brand-400">
            {availableStock} <span className="text-xs font-normal text-brand-500/70">{unit}</span>
          </div>
          <p className="text-[11px] text-brand-600/70 dark:text-brand-400/70 mt-1">
            Ready for customer dispatch
          </p>
        </div>

        {/* Reserved / Allocated */}
        <div className="p-4 rounded-xl border border-gray-100 bg-gray-50/70 dark:border-white/[0.04] dark:bg-white/[0.02]">
          <div className="flex items-center justify-between mb-1.5">
            <span className="text-[11px] font-semibold uppercase tracking-wider text-gray-500 dark:text-gray-400">
              Reserved / Allocated
            </span>
            <span className="size-2 rounded-full bg-amber-500" />
          </div>
          <div className="text-2xl font-bold text-gray-900 dark:text-white">
            {reservedStock} <span className="text-xs font-normal text-gray-500">{unit}</span>
          </div>
          <p className="text-[11px] text-gray-500 dark:text-gray-400 mt-1">
            Committed to active orders
          </p>
        </div>

        {/* Incoming / On-Order */}
        <div className="p-4 rounded-xl border border-gray-100 bg-gray-50/70 dark:border-white/[0.04] dark:bg-white/[0.02]">
          <div className="flex items-center justify-between mb-1.5">
            <span className="text-[11px] font-semibold uppercase tracking-wider text-gray-500 dark:text-gray-400">
              Incoming On-Order
            </span>
            <span className="size-2 rounded-full bg-emerald-500" />
          </div>
          <div className="text-2xl font-bold text-gray-900 dark:text-white">
            {onOrder} <span className="text-xs font-normal text-gray-500">{unit}</span>
          </div>
          <p className="text-[11px] text-gray-500 dark:text-gray-400 mt-1">
            Pending PO supplier shipments
          </p>
        </div>
      </div>

      {/* Threshold Reference Bar */}
      <div className="mt-4 flex flex-wrap items-center justify-between gap-3 p-3 rounded-xl bg-gray-50/50 dark:bg-white/[0.015] border border-gray-100 dark:border-white/[0.04] text-xs">
        <div className="flex items-center gap-1.5 text-gray-600 dark:text-gray-300">
          <span className="text-gray-400 font-medium">Min Floor:</span>
          <span className="font-semibold">{minStock} {unit}</span>
        </div>
        <div className="flex items-center gap-1.5 text-gray-600 dark:text-gray-300">
          <span className="text-gray-400 font-medium">Safety Buffer:</span>
          <span className="font-semibold text-orange-600 dark:text-orange-400">{safetyStock} {unit}</span>
        </div>
        <div className="flex items-center gap-1.5 text-gray-600 dark:text-gray-300">
          <span className="text-gray-400 font-medium">Reorder Point:</span>
          <span className="font-semibold text-amber-600 dark:text-amber-400">{reorderPoint} {unit}</span>
        </div>
        <div className="flex items-center gap-1.5 text-gray-600 dark:text-gray-300">
          <span className="text-gray-400 font-medium">Max Limit:</span>
          <span className="font-semibold text-emerald-600 dark:text-emerald-400">{maxStock || "Unlimited"} {maxStock ? unit : ""}</span>
        </div>
        {material.economicOrderQuantity != null && material.economicOrderQuantity > 0 && (
          <div className="flex items-center gap-1.5 text-brand-600 dark:text-brand-400 font-medium">
            <span>Recommended EOQ:</span>
            <span className="font-bold">{material.economicOrderQuantity} {unit}</span>
          </div>
        )}
      </div>
    </div>
  );
}
