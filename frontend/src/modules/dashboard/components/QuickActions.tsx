import { Link } from "react-router-dom";
import useAuth from "../../auth/hooks/useAuth";

interface QuickAction {
  label: string;
  description: string;
  to: string;
  permission: string;
}

/** Shortcuts to start the most common documents, shown only to users who may create them. */
const ACTIONS: QuickAction[] = [
  { label: "New Requisition", description: "Request materials", to: "/requisitions/create", permission: "requisition:write" },
  { label: "New Purchase Order", description: "Order from a supplier", to: "/purchase-orders/create", permission: "order:write" },
  { label: "Record Receipt", description: "Receive a delivery", to: "/goods-receipts/create", permission: "receipt:write" },
  { label: "Record Invoice", description: "Match a supplier invoice", to: "/invoices/create", permission: "invoice:write" },
  { label: "New Payment", description: "Pay verified invoices", to: "/payments/create", permission: "payment:write" },
  { label: "Return Goods", description: "Send rejected goods back", to: "/returns/create", permission: "return:write" },
];

export default function QuickActions() {
  const { hasPermission } = useAuth();
  const actions = ACTIONS.filter((a) => hasPermission(a.permission));
  if (actions.length === 0) return null;

  return (
    <div className="grid grid-cols-2 gap-3 md:grid-cols-3 xl:grid-cols-6">
      {actions.map((a) => (
        <Link
          key={a.to}
          to={a.to}
          className="group flex items-center gap-3 rounded-xl border border-gray-200 bg-white px-4 py-3 transition hover:border-brand-300 hover:bg-brand-25 dark:border-gray-800 dark:bg-white/[0.03] dark:hover:border-brand-500/40"
        >
          <span className="flex size-8 shrink-0 items-center justify-center rounded-lg bg-brand-50 text-brand-500 transition group-hover:bg-brand-500 group-hover:text-white dark:bg-brand-500/15">
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
  );
}
