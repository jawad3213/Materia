import { Link } from "react-router-dom";
import { DetailCard } from "../../../shared/components/page/DetailParts";
import { SectionIcons } from "../../../shared/components/page/pageIcons";
import type { ActivityType, DashboardActivity } from "../types/dashboard.types";
import { timeAgo } from "../utils/dashboardFormat";

const TYPES: Record<ActivityType, { label: string; tone: string }> = {
  REQUISITION: { label: "Requisition", tone: "bg-brand-50 text-brand-600 dark:bg-brand-500/15 dark:text-brand-400" },
  PURCHASE_ORDER: { label: "Order", tone: "bg-sky-50 text-sky-600 dark:bg-sky-500/15 dark:text-sky-400" },
  GOODS_RECEIPT: { label: "Receipt", tone: "bg-emerald-50 text-emerald-600 dark:bg-emerald-500/15 dark:text-emerald-400" },
  INVOICE: { label: "Invoice", tone: "bg-amber-50 text-amber-600 dark:bg-amber-500/15 dark:text-amber-400" },
  PAYMENT: { label: "Payment", tone: "bg-purple-50 text-purple-600 dark:bg-purple-500/15 dark:text-purple-400" },
  RETURN: { label: "Return", tone: "bg-orange-50 text-orange-600 dark:bg-orange-500/15 dark:text-orange-400" },
};

const humanize = (status?: string | null) => (status ? status.charAt(0) + status.slice(1).toLowerCase().replace(/_/g, " ") : "");

/** The latest documents created across procurement. */
export default function RecentActivities({ activities }: { activities: DashboardActivity[] }) {
  return (
    <DetailCard fill title="Recent Activity" icon={SectionIcons.calendar} tone="gray">
      {activities.length === 0 ? (
        <p className="py-10 text-center text-sm text-gray-500 dark:text-gray-400">Nothing has been recorded yet.</p>
      ) : (
        <ol className="relative space-y-5 border-l border-gray-100 pl-6 dark:border-gray-800">
          {activities.map((a) => {
            const type = TYPES[a.type] ?? TYPES.REQUISITION;
            return (
              <li key={`${a.type}-${a.link}`} className="relative">
                <span className={`absolute -left-[33px] top-0.5 flex size-4 items-center justify-center rounded-full ring-4 ring-white dark:ring-gray-900 ${type.tone}`}>
                  <span className="size-1.5 rounded-full bg-current" />
                </span>
                <div className="flex items-start justify-between gap-3">
                  <div className="min-w-0">
                    <p className="text-sm text-gray-800 dark:text-white/90">
                      <span className={`mr-2 rounded-md px-1.5 py-0.5 text-theme-xs font-medium ${type.tone}`}>{type.label}</span>
                      <Link to={a.link} className="font-mono font-medium hover:text-brand-500">
                        {a.code}
                      </Link>
                    </p>
                    <p className="mt-0.5 truncate text-theme-xs text-gray-500 dark:text-gray-400">
                      {[a.title, humanize(a.status)].filter(Boolean).join(" · ")}
                    </p>
                  </div>
                  <span className="shrink-0 text-theme-xs text-gray-400">{timeAgo(a.date)}</span>
                </div>
              </li>
            );
          })}
        </ol>
      )}
    </DetailCard>
  );
}
