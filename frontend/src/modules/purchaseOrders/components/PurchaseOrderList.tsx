import React, { useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import purchaseOrderService from "../services/purchaseOrderService";
import { DELIVERY_STATUS_INFO, ORDER_STATUS_INFO, type PurchaseOrder } from "../types";
import PurchaseOrderStatusBadge, { PurchaseOrderDeliveryStatusBadge } from "./PurchaseOrderStatusBadge";
import PurchaseOrderFilters, { type PurchaseOrderFilterValues } from "./PurchaseOrderFilters";
import PurchaseOrderExpandedRow from "./PurchaseOrderExpandedRow";
import PurchaseOrderCancelModal from "./PurchaseOrderCancelModal";
import useAuth from "../../auth/hooks/useAuth";
import usePurchaseOrderPermissions from "../hooks/usePurchaseOrder";
import { formatAmount } from "../../invoices/utils/invoiceLine";
import DeleteConfirmModal from "../../../shared/components/ui/modal/DeleteConfirmModal";
import Badge from "../../../shared/components/ui/badge/Badge";
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
import { parseAmount } from "../../../shared/utils/moneyUtils";

type QuickFilter = "ALL" | "WITH_SUPPLIER" | "READY_FOR_RECEIPT" | "COMPLETED";
type Feedback = { type: "success" | "error"; text: string };

const QUICK_FILTERS: Record<QuickFilter, (o: PurchaseOrder) => boolean> = {
  ALL: () => true,
  WITH_SUPPLIER: (o) => o.status === "SUBMITTED" || o.status === "CONFIRMED",
  READY_FOR_RECEIPT: (o) => o.status === "READY_FOR_RECEIPT" || o.status === "PARTIALLY_RECEIVED",
  COMPLETED: (o) => o.status === "COMPLETED",
};

const EMPTY_FILTERS: PurchaseOrderFilterValues = { status: "", deliveryStatus: "", dateFrom: "", dateTo: "" };
const COLUMNS = 8;

/** Sums order totals per currency, so amounts in different currencies are never added together. */
function sumByCurrency(orders: PurchaseOrder[]): string {
  const totals = new Map<string, number>();
  for (const o of orders) {
    const currency = o.currencyCode || "MAD";
    totals.set(currency, (totals.get(currency) ?? 0) + parseAmount(o.grandTotal));
  }
  if (totals.size === 0) return formatAmount(0);
  return [...totals.entries()].map(([currency, value]) => formatAmount(value, currency)).join(" + ");
}

/** Expected delivery date, flagged when late or due within three days (open orders only). */
function DeliveryDue({ order }: { order: PurchaseOrder }) {
  const date = order.expectedDeliveryDate;
  if (!date) return <>—</>;
  const open = ["SUBMITTED", "CONFIRMED", "READY_FOR_RECEIPT", "PARTIALLY_RECEIVED"].includes(order.status);
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  const days = Math.ceil((new Date(`${date}T00:00:00`).getTime() - today.getTime()) / 86_400_000);
  return (
    <StackedCell
      main={date}
      sub={
        open && days < 0 ? (
          <span className="font-semibold text-error-500">{Math.abs(days)} day(s) late</span>
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
  submit: (
    <svg className="size-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M14 5l7 7m0 0l-7 7m7-7H3" />
    </svg>
  ),
  confirm: (
    <svg className="size-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
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

export default function PurchaseOrderList() {
  const { user } = useAuth();
  const permissions = usePurchaseOrderPermissions();

  const [orders, setOrders] = useState<PurchaseOrder[]>([]);
  const [loading, setLoading] = useState(true);
  const [refreshKey, setRefreshKey] = useState(0);
  const [feedback, setFeedback] = useState<Feedback | null>(null);
  const [expanded, setExpanded] = useState<Set<string>>(new Set());
  const [keyword, setKeyword] = useState("");
  const [quick, setQuick] = useState<QuickFilter>("ALL");
  const [isFilterOpen, setIsFilterOpen] = useState(false);
  const [draftFilters, setDraftFilters] = useState<PurchaseOrderFilterValues>(EMPTY_FILTERS);
  const [filters, setFilters] = useState<PurchaseOrderFilterValues>(EMPTY_FILTERS);
  const [orderToDelete, setOrderToDelete] = useState<PurchaseOrder | null>(null);
  const [isDeleting, setIsDeleting] = useState(false);
  const [orderToCancel, setOrderToCancel] = useState<PurchaseOrder | null>(null);
  const [busyId, setBusyId] = useState<string | null>(null);

  useEffect(() => {
    let cancelled = false;
    purchaseOrderService
      .getAll()
      .then((res) => {
        if (!cancelled) setOrders(Array.isArray(res.data) ? res.data : []);
      })
      .catch((err) => {
        if (!cancelled) setFeedback({ type: "error", text: getApiErrorMessage(err, "Could not load purchase orders.") });
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

  const runAction = async (order: PurchaseOrder, action: () => Promise<unknown>, success: string, failure: string) => {
    try {
      setBusyId(order.id);
      await action();
      setFeedback({ type: "success", text: success });
      refresh();
    } catch (err) {
      setFeedback({ type: "error", text: getApiErrorMessage(err, failure) });
    } finally {
      setBusyId(null);
    }
  };

  const userId = user?.id || "";
  const handleSubmit = (order: PurchaseOrder) =>
    runAction(order, () => purchaseOrderService.submit(order.id, { userId }), `Order ${order.orderCode} was sent to the supplier.`, "Submitting the order failed.");
  const handleConfirm = (order: PurchaseOrder) =>
    runAction(order, () => purchaseOrderService.confirm(order.id, { userId }), `Order ${order.orderCode} is confirmed by the supplier.`, "Confirming the order failed.");

  const handleCancel = async (reason: string) => {
    if (!orderToCancel) return;
    await purchaseOrderService.cancel(orderToCancel.id, { userId, reason });
    setFeedback({ type: "success", text: `Order ${orderToCancel.orderCode} was cancelled.` });
    setOrderToCancel(null);
    refresh();
  };

  const handleDelete = async () => {
    if (!orderToDelete) return;
    try {
      setIsDeleting(true);
      await purchaseOrderService.delete(orderToDelete.id);
      setFeedback({ type: "success", text: `Order ${orderToDelete.orderCode} was deleted.` });
      setOrderToDelete(null);
      refresh();
    } catch (err) {
      setFeedback({ type: "error", text: getApiErrorMessage(err, "Deleting the order failed.") });
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

  const withSupplier = orders.filter(QUICK_FILTERS.WITH_SUPPLIER);
  const toReceive = orders.filter(QUICK_FILTERS.READY_FOR_RECEIPT);
  const completed = orders.filter(QUICK_FILTERS.COMPLETED);
  const drafts = orders.filter((o) => o.status === "DRAFT").length;
  const cards: StatCard<QuickFilter>[] = [
    { id: "ALL", title: "All Orders", value: orders.length, unit: "orders", subtitle: `${drafts} draft(s)`, tone: "brand", icon: StatIcons.all },
    {
      id: "WITH_SUPPLIER",
      title: "With Supplier",
      value: withSupplier.length,
      unit: "orders",
      subtitle: sumByCurrency(withSupplier),
      tone: "amber",
      icon: StatIcons.pending,
      badge: withSupplier.length,
    },
    {
      id: "READY_FOR_RECEIPT",
      title: "To Receive",
      value: toReceive.length,
      unit: "orders",
      subtitle: "Ready or partly received",
      tone: "blue",
      icon: StatIcons.warning,
      badge: toReceive.length,
    },
    { id: "COMPLETED", title: "Completed", value: completed.length, unit: "orders", subtitle: sumByCurrency(completed), tone: "green", icon: StatIcons.done },
  ];

  const visible = useMemo(() => {
    const term = keyword.trim().toLowerCase();
    return orders
      .filter(QUICK_FILTERS[quick])
      .filter((o) => !filters.status || o.status === filters.status)
      .filter((o) => !filters.deliveryStatus || o.deliveryStatus === filters.deliveryStatus)
      .filter((o) => !filters.dateFrom || o.orderDate >= filters.dateFrom)
      .filter((o) => !filters.dateTo || o.orderDate <= filters.dateTo)
      .filter(
        (o) =>
          !term ||
          [o.orderCode, o.supplierName, o.supplierCode, o.requisitionCode, o.orderedByName, o.assignedToName]
            .filter(Boolean)
            .some((v) => String(v).toLowerCase().includes(term))
      )
      .sort((a, b) => String(b.createdAt ?? b.orderDate ?? "").localeCompare(String(a.createdAt ?? a.orderDate ?? "")));
  }, [orders, quick, filters, keyword]);

  const paging = useClientPagination(visible);
  const pills: FilterPill[] = [
    ...(filters.status ? [{ label: `Status: ${ORDER_STATUS_INFO[filters.status].label}`, onRemove: () => setFilters({ ...filters, status: "" }) }] : []),
    ...(filters.deliveryStatus
      ? [{ label: `Delivery: ${DELIVERY_STATUS_INFO[filters.deliveryStatus].label}`, onRemove: () => setFilters({ ...filters, deliveryStatus: "" }) }]
      : []),
    ...(filters.dateFrom ? [{ label: `From: ${filters.dateFrom}`, onRemove: () => setFilters({ ...filters, dateFrom: "" }) }] : []),
    ...(filters.dateTo ? [{ label: `To: ${filters.dateTo}`, onRemove: () => setFilters({ ...filters, dateTo: "" }) }] : []),
  ];

  return (
    <>
      <FloatingToast feedback={feedback} onClose={() => setFeedback(null)} />

      <StatFilterCards cards={cards} active={quick} onSelect={setQuick} />

      <ListCard
        title="Purchase Orders List"
        search={{ value: keyword, onChange: setKeyword, placeholder: "Search orders..." }}
        filter={{
          activeCount: pills.length,
          isOpen: isFilterOpen,
          onToggle: () => {
            setDraftFilters(filters);
            setIsFilterOpen(!isFilterOpen);
          },
          panel: (
            <PurchaseOrderFilters
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
        action={permissions.canCreate ? { label: "New Order", to: "/purchase-orders/create" } : undefined}
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
                <TableCell isHeader className={HEAD_CELL}>Order</TableCell>
                <TableCell isHeader className={HEAD_CELL}>Supplier</TableCell>
                <TableCell isHeader className={HEAD_CELL}>Ordered</TableCell>
                <TableCell isHeader className={HEAD_CELL}>Expected</TableCell>
                <TableCell isHeader className={HEAD_CELL}>Total</TableCell>
                <TableCell isHeader className={HEAD_CELL}>Status</TableCell>
                <TableCell isHeader className={HEAD_CELL}>Delivery</TableCell>
                <TableCell isHeader className={HEAD_CELL}>Action</TableCell>
              </TableRow>
            </TableHeader>
            <TableBody className="divide-y divide-gray-100 dark:divide-white/[0.05]">
              {loading ? (
                <TableStateRow colSpan={COLUMNS} loading message="Fetching purchase orders..." />
              ) : paging.pageItems.length === 0 ? (
                <TableStateRow colSpan={COLUMNS} message="No purchase orders found." />
              ) : (
                paging.pageItems.map((order) => {
                  const isOpen = expanded.has(order.id);
                  const busy = busyId === order.id;
                  return (
                    <React.Fragment key={order.id}>
                      <TableRow>
                        <TableCell className={BODY_CELL}>
                          <div className="flex items-center gap-2">
                            <button
                              type="button"
                              onClick={() => toggleRow(order.id)}
                              title={isOpen ? "Hide lines" : "Show lines"}
                              aria-label={isOpen ? "Hide lines" : "Show lines"}
                              className="rounded-md p-1 text-gray-400 transition hover:bg-gray-100 hover:text-gray-600 dark:hover:bg-white/[0.05]"
                            >
                              <span className={`block transition-transform ${isOpen ? "rotate-90 text-brand-500" : ""}`}>{icons.expand}</span>
                            </button>
                            <StackedCell
                              main={
                                <Link to={`/purchase-orders/${order.id}`} className="font-mono hover:text-brand-500">
                                  {order.orderCode}
                                </Link>
                              }
                              sub={order.requisitionCode ? `From ${order.requisitionCode}` : `${order.lines?.length ?? 0} line(s)`}
                            />
                          </div>
                        </TableCell>
                        <TableCell className={BODY_CELL}>
                          <div className="flex items-center gap-3">
                            <InitialsAvatar name={order.supplierName} />
                            <StackedCell main={order.supplierName} sub={order.supplierCode} />
                          </div>
                        </TableCell>
                        <TableCell className={BODY_CELL}>
                          <StackedCell main={order.orderDate} sub={order.orderedByName ?? undefined} />
                        </TableCell>
                        <TableCell className={BODY_CELL}>
                          <DeliveryDue order={order} />
                        </TableCell>
                        <TableCell className={`${BODY_CELL} whitespace-nowrap`}>{formatAmount(order.grandTotal, order.currencyCode)}</TableCell>
                        <TableCell className={BODY_CELL}>
                          <PurchaseOrderStatusBadge status={order.status} />
                        </TableCell>
                        <TableCell className={BODY_CELL}>
                          <div className="flex flex-col items-start gap-1">
                            <PurchaseOrderDeliveryStatusBadge status={order.deliveryStatus} />
                            {order.assignedToName && <Badge size="sm" color="light">{order.assignedToName}</Badge>}
                          </div>
                        </TableCell>
                        <TableCell className={BODY_CELL}>
                          <div className="flex items-center">
                            <ViewAction to={`/purchase-orders/${order.id}`} title="View Order" />
                            {permissions.canSubmit(order) && (
                              <RowIconButton title="Send to supplier" tone="brand" disabled={busy} onClick={() => handleSubmit(order)}>
                                {icons.submit}
                              </RowIconButton>
                            )}
                            {permissions.canConfirm(order) && (
                              <RowIconButton title="Record supplier confirmation" tone="brand" disabled={busy} onClick={() => handleConfirm(order)}>
                                {icons.confirm}
                              </RowIconButton>
                            )}
                            {permissions.canCancel(order) && (
                              <RowIconButton title="Cancel order" tone="warning" disabled={busy} onClick={() => setOrderToCancel(order)}>
                                {icons.cancel}
                              </RowIconButton>
                            )}
                            {permissions.canDelete(order) && (
                              <RowIconButton title="Delete draft" tone="danger" disabled={busy} onClick={() => setOrderToDelete(order)}>
                                {icons.delete}
                              </RowIconButton>
                            )}
                          </div>
                        </TableCell>
                      </TableRow>
                      {isOpen && <PurchaseOrderExpandedRow order={order} colSpan={COLUMNS} />}
                    </React.Fragment>
                  );
                })
              )}
            </TableBody>
          </Table>
        </div>
      </ListCard>

      {orderToDelete && (
        <DeleteConfirmModal
          isOpen
          onClose={() => setOrderToDelete(null)}
          onConfirm={handleDelete}
          title="Delete Order"
          message={`Delete draft order ${orderToDelete.orderCode} permanently?`}
          isDeleting={isDeleting}
        />
      )}

      {orderToCancel && (
        <PurchaseOrderCancelModal isOpen onClose={() => setOrderToCancel(null)} order={orderToCancel} onConfirm={handleCancel} />
      )}
    </>
  );
}
