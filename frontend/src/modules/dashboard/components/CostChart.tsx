import { useState } from "react";
import LineChartOne, { type LineSeries } from "../../../shared/components/charts/line/LineChartOne";
import ChartTab from "../../../shared/components/common/ChartTab";
import { DetailCard } from "../../../shared/components/page/DetailParts";
import type { MonthlyTrend } from "../types/dashboard.types";
import { formatMoney, monthLabel, toQuarters, total } from "../utils/dashboardFormat";
import EmptyChart from "./EmptyChart";

type Period = "MONTH" | "QUARTER";

const SERIES = [
  { key: "ordered", name: "Ordered", color: "#465FFF" },
  { key: "invoiced", name: "Invoiced", color: "#F79009" },
  { key: "paid", name: "Paid", color: "#12B76A" },
] as const;

/** Ordered, invoiced and paid amounts over the last twelve months, by month or by quarter. */
export default function CostChart({ trend, currency }: { trend: MonthlyTrend; currency: string }) {
  const [period, setPeriod] = useState<Period>("MONTH");
  const data = period === "QUARTER" ? toQuarters(trend) : trend;
  const shown = SERIES.filter((s) => data[s.key].length > 0);
  const series: LineSeries[] = shown.map((s) => ({ name: s.name, data: data[s.key] }));
  const empty = shown.every((s) => total(data[s.key]) === 0);

  return (
    <DetailCard
      title="Procurement Spend"
      aside={
        <div className="w-56">
          <ChartTab<Period>
            options={[
              { value: "MONTH", label: "Monthly" },
              { value: "QUARTER", label: "Quarterly" },
            ]}
            value={period}
            onChange={setPeriod}
          />
        </div>
      }
    >
      <div className="mb-5 grid grid-cols-1 gap-4 sm:grid-cols-3">
        {shown.map((s) => (
          <div key={s.key} className="rounded-xl bg-gray-50 px-4 py-3 dark:bg-white/[0.03]">
            <p className="flex items-center gap-2 text-theme-xs text-gray-500 dark:text-gray-400">
              <span className="size-2 rounded-full" style={{ backgroundColor: s.color }} />
              {s.name} · last 12 months
            </p>
            <p className="mt-1 text-lg font-semibold text-gray-900 dark:text-white">{formatMoney(total(trend[s.key]), currency)}</p>
          </div>
        ))}
      </div>
      {empty ? (
        <EmptyChart message="No spend recorded in the last twelve months yet." height={310} />
      ) : (
        <LineChartOne
          series={series}
          categories={period === "QUARTER" ? data.months : data.months.map(monthLabel)}
          colors={shown.map((s) => s.color)}
          valueFormatter={(v) => formatMoney(v, currency, true)}
          showLegend
          minWidth={640}
        />
      )}
    </DetailCard>
  );
}
