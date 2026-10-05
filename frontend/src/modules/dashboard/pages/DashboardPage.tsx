import PageMeta from "../../../shared/components/common/PageMeta";
import Button from "../../../shared/components/ui/button/Button";
import { PageLoader } from "../../../shared/components/page/DetailParts";
import useAuth from "../../auth/hooks/useAuth";
import useDashboard from "../hooks/useDashboard";
import KPICards from "../components/KPICards";
import QuickActions from "../components/QuickActions";
import CostChart from "../components/CostChart";
import { InvoiceAgingChart, OrderStatusChart, ReceiptQualityChart, SpendByCategoryChart } from "../components/BreakdownCharts";
import SupplierPerformanceChart from "../components/SupplierPerformanceChart";
import StockChart from "../components/StockChart";
import RecentActivities from "../components/RecentActivities";

const greeting = () => {
  const hour = new Date().getHours();
  return hour < 12 ? "Good morning" : hour < 18 ? "Good afternoon" : "Good evening";
};

/** The procurement overview: headline figures, spend trend, breakdowns, suppliers, stock and recent activity. */
export default function DashboardPage() {
  const { user } = useAuth();
  const { data, loading, error, refresh } = useDashboard();

  if (loading && !data) return <PageLoader message="Loading dashboard..." />;

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

          <QuickActions />

          {data.monthlyTrend && (
            <div className="grid grid-cols-1 gap-6 xl:grid-cols-3">
              <div className="xl:col-span-2">
                <CostChart trend={data.monthlyTrend} currency={data.currency} />
              </div>
              <OrderStatusChart slices={data.orderStatus} />
            </div>
          )}

          <div className="grid grid-cols-1 gap-6 md:grid-cols-2 xl:grid-cols-3">
            {data.monthlyTrend && <SpendByCategoryChart slices={data.spendByCategory} currency={data.currency} />}
            {data.receiptQuality && <ReceiptQualityChart quality={data.receiptQuality} />}
            {data.invoiceAging.length > 0 && <InvoiceAgingChart buckets={data.invoiceAging} currency={data.currency} />}
          </div>

          <div className="grid grid-cols-1 gap-6 xl:grid-cols-3">
            {data.monthlyTrend && (
              <div className="xl:col-span-2">
                <SupplierPerformanceChart suppliers={data.topSuppliers} currency={data.currency} />
              </div>
            )}
            <StockChart alerts={data.stockAlerts} />
          </div>

          <RecentActivities activities={data.recentActivity} />
        </div>
      )}
    </>
  );
}
