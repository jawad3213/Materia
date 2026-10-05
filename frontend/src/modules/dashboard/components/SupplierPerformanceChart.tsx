import { Link } from "react-router-dom";
import BarChartOne from "../../../shared/components/charts/bar/BarChartOne";
import { DetailCard } from "../../../shared/components/page/DetailParts";
import { SectionIcons } from "../../../shared/components/page/pageIcons";
import type { SupplierStats } from "../types/dashboard.types";
import { formatMoney } from "../utils/dashboardFormat";
import EmptyChart from "./EmptyChart";

/** A rate as a coloured pill: green from 95%, amber from 80%, red below. */
function RatePill({ value }: { value?: number | null }) {
  if (value == null) return <span className="text-theme-xs text-gray-400">—</span>;
  const tone =
    value >= 95
      ? "bg-success-50 text-success-700 dark:bg-success-500/15 dark:text-success-400"
      : value >= 80
        ? "bg-warning-50 text-warning-700 dark:bg-warning-500/15 dark:text-orange-400"
        : "bg-error-50 text-error-700 dark:bg-error-500/15 dark:text-error-400";
  return <span className={`inline-flex rounded-full px-2 py-0.5 text-theme-xs font-medium ${tone}`}>{value}%</span>;
}

/** The suppliers with the most committed spend, with the quality and punctuality of their deliveries. */
export default function SupplierPerformanceChart({ suppliers, currency }: { suppliers: SupplierStats[]; currency: string }) {
  return (
    <DetailCard title="Top Suppliers" icon={SectionIcons.info} tone="brand" padded={false}>
      {suppliers.length === 0 ? (
        <div className="p-6">
          <EmptyChart message="No committed order with a supplier yet." />
        </div>
      ) : (
        <>
          <div className="px-4 pt-4">
            <BarChartOne
              series={[{ name: "Spend", data: suppliers.map((s) => s.spend) }]}
              categories={suppliers.map((s) => s.supplierName)}
              horizontal
              valueFormatter={(v) => formatMoney(v, currency, true)}
              showLegend={false}
              height={Math.max(180, suppliers.length * 48)}
              minWidth={320}
            />
          </div>
          <div className="overflow-x-auto">
            <table className="min-w-full text-theme-sm">
              <thead>
                <tr className="border-y border-gray-100 bg-gray-50/50 text-left text-theme-xs font-medium text-gray-500 dark:border-gray-800 dark:bg-gray-900/50 dark:text-gray-400">
                  <th className="px-6 py-3">Supplier</th>
                  <th className="px-3 py-3 text-right">Orders</th>
                  <th className="px-3 py-3 text-right">Spend</th>
                  <th className="px-3 py-3 text-center">Accepted</th>
                  <th className="px-6 py-3 text-center">On Time</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-100 dark:divide-gray-800">
                {suppliers.map((s) => (
                  <tr key={s.supplierId}>
                    <td className="px-6 py-3">
                      <Link to={`/suppliers/${s.supplierId}`} className="font-medium text-gray-800 hover:text-brand-500 dark:text-white/90">
                        {s.supplierName}
                      </Link>
                    </td>
                    <td className="px-3 py-3 text-right text-gray-600 dark:text-gray-400">{s.orders}</td>
                    <td className="whitespace-nowrap px-3 py-3 text-right font-medium text-gray-800 dark:text-white/90">{formatMoney(s.spend, currency)}</td>
                    <td className="px-3 py-3 text-center">
                      <RatePill value={s.acceptanceRate} />
                    </td>
                    <td className="px-6 py-3 text-center">
                      <RatePill value={s.onTimeRate} />
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </>
      )}
    </DetailCard>
  );
}
