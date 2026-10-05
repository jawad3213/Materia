import type { ReactNode } from "react";
import { Link } from "react-router-dom";
import type { DashboardKpi, KpiTone } from "../types/dashboard.types";
import { formatMoney } from "../utils/dashboardFormat";

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

/** Headline figures; each card opens the page where the figure can be acted on. */
export default function KPICards({ kpis }: { kpis: DashboardKpi[] }) {
  if (kpis.length === 0) return null;
  return (
    <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-4">
      {kpis.map((kpi) => {
        const tone = TONES[kpi.tone] ?? TONES.brand;
        const money = isCurrency(kpi.unit);
        const body = (
          <div className="relative h-full overflow-hidden rounded-2xl border border-gray-200 bg-white p-5 transition-shadow hover:shadow-md dark:border-gray-800 dark:bg-white/[0.03]">
            <span className={`absolute inset-y-0 left-0 w-1 ${tone.accent}`} />
            <div className="flex items-start justify-between gap-3">
              <div className="min-w-0">
                <p className="text-theme-sm font-medium text-gray-500 dark:text-gray-400">{kpi.label}</p>
                <p className="mt-2 truncate text-2xl font-bold tracking-tight text-gray-900 dark:text-white">
                  {money ? formatMoney(kpi.value, kpi.unit) : new Intl.NumberFormat("en-US").format(kpi.value)}
                </p>
                {!money && <p className="text-theme-xs text-gray-400 dark:text-gray-500">{kpi.unit}</p>}
              </div>
              <div className={`flex size-12 shrink-0 items-center justify-center rounded-xl ${tone.icon}`}>{ICONS[kpi.key] ?? ICONS.openOrders}</div>
            </div>
            {kpi.hint && <p className="mt-3 border-t border-gray-100 pt-3 text-theme-xs text-gray-500 dark:border-gray-800 dark:text-gray-400">{kpi.hint}</p>}
          </div>
        );
        return kpi.link ? (
          <Link key={kpi.key} to={kpi.link} className="block">
            {body}
          </Link>
        ) : (
          <div key={kpi.key}>{body}</div>
        );
      })}
    </div>
  );
}
