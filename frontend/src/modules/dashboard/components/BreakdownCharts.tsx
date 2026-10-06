import PieChartOne from "../../../shared/components/charts/pie/PieChartOne";
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
  COMPLETED: "#12B76A",
  CANCELLED: "#F04438",
  REJECTED: "#B42318",
};

/** How many purchase orders are in each status. */
export function OrderStatusChart({ slices }: { slices: DashboardSlice[] }) {
  return (
    <DetailCard fill title="Orders by Status" icon={SectionIcons.list} tone="brand">
      <div className="flex flex-1 flex-col justify-center">
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
      </div>
    </DetailCard>
  );
}

/** Committed spend by material category. */
export function SpendByCategoryChart({ slices, currency }: { slices: DashboardSlice[]; currency: string }) {
  return (
    <DetailCard fill title="Spend by Category" icon={SectionIcons.box} tone="warning">
      <div className="flex flex-1 flex-col justify-center">
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
      </div>
    </DetailCard>
  );
}

/** Share of received units accepted, and of receipts that arrived on time. */
export function ReceiptQualityChart({ quality }: { quality: ReceiptQuality }) {
  const hasData = quality.acceptanceRate != null || quality.onTimeRate != null;
  return (
    <DetailCard fill title="Delivery Quality" icon={SectionIcons.check} tone="success">
      <div className="flex flex-1 flex-col justify-center">
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
      </div>
    </DetailCard>
  );
}

const AGING_COLORS: Record<string, string> = {
  NOT_DUE: "#12B76A",
  "1_30": "#FDB022",
  "31_60": "#F79009",
  "60_PLUS": "#F04438",
};

/** What is still owed to suppliers, by how late it is: a split bar of the whole, then one row per bucket. */
export function InvoiceAgingChart({ buckets, currency }: { buckets: AgingBucket[]; currency: string }) {
  const owed = buckets.reduce((sum, b) => sum + b.amount, 0);
  const late = buckets.filter((b) => b.key !== "NOT_DUE");
  const overdue = late.reduce((sum, b) => sum + b.amount, 0);
  const share = (amount: number) => (owed > 0 ? (amount / owed) * 100 : 0);
  return (
    <DetailCard
      fill
      title="Payables Aging"
      icon={StatIcons.money}
      tone={overdue > 0 ? "error" : "success"}
      aside={<span className="text-theme-sm font-medium text-gray-500 dark:text-gray-400">{formatMoney(owed, currency)} owed</span>}
    >
      {owed === 0 ? (
        <div className="flex flex-1 flex-col justify-center">
          <EmptyChart message="Nothing is owed to suppliers." height={240} />
        </div>
      ) : (
        <div className="flex flex-1 flex-col">
          <div className="flex h-4 w-full gap-0.5 overflow-hidden rounded-full bg-gray-100 dark:bg-gray-800">
            {buckets
              .filter((b) => b.amount > 0)
              .map((b) => (
                <div
                  key={b.key}
                  className="h-full first:rounded-l-full last:rounded-r-full"
                  style={{ width: `${Math.max(share(b.amount), 1.5)}%`, backgroundColor: AGING_COLORS[b.key] ?? "#98A2B3" }}
                  title={`${b.label}: ${formatMoney(b.amount, currency)}`}
                />
              ))}
          </div>
          <div className="mt-2 flex justify-between text-theme-xs text-gray-400 dark:text-gray-500">
            <span>{Math.round(share(owed - overdue))}% not due</span>
            <span>{Math.round(share(overdue))}% overdue</span>
          </div>

          <ul className="mt-5 flex flex-1 flex-col justify-between gap-4">
            {buckets.map((b) => {
              const color = AGING_COLORS[b.key] ?? "#98A2B3";
              return (
                <li key={b.key}>
                  <div className="flex items-center justify-between gap-3">
                    <span className="flex min-w-0 items-center gap-2">
                      <span className="size-2.5 shrink-0 rounded-full" style={{ backgroundColor: color }} />
                      <span className="truncate text-theme-sm font-medium text-gray-700 dark:text-gray-300">{b.label}</span>
                      <span className="shrink-0 rounded-full bg-gray-100 px-2 py-0.5 text-theme-xs text-gray-500 dark:bg-white/[0.05] dark:text-gray-400">
                        {b.invoices} inv.
                      </span>
                    </span>
                    <span className="shrink-0 text-theme-sm font-semibold text-gray-900 dark:text-white">{formatMoney(b.amount, currency, true)}</span>
                  </div>
                  <div className="mt-1.5 h-1.5 w-full overflow-hidden rounded-full bg-gray-100 dark:bg-gray-800">
                    <div className="h-full rounded-full" style={{ width: `${share(b.amount)}%`, backgroundColor: color }} />
                  </div>
                </li>
              );
            })}
          </ul>

          <p className="mt-5 border-t border-gray-100 pt-4 text-theme-sm text-gray-600 dark:border-gray-800 dark:text-gray-400">
            Overdue: <span className={overdue > 0 ? "font-semibold text-error-500" : "font-semibold"}>{formatMoney(overdue, currency)}</span> on{" "}
            {late.reduce((n, b) => n + b.invoices, 0)} invoice(s)
          </p>
        </div>
      )}
    </DetailCard>
  );
}
