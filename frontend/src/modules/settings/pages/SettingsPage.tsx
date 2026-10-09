import { useEffect, useMemo, useState, type FormEvent } from "react";
import PageBreadcrumb from "../../../shared/components/common/PageBreadCrumb";
import PageMeta from "../../../shared/components/common/PageMeta";
import Button from "../../../shared/components/ui/button/Button";
import { DetailCard, FloatingToast, PageLoader } from "../../../shared/components/page/DetailParts";
import { SectionIcons } from "../../../shared/components/page/pageIcons";
import { getApiErrorMessage } from "../../../shared/utils/apiError";
import useAuth from "../../auth/hooks/useAuth";
import ChangeReasonField from "../../audit/components/ChangeReasonField";
import auditService from "../../audit/services/auditService";
import type { AuditEntry } from "../../audit/types";
import { displayValue } from "../../audit/utils/auditFormat";
import settingsService from "../services/settingsService";
import type { AppSetting } from "../types";

type Feedback = { type: "success" | "error"; text: string } | null;

const INPUT =
  "h-11 w-full rounded-lg border border-gray-300 bg-transparent px-4 text-sm text-gray-800 focus:border-brand-300 focus:outline-none focus:ring-3 focus:ring-brand-500/10 disabled:cursor-not-allowed disabled:bg-gray-50 disabled:text-gray-500 dark:border-gray-700 dark:text-white/90 dark:disabled:bg-white/[0.03]";

const INPUT_TYPE: Record<AppSetting["kind"], string> = { TEXT: "text", EMAIL: "email", PHONE: "tel", TAX_ID: "text", PERCENT: "number" };

/** Application settings: company details used on documents, and the invoice price tolerance used by matching. */
export default function SettingsPage() {
  const { hasPermission } = useAuth();
  const canWrite = hasPermission("setting:write");
  const canSeeHistory = hasPermission("log:read");

  const [settings, setSettings] = useState<AppSetting[] | null>(null);
  const [loadError, setLoadError] = useState<string | null>(null);
  const [draft, setDraft] = useState<Record<string, string>>({});
  const [reason, setReason] = useState("");
  const [saving, setSaving] = useState(false);
  const [saveError, setSaveError] = useState<string | null>(null);
  const [feedback, setFeedback] = useState<Feedback>(null);
  const [history, setHistory] = useState<AuditEntry[] | null>(null);
  const [historyKey, setHistoryKey] = useState(0);

  useEffect(() => {
    let cancelled = false;
    settingsService
      .getAll()
      .then((res) => {
        if (cancelled) return;
        setSettings(res.data);
        setDraft(Object.fromEntries(res.data.map((s) => [s.key, s.value ?? ""])));
      })
      .catch((err) => !cancelled && setLoadError(getApiErrorMessage(err, "Settings could not be loaded")));
    return () => {
      cancelled = true;
    };
  }, []);

  useEffect(() => {
    if (!canSeeHistory) return;
    let cancelled = false;
    auditService
      .recent({ entityType: "Setting", limit: 20 })
      .then((res) => !cancelled && setHistory(res.data ?? []))
      .catch(() => !cancelled && setHistory([]));
    return () => {
      cancelled = true;
    };
  }, [canSeeHistory, historyKey]);

  const groups = useMemo(() => {
    const byGroup = new Map<string, AppSetting[]>();
    (settings ?? []).forEach((s) => byGroup.set(s.group, [...(byGroup.get(s.group) ?? []), s]));
    return [...byGroup.entries()];
  }, [settings]);

  const changed = useMemo(
    () => Object.fromEntries((settings ?? []).filter((s) => (draft[s.key] ?? "") !== (s.value ?? "")).map((s) => [s.key, draft[s.key] ?? ""])),
    [settings, draft]
  );
  const changedCount = Object.keys(changed).length;
  const labelOf = (entry: AuditEntry) =>
    settings?.find((s) => s.id === entry.entityId)?.label ?? entry.summary?.replace(/^(Created|Deleted) /, "") ?? "Setting";

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    if (!changedCount) return;
    setSaving(true);
    setSaveError(null);
    try {
      const res = await settingsService.update(changed, reason);
      setSettings(res.data);
      setDraft(Object.fromEntries(res.data.map((s) => [s.key, s.value ?? ""])));
      setReason("");
      setHistoryKey((k) => k + 1);
      setFeedback({ type: "success", text: "Settings saved" });
    } catch (err) {
      setSaveError(getApiErrorMessage(err, "The settings could not be saved"));
    } finally {
      setSaving(false);
    }
  };

  return (
    <>
      <PageMeta title="Settings | Materia" description="Company details and invoice matching settings." />
      <PageBreadcrumb pageTitle="Settings" />

      {loadError ? (
        <p role="alert" className="rounded-xl border border-error-200 bg-error-50 px-4 py-3 text-sm text-error-700 dark:border-error-500/30 dark:bg-error-500/10 dark:text-error-400">
          {loadError}
        </p>
      ) : !settings ? (
        <PageLoader message="Loading settings..." />
      ) : (
        <form onSubmit={handleSubmit} noValidate className="space-y-6">
          {!canWrite && (
            <p className="rounded-xl border border-gray-200 bg-gray-50 px-4 py-3 text-sm text-gray-600 dark:border-gray-800 dark:bg-white/[0.03] dark:text-gray-400">
              You can view the settings; changing them needs the setting:write permission.
            </p>
          )}

          {groups.map(([group, items]) => (
            <DetailCard key={group} title={group} icon={SectionIcons.info} tone="brand">
              <div className="grid grid-cols-1 gap-5 md:grid-cols-2">
                {items.map((s) => (
                  <div key={s.key}>
                    <label htmlFor={`setting-${s.key}`} className="mb-1.5 block text-sm font-medium text-gray-700 dark:text-gray-400">
                      {s.label}
                    </label>
                    <input
                      id={`setting-${s.key}`}
                      type={INPUT_TYPE[s.kind]}
                      step={s.kind === "PERCENT" ? "0.1" : undefined}
                      min={s.kind === "PERCENT" ? 0 : undefined}
                      max={s.kind === "PERCENT" ? 50 : undefined}
                      maxLength={s.kind === "PERCENT" ? undefined : s.maxLength}
                      value={draft[s.key] ?? ""}
                      onChange={(e) => setDraft((d) => ({ ...d, [s.key]: e.target.value }))}
                      disabled={!canWrite}
                      className={INPUT}
                    />
                    <p className="mt-1 text-theme-xs text-gray-500 dark:text-gray-400">
                      {s.description}
                      {!s.customized && s.defaultValue ? ` Default: ${s.defaultValue}.` : ""}
                    </p>
                  </div>
                ))}
              </div>
            </DetailCard>
          ))}

          {canWrite && (
            <div className="rounded-2xl border border-gray-200 bg-white p-5 dark:border-gray-800 dark:bg-white/[0.03]">
              {saveError && (
                <p role="alert" className="mb-4 rounded-lg border border-error-200 bg-error-50 px-4 py-3 text-sm text-error-700 dark:border-error-500/30 dark:bg-error-500/10 dark:text-error-400">
                  {saveError}
                </p>
              )}
              <ChangeReasonField value={reason} onChange={setReason} />
              <div className="mt-2 flex items-center justify-between gap-3">
                <span className="text-theme-sm text-gray-500 dark:text-gray-400">
                  {changedCount ? `${changedCount} unsaved change(s)` : "No unsaved change"}
                </span>
                <Button size="sm" type="submit" disabled={saving || !changedCount}>
                  {saving ? "Saving..." : "Save settings"}
                </Button>
              </div>
            </div>
          )}

          {canSeeHistory && (
            <DetailCard title="Recent changes" icon={SectionIcons.calendar}>
              {history === null ? (
                <p className="text-sm text-gray-500 dark:text-gray-400">Loading...</p>
              ) : history.length === 0 ? (
                <p className="text-sm text-gray-500 dark:text-gray-400">No setting has been changed yet.</p>
              ) : (
                <ul className="divide-y divide-gray-100 text-sm dark:divide-gray-800">
                  {history.map((h) => {
                    const change = h.changes?.settingValue;
                    return (
                      <li key={h.id} className="py-2.5">
                        <p className="text-gray-800 dark:text-white/90">
                          <span className="font-medium">{labelOf(h)}</span>
                          {change && (
                            <>
                              {": "}
                              <span className="text-gray-400 line-through">{displayValue(change.from)}</span>
                              <span className="mx-1.5 text-gray-400">→</span>
                              <span className="font-medium">{displayValue(change.to)}</span>
                            </>
                          )}
                        </p>
                        <p className="text-theme-xs text-gray-500 dark:text-gray-400">
                          {h.actorName || h.actorId} · {new Date(h.occurredAt).toLocaleString()}
                          {h.reason && ` · ${h.reason}`}
                        </p>
                      </li>
                    );
                  })}
                </ul>
              )}
            </DetailCard>
          )}
        </form>
      )}

      <FloatingToast feedback={feedback} onClose={() => setFeedback(null)} />
    </>
  );
}
