export type StockFilterType = "ALL" | "REORDER_NEEDED" | "CRITICAL" | "OUT_OF_STOCK";

interface StockStatCardsProps {
  activeFilter: StockFilterType;
  onSelectFilter: (filter: StockFilterType) => void;
  counts: {
    total: number;
    reorderNeeded: number;
    critical: number;
    outOfStock: number;
  };
  loading?: boolean;
}

export default function StockStatCards({
  activeFilter,
  onSelectFilter,
  counts,
  loading = false,
}: StockStatCardsProps) {
  const cards = [
    {
      id: "ALL" as StockFilterType,
      title: "All Materials",
      count: counts.total,
      subtitle: "Total catalog items",
      icon: (
        <svg className="size-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4" />
        </svg>
      ),
      activeBorder: "border-brand-500 ring-2 ring-brand-500/20 bg-brand-50/40 dark:bg-brand-500/10 dark:border-brand-400",
      activeText: "text-brand-600 dark:text-brand-400",
      badgeColor: "bg-gray-100 text-gray-700 dark:bg-gray-800 dark:text-gray-300",
      iconBg: "bg-brand-50 text-brand-600 dark:bg-brand-500/15 dark:text-brand-400",
    },
    {
      id: "REORDER_NEEDED" as StockFilterType,
      title: "Reorder Needed",
      count: counts.reorderNeeded,
      subtitle: "Below reorder point",
      icon: (
        <svg className="size-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
        </svg>
      ),
      activeBorder: "border-amber-500 ring-2 ring-amber-500/20 bg-amber-50/40 dark:bg-amber-500/10 dark:border-amber-400",
      activeText: "text-amber-600 dark:text-amber-400",
      badgeColor: "bg-amber-100 text-amber-800 dark:bg-amber-500/20 dark:text-amber-300",
      iconBg: "bg-amber-50 text-amber-600 dark:bg-amber-500/15 dark:text-amber-400",
    },
    {
      id: "CRITICAL" as StockFilterType,
      title: "Critical Stock",
      count: counts.critical,
      subtitle: "Below safety stock",
      icon: (
        <svg className="size-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
        </svg>
      ),
      activeBorder: "border-orange-500 ring-2 ring-orange-500/20 bg-orange-50/40 dark:bg-orange-500/10 dark:border-orange-400",
      activeText: "text-orange-600 dark:text-orange-400",
      badgeColor: "bg-orange-100 text-orange-800 dark:bg-orange-500/20 dark:text-orange-300",
      iconBg: "bg-orange-50 text-orange-600 dark:bg-orange-500/15 dark:text-orange-400",
    },
    {
      id: "OUT_OF_STOCK" as StockFilterType,
      title: "Out of Stock",
      count: counts.outOfStock,
      subtitle: "Zero physical stock",
      icon: (
        <svg className="size-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M18.364 18.364A9 9 0 005.636 5.636m12.728 12.728A9 9 0 015.636 5.636m12.728 12.728L5.636 5.636" />
        </svg>
      ),
      activeBorder: "border-red-600 ring-2 ring-red-600/20 bg-red-50/40 dark:bg-red-500/10 dark:border-red-500",
      activeText: "text-red-700 dark:text-red-400",
      badgeColor: "bg-red-100 text-red-800 dark:bg-red-500/20 dark:text-red-300",
      iconBg: "bg-red-50 text-red-600 dark:bg-red-500/15 dark:text-red-400",
    },
  ];

  return (
    <div className="grid grid-cols-1 gap-3 sm:grid-cols-2 lg:grid-cols-4 mb-6">
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
              <div
                className={`flex size-11 items-center justify-center rounded-xl transition-colors ${
                  card.iconBg
                }`}
              >
                {card.icon}
              </div>
              <div>
                <span className="block text-xs font-medium text-gray-500 dark:text-gray-400">
                  {card.title}
                </span>
                <div className="flex items-baseline gap-2 mt-0.5">
                  <span
                    className={`text-xl font-bold tracking-tight ${
                      isActive
                        ? card.activeText
                        : "text-gray-900 dark:text-white"
                    }`}
                  >
                    {card.count ?? 0}
                  </span>
                  <span className="text-xs text-gray-400 dark:text-gray-500">
                    items
                  </span>
                </div>
              </div>
            </div>

            {/* Badge and Subtitle */}
            <div className="flex flex-col items-end gap-1">
              {card.count > 0 && card.id !== "ALL" ? (
                <span
                  className={`inline-flex items-center px-2 py-0.5 rounded-full text-[11px] font-semibold ${card.badgeColor}`}
                >
                  {card.count}
                </span>
              ) : null}
              <span className="text-[10px] text-gray-400 dark:text-gray-500 hidden sm:inline">
                {card.subtitle}
              </span>
            </div>
          </button>
        );
      })}
    </div>
  );
}
