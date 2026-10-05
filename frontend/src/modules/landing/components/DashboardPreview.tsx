import LineChartOne from "../../../shared/components/charts/line/LineChartOne";
import PieChartOne from "../../../shared/components/charts/pie/PieChartOne";
import Badge from "../../../shared/components/ui/badge/Badge";

/** Illustrative figures for the preview only; the real dashboard computes them from live documents. */
const MONTHS = ["May", "Jun", "Jul", "Aug", "Sep", "Oct"];
const ORDERED = [42, 55, 48, 66, 72, 81];
const PAID = [30, 41, 45, 52, 60, 69];

const KPIS = [
  { label: "Open orders", value: "24", hint: "3 late", tone: "text-brand-500" },
  { label: "Invoices to pay", value: "18.4K", hint: "MAD", tone: "text-warning-500" },
  { label: "Accepted at receipt", value: "97.2%", hint: "last 90 days", tone: "text-success-500" },
];

/** A stylised dashboard window built from the same chart components as the real dashboard. */
export default function DashboardPreview() {
  return (
    <div className="relative overflow-hidden rounded-2xl border border-gray-200 bg-white shadow-theme-xl dark:border-white/10 dark:bg-gray-900">
      <div className="flex items-center gap-2 border-b border-gray-100 px-4 py-3 dark:border-white/5">
        <span className="size-3 rounded-full bg-error-400" />
        <span className="size-3 rounded-full bg-warning-400" />
        <span className="size-3 rounded-full bg-success-400" />
        <span className="ml-3 truncate text-xs text-gray-400">materia · procurement overview</span>
        <span className="ml-auto">
          <Badge size="sm" color="light">
            Illustrative data
          </Badge>
        </span>
      </div>

      <div className="grid grid-cols-3 gap-3 p-4">
        {KPIS.map((k) => (
          <div key={k.label} data-preview-kpi className="rounded-xl border border-gray-100 p-3 dark:border-white/5">
            <p className="truncate text-[11px] text-gray-500 dark:text-gray-400">{k.label}</p>
            <p className={`mt-1 text-lg font-bold sm:text-xl ${k.tone}`}>{k.value}</p>
            <p className="truncate text-[10px] text-gray-400">{k.hint}</p>
          </div>
        ))}
      </div>

      <div className="grid grid-cols-1 gap-3 px-4 pb-4 sm:grid-cols-5">
        <div data-preview-chart className="rounded-xl border border-gray-100 p-2 sm:col-span-3 dark:border-white/5">
          <p className="px-2 pt-1 text-xs font-medium text-gray-600 dark:text-gray-300">Ordered vs paid</p>
          <LineChartOne
            series={[
              { name: "Ordered", data: ORDERED },
              { name: "Paid", data: PAID },
            ]}
            categories={MONTHS}
            colors={["#465FFF", "#12B76A"]}
            height={170}
            minWidth={0}
          />
        </div>
        <div data-preview-chart className="rounded-xl border border-gray-100 p-2 sm:col-span-2 dark:border-white/5">
          <p className="px-2 pt-1 text-xs font-medium text-gray-600 dark:text-gray-300">Orders by status</p>
          <PieChartOne series={[9, 6, 5, 4]} labels={["Confirmed", "To receive", "Completed", "Draft"]} height={190} />
        </div>
      </div>
    </div>
  );
}
