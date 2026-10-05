import React, { useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import { requisitionApi } from "../services/requisitionApi";
import type { Requisition } from "../types";
import RequisitionStatusBadge from "./RequisitionStatusBadge";
import RequisitionApprovalModal from "./RequisitionApprovalModal";
import RequisitionExpandedRow from "./RequisitionExpandedRow";
import useAuth from "../../auth/hooks/useAuth";
import { isOwnRequisition } from "../utils/requisitionOwnership";
import { formatAmount } from "../../invoices/utils/invoiceLine";
import Button from "../../../shared/components/ui/button/Button";
import ListCard from "../../../shared/components/page/ListCard";
import StatFilterCards, { type StatCard } from "../../../shared/components/page/StatFilterCards";
import { InitialsAvatar, ListFooter, StackedCell, TableStateRow, ViewAction } from "../../../shared/components/page/ListParts";
import { FloatingToast } from "../../../shared/components/page/DetailParts";
import { StatIcons } from "../../../shared/components/page/pageIcons";
import { BODY_CELL, HEAD_CELL } from "../../../shared/components/page/pageStyles";
import { useClientPagination } from "../../../shared/components/page/useClientPagination";
import { Table, TableBody, TableCell, TableHeader, TableRow } from "../../../shared/components/ui/table";
import { getApiErrorMessage } from "../../../shared/utils/apiError";
import { parseAmount } from "../../../shared/utils/moneyUtils";

export type ApprovalTab = "PENDING" | "APPROVED" | "REJECTED" | "ALL";
type Feedback = { type: "success" | "error"; text: string };

const TABS: Record<ApprovalTab, (r: Requisition) => boolean> = {
  ALL: (r) => r.status !== "DRAFT",
  PENDING: (r) => r.status === "SUBMITTED" || r.status === "UNDER_REVIEW",
  APPROVED: (r) => r.status === "APPROVED" || r.status === "CONVERTED",
  REJECTED: (r) => r.status === "REJECTED",
};

const COLUMNS = 7;

/** Sums estimated totals per currency, so amounts in different currencies are never added together. */
function sumByCurrency(requisitions: Requisition[]): string {
  const totals = new Map<string, number>();
  for (const r of requisitions) {
    const currency = r.currencyCode || "MAD";
    totals.set(currency, (totals.get(currency) ?? 0) + parseAmount(r.totalAmount));
  }
  if (totals.size === 0) return formatAmount(0);
  return [...totals.entries()].map(([currency, value]) => formatAmount(value, currency)).join(" + ");
}

/** Approvers review submitted requisitions; nobody approves a requisition they requested. */
export default function RequisitionApprovalsTable() {
  const { hasPermission, user } = useAuth();
  const canValidate = hasPermission("requisition:validate");

  const [requisitions, setRequisitions] = useState<Requisition[]>([]);
  const [loading, setLoading] = useState(true);
  const [refreshKey, setRefreshKey] = useState(0);
  const [feedback, setFeedback] = useState<Feedback | null>(null);
  const [tab, setTab] = useState<ApprovalTab>("PENDING");
  const [keyword, setKeyword] = useState("");
  const [expanded, setExpanded] = useState<Set<string>>(new Set());
  const [decision, setDecision] = useState<{ requisition: Requisition; mode: "APPROVE" | "REJECT" } | null>(null);

  useEffect(() => {
    let cancelled = false;
    requisitionApi
      .getAll()
      .then((res) => {
        if (!cancelled) setRequisitions(Array.isArray(res.data) ? res.data : []);
      })
      .catch((err) => {
        if (!cancelled) setFeedback({ type: "error", text: getApiErrorMessage(err, "Could not load requisitions.") });
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });
    return () => {
      cancelled = true;
    };
  }, [refreshKey]);

  const handleDecision = async (notesOrReason: string) => {
    if (!decision) return;
    const { requisition, mode } = decision;
    const approve = mode === "APPROVE";
    const approverId = user?.id || "";
    const approverName = user?.name || user?.email || "";
    try {
      if (approve) {
        await requisitionApi.approve(requisition.id, approverId, approverName, notesOrReason);
      } else {
        await requisitionApi.reject(requisition.id, notesOrReason, approverId, approverName);
      }
      setFeedback({ type: "success", text: `Requisition ${requisition.requisitionCode} was ${approve ? "approved" : "rejected"}.` });
      setLoading(true);
      setRefreshKey((k) => k + 1);
    } catch (err) {
      setFeedback({ type: "error", text: getApiErrorMessage(err, "The decision could not be saved.") });
      throw err;
    }
  };

  const toggleRow = (id: string) =>
    setExpanded((prev) => {
      const next = new Set(prev);
      if (next.has(id)) next.delete(id);
      else next.add(id);
      return next;
    });

  const pending = requisitions.filter(TABS.PENDING);
  const approved = requisitions.filter(TABS.APPROVED);
  const rejected = requisitions.filter(TABS.REJECTED);
  const cards: StatCard<ApprovalTab>[] = [
    { id: "PENDING", title: "Awaiting Decision", value: pending.length, unit: "requests", subtitle: sumByCurrency(pending), tone: "amber", icon: StatIcons.pending, badge: pending.length },
    { id: "APPROVED", title: "Approved", value: approved.length, unit: "requests", subtitle: "Including those already ordered", tone: "green", icon: StatIcons.done },
    { id: "REJECTED", title: "Rejected", value: rejected.length, unit: "requests", subtitle: "Turned down", tone: "red", icon: StatIcons.overdue },
    { id: "ALL", title: "All Reviewed", value: requisitions.filter(TABS.ALL).length, unit: "requests", subtitle: "Every submitted requisition", tone: "brand", icon: StatIcons.all },
  ];

  const visible = useMemo(() => {
    const term = keyword.trim().toLowerCase();
    return requisitions
      .filter(TABS[tab])
      .filter(
        (r) =>
          !term ||
          [r.requisitionCode, r.title, r.requesterName, r.justification].filter(Boolean).some((v) => String(v).toLowerCase().includes(term))
      )
      .sort((a, b) => String(a.submittedDate ?? a.createdAt ?? "").localeCompare(String(b.submittedDate ?? b.createdAt ?? "")));
  }, [requisitions, tab, keyword]);

  const paging = useClientPagination(visible);

  return (
    <>
      <FloatingToast feedback={feedback} onClose={() => setFeedback(null)} />

      <StatFilterCards cards={cards} active={tab} onSelect={setTab} />

      <ListCard
        title="Approvals"
        subtitle={tab === "PENDING" ? "Oldest submissions first" : undefined}
        search={{ value: keyword, onChange: setKeyword, placeholder: "Search requisitions..." }}
        footer={
          <ListFooter page={paging.page} size={paging.size} total={paging.total} totalPages={paging.totalPages} onPageChange={paging.setPage} />
        }
      >
        <div className="max-w-full overflow-x-auto">
          <Table>
            <TableHeader className="border-b border-gray-100 bg-gray-50/50 dark:border-white/[0.05] dark:bg-gray-900/50">
              <TableRow>
                <TableCell isHeader className={HEAD_CELL}>Requisition</TableCell>
                <TableCell isHeader className={HEAD_CELL}>Requester</TableCell>
                <TableCell isHeader className={HEAD_CELL}>Submitted / Needed</TableCell>
                <TableCell isHeader className={HEAD_CELL}>Estimated</TableCell>
                <TableCell isHeader className={HEAD_CELL}>Status</TableCell>
                <TableCell isHeader className={HEAD_CELL}>Decision</TableCell>
                <TableCell isHeader className={HEAD_CELL}>Action</TableCell>
              </TableRow>
            </TableHeader>
            <TableBody className="divide-y divide-gray-100 dark:divide-white/[0.05]">
              {loading ? (
                <TableStateRow colSpan={COLUMNS} loading message="Fetching requisitions..." />
              ) : paging.pageItems.length === 0 ? (
                <TableStateRow colSpan={COLUMNS} message={tab === "PENDING" ? "Nothing is waiting for a decision." : "No requisitions found."} />
              ) : (
                paging.pageItems.map((req) => {
                  const isOpen = expanded.has(req.id);
                  const isPending = TABS.PENDING(req);
                  const own = isOwnRequisition(req, user?.id);
                  return (
                    <React.Fragment key={req.id}>
                      <TableRow>
                        <TableCell className={BODY_CELL}>
                          <div className="flex items-center gap-2">
                            <button
                              type="button"
                              onClick={() => toggleRow(req.id)}
                              title={isOpen ? "Hide lines" : "Show lines"}
                              aria-label={isOpen ? "Hide lines" : "Show lines"}
                              className="rounded-md p-1 text-gray-400 transition hover:bg-gray-100 hover:text-gray-600 dark:hover:bg-white/[0.05]"
                            >
                              <svg className={`size-5 transition-transform ${isOpen ? "rotate-90 text-brand-500" : ""}`} fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
                              </svg>
                            </button>
                            <StackedCell
                              main={
                                <Link to={`/requisitions/view/${req.id}`} className="font-mono hover:text-brand-500">
                                  {req.requisitionCode}
                                </Link>
                              }
                              sub={<span className="block max-w-[220px] truncate">{req.title}</span>}
                            />
                          </div>
                        </TableCell>
                        <TableCell className={BODY_CELL}>
                          <div className="flex items-center gap-3">
                            <InitialsAvatar name={req.requesterName} />
                            <StackedCell main={req.requesterName} sub={`${req.lines?.length ?? 0} line(s)`} />
                          </div>
                        </TableCell>
                        <TableCell className={BODY_CELL}>
                          <StackedCell main={req.submittedDate ? new Date(req.submittedDate).toLocaleDateString() : "—"} sub={req.requiredDate ? `Needed ${req.requiredDate}` : undefined} />
                        </TableCell>
                        <TableCell className={`${BODY_CELL} whitespace-nowrap`}>{formatAmount(req.totalAmount, req.currencyCode)}</TableCell>
                        <TableCell className={BODY_CELL}>
                          <RequisitionStatusBadge status={req.status} />
                        </TableCell>
                        <TableCell className={BODY_CELL}>
                          {isPending && canValidate ? (
                            own ? (
                              <span className="text-theme-xs text-gray-500 dark:text-gray-400">Your own request</span>
                            ) : (
                              <div className="flex gap-2">
                                <Button size="sm" variant="outline" onClick={() => setDecision({ requisition: req, mode: "REJECT" })}>
                                  Reject
                                </Button>
                                <Button size="sm" onClick={() => setDecision({ requisition: req, mode: "APPROVE" })}>
                                  Approve
                                </Button>
                              </div>
                            )
                          ) : (
                            <StackedCell main={req.approverName || "—"} sub={req.rejectionReason || req.approvalNotes || undefined} />
                          )}
                        </TableCell>
                        <TableCell className={BODY_CELL}>
                          <ViewAction to={`/requisitions/view/${req.id}`} title="View Requisition" />
                        </TableCell>
                      </TableRow>
                      {isOpen && <RequisitionExpandedRow requisition={req} colSpan={COLUMNS} />}
                    </React.Fragment>
                  );
                })
              )}
            </TableBody>
          </Table>
        </div>
      </ListCard>

      {decision && (
        <RequisitionApprovalModal
          isOpen
          mode={decision.mode}
          requisition={decision.requisition}
          onClose={() => setDecision(null)}
          onConfirm={handleDecision}
        />
      )}
    </>
  );
}
