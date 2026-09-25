import React, { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import {
  Table,
  TableBody,
  TableCell,
  TableHeader,
  TableRow,
} from "../../../shared/components/ui/table";
import Button from "../../../shared/components/ui/button/Button";
import Pagination from "../../../shared/components/ui/Pagination";
import { requisitionApi } from "../services/requisitionApi";
import type { Requisition } from "../types";
import RequisitionStatusBadge from "./RequisitionStatusBadge";
import RequisitionApprovalModal from "./RequisitionApprovalModal";

export type ApprovalTab = "PENDING" | "APPROVED" | "REJECTED" | "ALL";

export default function RequisitionApprovalsTable() {
  const [requisitions, setRequisitions] = useState<Requisition[]>([]);
  const [loading, setLoading] = useState(true);
  const [activeTab, setActiveTab] = useState<ApprovalTab>("PENDING");
  const [searchKeyword, setSearchKeyword] = useState("");
  const [expandedRowIds, setExpandedRowIds] = useState<Set<string>>(new Set());
  const [refreshTrigger, setRefreshTrigger] = useState(0);

  // Approval / Rejection modal state
  const [modalState, setModalState] = useState<{
    isOpen: boolean;
    requisition: Requisition | null;
    mode: "APPROVE" | "REJECT";
  }>({
    isOpen: false,
    requisition: null,
    mode: "APPROVE",
  });

  // Feedback banner toast
  const [alertMessage, setAlertMessage] = useState<{
    type: "success" | "error";
    text: string;
  } | null>(null);

  // Pagination state
  const [page, setPage] = useState(0);
  const size = 10;

  useEffect(() => {
    loadRequisitions();
  }, [refreshTrigger]);

  const loadRequisitions = async () => {
    try {
      setLoading(true);
      const res = await requisitionApi.getAll();
      setRequisitions(res.data || []);
    } catch (err) {
      console.error("Failed to load requisitions for approval:", err);
      setAlertMessage({
        type: "error",
        text: "Failed to load requisitions. Please ensure the backend is running.",
      });
    } finally {
      setLoading(false);
    }
  };

  // Counts for KPI tabs
  const pendingItems = requisitions.filter(
    (r) => r.status === "SUBMITTED" || r.status === "UNDER_REVIEW"
  );
  const approvedItems = requisitions.filter(
    (r) => r.status === "APPROVED" || r.status === "CONVERTED"
  );
  const rejectedItems = requisitions.filter((r) => r.status === "REJECTED");

  const totalPendingValue = pendingItems.reduce((sum, r) => {
    const amt =
      typeof r.totalAmount === "number"
        ? r.totalAmount
        : parseFloat(String(r.totalAmount || "0").replace(/[^0-9.-]+/g, "")) || 0;
    return sum + amt;
  }, 0);

  // Filter based on active tab and search keyword
  const filteredRequisitions = requisitions.filter((req) => {
    // Tab filter
    if (activeTab === "PENDING" && req.status !== "SUBMITTED" && req.status !== "UNDER_REVIEW") {
      return false;
    }
    if (activeTab === "APPROVED" && req.status !== "APPROVED" && req.status !== "CONVERTED") {
      return false;
    }
    if (activeTab === "REJECTED" && req.status !== "REJECTED") {
      return false;
    }

    // Keyword filter
    if (searchKeyword.trim()) {
      const q = searchKeyword.toLowerCase();
      const codeMatch = req.requisitionCode?.toLowerCase().includes(q);
      const titleMatch = req.title?.toLowerCase().includes(q);
      const requesterMatch = req.requesterName?.toLowerCase().includes(q);
      const reasonMatch = req.justification?.toLowerCase().includes(q);
      if (!codeMatch && !titleMatch && !requesterMatch && !reasonMatch) {
        return false;
      }
    }

    return true;
  });

  // Pagination calculation
  const totalPages = Math.ceil(filteredRequisitions.length / size) || 1;
  const paginatedItems = filteredRequisitions.slice(page * size, (page + 1) * size);

  const handleToggleRow = (id: string) => {
    setExpandedRowIds((prev) => {
      const next = new Set(prev);
      if (next.has(id)) next.delete(id);
      else next.add(id);
      return next;
    });
  };

  const handleConfirmDecision = async (notesOrReason: string) => {
    if (!modalState.requisition) return;
    const req = modalState.requisition;
    const isApprove = modalState.mode === "APPROVE";

    try {
      if (isApprove) {
        await requisitionApi.approve(
          req.id,
          "manager-approver",
          "Management Approver",
          notesOrReason
        );
        setAlertMessage({
          type: "success",
          text: `Requisition ${req.requisitionCode} has been APPROVED successfully!`,
        });
      } else {
        await requisitionApi.reject(
          req.id,
          notesOrReason,
          "manager-approver",
          "Management Approver"
        );
        setAlertMessage({
          type: "success",
          text: `Requisition ${req.requisitionCode} has been REJECTED (Denied).`,
        });
      }
      setRefreshTrigger((prev) => prev + 1);
    } catch (err: any) {
      console.error("Decision failed:", err);
      setAlertMessage({
        type: "error",
        text: err?.response?.data?.message || err?.message || "Failed to process decision.",
      });
      throw err;
    }
  };

  const formatAmount = (amt: any, currency = "MAD") => {
    const num =
      typeof amt === "number"
        ? amt
        : parseFloat(String(amt || "0").replace(/[^0-9.-]+/g, "")) || 0;
    return `${num.toLocaleString("en-US", {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2,
    })} ${currency}`;
  };

  return (
    <div className="space-y-6">
      {/* Alert Notification */}
      {alertMessage && (
        <div
          className={`flex items-center justify-between p-4 rounded-xl text-sm border animate-fade-in ${
            alertMessage.type === "success"
              ? "bg-emerald-50 border-emerald-200 text-emerald-800 dark:bg-emerald-500/10 dark:border-emerald-500/20 dark:text-emerald-300"
              : "bg-red-50 border-red-200 text-red-800 dark:bg-red-500/10 dark:border-red-500/20 dark:text-red-300"
          }`}
        >
          <div className="flex items-center gap-2">
            {alertMessage.type === "success" ? (
              <svg className="size-5 text-emerald-600 dark:text-emerald-400 shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
              </svg>
            ) : (
              <svg className="size-5 text-red-600 dark:text-red-400 shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
              </svg>
            )}
            <span>{alertMessage.text}</span>
          </div>
          <button
            type="button"
            onClick={() => setAlertMessage(null)}
            className="text-gray-400 hover:text-gray-600 dark:hover:text-gray-200"
          >
            ✕
          </button>
        </div>
      )}

      {/* Top Banner & KPI Stat Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        {/* Pending Card */}
        <div
          onClick={() => {
            setActiveTab("PENDING");
            setPage(0);
          }}
          className={`cursor-pointer p-5 rounded-2xl border transition-all ${
            activeTab === "PENDING"
              ? "border-amber-500 bg-amber-50/40 shadow-sm dark:bg-amber-500/10 dark:border-amber-500/40"
              : "border-gray-200 bg-white hover:border-gray-300 dark:border-white/[0.07] dark:bg-gray-900"
          }`}
        >
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold text-gray-500 dark:text-gray-400">
              Pending Decisions
            </span>
            <span className="flex size-7 items-center justify-center rounded-lg bg-amber-100 text-amber-600 dark:bg-amber-500/20 dark:text-amber-400">
              <svg className="size-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
            </span>
          </div>
          <div className="mt-2 flex items-baseline gap-2">
            <span className="text-2xl font-bold text-gray-900 dark:text-white">
              {pendingItems.length}
            </span>
            <span className="text-xs font-medium text-amber-600 dark:text-amber-400">
              Requires Review
            </span>
          </div>
        </div>

        {/* Pending Commitment Value */}
        <div className="p-5 rounded-2xl border border-gray-200 bg-white dark:border-white/[0.07] dark:bg-gray-900">
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold text-gray-500 dark:text-gray-400">
              Value Under Review
            </span>
            <span className="flex size-7 items-center justify-center rounded-lg bg-brand-100 text-brand-600 dark:bg-brand-500/20 dark:text-brand-400">
              <svg className="size-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
            </span>
          </div>
          <div className="mt-2 flex items-baseline gap-2">
            <span className="text-xl font-bold text-brand-600 dark:text-brand-400">
              {formatAmount(totalPendingValue, requisitions[0]?.currencyCode || "MAD")}
            </span>
          </div>
        </div>

        {/* Approved History */}
        <div
          onClick={() => {
            setActiveTab("APPROVED");
            setPage(0);
          }}
          className={`cursor-pointer p-5 rounded-2xl border transition-all ${
            activeTab === "APPROVED"
              ? "border-emerald-500 bg-emerald-50/40 shadow-sm dark:bg-emerald-500/10 dark:border-emerald-500/40"
              : "border-gray-200 bg-white hover:border-gray-300 dark:border-white/[0.07] dark:bg-gray-900"
          }`}
        >
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold text-gray-500 dark:text-gray-400">
              Approved History
            </span>
            <span className="flex size-7 items-center justify-center rounded-lg bg-emerald-100 text-emerald-600 dark:bg-emerald-500/20 dark:text-emerald-400">
              <svg className="size-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
              </svg>
            </span>
          </div>
          <div className="mt-2 flex items-baseline gap-2">
            <span className="text-2xl font-bold text-gray-900 dark:text-white">
              {approvedItems.length}
            </span>
            <span className="text-xs font-medium text-emerald-600 dark:text-emerald-400">
              Ready for PO
            </span>
          </div>
        </div>

        {/* Rejected / Denied */}
        <div
          onClick={() => {
            setActiveTab("REJECTED");
            setPage(0);
          }}
          className={`cursor-pointer p-5 rounded-2xl border transition-all ${
            activeTab === "REJECTED"
              ? "border-red-500 bg-red-50/40 shadow-sm dark:bg-red-500/10 dark:border-red-500/40"
              : "border-gray-200 bg-white hover:border-gray-300 dark:border-white/[0.07] dark:bg-gray-900"
          }`}
        >
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold text-gray-500 dark:text-gray-400">
              Denied / Rejected
            </span>
            <span className="flex size-7 items-center justify-center rounded-lg bg-red-100 text-red-600 dark:bg-red-500/20 dark:text-red-400">
              <svg className="size-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
              </svg>
            </span>
          </div>
          <div className="mt-2 flex items-baseline gap-2">
            <span className="text-2xl font-bold text-gray-900 dark:text-white">
              {rejectedItems.length}
            </span>
            <span className="text-xs font-medium text-red-600 dark:text-red-400">
              Turned Down
            </span>
          </div>
        </div>
      </div>

      {/* Main Table Container */}
      <div className="rounded-2xl border border-gray-200 bg-white shadow-sm dark:border-white/[0.07] dark:bg-gray-900 overflow-hidden">
        {/* Table Header Controls */}
        <div className="flex flex-col sm:flex-row items-stretch sm:items-center justify-between gap-4 p-5 border-b border-gray-100 dark:border-white/[0.07]">
          {/* Tabs Filter */}
          <div className="flex items-center gap-1.5 p-1 rounded-xl bg-gray-100 dark:bg-gray-800/80 border border-gray-200/80 dark:border-white/[0.05] overflow-x-auto">
            <button
              type="button"
              onClick={() => {
                setActiveTab("PENDING");
                setPage(0);
              }}
              className={`px-3 py-1.5 rounded-lg text-xs font-semibold transition-all whitespace-nowrap ${
                activeTab === "PENDING"
                  ? "bg-white text-gray-900 shadow-sm dark:bg-gray-700 dark:text-white"
                  : "text-gray-600 hover:text-gray-900 dark:text-gray-400 dark:hover:text-white"
              }`}
            >
              Pending Decisions ({pendingItems.length})
            </button>
            <button
              type="button"
              onClick={() => {
                setActiveTab("APPROVED");
                setPage(0);
              }}
              className={`px-3 py-1.5 rounded-lg text-xs font-semibold transition-all whitespace-nowrap ${
                activeTab === "APPROVED"
                  ? "bg-white text-gray-900 shadow-sm dark:bg-gray-700 dark:text-white"
                  : "text-gray-600 hover:text-gray-900 dark:text-gray-400 dark:hover:text-white"
              }`}
            >
              Approved ({approvedItems.length})
            </button>
            <button
              type="button"
              onClick={() => {
                setActiveTab("REJECTED");
                setPage(0);
              }}
              className={`px-3 py-1.5 rounded-lg text-xs font-semibold transition-all whitespace-nowrap ${
                activeTab === "REJECTED"
                  ? "bg-white text-gray-900 shadow-sm dark:bg-gray-700 dark:text-white"
                  : "text-gray-600 hover:text-gray-900 dark:text-gray-400 dark:hover:text-white"
              }`}
            >
              Rejected ({rejectedItems.length})
            </button>
            <button
              type="button"
              onClick={() => {
                setActiveTab("ALL");
                setPage(0);
              }}
              className={`px-3 py-1.5 rounded-lg text-xs font-semibold transition-all whitespace-nowrap ${
                activeTab === "ALL"
                  ? "bg-white text-gray-900 shadow-sm dark:bg-gray-700 dark:text-white"
                  : "text-gray-600 hover:text-gray-900 dark:text-gray-400 dark:hover:text-white"
              }`}
            >
              All Records ({requisitions.length})
            </button>
          </div>

          {/* Search Box & Controls */}
          <div className="flex items-center gap-3">
            <div className="relative flex-1 sm:w-64">
              <div className="pointer-events-none absolute inset-y-0 left-0 flex items-center pl-3 text-gray-400">
                <svg className="size-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
                </svg>
              </div>
              <input
                type="text"
                value={searchKeyword}
                onChange={(e) => {
                  setSearchKeyword(e.target.value);
                  setPage(0);
                }}
                placeholder="Search by code, title, requester..."
                className="h-9.5 w-full rounded-lg border border-gray-300 bg-white pl-9 pr-3 text-xs text-gray-800 placeholder:text-gray-400 focus:border-brand-500 focus:outline-none focus:ring-1 focus:ring-brand-500 dark:border-gray-700 dark:bg-gray-900 dark:text-white"
              />
            </div>

            <Button
              variant="outline"
              size="sm"
              onClick={() => setRefreshTrigger((prev) => prev + 1)}
              title="Refresh"
            >
              <svg
                className={`size-4 ${loading ? "animate-spin text-brand-500" : ""}`}
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
              </svg>
            </Button>

            {/* Link to Standard PRs */}
            <Link to="/requisitions">
              <Button variant="outline" size="sm">
                <span className="flex items-center gap-1.5 text-xs">
                  <svg className="size-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                  </svg>
                  Requisition List
                </span>
              </Button>
            </Link>
          </div>
        </div>

        {/* Table Content */}
        <div className="overflow-x-auto">
          <Table>
            <TableHeader className="border-b border-gray-100 bg-gray-50/75 dark:border-white/[0.05] dark:bg-gray-800/40">
              <TableRow>
                <TableCell isHeader className="w-10 px-4 py-3.5">
                  <span className="sr-only">Expand</span>
                </TableCell>
                <TableCell isHeader className="px-4 py-3.5 text-left text-xs font-semibold text-gray-600 dark:text-gray-300">
                  Requisition
                </TableCell>
                <TableCell isHeader className="px-4 py-3.5 text-left text-xs font-semibold text-gray-600 dark:text-gray-300">
                  Requester
                </TableCell>
                <TableCell isHeader className="px-4 py-3.5 text-left text-xs font-semibold text-gray-600 dark:text-gray-300">
                  Need-By Date
                </TableCell>
                <TableCell isHeader className="px-4 py-3.5 text-left text-xs font-semibold text-gray-600 dark:text-gray-300">
                  Estimated Value
                </TableCell>
                <TableCell isHeader className="px-4 py-3.5 text-left text-xs font-semibold text-gray-600 dark:text-gray-300">
                  Status
                </TableCell>
                <TableCell isHeader className="px-4 py-3.5 text-right text-xs font-semibold text-gray-600 dark:text-gray-300">
                  Decision Actions
                </TableCell>
              </TableRow>
            </TableHeader>

            <TableBody>
              {loading ? (
                <TableRow>
                  <TableCell className="py-12 text-center text-xs text-gray-400" colSpan={7}>
                    <div className="flex items-center justify-center gap-2">
                      <svg className="size-4 animate-spin text-brand-500" fill="none" viewBox="0 0 24 24">
                        <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
                        <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z" />
                      </svg>
                      Loading requisitions for approval...
                    </div>
                  </TableCell>
                </TableRow>
              ) : paginatedItems.length === 0 ? (
                <TableRow>
                  <TableCell className="py-12 text-center text-xs text-gray-400" colSpan={7}>
                    <div className="flex flex-col items-center gap-2">
                      <svg className="size-8 text-gray-300 dark:text-gray-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
                      </svg>
                      <span>No requisitions found matching current criteria.</span>
                    </div>
                  </TableCell>
                </TableRow>
              ) : (
                paginatedItems.map((req) => {
                  const isExpanded = expandedRowIds.has(req.id);
                  const isPending = req.status === "SUBMITTED" || req.status === "UNDER_REVIEW";
                  const lines = req.lines || [];

                  return (
                    <React.Fragment key={req.id}>
                      <TableRow className="border-b border-gray-100 hover:bg-gray-50/60 dark:border-white/[0.05] dark:hover:bg-white/[0.02] transition-colors">
                        {/* Expand Button */}
                        <TableCell className="px-4 py-3.5">
                          <button
                            type="button"
                            onClick={() => handleToggleRow(req.id)}
                            className="p-1 rounded-md text-gray-400 hover:text-gray-700 hover:bg-gray-100 dark:hover:text-gray-200 dark:hover:bg-gray-800 transition-transform"
                            title={isExpanded ? "Collapse item lines" : "Expand item lines"}
                          >
                            <svg
                              className={`size-4 transition-transform duration-200 ${
                                isExpanded ? "rotate-90 text-brand-500" : ""
                              }`}
                              fill="none"
                              stroke="currentColor"
                              viewBox="0 0 24 24"
                            >
                              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
                            </svg>
                          </button>
                        </TableCell>

                        {/* Code & Title */}
                        <TableCell className="px-4 py-3.5">
                          <div className="flex flex-col">
                            <div className="flex items-center gap-2">
                              <Link
                                to={`/requisitions/view/${req.id}`}
                                className="font-bold text-xs text-brand-600 hover:text-brand-700 hover:underline dark:text-brand-400 dark:hover:text-brand-300 transition-colors inline-flex items-center gap-1 group"
                                title="Click to view all details of this requisition"
                              >
                                <span>{req.requisitionCode}</span>
                                <svg
                                  className="size-3 text-brand-500 opacity-60 group-hover:opacity-100 transition-opacity"
                                  fill="none"
                                  viewBox="0 0 24 24"
                                  stroke="currentColor"
                                >
                                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M10 6H6a2 2 0 00-2 2v10a2 2 0 002 2h10a2 2 0 002-2v-4M14 4h6m0 0v6m0-6L10 14" />
                                </svg>
                              </Link>
                              <span className="inline-flex items-center px-1.5 py-0.2 text-[10px] font-medium rounded bg-gray-100 text-gray-600 dark:bg-gray-800 dark:text-gray-400">
                                {lines.length} {lines.length === 1 ? "line" : "lines"}
                              </span>
                            </div>
                            <Link
                              to={`/requisitions/view/${req.id}`}
                              className="text-xs font-semibold text-gray-800 hover:text-brand-600 dark:text-gray-200 dark:hover:text-brand-400 truncate max-w-sm transition-colors"
                              title="Click to view all details of this requisition"
                            >
                              {req.title}
                            </Link>
                            {req.justification && (
                              <span className="text-[11px] text-gray-500 dark:text-gray-400 truncate max-w-sm italic mt-0.5">
                                Reason: "{req.justification}"
                              </span>
                            )}
                          </div>
                        </TableCell>

                        {/* Requester */}
                        <TableCell className="px-4 py-3.5">
                          <div className="flex items-center gap-2">
                            <div className="size-7 rounded-full bg-brand-100 text-brand-700 dark:bg-brand-500/20 dark:text-brand-300 flex items-center justify-center font-bold text-[10px]">
                              {(req.requesterName || "U")
                                .split(" ")
                                .map((n) => n[0])
                                .join("")
                                .slice(0, 2)
                                .toUpperCase()}
                            </div>
                            <div className="flex flex-col">
                              <span className="text-xs font-medium text-gray-800 dark:text-gray-200">
                                {req.requesterName}
                              </span>
                              <span className="text-[10px] text-gray-400 dark:text-gray-500">
                                ID: {req.requesterId}
                              </span>
                            </div>
                          </div>
                        </TableCell>

                        {/* Need-By Date */}
                        <TableCell className="px-4 py-3.5">
                          <span className="text-xs text-gray-700 dark:text-gray-300 font-medium">
                            {req.requiredDate ? new Date(req.requiredDate).toLocaleDateString() : "—"}
                          </span>
                        </TableCell>

                        {/* Total Amount */}
                        <TableCell className="px-4 py-3.5">
                          <span className="text-xs font-bold text-gray-900 dark:text-white">
                            {formatAmount(req.totalAmount, req.currencyCode)}
                          </span>
                        </TableCell>

                        {/* Status */}
                        <TableCell className="px-4 py-3.5">
                          <RequisitionStatusBadge status={req.status} size="sm" />
                        </TableCell>

                        {/* Actions */}
                        <TableCell className="px-4 py-3.5 text-right">
                          <div className="flex items-center justify-end gap-2">
                            {/* View Full Requisition Details Link */}
                            <Link
                              to={`/requisitions/view/${req.id}`}
                              className="p-1.5 rounded-lg text-gray-500 hover:text-brand-600 hover:bg-gray-100 dark:text-gray-400 dark:hover:text-brand-400 dark:hover:bg-gray-800 transition-colors"
                              title="View Full Requisition Details"
                            >
                              <svg className="size-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
                              </svg>
                            </Link>

                            {isPending ? (
                              <>
                                {/* Approve Button */}
                                <button
                                  type="button"
                                  onClick={() =>
                                    setModalState({
                                      isOpen: true,
                                      requisition: req,
                                      mode: "APPROVE",
                                    })
                                  }
                                  className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-emerald-600 hover:bg-emerald-700 text-white text-xs font-semibold shadow-xs transition-colors cursor-pointer"
                                >
                                  <svg className="size-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2.5} d="M5 13l4 4L19 7" />
                                  </svg>
                                  Approve
                                </button>

                                {/* Deny / Reject Button */}
                                <button
                                  type="button"
                                  onClick={() =>
                                    setModalState({
                                      isOpen: true,
                                      requisition: req,
                                      mode: "REJECT",
                                    })
                                  }
                                  className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-red-50 hover:bg-red-100 text-red-700 border border-red-200 dark:bg-red-500/15 dark:text-red-300 dark:border-red-500/30 text-xs font-semibold transition-colors cursor-pointer"
                                >
                                  <svg className="size-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2.5} d="M6 18L18 6M6 6l12 12" />
                                  </svg>
                                  Deny
                                </button>
                              </>
                            ) : (
                              <div className="flex flex-col items-end gap-0.5">
                                <span className="text-xs text-gray-500 dark:text-gray-400 font-medium">
                                  Decision Recorded
                                </span>
                                {req.approvalNotes && (
                                  <span className="text-[10px] text-gray-400 dark:text-gray-500 italic max-w-xs truncate">
                                    Notes: "{req.approvalNotes}"
                                  </span>
                                )}
                                {req.rejectionReason && (
                                  <span className="text-[10px] text-red-500 dark:text-red-400 italic max-w-xs truncate">
                                    Reason: "{req.rejectionReason}"
                                  </span>
                                )}
                              </div>
                            )}
                          </div>
                        </TableCell>
                      </TableRow>

                      {/* Expanded Item Lines Details */}
                      {isExpanded && (
                        <TableRow className="bg-gray-50/75 dark:bg-white/[0.015] border-b border-gray-100 dark:border-white/[0.05]">
                          <TableCell colSpan={7} className="px-6 py-4">
                            <div className="space-y-3">
                              <div className="flex items-center justify-between">
                                <h5 className="text-xs font-bold text-gray-800 dark:text-gray-200 flex items-center gap-1.5">
                                  <svg className="size-3.5 text-brand-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
                                  </svg>
                                  Procurement Items Breakdown ({lines.length})
                                </h5>
                                <div className="flex items-center gap-3">
                                  {req.description && (
                                    <span className="text-xs text-gray-500 dark:text-gray-400">
                                      Description: {req.description}
                                    </span>
                                  )}
                                  <Link
                                    to={`/requisitions/view/${req.id}`}
                                    className="inline-flex items-center gap-1 text-xs font-semibold text-brand-600 hover:text-brand-700 hover:underline dark:text-brand-400"
                                  >
                                    <span>View Full Requisition Page</span>
                                    <svg className="size-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M14 5l7 7m0 0l-7 7m7-7H3" />
                                    </svg>
                                  </Link>
                                </div>
                              </div>

                              <div className="rounded-xl border border-gray-200 dark:border-white/[0.07] overflow-hidden">
                                <table className="w-full text-left text-xs">
                                  <thead className="bg-gray-100/75 dark:bg-gray-800/60 border-b border-gray-200 dark:border-white/[0.07]">
                                    <tr>
                                      <th className="px-3 py-2 font-semibold text-gray-600 dark:text-gray-300">#</th>
                                      <th className="px-3 py-2 font-semibold text-gray-600 dark:text-gray-300">Material</th>
                                      <th className="px-3 py-2 font-semibold text-gray-600 dark:text-gray-300">Qty</th>
                                      <th className="px-3 py-2 font-semibold text-gray-600 dark:text-gray-300">Unit Price</th>
                                      <th className="px-3 py-2 font-semibold text-gray-600 dark:text-gray-300">Estimated Total</th>
                                      <th className="px-3 py-2 font-semibold text-gray-600 dark:text-gray-300">Supplier</th>
                                      <th className="px-3 py-2 font-semibold text-gray-600 dark:text-gray-300">Delivery Notes</th>
                                    </tr>
                                  </thead>
                                  <tbody className="divide-y divide-gray-100 dark:divide-white/[0.05] bg-white dark:bg-gray-900">
                                    {lines.map((l: any, idx: number) => (
                                      <tr key={l.id || idx} className="hover:bg-gray-50/50 dark:hover:bg-white/[0.01]">
                                        <td className="px-3 py-2 text-gray-400 font-mono">{idx + 1}</td>
                                        <td className="px-3 py-2 font-medium text-gray-800 dark:text-white">
                                          <div className="flex items-center gap-1.5">
                                            <span className="font-mono text-brand-600 dark:text-brand-400">
                                              [{l.materialCode || "ITEM"}]
                                            </span>
                                            <span>{l.materialName || l.materialDescription || "Material"}</span>
                                          </div>
                                        </td>
                                        <td className="px-3 py-2 font-semibold text-gray-700 dark:text-gray-300">
                                          {l.quantity} {l.unitOfMeasure || "PCS"}
                                        </td>
                                        <td className="px-3 py-2 text-gray-700 dark:text-gray-300">
                                          {formatAmount(l.unitPrice, req.currencyCode)}
                                        </td>
                                        <td className="px-3 py-2 font-bold text-gray-900 dark:text-white">
                                          {formatAmount(l.lineTotal, req.currencyCode)}
                                        </td>
                                        <td className="px-3 py-2 text-gray-600 dark:text-gray-300">
                                          {l.supplierName || l.supplierCode || "—"}
                                        </td>
                                        <td className="px-3 py-2 text-gray-500 dark:text-gray-400 italic">
                                          {l.notes || l.deliveryTerms || "—"}
                                        </td>
                                      </tr>
                                    ))}
                                  </tbody>
                                </table>
                              </div>
                            </div>
                          </TableCell>
                        </TableRow>
                      )}
                    </React.Fragment>
                  );
                })
              )}
            </TableBody>
          </Table>
        </div>

        {/* Pagination Footer */}
        <div className="p-4 border-t border-gray-100 dark:border-white/[0.07] flex items-center justify-between">
          <span className="text-xs text-gray-500 dark:text-gray-400">
            Showing {paginatedItems.length} of {filteredRequisitions.length} requisitions
          </span>
          {totalPages > 1 && (
            <Pagination
              currentPage={page + 1}
              totalPages={totalPages}
              onPageChange={(p) => setPage(p - 1)}
            />
          )}
        </div>
      </div>

      {/* Decision Modal (Approve / Deny) */}
      <RequisitionApprovalModal
        isOpen={modalState.isOpen}
        onClose={() => setModalState((prev) => ({ ...prev, isOpen: false }))}
        requisition={modalState.requisition}
        mode={modalState.mode}
        onConfirm={handleConfirmDecision}
      />
    </div>
  );
}
