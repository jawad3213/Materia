import { useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import PageBreadcrumb from "../../../shared/components/common/PageBreadCrumb";
import PageMeta from "../../../shared/components/common/PageMeta";
import Button from "../../../shared/components/ui/button/Button";
import { FilterSelect } from "../../../shared/components/page/FilterPanel";
import { FloatingToast, PageLoader } from "../../../shared/components/page/DetailParts";
import { getApiErrorMessage } from "../../../shared/utils/apiError";
import { fileNameFrom, saveBlob } from "../../../shared/utils/download";
import useAuth from "../../auth/hooks/useAuth";
import auditService from "../services/auditService";
import { AUDIT_READ_PERMISSIONS, ENTITY_LABELS, LINE_LABELS, type AuditEntry } from "../types";
import { displayValue, fieldLabel } from "../utils/auditFormat";
import { recordLink } from "../utils/auditLinks";

const LIMITS = [100, 250, 500];

const ACTION_TONES: Record<string, string> = {
  CREATE: "bg-success-50 text-success-700 dark:bg-success-500/15 dark:text-success-400",
  STATUS_CHANGE: "bg-brand-50 text-brand-700 dark:bg-brand-500/15 dark:text-brand-400",
  UPDATE: "bg-warning-50 text-warning-700 dark:bg-warning-500/15 dark:text-orange-400",
  DELETE: "bg-error-50 text-error-700 dark:bg-error-500/15 dark:text-error-400",
};
const ACTION_LABELS: Record<string, string> = { CREATE: "Created", STATUS_CHANGE: "Status", UPDATE: "Edited", DELETE: "Deleted" };

interface Feed {
  key: string;
  entries: AuditEntry[];
  error: string | null;
}

function when(iso: string) {
  return new Date(iso).toLocaleString([], { day: "2-digit", month: "short", year: "numeric", hour: "2-digit", minute: "2-digit" });
}

function actorOf(entry: AuditEntry) {
  return entry.actorName || (entry.actorId === "system" ? "System" : entry.actorId);
}

function FeedRow({ entry }: { entry: AuditEntry }) {
  const [open, setOpen] = useState(false);
  const fields = Object.entries(entry.changes ?? {});
  const link = recordLink(entry);
  const type = ENTITY_LABELS[entry.entityType] ?? LINE_LABELS[entry.entityType] ?? entry.entityType;

  return (
    <li className="px-5 py-4">
      <div className="flex flex-col gap-1 sm:flex-row sm:items-start sm:justify-between sm:gap-4">
        <div className="min-w-0">
          <div className="flex flex-wrap items-center gap-2">
            <span className={`rounded-md px-1.5 py-0.5 text-theme-xs font-medium ${ACTION_TONES[entry.action] ?? ACTION_TONES.UPDATE}`}>
              {ACTION_LABELS[entry.action] ?? entry.action}
            </span>
            <span className="rounded-md bg-gray-100 px-1.5 py-0.5 text-theme-xs text-gray-600 dark:bg-white/[0.05] dark:text-gray-400">{type}</span>
            {link ? (
              <Link to={link} className="truncate text-sm font-medium text-gray-800 hover:text-brand-500 dark:text-white/90">
                {entry.summary || entry.entityId}
              </Link>
            ) : (
              <span className="truncate text-sm font-medium text-gray-800 dark:text-white/90">{entry.summary || entry.entityId}</span>
            )}
          </div>
          {entry.reason && (
            <p className="mt-1.5 text-theme-sm text-gray-700 dark:text-gray-300">
              <span className="font-medium">Reason:</span> {entry.reason}
            </p>
          )}
        </div>
        <p className="shrink-0 text-theme-xs text-gray-500 dark:text-gray-400 sm:text-right">
          <span className="font-medium text-gray-700 dark:text-gray-300">{actorOf(entry)}</span>
          <br className="hidden sm:block" /> <span className="sm:hidden">· </span>
          {when(entry.occurredAt)}
        </p>
      </div>
      {fields.length > 0 && (
        <div className="mt-2">
          <button type="button" onClick={() => setOpen((o) => !o)} className="text-theme-xs font-medium text-brand-500 hover:underline">
            {open ? "Hide" : "Show"} {fields.length} field{fields.length > 1 ? "s" : ""}
          </button>
          {open && (
            <table className="mt-1.5 w-full text-theme-xs">
              <tbody className="divide-y divide-gray-100 dark:divide-gray-800">
                {fields.map(([field, change]) => (
                  <tr key={field}>
                    <td className="w-1/3 py-1 pr-3 text-gray-500 dark:text-gray-400">{fieldLabel(field)}</td>
                    <td className="py-1 text-gray-700 dark:text-gray-300">
                      {entry.action !== "CREATE" && (
                        <>
                          <span className="text-gray-400 line-through">{displayValue(change.from)}</span>
                          <span className="mx-1.5 text-gray-400">→</span>
                        </>
                      )}
                      <span className="font-medium">{displayValue(entry.action === "DELETE" ? change.from : change.to)}</span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      )}
    </li>
  );
}

/** Every recorded change across the platform (log:read), filterable and exportable as CSV (log:export). */
export default function AuditLogPage() {
  const { hasPermission } = useAuth();
  const canRead = hasPermission("log:read");
  const canExport = hasPermission("log:export");

  const [entityType, setEntityType] = useState("");
  const [limit, setLimit] = useState(100);
  const [actor, setActor] = useState("");
  const [search, setSearch] = useState("");
  const [feed, setFeed] = useState<Feed | null>(null);
  const [exporting, setExporting] = useState(false);
  const [feedback, setFeedback] = useState<{ type: "success" | "error"; text: string } | null>(null);

  const requestKey = `${entityType}|${limit}`;

  useEffect(() => {
    if (!canRead) return;
    let cancelled = false;
    auditService
      .recent({ entityType: entityType || undefined, limit })
      .then((res) => !cancelled && setFeed({ key: requestKey, entries: res.data ?? [], error: null }))
      .catch((err) => !cancelled && setFeed({ key: requestKey, entries: [], error: getApiErrorMessage(err, "Could not load the audit log") }));
    return () => {
      cancelled = true;
    };
  }, [canRead, entityType, limit, requestKey]);

  const loading = feed?.key !== requestKey;
  const actors = useMemo(
    () => [...new Set((feed?.entries ?? []).map(actorOf))].sort((a, b) => a.localeCompare(b)),
    [feed]
  );
  const shown = useMemo(() => {
    const term = search.trim().toLowerCase();
    return (feed?.entries ?? []).filter(
      (e) =>
        (!actor || actorOf(e) === actor) &&
        (!term || [e.summary, e.reason, actorOf(e), e.entityType].some((v) => v?.toLowerCase().includes(term)))
    );
  }, [feed, actor, search]);

  const exportCsv = async () => {
    setExporting(true);
    try {
      const res = await auditService.exportCsv({ entityType: entityType || undefined, limit: 5000 });
      saveBlob(res.data, fileNameFrom(res.headers?.["content-disposition"], "audit-log.csv"));
      setFeedback({ type: "success", text: "Audit log exported" });
    } catch (err) {
      setFeedback({ type: "error", text: getApiErrorMessage(err, "The export failed") });
    } finally {
      setExporting(false);
    }
  };

  const typeOptions = [
    { value: "", label: "All records" },
    ...Object.keys(AUDIT_READ_PERMISSIONS).map((t) => ({ value: t, label: ENTITY_LABELS[t] ?? t })),
  ];

  return (
    <>
      <PageMeta title="Audit Log | Materia" description="Every change made across Materia: who, what, when and why." />
      <PageBreadcrumb pageTitle="Audit Log" />

      {!canRead ? (
        <div className="rounded-2xl border border-gray-200 bg-white p-10 text-center text-sm text-gray-500 dark:border-gray-800 dark:bg-white/[0.03]">
          Reading the audit log needs the log:read permission.
        </div>
      ) : (
        <div className="overflow-hidden rounded-2xl border border-gray-200 bg-white dark:border-gray-800 dark:bg-white/[0.03]">
          <div className="flex flex-col gap-4 border-b border-gray-100 px-5 py-4 dark:border-white/[0.05] lg:flex-row lg:items-end lg:justify-between">
            <div className="grid flex-1 grid-cols-1 gap-3 sm:grid-cols-2 xl:grid-cols-4">
              <FilterSelect label="Record type" value={entityType} onChange={setEntityType} options={typeOptions} />
              <FilterSelect
                label="User"
                value={actor}
                onChange={setActor}
                options={[{ value: "", label: "Everyone" }, ...actors.map((a) => ({ value: a, label: a }))]}
              />
              <FilterSelect
                label="Latest"
                value={String(limit)}
                onChange={(v) => setLimit(Number(v))}
                options={LIMITS.map((l) => ({ value: String(l), label: `${l} changes` }))}
              />
              <div>
                <label htmlFor="audit-search" className="mb-1.5 block text-xs font-medium text-gray-700 dark:text-gray-300">
                  Search
                </label>
                <input
                  id="audit-search"
                  value={search}
                  onChange={(e) => setSearch(e.target.value)}
                  placeholder="Code, reason, user..."
                  className="h-11 w-full rounded-lg border border-gray-300 bg-transparent px-4 text-sm text-gray-800 focus:border-brand-300 focus:outline-none focus:ring-3 focus:ring-brand-500/10 dark:border-gray-700 dark:text-white/90"
                />
              </div>
            </div>
            {canExport && (
              <Button size="sm" variant="outline" onClick={exportCsv} disabled={exporting}>
                {exporting ? "Exporting..." : "Export CSV"}
              </Button>
            )}
          </div>

          {!feed ? (
            <PageLoader message="Loading the audit log..." />
          ) : feed.error ? (
            <p role="alert" className="px-5 py-6 text-sm text-error-600">{feed.error}</p>
          ) : shown.length === 0 ? (
            <p className="px-5 py-10 text-center text-sm text-gray-500 dark:text-gray-400">No change matches these filters.</p>
          ) : (
            <>
              <p className="px-5 pt-3 text-theme-xs text-gray-500 dark:text-gray-400">
                {shown.length} of {feed.entries.length} latest change(s)
              </p>
              <ul aria-busy={loading} className={`divide-y divide-gray-100 dark:divide-white/[0.05] ${loading ? "opacity-50" : ""}`}>
                {shown.map((entry) => (
                  <FeedRow key={entry.id} entry={entry} />
                ))}
              </ul>
            </>
          )}
        </div>
      )}

      <FloatingToast feedback={feedback} onClose={() => setFeedback(null)} />
    </>
  );
}
