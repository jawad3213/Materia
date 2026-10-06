import { Link } from "react-router-dom";
import Badge from "../../../shared/components/ui/badge/Badge";
import { DetailCard } from "../../../shared/components/page/DetailParts";
import { StatIcons } from "../../../shared/components/page/pageIcons";
import type { StockAlert } from "../types/dashboard.types";

const STATUS: Record<StockAlert["status"], { label: string; color: "error" | "warning" | "info"; bar: string }> = {
  OUT_OF_STOCK: { label: "Out of stock", color: "error", bar: "bg-error-500" },
  CRITICAL: { label: "Critical", color: "warning", bar: "bg-orange-500" },
  REORDER_NEEDED: { label: "Reorder", color: "info", bar: "bg-warning-400" },
};

/** Materials at or below their reorder point, with the stock level against that point. */
export default function StockChart({ alerts }: { alerts: StockAlert[] }) {
  return (
    <DetailCard
      fill
      title="Stock Alerts"
      icon={StatIcons.warning}
      tone={alerts.length > 0 ? "warning" : "success"}
      aside={
        <Link to="/materials" className="text-theme-sm font-medium text-brand-500 hover:underline">
          View materials
        </Link>
      }
    >
      {alerts.length === 0 ? (
        <p className="py-10 text-center text-sm text-gray-500 dark:text-gray-400">Every active material is above its reorder point.</p>
      ) : (
        <ul className="space-y-4">
          {alerts.map((a) => {
            const status = STATUS[a.status] ?? STATUS.REORDER_NEEDED;
            const target = Math.max(a.reorderPoint ?? 0, 1);
            const fill = Math.min(100, Math.round((a.currentStock / target) * 100));
            return (
              <li key={a.materialId ?? a.code}>
                <div className="mb-1.5 flex items-center justify-between gap-3">
                  <Link to={`/materials/${a.materialId}`} className="min-w-0 hover:text-brand-500">
                    <span className="block truncate text-sm font-medium text-gray-800 dark:text-white/90">{a.name}</span>
                    <span className="font-mono text-theme-xs text-gray-500 dark:text-gray-400">{a.code}</span>
                  </Link>
                  <Badge size="sm" color={status.color}>
                    {status.label}
                  </Badge>
                </div>
                <div className="h-2 w-full overflow-hidden rounded-full bg-gray-100 dark:bg-gray-800">
                  <div className={`h-full rounded-full ${status.bar}`} style={{ width: `${Math.max(fill, 3)}%` }} />
                </div>
                <p className="mt-1 text-theme-xs text-gray-500 dark:text-gray-400">
                  {a.currentStock} {a.unit ?? ""} in stock · reorder at {a.reorderPoint ?? "—"}
                  {a.stockOnOrder > 0 && ` · ${a.stockOnOrder} on order`}
                </p>
              </li>
            );
          })}
        </ul>
      )}
    </DetailCard>
  );
}
