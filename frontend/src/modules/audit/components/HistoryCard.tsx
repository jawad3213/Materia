import { useEffect, useState } from "react";
import useAuth from "../../auth/hooks/useAuth";
import auditService from "../services/auditService";
import { AUDIT_READ_PERMISSIONS, LINE_LABELS, type AuditEntityType, type AuditEntry } from "../types";
import { DetailCard } from "../../../shared/components/page/DetailParts";
import { SectionIcons } from "../../../shared/components/page/pageIcons";
import { getApiErrorMessage } from "../../../shared/utils/apiError";
import { displayValue, fieldLabel } from "../utils/auditFormat";

const COLLAPSED_ENTRIES = 6;

const ACTION_TONES: Record<string, string> = {
  CREATE: "bg-success-50 text-success-700 dark:bg-success-500/15 dark:text-success-400",
  STATUS_CHANGE: "bg-brand-50 text-brand-700 dark:bg-brand-500/15 dark:text-brand-400",
  UPDATE: "bg-warning-50 text-warning-700 dark:bg-warning-500/15 dark:text-orange-400",
  DELETE: "bg-error-50 text-error-700 dark:bg-error-500/15 dark:text-error-400",
};

const ACTION_LABELS: Record<string, string> = {
  CREATE: "Created",
  STATUS_CHANGE: "Status",
  UPDATE: "Edited",
  DELETE: "Deleted",
};

function when(iso: string): string {
  return new Date(iso).toLocaleString([], { day: "2-digit", month: "short", year: "numeric", hour: "2-digit", minute: "2-digit" });
}

function Entry({ entry, ownType }: { entry: AuditEntry; ownType: AuditEntityType }) {
  const [open, setOpen] = useState(entry.action !== "CREATE");
  const fields = Object.entries(entry.changes ?? {});
  const lineLabel = entry.entityType !== ownType ? LINE_LABELS[entry.entityType] ?? entry.entityType : null;

  return (
    <li className="relative pl-6">
      <span className="absolute left-0 top-1.5 size-2.5 rounded-full bg-gray-300 ring-4 ring-white dark:bg-gray-600 dark:ring-gray-900" />
      <div className="flex flex-wrap items-center gap-x-2 gap-y-1">
        <span className={`rounded-md px-1.5 py-0.5 text-theme-xs font-medium ${ACTION_TONES[entry.action] ?? ACTION_TONES.UPDATE}`}>
          {ACTION_LABELS[entry.action] ?? entry.action}
        </span>
        {lineLabel && (
          <span className="rounded-md bg-gray-100 px-1.5 py-0.5 text-theme-xs text-gray-600 dark:bg-white/[0.05] dark:text-gray-400">
            {lineLabel}
          </span>
        )}
        <span className="text-sm font-medium text-gray-800 dark:text-white/90">{entry.summary}</span>
      </div>
      <p className="mt-0.5 text-theme-xs text-gray-500 dark:text-gray-400">
        {entry.actorName || (entry.actorId === "system" ? "System" : entry.actorId)} · {when(entry.occurredAt)}
      </p>
      {entry.reason && (
        <p className="mt-1.5 rounded-lg bg-gray-50 px-3 py-2 text-theme-sm text-gray-700 dark:bg-white/[0.03] dark:text-gray-300">
          <span className="font-medium">Reason:</span> {entry.reason}
        </p>
      )}
      {fields.length > 0 && (
        <div className="mt-1.5">
          <button
            type="button"
            onClick={() => setOpen((o) => !o)}
            className="text-theme-xs font-medium text-brand-500 hover:underline"
          >
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

/**
 * The change history of a record and of its lines: who changed what, when, and why. Shown only to users who may
 * read the record.
 */
export default function HistoryCard({ entityType, entityId }: { entityType: AuditEntityType; entityId: string }) {
  const { hasPermission } = useAuth();
  const allowed = hasPermission(AUDIT_READ_PERMISSIONS[entityType]) || hasPermission("log:read");
  const [entries, setEntries] = useState<AuditEntry[] | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [expanded, setExpanded] = useState(false);

  useEffect(() => {
    if (!allowed || !entityId) return;
    let active = true;
    auditService
      .history(entityType, entityId)
      .then((res) => active && setEntries(res.data))
      .catch((err) => active && setError(getApiErrorMessage(err, "Could not load the history")));
    return () => {
      active = false;
    };
  }, [allowed, entityType, entityId]);

  if (!allowed) return null;

  const shown = entries && !expanded ? entries.slice(0, COLLAPSED_ENTRIES) : entries;
  return (
    <DetailCard
      title="History"
      icon={SectionIcons.calendar}
      aside={entries && <span className="text-theme-sm text-gray-500 dark:text-gray-400">{entries.length} change(s)</span>}
    >
      {error ? (
        <p className="text-sm text-error-600">{error}</p>
      ) : entries === null ? (
        <p className="text-sm text-gray-500 dark:text-gray-400">Loading history...</p>
      ) : entries.length === 0 ? (
        <p className="text-sm text-gray-500 dark:text-gray-400">No change recorded yet.</p>
      ) : (
        <>
          <ol className="relative space-y-5 border-l border-gray-100 pl-0 dark:border-gray-800 [&>li]:-ml-[5px]">
            {shown!.map((entry) => (
              <Entry key={entry.id} entry={entry} ownType={entityType} />
            ))}
          </ol>
          {entries.length > COLLAPSED_ENTRIES && (
            <button
              type="button"
              onClick={() => setExpanded((e) => !e)}
              className="mt-4 text-theme-sm font-medium text-brand-500 hover:underline"
            >
              {expanded ? "Show less" : `Show all ${entries.length} changes`}
            </button>
          )}
        </>
      )}
    </DetailCard>
  );
}
