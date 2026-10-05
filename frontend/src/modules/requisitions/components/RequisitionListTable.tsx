import React, { useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import { requisitionApi } from "../services/requisitionApi";
import purchaseOrderService from "../../purchaseOrders/services/purchaseOrderService";
import useAuth from "../../auth/hooks/useAuth";
import { REQUISITION_STATUS_INFO, type Requisition } from "../types";
import RequisitionStatusBadge from "./RequisitionStatusBadge";
import RequisitionFilters, { type RequisitionFilterValues } from "./RequisitionFilters";
import RequisitionExpandedRow from "./RequisitionExpandedRow";
import RequisitionCancelModal from "./RequisitionCancelModal";
import RequisitionConvertToPoModal from "./RequisitionConvertToPoModal";
import { toPurchaseOrderRequest, type OrderSupplier } from "../utils/convertToPurchaseOrder";
import { formatAmount } from "../../invoices/utils/invoiceLine";
import DeleteConfirmModal from "../../../shared/components/ui/modal/DeleteConfirmModal";
import ListCard, { type FilterPill } from "../../../shared/components/page/ListCard";
import StatFilterCards, { type StatCard } from "../../../shared/components/page/StatFilterCards";
import {
  InitialsAvatar,
  ListFooter,
  RowIconButton,
  StackedCell,
  TableStateRow,
  ViewAction,
} from "../../../shared/components/page/ListParts";
import { FloatingToast } from "../../../shared/components/page/DetailParts";
import { StatIcons } from "../../../shared/components/page/pageIcons";
import { BODY_CELL, HEAD_CELL } from "../../../shared/components/page/pageStyles";
import { useClientPagination } from "../../../shared/components/page/useClientPagination";
import { Table, TableBody, TableCell, TableHeader, TableRow } from "../../../shared/components/ui/table";
import { getApiErrorMessage } from "../../../shared/utils/apiError";

type QuickFilter = "ALL" | "PENDING" | "APPROVED" | "CONVERTED";
type Feedback = { type: "success" | "error"; text: string };

const QUICK_FILTERS: Record<QuickFilter, (r: Requisition) => boolean> = {
  ALL: () => true,
  PENDING: (r) => r.status === "SUBMITTED" || r.status === "UNDER_REVIEW",
  APPROVED: (r) => r.status === "APPROVED",
  CONVERTED: (r) => r.status === "CONVERTED",
};

const EMPTY_FILTERS: RequisitionFilterValues = { status: "", requester: "", neededFrom: "", neededTo: "" };
const COLUMNS = 8;

/** Need-by date, flagged when late or due within three days while the requisition is still open. */
function NeededBy({ requisition }: { requisition: Requisition }) {
  const date = requisition.requiredDate;
  if (!date) return <>—</>;
  const open = ["DRAFT", "SUBMITTED", "UNDER_REVIEW", "APPROVED"].includes(requisition.status);
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  const days = Math.ceil((new Date(`${date}T00:00:00`).getTime() - today.getTime()) / 86_400_000);
  return (
    <StackedCell
      main={date}
      sub={
        open && days < 0 ? (
          <span className="font-semibold text-error-500">Overdue</span>
        ) : open && days <= 3 ? (
          <span className="text-warning-600 dark:text-orange-400">{days === 0 ? "Due today" : `Due in ${days} day(s)`}</span>
        ) : undefined
      }
    />
  );
}

const icons = {
  expand: (
    <svg className="size-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
    </svg>
  ),
  edit: (
    <svg className="size-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
    </svg>
  ),
  submit: (
    <svg className="size-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M14 5l7 7m0 0l-7 7m7-7H3" />
    </svg>
  ),
  order: (
    <svg className="size-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 3h2l.4 2M7 13h10l4-8H5.4M7 13L5.4 5M7 13l-2.293 2.293c-.63.63-.184 1.707.707 1.707H17m0 0a2 2 0 100 4 2 2 0 000-4zm-8 2a2 2 0 11-4 0 2 2 0 014 0z" />
    </svg>
  ),
  cancel: (
    <svg className="size-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M18.364 18.364A9 9 0 005.636 5.636m12.728 12.728L5.636 5.636" />
    </svg>
  ),
  delete: (
    <svg className="size-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
      <path
        strokeLinecap="round"
        strokeLinejoin="round"
        strokeWidth={2}
        d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"
      />
    </svg>
  ),
};

export default function RequisitionListTable() {
  const { user, hasPermission } = useAuth();
  const canWrite = hasPermission("requisition:write");
  const canConvert = hasPermission("requisition:convert");

  const [requisitions, setRequisitions] = useState<Requisition[]>([]);
  const [loading, setLoading] = useState(true);
  const [refreshKey, setRefreshKey] = useState(0);
  const [feedback, setFeedback] = useState<Feedback | null>(null);
  const [expanded, setExpanded] = useState<Set<string>>(new Set());
  const [keyword, setKeyword] = useState("");
  const [quick, setQuick] = useState<QuickFilter>("ALL");
  const [isFilterOpen, setIsFilterOpen] = useState(false);
  const [draftFilters, setDraftFilters] = useState<RequisitionFilterValues>(EMPTY_FILTERS);
  const [filters, setFilters] = useState<RequisitionFilterValues>(EMPTY_FILTERS);
  const [busyId, setBusyId] = useState<string | null>(null);
  const [toDelete, setToDelete] = useState<Requisition | null>(null);
  const [isDeleting, setIsDeleting] = useState(false);
  const [toCancel, setToCancel] = useState<Requisition | null>(null);
  const [toConvert, setToConvert] = useState<Requisition | null>(null);
  const [isConverting, setIsConverting] = useState(false);

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

  const refresh = () => {
    setLoading(true);
    setRefreshKey((k) => k + 1);
  };

  const handleSubmit = async (req: Requisition) => {
    try {
      setBusyId(req.id);
      await requisitionApi.submit(req.id);
      setFeedback({ type: "success", text: `Requisition ${req.requisitionCode} was submitted for approval.` });
      refresh();
    } catch (err) {
      setFeedback({ type: "error", text: getApiErrorMessage(err, "Submitting the requisition failed.") });
    } finally {
      setBusyId(null);
    }
  };

  const handleConvert = async (supplier: OrderSupplier) => {
    if (!toConvert) return;
    try {
      setIsConverting(true);
      const full = (await requisitionApi.getById(toConvert.id)).data;
      const created = await purchaseOrderService.create(toPurchaseOrderRequest(full, supplier, user));
      setToConvert(null);
      setFeedback({ type: "success", text: `Requisition ${full.requisitionCode} is now purchase order ${created.data.orderCode}.` });
      refresh();
    } catch (err) {
      setFeedback({ type: "error", text: getApiErrorMessage(err, "Creating the purchase order failed.") });
    } finally {
      setIsConverting(false);
    }
  };

  const handleCancel = async (reason: string) => {
    if (!toCancel) return;
    await requisitionApi.cancel(toCancel.id, undefined, reason);
    setFeedback({ type: "success", text: `Requisition ${toCancel.requisitionCode} was cancelled.` });
    setToCancel(null);
    refresh();
  };

  const handleDelete = async () => {
    if (!toDelete) return;
    try {
      setIsDeleting(true);
      await requisitionApi.delete(toDelete.id);
      setFeedback({ type: "success", text: `Requisition ${toDelete.requisitionCode} was deleted.` });
      setToDelete(null);
      refresh();
    } catch (err) {
      setFeedback({ type: "error", text: getApiErrorMessage(err, "Deleting the requisition failed.") });
    } finally {
      setIsDeleting(false);
    }
  };

  const toggleRow = (id: string) =>
    setExpanded((prev) => {
      const next = new Set(prev);
      if (next.has(id)) next.delete(id);
      else next.add(id);
      return next;
    });

  const pending = requisitions.filter(QUICK_FILTERS.PENDING);
  const approved = requisitions.filter(QUICK_FILTERS.APPROVED);
  const converted = requisitions.filter(QUICK_FILTERS.CONVERTED);
  const drafts = requisitions.filter((r) => r.status === "DRAFT").length;
  const cards: StatCard<QuickFilter>[] = [
    { id: "ALL", title: "All Requisitions", value: requisitions.length, unit: "requests", subtitle: `${drafts} draft(s)`, tone: "brand", icon: StatIcons.all },
    { id: "PENDING", title: "Awaiting Approval", value: pending.length, unit: "requests", subtitle: "Submitted or under review", tone: "amber", icon: StatIcons.pending, badge: pending.length },
    { id: "APPROVED", title: "Ready to Order", value: approved.length, unit: "requests", subtitle: "Approved, no order yet", tone: "blue", icon: StatIcons.warning, badge: approved.length },
    { id: "CONVERTED", title: "Ordered", value: converted.length, unit: "requests", subtitle: "Turned into purchase orders", tone: "green", icon: StatIcons.done },
  ];

  const visible = useMemo(() => {
    const term = keyword.trim().toLowerCase();
    const requester = filters.requester.trim().toLowerCase();
    return requisitions
      .filter(QUICK_FILTERS[quick])
      .filter((r) => !filters.status || r.status === filters.status)
      .filter((r) => !requester || String(r.requesterName ?? "").toLowerCase().includes(requester))
      .filter((r) => !filters.neededFrom || (r.requiredDate ?? "") >= filters.neededFrom)
      .filter((r) => !filters.neededTo || (!!r.requiredDate && r.requiredDate <= filters.neededTo))
      .filter(
        (r) =>
          !term ||
          [r.requisitionCode, r.title, r.description, r.requesterName, r.purchaseOrderCode, ...(r.lines ?? []).map((l) => l.materialCode)]
            .filter(Boolean)
            .some((v) => String(v).toLowerCase().includes(term))
      )
      .sort((a, b) => String(b.createdAt ?? "").localeCompare(String(a.createdAt ?? "")));
  }, [requisitions, quick, filters, keyword]);

  const paging = useClientPagination(visible);
  const pills: FilterPill[] = [
    ...(filters.status ? [{ label: `Status: ${REQUISITION_STATUS_INFO[filters.status].label}`, onRemove: () => setFilters({ ...filters, status: "" }) }] : []),
    ...(filters.requester ? [{ label: `Requester: ${filters.requester}`, onRemove: () => setFilters({ ...filters, requester: "" }) }] : []),
    ...(filters.neededFrom ? [{ label: `Needed from: ${filters.neededFrom}`, onRemove: () => setFilters({ ...filters, neededFrom: "" }) }] : []),
    ...(filters.neededTo ? [{ label: `Needed to: ${filters.neededTo}`, onRemove: () => setFilters({ ...filters, neededTo: "" }) }] : []),
  ];

  return (
    <>
      <FloatingToast feedback={feedback} onClose={() => setFeedback(null)} />

      <StatFilterCards cards={cards} active={quick} onSelect={setQuick} />

      <ListCard
        title="Requisitions List"
        search={{ value: keyword, onChange: setKeyword, placeholder: "Search requisitions..." }}
        filter={{
          activeCount: pills.length,
          isOpen: isFilterOpen,
          onToggle: () => {
            setDraftFilters(filters);
            setIsFilterOpen(!isFilterOpen);
          },
          panel: (
            <RequisitionFilters
              isOpen={isFilterOpen}
              onClose={() => setIsFilterOpen(false)}
              value={draftFilters}
              onChange={setDraftFilters}
              onApply={() => {
                setFilters(draftFilters);
                setIsFilterOpen(false);
              }}
              onClear={() => {
                setDraftFilters(EMPTY_FILTERS);
                setFilters(EMPTY_FILTERS);
                setIsFilterOpen(false);
              }}
            />
          ),
        }}
        action={canWrite ? { label: "New Requisition", to: "/requisitions/create" } : undefined}
        pills={pills}
        onClearPills={() => setFilters(EMPTY_FILTERS)}
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
                <TableCell isHeader className={HEAD_CELL}>Needed By</TableCell>
                <TableCell isHeader className={HEAD_CELL}>Estimated</TableCell>
                <TableCell isHeader className={HEAD_CELL}>Status</TableCell>
                <TableCell isHeader className={HEAD_CELL}>Order</TableCell>
                <TableCell isHeader className={HEAD_CELL}>Approver</TableCell>
                <TableCell isHeader className={HEAD_CELL}>Action</TableCell>
              </TableRow>
            </TableHeader>
            <TableBody className="divide-y divide-gray-100 dark:divide-white/[0.05]">
              {loading ? (
                <TableStateRow colSpan={COLUMNS} loading message="Fetching requisitions..." />
              ) : paging.pageItems.length === 0 ? (
                <TableStateRow colSpan={COLUMNS} message="No requisitions found." />
              ) : (
                paging.pageItems.map((req) => {
                  const isOpen = expanded.has(req.id);
                  const busy = busyId === req.id;
                  const isPending = req.status === "SUBMITTED" || req.status === "UNDER_REVIEW";
                  const canEdit = canWrite && (req.status === "DRAFT" || isPending);
                  const canDelete = canWrite && ["DRAFT", "REJECTED", "CANCELLED"].includes(req.status);
                  const canCancel = canWrite && (isPending || req.status === "APPROVED");
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
                              <span className={`block transition-transform ${isOpen ? "rotate-90 text-brand-500" : ""}`}>{icons.expand}</span>
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
                          <NeededBy requisition={req} />
                        </TableCell>
                        <TableCell className={`${BODY_CELL} whitespace-nowrap`}>{formatAmount(req.totalAmount, req.currencyCode)}</TableCell>
                        <TableCell className={BODY_CELL}>
                          <RequisitionStatusBadge status={req.status} />
                        </TableCell>
                        <TableCell className={BODY_CELL}>
                          {req.purchaseOrderId ? (
                            <Link to={`/purchase-orders/${req.purchaseOrderId}`} className="font-mono hover:text-brand-500">
                              {req.purchaseOrderCode || "View"}
                            </Link>
                          ) : (
                            "—"
                          )}
                        </TableCell>
                        <TableCell className={BODY_CELL}>{req.approverName || "—"}</TableCell>
                        <TableCell className={BODY_CELL}>
                          <div className="flex items-center">
                            <ViewAction to={`/requisitions/view/${req.id}`} title="View Requisition" />
                            {canEdit && (
                              <Link
                                to={`/requisitions/edit/${req.id}`}
                                title="Edit"
                                aria-label="Edit"
                                className="flex items-center justify-center rounded-lg p-2 text-gray-400 transition-colors hover:bg-brand-50 hover:text-brand-500 dark:hover:bg-brand-500/10"
                              >
                                {icons.edit}
                              </Link>
                            )}
                            {canWrite && req.status === "DRAFT" && (
                              <RowIconButton title="Submit for approval" tone="brand" disabled={busy} onClick={() => handleSubmit(req)}>
                                {icons.submit}
                              </RowIconButton>
                            )}
                            {canConvert && req.status === "APPROVED" && (
                              <RowIconButton title="Create purchase order" tone="brand" disabled={busy} onClick={() => setToConvert(req)}>
                                {icons.order}
                              </RowIconButton>
                            )}
                            {canCancel && (
                              <RowIconButton title="Cancel requisition" tone="warning" disabled={busy} onClick={() => setToCancel(req)}>
                                {icons.cancel}
                              </RowIconButton>
                            )}
                            {canDelete && (
                              <RowIconButton title="Delete" tone="danger" disabled={busy} onClick={() => setToDelete(req)}>
                                {icons.delete}
                              </RowIconButton>
                            )}
                          </div>
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

      <RequisitionConvertToPoModal
        isOpen={!!toConvert}
        onClose={() => setToConvert(null)}
        onConfirm={handleConvert}
        requisition={toConvert}
        isConverting={isConverting}
      />

      {toCancel && <RequisitionCancelModal isOpen onClose={() => setToCancel(null)} requisition={toCancel} onConfirm={handleCancel} />}

      {toDelete && (
        <DeleteConfirmModal
          isOpen
          onClose={() => setToDelete(null)}
          onConfirm={handleDelete}
          title="Delete Requisition"
          message={`Delete requisition ${toDelete.requisitionCode} permanently?`}
          isDeleting={isDeleting}
        />
      )}
    </>
  );
}
