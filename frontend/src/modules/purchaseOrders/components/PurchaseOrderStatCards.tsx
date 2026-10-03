import React from "react";
import type { PurchaseOrderFilterTab } from "../types/PurchaseOrderFilterParams";

interface PurchaseOrderStatCardsProps {
  activeFilter: PurchaseOrderFilterTab;
  onSelectFilter: (filter: PurchaseOrderFilterTab) => void;
  counts: {
    total: number;
    draft: number;
    submitted: number;
    confirmed: number;
    readyForReceipt: number;
    completed: number;
    cancelled: number;
    totalValue?: number;
    currency?: string;
  };
}

export default function PurchaseOrderStatCards({
  activeFilter,
  onSelectFilter,
  counts,
}: PurchaseOrderStatCardsProps) {
  const formatCurrency = (val?: number, curr = "MAD") => {
    if (val == null) return "0.00 " + curr;
    return `${new Intl.NumberFormat("en-US", {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2,
    }).format(val)} ${curr}`;
  };

  const cards = [
    {
      id: "ALL" as PurchaseOrderFilterTab,
      title: "Total Commandes",
      count: counts.total,
      subtitle: "Toutes les commandes",
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
      id: "SUBMITTED" as PurchaseOrderFilterTab,
      title: "Soumises & En cours",
      count: counts.submitted + counts.confirmed,
      subtitle: "Auprès des fournisseurs",
      isAlert: counts.submitted > 0,
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
      id: "READY_FOR_RECEIPT" as PurchaseOrderFilterTab,
      title: "Prêtes Réception",
      count: counts.readyForReceipt,
      subtitle: "Assignées au magasin",
      icon: (
        <svg className="size-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            strokeWidth={2}
            d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4"
          />
        </svg>
      ),
      activeBorder:
        "border-sky-500 ring-2 ring-sky-500/20 bg-sky-50/40 dark:bg-sky-500/10 dark:border-sky-400",
      activeText: "text-sky-600 dark:text-sky-400",
      badgeColor: "bg-sky-100 text-sky-800 dark:bg-sky-500/20 dark:text-sky-300",
      iconBg: "bg-sky-50 text-sky-600 dark:bg-sky-500/15 dark:text-sky-400",
    },
    {
      id: "COMPLETED" as PurchaseOrderFilterTab,
      title: "Terminées",
      count: counts.completed,
      subtitle: "Livrées et clôturées",
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
      id: "DRAFT" as PurchaseOrderFilterTab,
      title: "Brouillons",
      count: counts.draft,
      subtitle: "En cours de préparation",
      icon: (
        <svg className="size-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            strokeWidth={2}
            d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"
          />
        </svg>
      ),
      activeBorder:
        "border-gray-500 ring-2 ring-gray-500/20 bg-gray-50/40 dark:bg-gray-500/10 dark:border-gray-400",
      activeText: "text-gray-700 dark:text-gray-300",
      badgeColor: "bg-gray-100 text-gray-700 dark:bg-gray-800 dark:text-gray-300",
      iconBg: "bg-gray-100 text-gray-600 dark:bg-gray-800 dark:text-gray-400",
    },
  ];

  return (
    <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-5 gap-3.5">
      {cards.map((card) => {
        const isSelected = activeFilter === card.id;

        return (
          <button
            key={card.id}
            type="button"
            onClick={() => onSelectFilter(card.id)}
            className={`flex flex-col text-left p-4 rounded-xl border transition-all duration-150 cursor-pointer ${
              isSelected
                ? card.activeBorder
                : "bg-white dark:bg-gray-900 border-gray-200 dark:border-white/[0.07] hover:border-gray-300 dark:hover:border-white/[0.15] hover:shadow-xs"
            }`}
          >
            <div className="flex items-center justify-between w-full mb-2">
              <span
                className={`p-2 rounded-lg ${
                  isSelected ? card.iconBg : "bg-gray-50 text-gray-500 dark:bg-gray-800/80 dark:text-gray-400"
                }`}
              >
                {card.icon}
              </span>
              <span
                className={`px-2 py-0.5 rounded-full text-xs font-bold ${
                  card.isAlert && !isSelected
                    ? "bg-amber-100 text-amber-800 dark:bg-amber-500/20 dark:text-amber-300"
                    : card.badgeColor
                }`}
              >
                {card.count}
              </span>
            </div>

            <div className="mt-1">
              <h4
                className={`text-sm font-semibold tracking-tight ${
                  isSelected ? card.activeText : "text-gray-800 dark:text-white/90"
                }`}
              >
                {card.title}
              </h4>
              <p className="text-[11px] text-gray-500 dark:text-gray-400 mt-0.5 truncate">
                {card.subtitle}
              </p>
            </div>
          </button>
        );
      })}
    </div>
  );
}
