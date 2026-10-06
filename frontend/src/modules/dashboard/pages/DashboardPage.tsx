import type { ReactNode } from "react";
import PageMeta from "../../../shared/components/common/PageMeta";
import Button from "../../../shared/components/ui/button/Button";
import { PageLoader } from "../../../shared/components/page/DetailParts";
import useAuth from "../../auth/hooks/useAuth";
import useDashboard from "../hooks/useDashboard";
import KPICards from "../components/KPICards";
import QuickActions from "../components/QuickActions";
import { QUICK_ACTIONS } from "../utils/quickActions";
import CostChart from "../components/CostChart";
import { InvoiceAgingChart, OrderStatusChart, ReceiptQualityChart, SpendByCategoryChart } from "../components/BreakdownCharts";
import SupplierPerformanceChart from "../components/SupplierPerformanceChart";
import StockChart from "../components/StockChart";
import RecentActivities from "../components/RecentActivities";

const greeting = () => {
  const hour = new Date().getHours();
  return hour < 12 ? "Good morning" : hour < 18 ? "Good afternoon" : "Good evening";
};

/** A wide panel and a narrow one side by side; either alone takes the whole row. */
function SplitRow({ main, side }: { main?: ReactNode; side?: ReactNode }) {
  if (!main || !side) {
    const only = main || side;
    return only ? <div>{only}</div> : null;
  }
  return (
    <div className="grid grid-cols-1 gap-6 xl:grid-cols-12">
      <div className="xl:col-span-8">{main}</div>
      <div className="xl:col-span-4">{side}</div>
    </div>
  );
}

/** Equal panels sharing a row: one, two or three columns depending on how many there are. */
function EvenRow({ panels }: { panels: ReactNode[] }) {
  if (panels.length === 0) return null;
  const cols = panels.length === 1 ? "" : panels.length === 2 ? "md:grid-cols-2" : "md:grid-cols-2 xl:grid-cols-3";
  return (
    <div className={`grid grid-cols-1 gap-6 ${cols}`}>
      {panels.map((panel, i) => (
        // With three panels on two columns, the last one takes the full second line.
        <div key={i} className={panels.length === 3 && i === 2 ? "md:col-span-2 xl:col-span-1" : ""}>
          {panel}
        </div>
      ))}
    </div>
  );
}

/**
 * The procurement overview, most important first: headline amounts and the counts that need action, then the
 * spend trend, payables and delivery quality, suppliers and stock, and finally recent activity with shortcuts.
 */
export default function DashboardPage() {
  const { user, hasPermission } = useAuth();
  const { data, loading, error, refresh } = useDashboard();

  if (loading && !data) return <PageLoader message="Loading dashboard..." />;

  const canSeeStock = !!data?.kpis.some((k) => k.key === "stockAlerts");
  const hasActions = QUICK_ACTIONS.some((a) => hasPermission(a.permission));

  return (
    <>
      <PageMeta title="Dashboard | Materia Dashboard" description="Procurement overview: spend, orders, deliveries, payables and stock" />

      <div className="mb-6 flex flex-col gap-4 sm:flex-row sm:items-end sm:justify-between">
        <div>
          <h2 className="text-title-sm font-semibold text-gray-900 dark:text-white">
            {greeting()}
            {user?.name ? `, ${user.name.split(" ")[0]}` : ""}
          </h2>
          <p className="mt-1 text-theme-sm text-gray-500 dark:text-gray-400">
            Procurement overview
            {data && ` · amounts in ${data.currency}`}
            {data && data.otherCurrencyDocuments > 0 && ` (${data.otherCurrencyDocuments} document(s) in other currencies not included)`}
            {data && ` · updated ${new Date(data.generatedAt).toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" })}`}
          </p>
        </div>
        <Button size="sm" variant="outline" onClick={refresh} disabled={loading}>
          {loading ? "Refreshing..." : "Refresh"}
        </Button>
      </div>

      {error && (
        <div className="mb-6 rounded-xl border border-error-200 bg-error-50 px-4 py-3 text-sm text-error-700 dark:border-error-500/30 dark:bg-error-500/10 dark:text-error-400">
          {error}
        </div>
      )}

      {data && (
        <div className="space-y-6">
          <KPICards kpis={data.kpis} />

          <SplitRow
            main={data.monthlyTrend && <CostChart trend={data.monthlyTrend} currency={data.currency} />}
            side={data.monthlyTrend && <OrderStatusChart slices={data.orderStatus} />}
          />

          <EvenRow
            panels={[
              data.invoiceAging.length > 0 && <InvoiceAgingChart buckets={data.invoiceAging} currency={data.currency} />,
              data.monthlyTrend && <SpendByCategoryChart slices={data.spendByCategory} currency={data.currency} />,
              data.receiptQuality && <ReceiptQualityChart quality={data.receiptQuality} />,
            ].filter(Boolean)}
          />

          <SplitRow
            main={data.monthlyTrend && <SupplierPerformanceChart suppliers={data.topSuppliers} currency={data.currency} />}
            side={canSeeStock && <StockChart alerts={data.stockAlerts} />}
          />

          <SplitRow main={<RecentActivities activities={data.recentActivity} />} side={hasActions && <QuickActions />} />
        </div>
      )}
    </>
  );
}
