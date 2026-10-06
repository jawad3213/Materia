import { Link } from "react-router-dom";
import useAuth from "../../auth/hooks/useAuth";
import { DetailCard } from "../../../shared/components/page/DetailParts";
import { SectionIcons } from "../../../shared/components/page/pageIcons";
import { QUICK_ACTIONS } from "../utils/quickActions";

/** Shortcuts as a panel whose tiles share the height of the row it sits in. */
export default function QuickActions() {
  const { hasPermission } = useAuth();
  const actions = QUICK_ACTIONS.filter((a) => hasPermission(a.permission));
  if (actions.length === 0) return null;

  return (
    <DetailCard title="Quick Actions" icon={SectionIcons.note} tone="brand" fill>
      <div className="grid flex-1 auto-rows-fr grid-cols-1 gap-3 sm:grid-cols-2">
        {actions.map((a) => (
          <Link
            key={a.to}
            to={a.to}
            className="group flex flex-col justify-center gap-2 rounded-xl border border-gray-200 px-4 py-3 transition hover:border-brand-300 hover:bg-brand-25 dark:border-gray-800 dark:hover:border-brand-500/40"
          >
            <span className="flex size-8 items-center justify-center rounded-lg bg-brand-50 text-brand-500 transition group-hover:bg-brand-500 group-hover:text-white dark:bg-brand-500/15">
              <svg className="size-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v16m8-8H4" />
              </svg>
            </span>
            <span className="min-w-0">
              <span className="block truncate text-theme-sm font-medium text-gray-800 dark:text-white/90">{a.label}</span>
              <span className="block truncate text-theme-xs text-gray-500 dark:text-gray-400">{a.description}</span>
            </span>
          </Link>
        ))}
      </div>
    </DetailCard>
  );
}
