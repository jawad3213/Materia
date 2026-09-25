import React, { useEffect, useState, useRef } from "react";
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
import { requisitionApi } from "../services/requisitionApi";
import type {
  Requisition,
  RequisitionFilterTab,
  RequisitionSearchRequest,
} from "../types";
import RequisitionStatCards from "./RequisitionStatCards";
import RequisitionCancelModal from "./RequisitionCancelModal";
import RequisitionTableToolbar from "./RequisitionTableToolbar";
import RequisitionTableRow from "./RequisitionTableRow";
import RequisitionExpandedRow from "./RequisitionExpandedRow";
import RequisitionConvertToPoModal from "./RequisitionConvertToPoModal";

export default function RequisitionListTable() {
  const [requisitions, setRequisitions] = useState<Requisition[]>([]);
  const [loading, setLoading] = useState(true);
  const [selectedIds, setSelectedIds] = useState<string[]>([]);
  const [expandedRowIds, setExpandedRowIds] = useState<Set<string>>(new Set());

  // Delete modal state
  const [requisitionToDelete, setRequisitionToDelete] =
    useState<Requisition | null>(null);
  const [isDeleting, setIsDeleting] = useState(false);

  // Convert to PO modal state
  const [convertToPoRequisition, setConvertToPoRequisition] =
    useState<Requisition | null>(null);
  const [isConverting, setIsConverting] = useState(false);

  // Cancel modal state
  const [requisitionToCancel, setRequisitionToCancel] =
    useState<Requisition | null>(null);
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
  const [filterRequester, setFilterRequester] = useState("");
  const [filterDateFrom, setFilterDateFrom] = useState("");
  const [filterDateTo, setFilterDateTo] = useState("");

  // Top KPI Card filter state
  const [activeStatFilter, setActiveStatFilter] =
    useState<RequisitionFilterTab>("ALL");
  const [statCounts, setStatCounts] = useState({
    total: 0,
    pendingReview: 0,
    readyForPo: 0,
    converted: 0,
    totalEstimatedValue: 0,
    currency: "MAD",
  });

  // Pagination state
  const [page, setPage] = useState(0);
  const [size] = useState(10);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);

  const [refreshTrigger, setRefreshTrigger] = useState(0);
  const isFirstMount = useRef(true);

  // Active filters count
  const activeFiltersCount = [
    filterStatus,
    filterRequester,
    filterDateFrom,
    filterDateTo,
  ].filter(Boolean).length;

  useEffect(() => {
    fetchStatCounts();
  }, [refreshTrigger]);

  const fetchStatCounts = async () => {
    try {
      const res = await requisitionApi.getAll();
      const all = res.data || [];
      const pending = all.filter(
        (r) => r.status === "SUBMITTED" || r.status === "UNDER_REVIEW"
      ).length;
      const approved = all.filter((r) => r.status === "APPROVED").length;
      const converted = all.filter((r) => r.status === "CONVERTED").length;

      const totalVal = all.reduce((sum, r) => {
        const amt =
          typeof r.totalAmount === "number"
            ? r.totalAmount
            : parseFloat(String(r.totalAmount || "0").replace(/[^0-9.-]+/g, "")) || 0;
        return sum + amt;
      }, 0);

      const detectedCurr = all[0]?.currencyCode || "MAD";

      setStatCounts({
        total: all.length,
        pendingReview: pending,
        readyForPo: approved,
        converted: converted,
        totalEstimatedValue: totalVal,
        currency: detectedCurr,
      });
    } catch (err) {
      console.error("Failed to load requisition stats:", err);
    }
  };

  useEffect(() => {
    if (isFirstMount.current) {
      isFirstMount.current = false;
      fetchRequisitions();
      return;
    }
    fetchRequisitions();
  }, [page, refreshTrigger, activeStatFilter]);

  const fetchRequisitions = async () => {
    try {
      setLoading(true);

      const criteria: RequisitionSearchRequest = {};

      if (searchKeyword.trim()) {
        criteria.keyword = searchKeyword.trim();
      }

      if (filterRequester.trim()) {
        criteria.requesterId = filterRequester.trim();
      }

      if (filterDateFrom) {
        criteria.requiredDateFrom = filterDateFrom;
      }

      if (filterDateTo) {
        criteria.requiredDateTo = filterDateTo;
      }

      // Merge activeStatFilter with filterStatus
      if (activeStatFilter === "PENDING") {
        criteria.status = "SUBMITTED";
      } else if (activeStatFilter === "APPROVED") {
        criteria.status = "APPROVED";
      } else if (activeStatFilter === "CONVERTED") {
        criteria.status = "CONVERTED";
      } else if (activeStatFilter === "DRAFT") {
        criteria.status = "DRAFT";
      } else if (filterStatus) {
        criteria.status = filterStatus;
      }

      const hasCriteria =
        criteria.keyword ||
        criteria.status ||
        criteria.requesterId ||
        criteria.requiredDateFrom ||
        criteria.requiredDateTo;

      if (hasCriteria) {
        try {
          const res = await requisitionApi.searchAdvanced(criteria, page, size);
          const data = res.data;
          setRequisitions(data.content || []);
          setTotalPages(data.totalPages || 0);
          setTotalElements(data.totalElements || 0);
        } catch (searchErr) {
          // Fallback to client-side filtering if search endpoint has criteria issues
          const allRes = await requisitionApi.getAll();
          let items = allRes.data || [];

          if (criteria.keyword) {
            const kw = criteria.keyword.toLowerCase();
            items = items.filter(
              (r) =>
                r.requisitionCode?.toLowerCase().includes(kw) ||
                r.title?.toLowerCase().includes(kw) ||
                r.requesterName?.toLowerCase().includes(kw) ||
                r.description?.toLowerCase().includes(kw)
            );
          }

          if (activeStatFilter === "PENDING") {
            items = items.filter(
              (r) => r.status === "SUBMITTED" || r.status === "UNDER_REVIEW"
            );
          } else if (criteria.status) {
            items = items.filter((r) => r.status === criteria.status);
          }

          if (criteria.requesterId) {
            items = items.filter(
              (r) =>
                r.requesterId?.toLowerCase().includes(criteria.requesterId!.toLowerCase()) ||
                r.requesterName?.toLowerCase().includes(criteria.requesterId!.toLowerCase())
            );
          }

          setTotalElements(items.length);
          setTotalPages(Math.ceil(items.length / size) || 1);
          setRequisitions(items.slice(page * size, (page + 1) * size));
        }
      } else {
        const allRes = await requisitionApi.getAll();
        const items = allRes.data || [];
        setTotalElements(items.length);
        setTotalPages(Math.ceil(items.length / size) || 1);
        setRequisitions(items.slice(page * size, (page + 1) * size));
      }
    } catch (err) {
      console.error("Failed to load requisitions:", err);
      setRequisitions([]);
    } finally {
      setLoading(false);
    }
  };

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
      setSelectedIds(requisitions.map((r) => r.id));
    } else {
      setSelectedIds([]);
    }
  };

  const handleSelectOne = (id: string) => {
    setSelectedIds((prev) =>
      prev.includes(id) ? prev.filter((item) => item !== id) : [...prev, id]
    );
  };

  const handleSelectStatFilter = (filter: RequisitionFilterTab) => {
    setActiveStatFilter((prev) => (prev === filter ? "ALL" : filter));
    setPage(0);
  };

  const handleApplyFilters = () => {
    setIsFilterOpen(false);
    setPage(0);
    setRefreshTrigger((prev) => prev + 1);
  };

  const handleClearFilters = () => {
    setFilterStatus("");
    setFilterRequester("");
    setFilterDateFrom("");
    setFilterDateTo("");
    setIsFilterOpen(false);
    setPage(0);
    setRefreshTrigger((prev) => prev + 1);
  };

  // Workflow Handlers
  const handleQuickSubmit = async (requisition: Requisition) => {
    try {
      setLoading(true);
      await requisitionApi.submit(requisition.id, "current-user");
      setRefreshTrigger((prev) => prev + 1);
      setFeedback({
        type: "success",
        text: `Requisition ${requisition.requisitionCode} submitted for approval.`,
      });
    } catch (err: any) {
      console.error("Failed to submit requisition:", err);
      setFeedback({
        type: "error",
        text: err?.response?.data?.message || `Failed to submit requisition ${requisition.requisitionCode}.`,
      });
    } finally {
      setLoading(false);
    }
  };

  const handleConvertToPoConfirm = async () => {
    if (!convertToPoRequisition) return;
    const reqCode = convertToPoRequisition.requisitionCode;
    try {
      setIsConverting(true);
      const generatedPoCode = `PO-${new Date().getFullYear()}-${Math.floor(
        1000 + Math.random() * 9000
      )}`;
      await requisitionApi.convert(
        convertToPoRequisition.id,
        crypto.randomUUID(),
        generatedPoCode,
        "current-user"
      );
      setConvertToPoRequisition(null);
      setRefreshTrigger((prev) => prev + 1);
      setFeedback({
        type: "success",
        text: `Requisition ${reqCode} successfully converted to Purchase Order ${generatedPoCode}.`,
      });
    } catch (err: any) {
      console.error("Failed to convert requisition to PO:", err);
      setFeedback({
        type: "error",
        text: err?.response?.data?.message || `Failed to convert requisition ${reqCode} to PO.`,
      });
    } finally {
      setIsConverting(false);
    }
  };

  const handleDeleteConfirm = async () => {
    if (!requisitionToDelete) return;
    const reqCode = requisitionToDelete.requisitionCode;
    const reqId = requisitionToDelete.id;
    try {
      setIsDeleting(true);
      await requisitionApi.delete(reqId);
      setRequisitionToDelete(null);
      setSelectedIds((prev) => prev.filter((id) => id !== reqId));
      setRefreshTrigger((prev) => prev + 1);
      setFeedback({
        type: "success",
        text: `Requisition ${reqCode} has been successfully deleted.`,
      });
    } catch (err: any) {
      console.error("Failed to delete requisition:", err);
      setFeedback({
        type: "error",
        text: err?.response?.data?.message || `Failed to delete requisition ${reqCode}.`,
      });
    } finally {
      setIsDeleting(false);
    }
  };

  const handleCancelConfirm = async (reason: string) => {
    if (!requisitionToCancel) return;
    const reqCode = requisitionToCancel.requisitionCode;
    try {
      setIsCancelling(true);
      await requisitionApi.cancel(requisitionToCancel.id, "current-user", reason);
      setRequisitionToCancel(null);
      setRefreshTrigger((prev) => prev + 1);
      setFeedback({
        type: "success",
        text: `Requisition ${reqCode} has been cancelled.`,
      });
    } catch (err: any) {
      console.error("Failed to cancel requisition:", err);
      setFeedback({
        type: "error",
        text: err?.response?.data?.message || `Failed to cancel requisition ${reqCode}.`,
      });
    } finally {
      setIsCancelling(false);
    }
  };

  const formatAmount = (amt: string | number, curr = "MAD") => {
    if (amt === undefined || amt === null) return `0.00 ${curr}`;
    const str = String(amt).trim();
    let detectedCurr = curr;
    if (!curr || curr === "MAD") {
      const upper = str.toUpperCase();
      if (upper.includes("EUR") || upper.includes("€") || upper.includes("â‚¬") || upper.includes("\u20AC")) {
        detectedCurr = "EUR";
      } else if (upper.includes("USD") || upper.includes("$")) {
        detectedCurr = "USD";
      } else if (upper.includes("MAD") || upper.includes("DH") || upper.includes("DIRHAM")) {
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
    return `${new Intl.NumberFormat("en-US", {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2,
    }).format(validNum)} ${detectedCurr}`;
  };

  const getUrgencyBadge = (requiredDateStr?: string) => {
    if (!requiredDateStr) return null;
    const reqDate = new Date(requiredDateStr);
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    reqDate.setHours(0, 0, 0, 0);

    const diffDays = Math.ceil(
      (reqDate.getTime() - today.getTime()) / (1000 * 60 * 60 * 24)
    );

    if (diffDays < 0) {
      return (
        <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-[10px] font-semibold bg-red-50 text-red-700 dark:bg-red-500/15 dark:text-red-400 border border-red-200/60 dark:border-red-500/20">
          <span className="size-1 rounded-full bg-red-500" />
          Overdue
        </span>
      );
    } else if (diffDays <= 3) {
      return (
        <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-[10px] font-semibold bg-amber-50 text-amber-700 dark:bg-amber-500/15 dark:text-amber-400 border border-amber-200/60 dark:border-amber-500/20">
          <span className="size-1 rounded-full bg-amber-500 animate-ping" />
          Due in {diffDays}d
        </span>
      );
    }
    return (
      <span className="text-xs text-gray-500 dark:text-gray-400">
        {requiredDateStr}
      </span>
    );
  };

  const isAllSelected =
    requisitions.length > 0 &&
    requisitions.every((r) => selectedIds.includes(r.id));

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
              <svg className="size-4 text-emerald-600 dark:text-emerald-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
              </svg>
            ) : (
              <svg className="size-4 text-red-600 dark:text-red-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
            )}
            <span>{feedback.text}</span>
          </div>
          <button
            type="button"
            onClick={() => setFeedback(null)}
            className="hover:opacity-75"
          >
            <svg className="size-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
        </div>
      )}

      {/* Top Metric / Filter Stat Cards */}
      <RequisitionStatCards
        activeFilter={activeStatFilter}
        onSelectFilter={handleSelectStatFilter}
        counts={statCounts}
      />

      {/* Main Table Card Container */}
      <div className="rounded-2xl border border-gray-200 bg-white shadow-sm dark:border-white/[0.07] dark:bg-gray-900">
        {/* Table Toolbar & Search */}
        <RequisitionTableToolbar
          searchKeyword={searchKeyword}
          onSearchKeywordChange={setSearchKeyword}
          onSearchSubmit={() => {
            setPage(0);
            fetchRequisitions();
          }}
          isFilterOpen={isFilterOpen}
          onToggleFilter={() => setIsFilterOpen(!isFilterOpen)}
          activeFiltersCount={activeFiltersCount}
          onRefresh={() => setRefreshTrigger((prev) => prev + 1)}
          loading={loading}
          pendingReviewCount={statCounts.pendingReview}
          activeStatFilter={activeStatFilter}
          onClearStatFilter={() => setActiveStatFilter("ALL")}
          filterStatus={filterStatus}
          onClearFilterStatus={() => {
            setFilterStatus("");
            setRefreshTrigger((prev) => prev + 1);
          }}
          filterRequester={filterRequester}
          onClearFilterRequester={() => {
            setFilterRequester("");
            setRefreshTrigger((prev) => prev + 1);
          }}
          onClearFilterDateFrom={() => {
            setFilterDateFrom("");
            setRefreshTrigger((prev) => prev + 1);
          }}
          onClearFilterDateTo={() => {
            setFilterDateTo("");
            setRefreshTrigger((prev) => prev + 1);
          }}
          filterDateFrom={filterDateFrom}
          setFilterDateFrom={setFilterDateFrom}
          filterDateTo={filterDateTo}
          setFilterDateTo={setFilterDateTo}
          setFilterStatus={setFilterStatus}
          setFilterRequester={setFilterRequester}
          onApplyFilters={handleApplyFilters}
          onClearFilters={handleClearFilters}
        />

        {/* Data Table */}
        <div className="overflow-x-auto">
          <Table>
            <TableHeader className="bg-gray-50/75 dark:bg-white/[0.02]">
              <TableRow className="border-b border-gray-100 dark:border-white/[0.05]">
                <TableCell isHeader className="w-10 px-4 py-3.5">
                  <span className="sr-only">Expand</span>
                </TableCell>
                <TableCell isHeader className="w-10 px-4 py-3.5">
                  <Checkbox
                    checked={isAllSelected}
                    onChange={handleSelectAll}
                  />
                </TableCell>
                <TableCell isHeader className="px-4 py-3.5 text-xs font-semibold text-gray-600 dark:text-gray-300">
                  Requisition
                </TableCell>
                <TableCell isHeader className="px-4 py-3.5 text-xs font-semibold text-gray-600 dark:text-gray-300">
                  Requester
                </TableCell>
                <TableCell isHeader className="px-4 py-3.5 text-xs font-semibold text-gray-600 dark:text-gray-300">
                  Need By
                </TableCell>
                <TableCell isHeader className="px-4 py-3.5 text-xs font-semibold text-gray-600 dark:text-gray-300">
                  Total Value
                </TableCell>
                <TableCell isHeader className="px-4 py-3.5 text-xs font-semibold text-gray-600 dark:text-gray-300">
                  Status
                </TableCell>
                <TableCell isHeader className="px-4 py-3.5 text-xs font-semibold text-gray-600 dark:text-gray-300">
                  PO Reference
                </TableCell>
                <TableCell isHeader className="px-4 py-3.5 text-right text-xs font-semibold text-gray-600 dark:text-gray-300">
                  Actions
                </TableCell>
              </TableRow>
            </TableHeader>

            <TableBody>
              {loading ? (
                <TableRow>
                  <TableCell colSpan={9} className="px-4 py-12 text-center text-xs text-gray-500 dark:text-gray-400">
                    <div className="flex flex-col items-center justify-center gap-2">
                      <div className="size-6 animate-spin rounded-full border-2 border-brand-500 border-t-transparent" />
                      <span>Loading purchase requisitions...</span>
                    </div>
                  </TableCell>
                </TableRow>
              ) : requisitions.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={9} className="px-4 py-16 text-center text-gray-500 dark:text-gray-400">
                    <div className="flex flex-col items-center justify-center gap-3">
                      <div className="p-3 rounded-2xl bg-gray-100 text-gray-400 dark:bg-white/[0.05] dark:text-gray-500">
                        <svg className="size-8" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                          <path
                            strokeLinecap="round"
                            strokeLinejoin="round"
                            strokeWidth={1.5}
                            d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"
                          />
                        </svg>
                      </div>
                      <p className="text-sm font-semibold text-gray-700 dark:text-gray-300">
                        No purchase requisitions found
                      </p>
                      <p className="text-xs text-gray-400 dark:text-gray-500 max-w-sm">
                        {searchKeyword || activeFiltersCount > 0 || activeStatFilter !== "ALL"
                          ? "Try adjusting your search criteria or resetting filters."
                          : "Create your first purchase requisition to start managing material procurement."}
                      </p>
                      <Link to="/requisitions/create">
                        <Button size="sm">Create Requisition</Button>
                      </Link>
                    </div>
                  </TableCell>
                </TableRow>
              ) : (
                requisitions.map((req) => {
                  const isExpanded = expandedRowIds.has(req.id);
                  const isSelected = selectedIds.includes(req.id);

                  return (
                    <React.Fragment key={req.id}>
                      <RequisitionTableRow
                        requisition={req}
                        isExpanded={isExpanded}
                        isSelected={isSelected}
                        onToggleExpand={handleToggleRow}
                        onSelect={handleSelectOne}
                        formatAmount={formatAmount}
                        getUrgencyBadge={getUrgencyBadge}
                        onQuickSubmit={handleQuickSubmit}
                        onConvertToPo={(r) => setConvertToPoRequisition(r)}
                        onCancel={(r) => setRequisitionToCancel(r)}
                        onDelete={(r) => setRequisitionToDelete(r)}
                      />

                      {/* Expandable Nested Line Items Row */}
                      {isExpanded && (
                        <RequisitionExpandedRow
                          requisition={req}
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

        {/* Table Footer / Pagination */}
        <div className="flex flex-col gap-3 p-4 sm:flex-row sm:items-center sm:justify-between border-t border-gray-100 dark:border-white/[0.07]">
          <div className="text-xs text-gray-500 dark:text-gray-400">
            Showing <span className="font-semibold">{requisitions.length}</span> of{" "}
            <span className="font-semibold">{totalElements}</span> requisitions
            {selectedIds.length > 0 && (
              <span className="ml-2 font-medium text-brand-600 dark:text-brand-400">
                ({selectedIds.length} selected)
              </span>
            )}
          </div>

          {totalPages > 1 && (
            <Pagination
              currentPage={page}
              totalPages={totalPages}
              onPageChange={(p) => setPage(p)}
            />
          )}
        </div>
      </div>

      {/* Convert to PO Confirmation Modal */}
      <RequisitionConvertToPoModal
        isOpen={!!convertToPoRequisition}
        onClose={() => setConvertToPoRequisition(null)}
        onConfirm={handleConvertToPoConfirm}
        requisition={convertToPoRequisition}
        isConverting={isConverting}
      />

      {/* Delete Confirmation Modal */}
      <DeleteConfirmModal
        isOpen={!!requisitionToDelete}
        onClose={() => setRequisitionToDelete(null)}
        onConfirm={handleDeleteConfirm}
        title="Delete Purchase Requisition"
        message={`Are you sure you want to delete requisition ${requisitionToDelete?.requisitionCode}? This action cannot be undone.`}
        isDeleting={isDeleting}
      />

      {/* Cancel Confirmation Modal */}
      {requisitionToCancel && (
        <RequisitionCancelModal
          isOpen={!!requisitionToCancel}
          onClose={() => setRequisitionToCancel(null)}
          requisition={requisitionToCancel}
          onConfirm={handleCancelConfirm}
          isLoading={isCancelling}
        />
      )}
    </div>
  );
}
