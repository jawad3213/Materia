import React from "react";
import type { RequisitionFilterTab } from "../types/requisition.types";

interface RequisitionStatCardsProps {
  activeFilter: RequisitionFilterTab;
  onSelectFilter: (filter: RequisitionFilterTab) => void;
  counts: {
    total: number;
    pendingReview: number;
    readyForPo: number;
    converted: number;
    totalEstimatedValue?: number;
    currency?: string;
  };
}

export default function RequisitionStatCards({
  activeFilter,
  onSelectFilter,
  counts,
}: RequisitionStatCardsProps) {
  const formatCurrency = (val?: number, curr = "MAD") => {
    if (val == null) return "0.00 " + curr;
    return `${new Intl.NumberFormat("en-US", {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2,
    }).format(val)} ${curr}`;
  };

  const cards = [
    {
      id: "ALL" as RequisitionFilterTab,
      title: "All Requisitions",
      count: counts.total,
      subtitle: "Total requisitions logged",
      icon: (
        <svg className="size-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            strokeWidth={2}
            d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"
          />
        </svg>
      ),
      activeBorder:
        "border-brand-500 ring-2 ring-brand-500/20 bg-brand-50/40 dark:bg-brand-500/10 dark:border-brand-400",
      activeText: "text-brand-600 dark:text-brand-400",
      badgeColor: "bg-gray-100 text-gray-700 dark:bg-gray-800 dark:text-gray-300",
      iconBg: "bg-brand-50 text-brand-600 dark:bg-brand-500/15 dark:text-brand-400",
    },
    {
      id: "PENDING" as RequisitionFilterTab,
      title: "Pending Review",
      count: counts.pendingReview,
      subtitle: "Requires approval sign-off",
      isAlert: counts.pendingReview > 0,
      icon: (
        <svg className="size-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            strokeWidth={2}
            d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"
          />
        </svg>
      ),
      activeBorder:
        "border-amber-500 ring-2 ring-amber-500/20 bg-amber-50/40 dark:bg-amber-500/10 dark:border-amber-400",
      activeText: "text-amber-600 dark:text-amber-400",
      badgeColor: "bg-amber-100 text-amber-800 dark:bg-amber-500/20 dark:text-amber-300",
      iconBg: "bg-amber-50 text-amber-600 dark:bg-amber-500/15 dark:text-amber-400",
    },
    {
      id: "APPROVED" as RequisitionFilterTab,
      title: "Ready for PO",
      count: counts.readyForPo,
      subtitle: "Approved for conversion",
      icon: (
        <svg className="size-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            strokeWidth={2}
            d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"
          />
        </svg>
      ),
      activeBorder:
        "border-emerald-500 ring-2 ring-emerald-500/20 bg-emerald-50/40 dark:bg-emerald-500/10 dark:border-emerald-400",
      activeText: "text-emerald-600 dark:text-emerald-400",
      badgeColor: "bg-emerald-100 text-emerald-800 dark:bg-emerald-500/20 dark:text-emerald-300",
      iconBg: "bg-emerald-50 text-emerald-600 dark:bg-emerald-500/15 dark:text-emerald-400",
    },
    {
      id: "CONVERTED" as RequisitionFilterTab,
      title: "Converted to PO",
      count: counts.converted,
      subtitle: "Issued purchase orders",
      icon: (
        <svg className="size-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            strokeWidth={2}
            d="M16 11V7a4 4 0 00-8 0v4M5 9h14l1 12H4L5 9z"
          />
        </svg>
      ),
      activeBorder:
        "border-purple-500 ring-2 ring-purple-500/20 bg-purple-50/40 dark:bg-purple-500/10 dark:border-purple-400",
      activeText: "text-purple-600 dark:text-purple-400",
      badgeColor: "bg-purple-100 text-purple-800 dark:bg-purple-500/20 dark:text-purple-300",
      iconBg: "bg-purple-50 text-purple-600 dark:bg-purple-500/15 dark:text-purple-400",
    },
  ];

  return (
    <div className="space-y-3 mb-6">
      <div className="grid grid-cols-1 gap-3 sm:grid-cols-2 lg:grid-cols-4">
        {cards.map((card) => {
          const isActive = activeFilter === card.id;

          return (
            <button
              key={card.id}
              type="button"
              onClick={() => onSelectFilter(card.id)}
              className={`flex items-center justify-between p-4 rounded-xl border text-left transition-all duration-200 cursor-pointer shadow-sm hover:shadow-md ${
                isActive
                  ? `${card.activeBorder} shadow-sm`
                  : "border-gray-200 bg-white hover:border-gray-300 dark:border-white/[0.07] dark:bg-white/[0.03] dark:hover:border-white/[0.15]"
              }`}
            >
              <div className="flex items-center gap-3">
                <div className={`p-2.5 rounded-lg ${card.iconBg}`}>
                  {card.icon}
                </div>
                <div>
                  <div className="flex items-center gap-1.5">
                    <span className="text-xs font-medium text-gray-500 dark:text-gray-400">
                      {card.title}
                    </span>
                    {card.isAlert && (
                      <span className="relative flex h-2 w-2">
                        <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-amber-400 opacity-75" />
                        <span className="relative inline-flex rounded-full h-2 w-2 bg-amber-500" />
                      </span>
                    )}
                  </div>
                  <span className="text-xl font-bold text-gray-800 dark:text-white/90">
                    {card.count}
                  </span>
                  <p className="text-[11px] text-gray-400 dark:text-gray-500 mt-0.5">
                    {card.subtitle}
                  </p>
                </div>
              </div>

              <div
                className={`px-2.5 py-1 rounded-full text-xs font-bold transition-colors ${
                  isActive
                    ? "bg-brand-500 text-white dark:bg-brand-500 dark:text-white"
                    : card.badgeColor
                }`}
              >
                {card.count}
              </div>
            </button>
          );
        })}
      </div>

      {counts.totalEstimatedValue != null && counts.totalEstimatedValue > 0 && (
        <div className="flex items-center justify-between px-4 py-2 bg-gray-50/80 dark:bg-gray-800/40 rounded-lg border border-gray-200/70 dark:border-white/[0.05] text-xs">
          <span className="text-gray-500 dark:text-gray-400 flex items-center gap-1.5">
            <svg className="size-4 text-brand-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 7h8m0 0v8m0-8l-8 8-4-4-6 6" />
            </svg>
            Pipeline Value:
          </span>
          <span className="font-semibold text-gray-900 dark:text-white">
            {formatCurrency(counts.totalEstimatedValue, counts.currency || "MAD")}
          </span>
        </div>
      )}
    </div>
  );
}
