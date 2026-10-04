import type { ReactNode } from "react";

/** Clickable summary cards above a list, in the style of the materials stock cards. */
export type StatTone = "brand" | "amber" | "orange" | "red" | "green" | "blue";

export interface StatCard<T extends string> {
  id: T;
  title: string;
  value: ReactNode;
  unit?: string;
  subtitle?: string;
  tone: StatTone;
  icon: ReactNode;
  /** Shown as a small badge when positive. */
  badge?: number;
}

const TONES: Record<StatTone, { active: string; text: string; badge: string; icon: string }> = {
  brand: {
    active: "border-brand-500 ring-2 ring-brand-500/20 bg-brand-50/40 dark:bg-brand-500/10 dark:border-brand-400",
    text: "text-brand-600 dark:text-brand-400",
    badge: "bg-brand-100 text-brand-800 dark:bg-brand-500/20 dark:text-brand-300",
    icon: "bg-brand-50 text-brand-600 dark:bg-brand-500/15 dark:text-brand-400",
  },
  amber: {
    active: "border-amber-500 ring-2 ring-amber-500/20 bg-amber-50/40 dark:bg-amber-500/10 dark:border-amber-400",
    text: "text-amber-600 dark:text-amber-400",
    badge: "bg-amber-100 text-amber-800 dark:bg-amber-500/20 dark:text-amber-300",
    icon: "bg-amber-50 text-amber-600 dark:bg-amber-500/15 dark:text-amber-400",
  },
  orange: {
    active: "border-orange-500 ring-2 ring-orange-500/20 bg-orange-50/40 dark:bg-orange-500/10 dark:border-orange-400",
    text: "text-orange-600 dark:text-orange-400",
    badge: "bg-orange-100 text-orange-800 dark:bg-orange-500/20 dark:text-orange-300",
    icon: "bg-orange-50 text-orange-600 dark:bg-orange-500/15 dark:text-orange-400",
  },
  red: {
    active: "border-red-600 ring-2 ring-red-600/20 bg-red-50/40 dark:bg-red-500/10 dark:border-red-500",
    text: "text-red-700 dark:text-red-400",
    badge: "bg-red-100 text-red-800 dark:bg-red-500/20 dark:text-red-300",
    icon: "bg-red-50 text-red-600 dark:bg-red-500/15 dark:text-red-400",
  },
  green: {
    active: "border-emerald-500 ring-2 ring-emerald-500/20 bg-emerald-50/40 dark:bg-emerald-500/10 dark:border-emerald-400",
    text: "text-emerald-600 dark:text-emerald-400",
    badge: "bg-emerald-100 text-emerald-800 dark:bg-emerald-500/20 dark:text-emerald-300",
    icon: "bg-emerald-50 text-emerald-600 dark:bg-emerald-500/15 dark:text-emerald-400",
  },
  blue: {
    active: "border-sky-500 ring-2 ring-sky-500/20 bg-sky-50/40 dark:bg-sky-500/10 dark:border-sky-400",
    text: "text-sky-600 dark:text-sky-400",
    badge: "bg-sky-100 text-sky-800 dark:bg-sky-500/20 dark:text-sky-300",
    icon: "bg-sky-50 text-sky-600 dark:bg-sky-500/15 dark:text-sky-400",
  },
};

export default function StatFilterCards<T extends string>({
  cards,
  active,
  onSelect,
}: {
  cards: StatCard<T>[];
  active: T;
  onSelect: (id: T) => void;
}) {
  return (
    <div className="mb-6 grid grid-cols-1 gap-3 sm:grid-cols-2 lg:grid-cols-4">
      {cards.map((card) => {
        const tone = TONES[card.tone];
        const isActive = active === card.id;
        return (
          <button
            key={card.id}
            type="button"
            onClick={() => onSelect(card.id)}
            className={`flex cursor-pointer items-center justify-between rounded-xl border p-4 text-left shadow-sm transition-all duration-200 hover:shadow-md ${
              isActive
                ? tone.active
                : "border-gray-200 bg-white hover:border-gray-300 dark:border-white/[0.07] dark:bg-white/[0.03] dark:hover:border-white/[0.15]"
            }`}
          >
            <div className="flex min-w-0 items-center gap-3">
              <div className={`flex size-11 shrink-0 items-center justify-center rounded-xl ${tone.icon}`}>{card.icon}</div>
              <div className="min-w-0">
                <span className="block text-xs font-medium text-gray-500 dark:text-gray-400">{card.title}</span>
                <div className="mt-0.5 flex items-baseline gap-2">
                  <span
                    className={`truncate text-xl font-bold tracking-tight ${isActive ? tone.text : "text-gray-900 dark:text-white"}`}
                  >
                    {card.value}
                  </span>
                  {card.unit && <span className="text-xs text-gray-400 dark:text-gray-500">{card.unit}</span>}
                </div>
              </div>
            </div>
            <div className="flex flex-col items-end gap-1">
              {card.badge != null && card.badge > 0 && (
                <span className={`inline-flex items-center rounded-full px-2 py-0.5 text-[11px] font-semibold ${tone.badge}`}>
                  {card.badge}
                </span>
              )}
              {card.subtitle && (
                <span className="hidden text-[10px] text-gray-400 dark:text-gray-500 sm:inline">{card.subtitle}</span>
              )}
            </div>
          </button>
        );
      })}
    </div>
  );
}

