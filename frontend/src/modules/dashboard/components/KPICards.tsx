import type { ReactNode } from "react";
import { Link } from "react-router-dom";
import type { DashboardKpi, KpiTone } from "../types/dashboard.types";

const TONES: Record<KpiTone, { icon: string; accent: string }> = {
  brand: { icon: "bg-brand-50 text-brand-600 dark:bg-brand-500/15 dark:text-brand-400", accent: "bg-brand-500" },
  amber: { icon: "bg-amber-50 text-amber-600 dark:bg-amber-500/15 dark:text-amber-400", accent: "bg-amber-500" },
  orange: { icon: "bg-orange-50 text-orange-600 dark:bg-orange-500/15 dark:text-orange-400", accent: "bg-orange-500" },
  red: { icon: "bg-red-50 text-red-600 dark:bg-red-500/15 dark:text-red-400", accent: "bg-red-500" },
  green: { icon: "bg-emerald-50 text-emerald-600 dark:bg-emerald-500/15 dark:text-emerald-400", accent: "bg-emerald-500" },
  blue: { icon: "bg-sky-50 text-sky-600 dark:bg-sky-500/15 dark:text-sky-400", accent: "bg-sky-500" },
};

const svg = (d: string) => (
  <svg className="size-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.8} d={d} />
  </svg>
);

const ICONS: Record<string, ReactNode> = {
  requisitionsAwaiting: svg("M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-6 9l2 2 4-4"),
  openOrders: svg("M3 3h2l.4 2M7 13h10l4-8H5.4M7 13L5.4 5M7 13l-2.293 2.293c-.63.63-.184 1.707.707 1.707H17m0 0a2 2 0 100 4 2 2 0 000-4zm-8 2a2 2 0 11-4 0 2 2 0 014 0z"),
  receiptsThisMonth: svg("M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4"),
  invoicesToPay: svg("M9 14l6-6m-5.5.5h.01m4.99 5h.01M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16l3.5-2 3.5 2 3.5-2 3.5 2z"),
  paidThisMonth: svg("M17 9V7a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2m2 4h10a2 2 0 002-2v-6a2 2 0 00-2-2H9a2 2 0 00-2 2v6a2 2 0 002 2zm7-5a2 2 0 11-4 0 2 2 0 014 0z"),
  stockAlerts: svg("M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"),
  returnsOpen: svg("M3 10h10a8 8 0 018 8v2M3 10l6 6m-6-6l6-6"),
};

const isCurrency = (unit: string) => /^[A-Z]{3}$/.test(unit);

/** Grid columns for n cards (Tailwind needs the class names written out). */
const HEADLINE_COLS = ["", "sm:grid-cols-1", "sm:grid-cols-2", "sm:grid-cols-2 xl:grid-cols-3", "sm:grid-cols-2 xl:grid-cols-4"];
const STRIP_COLS = ["", "sm:grid-cols-1", "sm:grid-cols-2", "sm:grid-cols-3", "sm:grid-cols-2 xl:grid-cols-4"];

function KpiLink({ kpi, className, children }: { kpi: DashboardKpi; className: string; children: ReactNode }) {
  return kpi.link ? (
    <Link to={kpi.link} className={`group block ${className}`}>
      {children}
    </Link>
  ) : (
    <div className={className}>{children}</div>
  );
}

/** An amount: large card with the icon, the figure and what it is made of. */
function HeadlineCard({ kpi }: { kpi: DashboardKpi }) {
  const tone = TONES[kpi.tone] ?? TONES.brand;
  return (
    <KpiLink kpi={kpi} className="h-full">
      <div className="relative flex h-full flex-col overflow-hidden rounded-2xl border border-gray-200 bg-white p-6 transition-shadow group-hover:shadow-md dark:border-gray-800 dark:bg-white/[0.03]">
        <span className={`absolute inset-x-0 top-0 h-1 ${tone.accent}`} />
        <div className="flex items-center justify-between gap-3">
          <p className="text-theme-sm font-medium text-gray-500 dark:text-gray-400">{kpi.label}</p>
          <div className={`flex size-11 shrink-0 items-center justify-center rounded-xl ${tone.icon}`}>{ICONS[kpi.key] ?? ICONS.openOrders}</div>
        </div>
        <p className="mt-3 truncate text-3xl font-bold tracking-tight text-gray-900 dark:text-white">
          {new Intl.NumberFormat("en-US", { minimumFractionDigits: 2, maximumFractionDigits: 2 }).format(kpi.value)}
          <span className="ml-1.5 text-base font-semibold text-gray-400 dark:text-gray-500">{kpi.unit}</span>
        </p>
        <div className="mt-auto flex items-center justify-between gap-3 pt-4">
          {kpi.hint && <p className="text-theme-xs text-gray-500 dark:text-gray-400">{kpi.hint}</p>}
          {kpi.link && <span className="shrink-0 text-theme-xs font-medium text-brand-500 group-hover:underline">View</span>}
        </div>
      </div>
    </KpiLink>
  );
}

/** A count that may need action: compact tile. */
function StripTile({ kpi }: { kpi: DashboardKpi }) {
  const tone = TONES[kpi.tone] ?? TONES.brand;
  return (
    <KpiLink kpi={kpi} className="h-full">
      <div className="flex h-full items-center gap-4 rounded-2xl border border-gray-200 bg-white px-5 py-4 transition-shadow group-hover:shadow-md dark:border-gray-800 dark:bg-white/[0.03]">
        <div className={`flex size-11 shrink-0 items-center justify-center rounded-xl ${tone.icon}`}>{ICONS[kpi.key] ?? ICONS.openOrders}</div>
        <div className="min-w-0">
          <p className="flex items-baseline gap-2">
            <span className="text-2xl font-bold text-gray-900 dark:text-white">{new Intl.NumberFormat("en-US").format(kpi.value)}</span>
            <span className="truncate text-theme-sm font-medium text-gray-700 dark:text-gray-300">{kpi.label}</span>
          </p>
          {kpi.hint && <p className="truncate text-theme-xs text-gray-500 dark:text-gray-400">{kpi.hint}</p>}
        </div>
      </div>
    </KpiLink>
  );
}

/**
 * Headline figures: amounts first as large cards, then the counts that may need action as a strip.
 * Each opens the page where the figure can be acted on.
 */
export default function KPICards({ kpis }: { kpis: DashboardKpi[] }) {
  if (kpis.length === 0) return null;
  const amounts = kpis.filter((k) => isCurrency(k.unit));
  const counts = kpis.filter((k) => !isCurrency(k.unit));
  return (
    <div className="space-y-4">
      {amounts.length > 0 && (
        <div className={`grid grid-cols-1 gap-4 ${HEADLINE_COLS[Math.min(amounts.length, 4)]}`}>
          {amounts.map((kpi) => (
            <HeadlineCard key={kpi.key} kpi={kpi} />
          ))}
        </div>
      )}
      {counts.length > 0 && (
        <div className={`grid grid-cols-1 gap-4 ${STRIP_COLS[Math.min(counts.length, 4)]}`}>
          {counts.map((kpi) => (
            <StripTile key={kpi.key} kpi={kpi} />
          ))}
        </div>
      )}
    </div>
  );
}
