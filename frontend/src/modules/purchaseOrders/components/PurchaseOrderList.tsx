import React, { useEffect, useState, useRef, useMemo } from "react";
import { Link } from "react-router-dom";
import {
  Table,
  TableBody,
  TableCell,
  TableHeader,
  TableRow,
} from "../../../shared/components/ui/table";
import Checkbox from "../../../shared/components/form/input/Checkbox";
import Button from "../../../shared/components/ui/button/Button";
import DeleteConfirmModal from "../../../shared/components/ui/modal/DeleteConfirmModal";
import Pagination from "../../../shared/components/ui/Pagination";
import purchaseOrderService from "../services/purchaseOrderService";
import type {
  PurchaseOrder,
  PurchaseOrderFilterTab,
  OrderStatus,
  DeliveryStatus,
} from "../types";
import PurchaseOrderStatusBadge, {
  PurchaseOrderDeliveryStatusBadge,
} from "./PurchaseOrderStatusBadge";
import PurchaseOrderStatCards from "./PurchaseOrderStatCards";
import PurchaseOrderFilters from "./PurchaseOrderFilters";
import PurchaseOrderExpandedRow from "./PurchaseOrderExpandedRow";
import PurchaseOrderCancelModal from "./PurchaseOrderCancelModal";
import useAuth from "../../auth/hooks/useAuth";
import usePurchaseOrderPermissions from "../hooks/usePurchaseOrder";

export default function PurchaseOrderList() {
  const { user } = useAuth();
  const permissions = usePurchaseOrderPermissions();
  const canCreate = permissions.canCreate;

  const [orders, setOrders] = useState<PurchaseOrder[]>([]);
  const [loading, setLoading] = useState(true);
  const [selectedIds, setSelectedIds] = useState<string[]>([]);
  const [expandedRowIds, setExpandedRowIds] = useState<Set<string>>(new Set());

  // Delete modal state
  const [orderToDelete, setOrderToDelete] = useState<PurchaseOrder | null>(null);
  const [isDeleting, setIsDeleting] = useState(false);

  // Cancel modal state
  const [orderToCancel, setOrderToCancel] = useState<PurchaseOrder | null>(null);
  const [isCancelling, setIsCancelling] = useState(false);

  // User feedback toast/alert state
  const [feedback, setFeedback] = useState<{
    type: "success" | "error";
    text: string;
  } | null>(null);

  // Search & Filter state
  const [searchKeyword, setSearchKeyword] = useState("");
  const [isFilterOpen, setIsFilterOpen] = useState(false);
  const [filterStatus, setFilterStatus] = useState("");
  const [filterDeliveryStatus, setFilterDeliveryStatus] = useState("");
  const [filterDateFrom, setFilterDateFrom] = useState("");
  const [filterDateTo, setFilterDateTo] = useState("");

  // Top KPI Card filter state
  const [activeStatFilter, setActiveStatFilter] =
    useState<PurchaseOrderFilterTab>("ALL");

  // Pagination state
  const [page, setPage] = useState(0);
  const [size] = useState(10);
  const [refreshTrigger, setRefreshTrigger] = useState(0);
  const isFirstMount = useRef(true);

  // Active filters count
  const activeFiltersCount = [
    filterStatus,
    filterDeliveryStatus,
    filterDateFrom,
    filterDateTo,
  ].filter(Boolean).length;

  useEffect(() => {
    if (isFirstMount.current) {
      isFirstMount.current = false;
      fetchOrders();
      return;
    }
    fetchOrders();
  }, [refreshTrigger]);

  const fetchOrders = async () => {
    try {
      setLoading(true);
      const res = await purchaseOrderService.getAll();
      setOrders(res.data || []);
    } catch (err) {
      console.error("Failed to load purchase orders:", err);
      setOrders([]);
      setFeedback({
        type: "error",
        text: "Impossible de charger les bons de commande. Veuillez réessayer.",
      });
    } finally {
      setLoading(false);
    }
  };

  // Filter and search logic
  const filteredOrders = useMemo(() => {
    let result = [...orders];

    // Quick tab filter from Stat Cards
    if (activeStatFilter === "SUBMITTED") {
      result = result.filter(
        (o) =>
          o.status === "SUBMITTED" ||
          o.status === "CONFIRMED"
      );
    } else if (activeStatFilter === "READY_FOR_RECEIPT") {
      result = result.filter((o) => o.status === "READY_FOR_RECEIPT");
    } else if (activeStatFilter === "COMPLETED") {
      result = result.filter((o) => o.status === "COMPLETED");
    } else if (activeStatFilter === "DRAFT") {
      result = result.filter((o) => o.status === "DRAFT");
    }

    // Modal filters
    if (filterStatus) {
      result = result.filter((o) => o.status === filterStatus);
    }
    if (filterDeliveryStatus) {
      result = result.filter((o) => o.deliveryStatus === filterDeliveryStatus);
    }
    if (filterDateFrom) {
      result = result.filter((o) => o.orderDate >= filterDateFrom);
    }
    if (filterDateTo) {
      result = result.filter((o) => o.orderDate <= filterDateTo);
    }

    // Search keyword
    if (searchKeyword.trim()) {
      const kw = searchKeyword.trim().toLowerCase();
      result = result.filter(
        (o) =>
          o.orderCode?.toLowerCase().includes(kw) ||
          o.supplierName?.toLowerCase().includes(kw) ||
          o.supplierCode?.toLowerCase().includes(kw) ||
          o.requisitionCode?.toLowerCase().includes(kw) ||
          o.orderedByName?.toLowerCase().includes(kw) ||
          o.assignedToName?.toLowerCase().includes(kw)
      );
    }

    return result;
  }, [
    orders,
    activeStatFilter,
    filterStatus,
    filterDeliveryStatus,
    filterDateFrom,
    filterDateTo,
    searchKeyword,
  ]);

  // Paginated slice
  const paginatedOrders = useMemo(() => {
    const start = page * size;
    return filteredOrders.slice(start, start + size);
  }, [filteredOrders, page, size]);

  const totalPages = Math.ceil(filteredOrders.length / size) || 1;

  // KPI statistics calculation
  const statCounts = useMemo(() => {
    const draft = orders.filter((o) => o.status === "DRAFT").length;
    const submitted = orders.filter((o) => o.status === "SUBMITTED").length;
    const confirmed = orders.filter((o) => o.status === "CONFIRMED").length;
    const readyForReceipt = orders.filter((o) => o.status === "READY_FOR_RECEIPT").length;
    const completed = orders.filter((o) => o.status === "COMPLETED").length;
    const cancelled = orders.filter((o) => o.status === "CANCELLED").length;

    const totalVal = orders.reduce((sum, o) => {
      const amt =
        typeof o.grandTotal === "number"
          ? o.grandTotal
          : parseFloat(String(o.grandTotal || "0").replace(/[^0-9.-]+/g, "")) || 0;
      return sum + amt;
    }, 0);

    const currency = orders[0]?.currencyCode || "MAD";

    return {
      total: orders.length,
      draft,
      submitted,
      confirmed,
      readyForReceipt,
      completed,
      cancelled,
      totalValue: totalVal,
      currency,
    };
  }, [orders]);

  const handleToggleRow = (id: string) => {
    setExpandedRowIds((prev) => {
      const next = new Set(prev);
      if (next.has(id)) {
        next.delete(id);
      } else {
        next.add(id);
      }
      return next;
    });
  };

  const handleSelectAll = (checked: boolean) => {
    if (checked) {
      setSelectedIds(paginatedOrders.map((o) => o.id));
    } else {
      setSelectedIds([]);
    }
  };

  const handleSelectRow = (id: string) => {
    setSelectedIds((prev) =>
      prev.includes(id) ? prev.filter((item) => item !== id) : [...prev, id]
    );
  };

  // Quick Action: Submit to Supplier
  const handleSubmitOrder = async (order: PurchaseOrder) => {
    try {
      const userId = user?.id || "CURRENT_USER";
      await purchaseOrderService.submit(order.id, { userId });
      setFeedback({
        type: "success",
        text: `La commande ${order.orderCode} a été soumise au fournisseur avec succès.`,
      });
      setRefreshTrigger((prev) => prev + 1);
    } catch (err: any) {
      setFeedback({
        type: "error",
        text:
          err?.response?.data?.message ||
          "Échec de la soumission de la commande.",
      });
    }
  };

  // Quick Action: Confirm Order
  const handleConfirmOrder = async (order: PurchaseOrder) => {
    try {
      const userId = user?.id || "CURRENT_USER";
      await purchaseOrderService.confirm(order.id, { userId });
      setFeedback({
        type: "success",
        text: `La commande ${order.orderCode} a été confirmée par le fournisseur.`,
      });
      setRefreshTrigger((prev) => prev + 1);
    } catch (err: any) {
      setFeedback({
        type: "error",
        text:
          err?.response?.data?.message ||
          "Échec de la confirmation de la commande.",
      });
    }
  };

  // Cancel modal confirmation
  const handleConfirmCancel = async (reason: string) => {
    if (!orderToCancel) return;
    try {
      setIsCancelling(true);
      const userId = user?.id || "CURRENT_USER";
      await purchaseOrderService.cancel(orderToCancel.id, {
        userId,
        reason,
      });
      setFeedback({
        type: "success",
        text: `La commande ${orderToCancel.orderCode} a été annulée avec succès.`,
      });
      setOrderToCancel(null);
      setRefreshTrigger((prev) => prev + 1);
    } catch (err: any) {
      setFeedback({
        type: "error",
        text:
          err?.response?.data?.message ||
          "Échec de l'annulation de la commande.",
      });
    } finally {
      setIsCancelling(false);
    }
  };

  // Delete modal confirmation
  const handleConfirmDelete = async () => {
    if (!orderToDelete) return;
    try {
      setIsDeleting(true);
      await purchaseOrderService.delete(orderToDelete.id);
      setFeedback({
        type: "success",
        text: `Le bon de commande ${orderToDelete.orderCode} a été supprimé.`,
      });
      setOrderToDelete(null);
      setRefreshTrigger((prev) => prev + 1);
    } catch (err: any) {
      setFeedback({
        type: "error",
        text:
          err?.response?.data?.message ||
          "Échec de la suppression de la commande.",
      });
    } finally {
      setIsDeleting(false);
    }
  };

  const formatAmount = (amt: string | number, curr = "MAD") => {
    if (amt === undefined || amt === null) return `0.00 ${curr}`;
    const str = String(amt).trim();
    let detectedCurr = curr;
    if (!curr || curr === "MAD") {
      const upper = str.toUpperCase();
      if (
        upper.includes("EUR") ||
        upper.includes("€") ||
        upper.includes("â‚¬")
      ) {
        detectedCurr = "EUR";
      } else if (upper.includes("USD") || upper.includes("$")) {
        detectedCurr = "USD";
      } else if (upper.includes("MAD") || upper.includes("DH")) {
        detectedCurr = "MAD";
      }
    }

    let cleaned = str.replace(/[^0-9.,-]+/g, "");
    if (cleaned.includes(",") && cleaned.includes(".")) {
      if (cleaned.indexOf(",") < cleaned.indexOf(".")) {
        cleaned = cleaned.replace(/,/g, "");
      } else {
        cleaned = cleaned.replace(/\./g, "").replace(/,/g, ".");
      }
    } else if (cleaned.includes(",")) {
      cleaned = cleaned.replace(/,/g, ".");
    }
    const num = parseFloat(cleaned);
    const validNum = isNaN(num) ? 0 : num;
    return `${new Intl.NumberFormat("fr-FR", {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2,
    }).format(validNum)} ${detectedCurr}`;
  };

  const getUrgencyBadge = (expectedDeliveryDateStr?: string | null) => {
    if (!expectedDeliveryDateStr) return null;
    const expDate = new Date(expectedDeliveryDateStr);
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    expDate.setHours(0, 0, 0, 0);

    const diffDays = Math.ceil(
      (expDate.getTime() - today.getTime()) / (1000 * 60 * 60 * 24)
    );

    if (diffDays < 0) {
      return (
        <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-[10px] font-semibold bg-red-50 text-red-700 dark:bg-red-500/15 dark:text-red-400 border border-red-200/60 dark:border-red-500/20">
          <span className="size-1 rounded-full bg-red-500" />
          En retard ({Math.abs(diffDays)}j)
        </span>
      );
    } else if (diffDays <= 3) {
      return (
        <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-[10px] font-semibold bg-amber-50 text-amber-700 dark:bg-amber-500/15 dark:text-amber-400 border border-amber-200/60 dark:border-amber-500/20">
          <span className="size-1 rounded-full bg-amber-500 animate-ping" />
          Dans {diffDays}j
        </span>
      );
    }
    return (
      <span className="text-xs text-gray-500 dark:text-gray-400">
        {expectedDeliveryDateStr}
      </span>
    );
  };

  const isAllSelected =
    paginatedOrders.length > 0 &&
    paginatedOrders.every((o) => selectedIds.includes(o.id));

  return (
    <div className="space-y-6">
      {/* Toast Feedback Banner */}
      {feedback && (
        <div
          className={`flex items-center justify-between p-4 rounded-xl text-xs font-medium border transition-all ${
            feedback.type === "success"
              ? "bg-emerald-50 text-emerald-800 border-emerald-200 dark:bg-emerald-500/10 dark:text-emerald-300 dark:border-emerald-500/20"
              : "bg-red-50 text-red-800 border-red-200 dark:bg-red-500/10 dark:text-red-300 dark:border-red-500/20"
          }`}
        >
          <div className="flex items-center gap-2">
            {feedback.type === "success" ? (
              <svg
                className="size-4 text-emerald-600 dark:text-emerald-400"
                fill="none"
                viewBox="0 0 24 24"
                stroke="currentColor"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M5 13l4 4L19 7"
                />
              </svg>
            ) : (
              <svg
                className="size-4 text-red-600 dark:text-red-400"
                fill="none"
                viewBox="0 0 24 24"
                stroke="currentColor"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"
                />
              </svg>
            )}
            <span>{feedback.text}</span>
          </div>
          <button
            type="button"
            onClick={() => setFeedback(null)}
            className="hover:opacity-75"
          >
            <svg
              className="size-4"
              fill="none"
              viewBox="0 0 24 24"
              stroke="currentColor"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M6 18L18 6M6 6l12 12"
              />
            </svg>
          </button>
        </div>
      )}

      {/* Top Metric / Filter Stat Cards */}
      <PurchaseOrderStatCards
        activeFilter={activeStatFilter}
        onSelectFilter={(tab) => {
          setActiveStatFilter(tab);
          setPage(0);
        }}
        counts={statCounts}
      />

      {/* Main Table Card Container */}
      <div className="rounded-2xl border border-gray-200 bg-white shadow-sm dark:border-white/[0.07] dark:bg-gray-900">
        {/* Table Toolbar & Search */}
        <div className="flex flex-col gap-4 p-5 sm:flex-row sm:items-center sm:justify-between border-b border-gray-100 dark:border-white/[0.05]">
          <div>
            <h3 className="text-base font-bold text-gray-800 dark:text-white/90 flex items-center gap-2">
              Bons de Commande Fournisseurs
              <span className="px-2 py-0.5 rounded-full text-xs font-semibold bg-gray-100 text-gray-700 dark:bg-gray-800 dark:text-gray-300">
                {filteredOrders.length}
              </span>
            </h3>
            <p className="text-xs text-gray-500 dark:text-gray-400 mt-0.5">
              Gérez le cycle de vie de vos commandes d'achat, du devis à la réception magasin.
            </p>
          </div>

          <div className="flex flex-wrap items-center gap-3">
            {/* Search Input */}
            <div className="relative flex-1 sm:flex-none">
              <svg
                className="absolute left-3 top-1/2 -translate-y-1/2 size-4 text-gray-400"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"
                />
              </svg>
              <input
                type="text"
                placeholder="Rechercher code, fournisseur..."
                value={searchKeyword}
                onChange={(e) => {
                  setSearchKeyword(e.target.value);
                  setPage(0);
                }}
                className="w-full rounded-xl border border-gray-200 bg-transparent py-2 pl-9 pr-8 text-xs text-gray-700 outline-none focus:border-brand-500 dark:border-gray-800 dark:text-gray-300 sm:w-64"
              />
              {searchKeyword && (
                <button
                  type="button"
                  onClick={() => setSearchKeyword("")}
                  className="absolute right-2.5 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600 dark:hover:text-gray-200"
                >
                  <svg
                    className="size-3.5"
                    fill="none"
                    stroke="currentColor"
                    viewBox="0 0 24 24"
                  >
                    <path
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      strokeWidth={2}
                      d="M6 18L18 6M6 6l12 12"
                    />
                  </svg>
                </button>
              )}
            </div>

            {/* Filter Dropdown Toggle Button */}
            <div className="relative">
              <Button
                variant="outline"
                size="sm"
                onClick={() => setIsFilterOpen(!isFilterOpen)}
              >
                <span className="flex items-center gap-1.5 text-xs">
                  <svg
                    className="size-4"
                    fill="none"
                    stroke="currentColor"
                    viewBox="0 0 24 24"
                  >
                    <path
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      strokeWidth={2}
                      d="M3 4a1 1 0 011-1h16a1 1 0 011 1v2.586a1 1 0 01-.293.707l-6.414 6.414a1 1 0 00-.293.707V17l-4 4v-6.586a1 1 0 00-.293-.707L3.293 7.293A1 1 0 013 6.586V4z"
                    />
                  </svg>
                  Filtres
                </span>
              </Button>
              {activeFiltersCount > 0 && (
                <span className="absolute -top-1.5 -right-1.5 flex h-4 w-4 items-center justify-center rounded-full bg-brand-500 text-[10px] font-bold text-white shadow-sm">
                  {activeFiltersCount}
                </span>
              )}

              <PurchaseOrderFilters
                isOpen={isFilterOpen}
                onClose={() => setIsFilterOpen(false)}
                filterStatus={filterStatus}
                setFilterStatus={setFilterStatus}
                filterDeliveryStatus={filterDeliveryStatus}
                setFilterDeliveryStatus={setFilterDeliveryStatus}
                filterDateFrom={filterDateFrom}
                setFilterDateFrom={setFilterDateFrom}
                filterDateTo={filterDateTo}
                setFilterDateTo={setFilterDateTo}
                onApply={() => setPage(0)}
                onClear={() => {
                  setFilterStatus("");
                  setFilterDeliveryStatus("");
                  setFilterDateFrom("");
                  setFilterDateTo("");
                  setPage(0);
                }}
              />
            </div>

            {/* Refresh Button */}
            <Button
              variant="outline"
              size="sm"
              onClick={() => setRefreshTrigger((prev) => prev + 1)}
              disabled={loading}
              title="Actualiser la liste"
            >
              <svg
                className={`size-4 ${loading ? "animate-spin text-brand-500" : ""}`}
                fill="none"
                viewBox="0 0 24 24"
                stroke="currentColor"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"
                />
              </svg>
            </Button>

            {/* New Purchase Order Action */}
            {canCreate && (
              <Link to="/purchase-orders/create">
                <Button size="sm">
                  <span className="flex items-center gap-1.5 text-xs font-semibold">
                    <svg
                      className="size-4"
                      fill="none"
                      stroke="currentColor"
                      viewBox="0 0 24 24"
                    >
                      <path
                        strokeLinecap="round"
                        strokeLinejoin="round"
                        strokeWidth={2}
                        d="M12 4v16m8-8H4"
                      />
                    </svg>
                    Nouvelle Commande
                  </span>
                </Button>
              </Link>
            )}
          </div>
        </div>

        {/* Active Filter Chips */}
        {(activeFiltersCount > 0 || activeStatFilter !== "ALL") && (
          <div className="flex flex-wrap items-center gap-2 px-5 py-2.5 bg-gray-50/75 border-b border-gray-100 dark:bg-gray-900/40 dark:border-white/[0.05]">
            <span className="text-xs text-gray-500 dark:text-gray-400 font-medium">
              Filtres actifs :
            </span>

            {activeStatFilter !== "ALL" && (
              <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-medium bg-brand-50 text-brand-700 dark:bg-brand-500/15 dark:text-brand-300">
                Vue : {activeStatFilter}
                <button
                  type="button"
                  onClick={() => setActiveStatFilter("ALL")}
                  className="hover:text-brand-900 dark:hover:text-white"
                >
                  <svg className="size-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                  </svg>
                </button>
              </span>
            )}

            {filterStatus && (
              <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-medium bg-brand-50 text-brand-700 dark:bg-brand-500/15 dark:text-brand-300">
                Statut : {filterStatus}
                <button
                  type="button"
                  onClick={() => setFilterStatus("")}
                  className="hover:text-brand-900 dark:hover:text-white"
                >
                  <svg className="size-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                  </svg>
                </button>
              </span>
            )}

            {filterDeliveryStatus && (
              <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-medium bg-brand-50 text-brand-700 dark:bg-brand-500/15 dark:text-brand-300">
                Livraison : {filterDeliveryStatus}
                <button
                  type="button"
                  onClick={() => setFilterDeliveryStatus("")}
                  className="hover:text-brand-900 dark:hover:text-white"
                >
                  <svg className="size-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                  </svg>
                </button>
              </span>
            )}

            {filterDateFrom && (
              <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-medium bg-brand-50 text-brand-700 dark:bg-brand-500/15 dark:text-brand-300">
                Depuis : {filterDateFrom}
                <button
                  type="button"
                  onClick={() => setFilterDateFrom("")}
                  className="hover:text-brand-900 dark:hover:text-white"
                >
                  <svg className="size-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                  </svg>
                </button>
              </span>
            )}
          </div>
        )}

        {/* Bulk Selection Notification Bar */}
        {selectedIds.length > 0 && (
          <div className="flex items-center justify-between px-5 py-2.5 bg-brand-50/70 border-b border-brand-100 text-xs text-brand-900 dark:bg-brand-500/10 dark:border-brand-500/20 dark:text-brand-300 font-medium">
            <span className="flex items-center gap-2">
              <span className="flex h-5 w-5 items-center justify-center rounded-full bg-brand-500 text-[10px] font-bold text-white">
                {selectedIds.length}
              </span>
              commande(s) sélectionnée(s)
            </span>
            <button
              type="button"
              onClick={() => setSelectedIds([])}
              className="text-xs text-brand-700 hover:text-brand-900 dark:text-brand-400 dark:hover:text-brand-200 underline"
            >
              Désélectionner tout
            </button>
          </div>
        )}

        {/* Table Content */}
        <div className="overflow-x-auto">
          <Table>
            <TableHeader className="border-b border-gray-100 dark:border-white/[0.05] bg-gray-50/50 dark:bg-gray-800/20">
              <TableRow>
                <TableCell isHeader className="w-10 px-4 py-3 text-center">
                  <Checkbox
                    checked={isAllSelected}
                    onChange={handleSelectAll}
                  />
                </TableCell>
                <TableCell isHeader className="w-10 px-2 py-3 text-center" />
                <TableCell isHeader className="py-3 px-4 text-xs font-semibold text-gray-500 dark:text-gray-400">
                  Code Commande
                </TableCell>
                <TableCell isHeader className="py-3 px-4 text-xs font-semibold text-gray-500 dark:text-gray-400">
                  Fournisseur
                </TableCell>
                <TableCell isHeader className="py-3 px-4 text-xs font-semibold text-gray-500 dark:text-gray-400">
                  Date Commande
                </TableCell>
                <TableCell isHeader className="py-3 px-4 text-xs font-semibold text-gray-500 dark:text-gray-400">
                  Livraison Prévue
                </TableCell>
                <TableCell isHeader className="py-3 px-4 text-xs font-semibold text-gray-500 dark:text-gray-400 text-right">
                  Total TTC
                </TableCell>
                <TableCell isHeader className="py-3 px-4 text-xs font-semibold text-gray-500 dark:text-gray-400">
                  Statut
                </TableCell>
                <TableCell isHeader className="py-3 px-4 text-xs font-semibold text-gray-500 dark:text-gray-400">
                  Livraison
                </TableCell>
                <TableCell isHeader className="py-3 px-4 text-xs font-semibold text-gray-500 dark:text-gray-400 text-right">
                  Actions
                </TableCell>
              </TableRow>
            </TableHeader>

            <TableBody className="divide-y divide-gray-100 dark:divide-white/[0.05]">
              {loading ? (
                // Skeleton loading rows
                Array.from({ length: 5 }).map((_, idx) => (
                  <TableRow key={idx} className="animate-pulse">
                    <TableCell className="px-4 py-4 text-center">
                      <div className="h-4 w-4 mx-auto bg-gray-200 dark:bg-gray-700 rounded" />
                    </TableCell>
                    <TableCell className="px-2 py-4">
                      <div className="h-4 w-4 bg-gray-200 dark:bg-gray-700 rounded" />
                    </TableCell>
                    <TableCell className="px-4 py-4">
                      <div className="h-4 w-28 bg-gray-200 dark:bg-gray-700 rounded" />
                    </TableCell>
                    <TableCell className="px-4 py-4">
                      <div className="h-4 w-36 bg-gray-200 dark:bg-gray-700 rounded" />
                    </TableCell>
                    <TableCell className="px-4 py-4">
                      <div className="h-4 w-20 bg-gray-200 dark:bg-gray-700 rounded" />
                    </TableCell>
                    <TableCell className="px-4 py-4">
                      <div className="h-4 w-20 bg-gray-200 dark:bg-gray-700 rounded" />
                    </TableCell>
                    <TableCell className="px-4 py-4 text-right">
                      <div className="h-4 w-24 ml-auto bg-gray-200 dark:bg-gray-700 rounded" />
                    </TableCell>
                    <TableCell className="px-4 py-4">
                      <div className="h-5 w-20 bg-gray-200 dark:bg-gray-700 rounded-full" />
                    </TableCell>
                    <TableCell className="px-4 py-4">
                      <div className="h-5 w-20 bg-gray-200 dark:bg-gray-700 rounded-full" />
                    </TableCell>
                    <TableCell className="px-4 py-4 text-right">
                      <div className="h-4 w-16 ml-auto bg-gray-200 dark:bg-gray-700 rounded" />
                    </TableCell>
                  </TableRow>
                ))
              ) : paginatedOrders.length === 0 ? (
                // Empty state
                <TableRow>
                  <TableCell colSpan={10} className="py-14 text-center">
                    <div className="flex flex-col items-center justify-center max-w-sm mx-auto">
                      <div className="flex items-center justify-center w-14 h-14 rounded-2xl bg-gray-50 text-gray-400 dark:bg-gray-800 dark:text-gray-500 mb-3">
                        <svg
                          className="size-7"
                          fill="none"
                          viewBox="0 0 24 24"
                          stroke="currentColor"
                        >
                          <path
                            strokeLinecap="round"
                            strokeLinejoin="round"
                            strokeWidth={1.5}
                            d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"
                          />
                        </svg>
                      </div>
                      <h4 className="text-sm font-bold text-gray-800 dark:text-white">
                        Aucun bon de commande trouvé
                      </h4>
                      <p className="text-xs text-gray-500 dark:text-gray-400 mt-1 mb-4 text-center">
                        {searchKeyword || activeFiltersCount > 0 || activeStatFilter !== "ALL"
                          ? "Aucun résultat ne correspond à vos filtres actuels."
                          : "Commencez par créer votre premier bon de commande pour vos fournisseurs."}
                      </p>
                      {canCreate && (
                        <Link to="/purchase-orders/create">
                          <Button size="sm">
                            <span className="flex items-center gap-1.5 text-xs font-semibold">
                              <svg className="size-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v16m8-8H4" />
                              </svg>
                              Créer un Bon de Commande
                            </span>
                          </Button>
                        </Link>
                      )}
                    </div>
                  </TableCell>
                </TableRow>
              ) : (
                paginatedOrders.map((order) => {
                  const isExpanded = expandedRowIds.has(order.id);
                  const isSelected = selectedIds.includes(order.id);
                  const linesCount = order.lines?.length || 0;

                  return (
                    <React.Fragment key={order.id}>
                      <TableRow
                        className={`transition-colors hover:bg-gray-50/50 dark:hover:bg-white/[0.02] ${
                          isSelected ? "bg-brand-50/30 dark:bg-brand-500/5" : ""
                        }`}
                      >
                        {/* Checkbox */}
                        <TableCell className="w-10 px-4 py-3.5 text-center">
                          <Checkbox
                            checked={isSelected}
                            onChange={() => handleSelectRow(order.id)}
                          />
                        </TableCell>

                        {/* Expand/Collapse Toggle */}
                        <TableCell className="w-10 px-2 py-3.5 text-center">
                          <button
                            type="button"
                            onClick={() => handleToggleRow(order.id)}
                            className="p-1 rounded-lg text-gray-400 hover:text-gray-600 hover:bg-gray-100 dark:hover:bg-gray-800 dark:hover:text-gray-200 transition-colors"
                            title={isExpanded ? "Masquer les articles" : "Afficher les articles"}
                          >
                            <svg
                              className={`size-4 transition-transform duration-200 ${
                                isExpanded ? "rotate-90 text-brand-500" : ""
                              }`}
                              fill="none"
                              viewBox="0 0 24 24"
                              stroke="currentColor"
                            >
                              <path
                                strokeLinecap="round"
                                strokeLinejoin="round"
                                strokeWidth={2}
                                d="M9 5l7 7-7 7"
                              />
                            </svg>
                          </button>
                        </TableCell>

                        {/* Order Code */}
                        <TableCell className="py-3.5 px-4 font-medium">
                          <Link
                            to={`/purchase-orders/${order.id}`}
                            className="font-semibold text-brand-600 dark:text-brand-400 hover:underline flex items-center gap-1.5"
                          >
                            {order.orderCode}
                            {linesCount > 0 && (
                              <span className="text-[10px] text-gray-400 font-normal">
                                ({linesCount} art.)
                              </span>
                            )}
                          </Link>
                          {order.requisitionCode && (
                            <span className="text-[10px] text-gray-400 block mt-0.5">
                              DA: {order.requisitionCode}
                            </span>
                          )}
                        </TableCell>

                        {/* Supplier */}
                        <TableCell className="py-3.5 px-4">
                          <div className="font-medium text-gray-800 dark:text-white">
                            {order.supplierName}
                          </div>
                          {order.supplierCode && (
                            <span className="text-[10px] text-gray-400 block">
                              Code: {order.supplierCode}
                            </span>
                          )}
                        </TableCell>

                        {/* Order Date */}
                        <TableCell className="py-3.5 px-4 text-xs text-gray-600 dark:text-gray-300">
                          {order.orderDate}
                        </TableCell>

                        {/* Expected Delivery Date */}
                        <TableCell className="py-3.5 px-4">
                          {getUrgencyBadge(order.expectedDeliveryDate)}
                        </TableCell>

                        {/* Total Grand Total */}
                        <TableCell className="py-3.5 px-4 text-right font-bold text-gray-900 dark:text-white">
                          {formatAmount(order.grandTotal, order.currencyCode)}
                        </TableCell>

                        {/* Order Status */}
                        <TableCell className="py-3.5 px-4">
                          <PurchaseOrderStatusBadge status={order.status} size="sm" />
                        </TableCell>

                        {/* Delivery Status */}
                        <TableCell className="py-3.5 px-4">
                          <PurchaseOrderDeliveryStatusBadge
                            status={order.deliveryStatus}
                            size="sm"
                          />
                        </TableCell>

                        {/* Row Actions */}
                        <TableCell className="py-3.5 px-4 text-right">
                          <div className="flex items-center justify-end gap-1.5">
                            {/* View Details */}
                            <Link
                              to={`/purchase-orders/${order.id}`}
                              className="p-1.5 text-gray-400 hover:text-brand-600 hover:bg-gray-100 rounded-lg dark:hover:bg-gray-800 dark:hover:text-brand-400 transition-colors"
                              title="Voir les détails"
                            >
                              <svg className="size-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
                              </svg>
                            </Link>

                            {/* Submit Order action if DRAFT */}
                            {permissions.canSubmit(order) && (
                              <button
                                type="button"
                                onClick={() => handleSubmitOrder(order)}
                                className="p-1.5 text-amber-500 hover:text-amber-700 hover:bg-amber-50 rounded-lg dark:hover:bg-amber-500/10 transition-colors"
                                title="Soumettre au fournisseur"
                              >
                                <svg className="size-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M14 5l7 7m0 0l-7 7m7-7H3" />
                                </svg>
                              </button>
                            )}

                            {/* Confirm Order action if SUBMITTED */}
                            {permissions.canConfirm(order) && (
                              <button
                                type="button"
                                onClick={() => handleConfirmOrder(order)}
                                className="p-1.5 text-blue-500 hover:text-blue-700 hover:bg-blue-50 rounded-lg dark:hover:bg-blue-500/10 transition-colors"
                                title="Confirmer la commande fournisseur"
                              >
                                <svg className="size-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                                </svg>
                              </button>
                            )}

                            {/* Cancel Order action if cancellable */}
                            {permissions.canCancel(order) && (
                                <button
                                  type="button"
                                  onClick={() => setOrderToCancel(order)}
                                  className="p-1.5 text-rose-400 hover:text-rose-600 hover:bg-rose-50 rounded-lg dark:hover:bg-rose-500/10 transition-colors"
                                  title="Annuler la commande"
                                >
                                  <svg className="size-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                                  </svg>
                                </button>
                              )}

                            {/* Delete Order action if DRAFT and admin */}
                            {permissions.canDelete(order) && (
                              <button
                                type="button"
                                onClick={() => setOrderToDelete(order)}
                                className="p-1.5 text-gray-400 hover:text-red-600 hover:bg-red-50 rounded-lg dark:hover:bg-red-500/10 transition-colors"
                                title="Supprimer le brouillon"
                              >
                                <svg className="size-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                                </svg>
                              </button>
                            )}
                          </div>
                        </TableCell>
                      </TableRow>

                      {/* Inline Expanded Row */}
                      {isExpanded && (
                        <PurchaseOrderExpandedRow
                          order={order}
                          formatAmount={formatAmount}
                        />
                      )}
                    </React.Fragment>
                  );
                })
              )}
            </TableBody>
          </Table>
        </div>

        {/* Pagination Footer */}
        {filteredOrders.length > size && (
          <div className="flex flex-col sm:flex-row items-center justify-between p-4 border-t border-gray-100 dark:border-white/[0.05] gap-3">
            <span className="text-xs text-gray-500 dark:text-gray-400">
              Affichage de {page * size + 1} à{" "}
              {Math.min((page + 1) * size, filteredOrders.length)} sur{" "}
              {filteredOrders.length} commandes
            </span>

            <Pagination
              currentPage={page + 1}
              totalPages={totalPages}
              onPageChange={(p) => setPage(p - 1)}
            />
          </div>
        )}
      </div>

      {/* Delete Confirmation Modal */}
      {orderToDelete && (
        <DeleteConfirmModal
          isOpen={Boolean(orderToDelete)}
          onClose={() => setOrderToDelete(null)}
          onConfirm={handleConfirmDelete}
          title="Supprimer la commande"
          message={`Êtes-vous sûr de vouloir supprimer définitivement le bon de commande ${orderToDelete.orderCode} ?`}
          isDeleting={isDeleting}
        />
      )}

      {/* Cancel Order Modal */}
      {orderToCancel && (
        <PurchaseOrderCancelModal
          isOpen={Boolean(orderToCancel)}
          onClose={() => setOrderToCancel(null)}
          order={orderToCancel}
          onConfirm={handleConfirmCancel}
          isLoading={isCancelling}
        />
      )}
    </div>
  );
}
