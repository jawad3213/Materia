import PieChartOne from "../../../shared/components/charts/pie/PieChartOne";
import BarChartOne from "../../../shared/components/charts/bar/BarChartOne";
import RadialChartOne from "../../../shared/components/charts/radial/RadialChartOne";
import { DetailCard } from "../../../shared/components/page/DetailParts";
import { SectionIcons, StatIcons } from "../../../shared/components/page/pageIcons";
import type { AgingBucket, DashboardSlice, ReceiptQuality } from "../types/dashboard.types";
import { formatMoney } from "../utils/dashboardFormat";
import EmptyChart from "./EmptyChart";

const STATUS_COLORS: Record<string, string> = {
  DRAFT: "#98A2B3",
  SUBMITTED: "#F79009",
  CONFIRMED: "#36BFFA",
  READY_FOR_RECEIPT: "#465FFF",
  PARTIALLY_RECEIVED: "#FDB022",
  RECEIVED: "#32D583",
  COMPLETED: "#12B76A",
  CANCELLED: "#F04438",
  REJECTED: "#B42318",
};

/** How many purchase orders are in each status. */
export function OrderStatusChart({ slices }: { slices: DashboardSlice[] }) {
  return (
    <DetailCard title="Orders by Status" icon={SectionIcons.list} tone="brand">
      {slices.length === 0 ? (
        <EmptyChart message="No purchase order yet." />
      ) : (
        <PieChartOne
          series={slices.map((s) => s.count)}
          labels={slices.map((s) => s.label)}
          colors={slices.map((s) => STATUS_COLORS[s.key] ?? "#465FFF")}
          totalLabel="Orders"
          height={320}
        />
      )}
    </DetailCard>
  );
}

/** Committed spend by material category. */
export function SpendByCategoryChart({ slices, currency }: { slices: DashboardSlice[]; currency: string }) {
  return (
    <DetailCard title="Spend by Category" icon={SectionIcons.box} tone="warning">
      {slices.length === 0 ? (
        <EmptyChart message="No committed spend yet." />
      ) : (
        <PieChartOne
          series={slices.map((s) => s.amount ?? 0)}
          labels={slices.map((s) => s.label)}
          valueFormatter={(v) => formatMoney(v, currency, true)}
          totalLabel="Total"
          height={320}
        />
      )}
    </DetailCard>
  );
}

/** Share of received units accepted, and of receipts that arrived on time. */
export function ReceiptQualityChart({ quality }: { quality: ReceiptQuality }) {
  const hasData = quality.acceptanceRate != null || quality.onTimeRate != null;
  return (
    <DetailCard title="Delivery Quality" icon={SectionIcons.check} tone="success">
      {!hasData ? (
        <EmptyChart message="No completed goods receipt yet." />
      ) : (
        <>
          <RadialChartOne
            series={[quality.acceptanceRate ?? 0, quality.onTimeRate ?? 0]}
            labels={["Accepted", "On time"]}
            colors={["#12B76A", "#465FFF"]}
            height={300}
          />
          <div className="mt-2 grid grid-cols-3 gap-3 border-t border-gray-100 pt-4 text-center dark:border-gray-800">
            <div>
              <p className="text-lg font-semibold text-gray-900 dark:text-white">{quality.receipts}</p>
              <p className="text-theme-xs text-gray-500 dark:text-gray-400">Receipts</p>
            </div>
            <div>
              <p className="text-lg font-semibold text-gray-900 dark:text-white">{quality.unitsReceived}</p>
              <p className="text-theme-xs text-gray-500 dark:text-gray-400">Units received</p>
            </div>
            <div>
              <p className="text-lg font-semibold text-error-500">{quality.unitsRejected}</p>
              <p className="text-theme-xs text-gray-500 dark:text-gray-400">Units rejected</p>
            </div>
          </div>
        </>
      )}
    </DetailCard>
  );
}

const AGING_COLORS = ["#12B76A", "#FDB022", "#F79009", "#F04438"];

/** What is still owed to suppliers, by how late it is. */
export function InvoiceAgingChart({ buckets, currency }: { buckets: AgingBucket[]; currency: string }) {
  const owed = buckets.reduce((sum, b) => sum + b.amount, 0);
  const overdue = buckets.filter((b) => b.key !== "NOT_DUE").reduce((sum, b) => sum + b.amount, 0);
  return (
    <DetailCard
      title="Payables Aging"
      icon={StatIcons.money}
      tone={overdue > 0 ? "error" : "success"}
      aside={<span className="text-theme-sm font-medium text-gray-500 dark:text-gray-400">{formatMoney(owed, currency)} owed</span>}
    >
      {owed === 0 ? (
        <EmptyChart message="Nothing is owed to suppliers." height={220} />
      ) : (
        <>
          <BarChartOne
            series={[{ name: "Outstanding", data: buckets.map((b) => b.amount) }]}
            categories={buckets.map((b) => b.label)}
            colors={AGING_COLORS}
            valueFormatter={(v) => formatMoney(v, currency, true)}
            showLegend={false}
            height={220}
            minWidth={320}
          />
          <p className="mt-3 text-theme-sm text-gray-600 dark:text-gray-400">
            Overdue: <span className={overdue > 0 ? "font-semibold text-error-500" : "font-semibold"}>{formatMoney(overdue, currency)}</span> on{" "}
            {buckets.filter((b) => b.key !== "NOT_DUE").reduce((n, b) => n + b.invoices, 0)} invoice(s)
          </p>
        </>
      )}
    </DetailCard>
  );
}
