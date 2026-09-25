import React, { useEffect, useState } from "react";
import { useParams, useNavigate, Link } from "react-router-dom";
import { requisitionApi } from "../services/requisitionApi";
import type { Requisition } from "../types";
import RequisitionStatusBadge from "./RequisitionStatusBadge";
import RequisitionApprovalModal from "./RequisitionApprovalModal";
import RequisitionCancelModal from "./RequisitionCancelModal";
import Button from "../../../shared/components/ui/button/Button";
import DeleteConfirmModal from "../../../shared/components/ui/modal/DeleteConfirmModal";
import {
  Table,
  TableBody,
  TableCell,
  TableHeader,
  TableRow,
} from "../../../shared/components/ui/table";

export default function RequisitionDetail() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();

  const [requisition, setRequisition] = useState<Requisition | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [actionLoading, setActionLoading] = useState(false);

  // Approval / Rejection modal state
  const [modalState, setModalState] = useState<{
    isOpen: boolean;
    mode: "APPROVE" | "REJECT";
  }>({
    isOpen: false,
    mode: "APPROVE",
  });

  // Cancel modal state
  const [showCancelModal, setShowCancelModal] = useState(false);

  // Success toast feedback
  const [feedback, setFeedback] = useState<{
    type: "success" | "error";
    text: string;
  } | null>(null);

  useEffect(() => {
    if (id) {
      loadRequisition(id);
    }
  }, [id]);

  const loadRequisition = async (reqId: string) => {
    try {
      setLoading(true);
      setError(null);
      const res = await requisitionApi.getById(reqId);
      setRequisition(res.data);
    } catch (err: any) {
      console.error("Failed to load requisition details:", err);
      setError(
        err?.response?.data?.message ||
        "Could not retrieve requisition details. Please ensure the record exists."
      );
    } finally {
      setLoading(false);
    }
  };

  const handleQuickSubmit = async () => {
    if (!requisition) return;
    try {
      setActionLoading(true);
      const res = await requisitionApi.submit(requisition.id, "current-user");
      setRequisition(res.data);
      setFeedback({
        type: "success",
        text: `Requisition ${res.data.requisitionCode} has been submitted for approval.`,
      });
    } catch (err: any) {
      console.error("Failed to submit requisition:", err);
      setFeedback({
        type: "error",
        text: err?.response?.data?.message || "Failed to submit requisition.",
      });
    } finally {
      setActionLoading(false);
    }
  };

  const handleApprovalDecision = async (notesOrReason: string) => {
    if (!requisition) return;
    const isApprove = modalState.mode === "APPROVE";
    try {
      setActionLoading(true);
      let res;
      if (isApprove) {
        res = await requisitionApi.approve(
          requisition.id,
          "manager-approver",
          "Management Approver",
          notesOrReason
        );
        setFeedback({
          type: "success",
          text: `Requisition ${res.data.requisitionCode} has been approved.`,
        });
      } else {
        res = await requisitionApi.reject(
          requisition.id,
          notesOrReason,
          "manager-approver",
          "Management Approver"
        );
        setFeedback({
          type: "success",
          text: `Requisition ${res.data.requisitionCode} has been rejected.`,
        });
      }
      setRequisition(res.data);
      setModalState({ isOpen: false, mode: "APPROVE" });
    } catch (err: any) {
      console.error("Approval action failed:", err);
      setFeedback({
        type: "error",
        text: err?.response?.data?.message || "Action failed.",
      });
    } finally {
      setActionLoading(false);
    }
  };

  const handleCancelRequisition = async (reason: string) => {
    if (!requisition) return;
    try {
      setActionLoading(true);
      const res = await requisitionApi.cancel(requisition.id, "current-user", reason);
      setRequisition(res.data);
      setFeedback({
        type: "success",
        text: `Requisition ${res.data.requisitionCode} has been cancelled.`,
      });
      setShowCancelModal(false);
    } catch (err: any) {
      console.error("Failed to cancel requisition:", err);
      setFeedback({
        type: "error",
        text: err?.response?.data?.message || "Failed to cancel requisition.",
      });
    } finally {
      setActionLoading(false);
    }
  };

  const handleConvertToPo = async () => {
    if (!requisition) return;
    try {
      setActionLoading(true);
      const generatedPoCode = `PO-${new Date().getFullYear()}-${Math.floor(
        1000 + Math.random() * 9000
      )}`;
      const res = await requisitionApi.convert(
        requisition.id,
        crypto.randomUUID(),
        generatedPoCode,
        "current-user"
      );
      setRequisition(res.data);
      setFeedback({
        type: "success",
        text: `Requisition converted to Purchase Order ${generatedPoCode}.`,
      });
    } catch (err: any) {
      console.error("Failed to convert requisition to PO:", err);
      setFeedback({
        type: "error",
        text: err?.response?.data?.message || "Failed to convert to PO.",
      });
    } finally {
      setActionLoading(false);
    }
  };

  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [isDeleting, setIsDeleting] = useState(false);

  const handleDeleteConfirm = async () => {
    if (!requisition) return;
    try {
      setIsDeleting(true);
      await requisitionApi.delete(requisition.id);
      navigate("/requisitions");
    } catch (err: any) {
      console.error("Failed to delete requisition:", err);
      setFeedback({
        type: "error",
        text: err?.response?.data?.message || "Failed to delete requisition.",
      });
      setShowDeleteModal(false);
    } finally {
      setIsDeleting(false);
    }
  };

  const formatAmount = (amt: string | number | undefined, curr = "MAD") => {
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

  if (loading) {
    return (
      <div className="flex flex-col items-center justify-center py-20 gap-3">
        <div className="size-8 animate-spin rounded-full border-2 border-brand-500 border-t-transparent" />
        <span className="text-xs text-gray-500 dark:text-gray-400 font-medium">
          Loading purchase requisition details...
        </span>
      </div>
    );
  }

  if (error || !requisition) {
    return (
      <div className="rounded-2xl border border-red-200 bg-red-50/50 p-8 text-center dark:border-red-500/20 dark:bg-red-500/10">
        <div className="mx-auto flex size-12 items-center justify-center rounded-full bg-red-100 dark:bg-red-500/20 text-red-600 dark:text-red-400 mb-4">
          <svg className="size-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
          </svg>
        </div>
        <h3 className="text-base font-semibold text-gray-900 dark:text-white mb-2">
          Requisition Not Found
        </h3>
        <p className="text-xs text-gray-500 dark:text-gray-400 max-w-md mx-auto mb-6">
          {error || "The requested purchase requisition could not be located."}
        </p>
        <Button size="sm" onClick={() => navigate("/purchase-requisitions")}>
          Return to Requisitions List
        </Button>
      </div>
    );
  }

  const isDraft = requisition.status === "DRAFT";
  const isPending =
    requisition.status === "SUBMITTED" || requisition.status === "UNDER_REVIEW";
  const isApproved = requisition.status === "APPROVED";
  const isRejected = requisition.status === "REJECTED";
  const isConverted = requisition.status === "CONVERTED";
  const isCancelled = requisition.status === "CANCELLED";

  // Matrix Lifecycle Rules:
  // Modifier: DRAFT ✅ OUI, SUBMITTED ✅ OUI*, APPROVED ❌ NON, REJECTED ❌ NON, CONVERTED ❌ NON, CANCELLED ❌ NON
  const canEdit = isDraft || isPending;

  // Supprimer: DRAFT ✅ OUI, SUBMITTED ❌ NON, APPROVED ❌ NON, REJECTED ✅ OUI, CONVERTED ❌ NON, CANCELLED ✅ OUI
  const canDelete = isDraft || isRejected || isCancelled;

  // Soumettre: DRAFT ✅ OUI, others ❌ NON
  const canSubmit = isDraft;

  // Approuver / Rejeter: SUBMITTED ✅ OUI, others ❌ NON
  const canApprove = isPending;
  const canReject = isPending;

  // Convertir: APPROVED ✅ OUI, others ❌ NON
  const canConvert = isApproved;

  // Annuler: SUBMITTED ✅ OUI, APPROVED ✅ OUI, DRAFT ❌ NON (delete directly instead)
  const canCancel = isPending || isApproved;

  return (
    <div className="space-y-6">
      {/* Toast Alert */}
      {feedback && (
        <div
          className={`flex items-center justify-between p-4 rounded-xl text-xs font-medium border ${feedback.type === "success"
              ? "bg-emerald-50 text-emerald-800 border-emerald-200 dark:bg-emerald-500/10 dark:text-emerald-300 dark:border-emerald-500/20"
              : "bg-red-50 text-red-800 border-red-200 dark:bg-red-500/10 dark:text-red-300 dark:border-red-500/20"
            }`}
        >
          <span>{feedback.text}</span>
          <button
            type="button"
            onClick={() => setFeedback(null)}
            className="hover:opacity-75 font-bold"
          >
            ×
          </button>
        </div>
      )}

      {/* Top Header Card */}
      <div className="rounded-2xl border border-gray-200 bg-white p-6 shadow-sm dark:border-white/[0.07] dark:bg-gray-900">
        <div className="flex flex-col gap-4 md:flex-row md:items-center md:justify-between">
          <div>
            <div className="flex items-center gap-3 mb-2">
              <button
                type="button"
                onClick={() => navigate("/requisitions")}
                className="inline-flex items-center gap-1 text-xs font-medium text-gray-500 hover:text-brand-600 dark:text-gray-400 dark:hover:text-brand-400"
              >
                <svg className="size-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M10 19l-7-7m0 0l7-7m-7 7h18" />
                </svg>
                Back to List
              </button>
              <span className="text-gray-300 dark:text-gray-700">|</span>
              <span className="text-xs font-bold text-brand-600 dark:text-brand-400">
                {requisition.requisitionCode}
              </span>
              <RequisitionStatusBadge status={requisition.status} size="sm" />
            </div>
            <h1 className="text-xl font-bold text-gray-900 dark:text-white">
              {requisition.title}
            </h1>
            {requisition.description && (
              <p className="mt-1 text-xs text-gray-500 dark:text-gray-400 max-w-2xl">
                {requisition.description}
              </p>
            )}
          </div>

          {/* Action Buttons */}
          <div className="flex flex-wrap items-center gap-2">
            <button
              type="button"
              onClick={() => window.print()}
              className="inline-flex items-center gap-1.5 px-3 py-2 text-xs font-semibold rounded-xl border border-gray-200 bg-white text-gray-700 hover:bg-gray-50 dark:border-white/[0.1] dark:bg-gray-800 dark:text-gray-200 dark:hover:bg-gray-700 transition-colors"
              title="Print Requisition"
            >
              <svg className="size-4 text-gray-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17 17h2a2 2 0 002-2v-4a2 2 0 00-2-2H5a2 2 0 00-2 2v4a2 2 0 002 2h2m2 4h6a2 2 0 002-2v-4a2 2 0 00-2-2H9a2 2 0 00-2 2v4a2 2 0 002 2zm8-12V5a2 2 0 00-2-2H9a2 2 0 00-2 2v4h10z" />
              </svg>
              Print
            </button>

            {/* Edit Requisition Button (DRAFT & SUBMITTED only) or Locked Badge */}
            {canEdit ? (
              <Link
                to={`/requisitions/edit/${requisition.id}`}
                className="inline-flex items-center gap-1.5 px-3 py-2 text-xs font-semibold rounded-xl border border-gray-200 bg-white text-gray-700 hover:text-brand-600 hover:bg-brand-50/50 dark:border-white/[0.1] dark:bg-gray-800 dark:text-gray-200 dark:hover:text-brand-400 dark:hover:bg-brand-500/10 transition-colors"
                title="Edit Requisition"
              >
                <svg className="size-4 text-gray-500 hover:text-brand-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
                </svg>
                Edit
              </Link>
            ) : (
              <span
                className="inline-flex items-center gap-1.5 px-3 py-2 text-xs font-semibold rounded-xl border border-gray-200/60 bg-gray-50 text-gray-400 cursor-not-allowed dark:border-white/[0.05] dark:bg-gray-800/50 dark:text-gray-500"
                title={`Locked: Requisition in ${requisition.status} status cannot be modified`}
              >
                <svg className="size-4 text-gray-400 dark:text-gray-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" />
                </svg>
                Locked ({requisition.status})
              </span>
            )}

            {/* Delete Requisition Button (DRAFT, REJECTED, CANCELLED) or Disabled */}
            {canDelete ? (
              <button
                type="button"
                onClick={() => setShowDeleteModal(true)}
                className="inline-flex items-center gap-1.5 px-3 py-2 text-xs font-semibold rounded-xl border border-red-200 bg-red-50 text-red-700 hover:bg-red-100 dark:border-red-500/30 dark:bg-red-500/10 dark:text-red-400 dark:hover:bg-red-500/20 transition-colors cursor-pointer"
                title="Delete Requisition"
              >
                <svg className="size-4 text-red-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                </svg>
                Delete
              </button>
            ) : (
              <span
                className="inline-flex items-center gap-1.5 px-3 py-2 text-xs font-semibold rounded-xl border border-gray-200/60 bg-gray-50 text-gray-300 dark:border-white/[0.05] dark:bg-gray-800/50 dark:text-gray-600 cursor-not-allowed opacity-50"
                title={`Cannot delete: Requisition in ${requisition.status} status cannot be deleted`}
              >
                <svg className="size-4 text-gray-300 dark:text-gray-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                </svg>
                Delete
              </span>
            )}

            {/* Submit for Approval Button (DRAFT only) */}
            {canSubmit && (
              <Button
                size="sm"
                onClick={handleQuickSubmit}
                disabled={actionLoading}
              >
                Submit for Approval
              </Button>
            )}

            {/* Approve and Reject Buttons (SUBMITTED only) */}
            {canApprove && (
              <Button
                size="sm"
                onClick={() => setModalState({ isOpen: true, mode: "APPROVE" })}
                disabled={actionLoading}
                className="bg-emerald-600 hover:bg-emerald-700 text-white"
              >
                Approve Requisition
              </Button>
            )}

            {canReject && (
              <Button
                size="sm"
                variant="outline"
                onClick={() => setModalState({ isOpen: true, mode: "REJECT" })}
                disabled={actionLoading}
                className="border-red-300 text-red-700 hover:bg-red-50 dark:border-red-500/30 dark:text-red-400"
              >
                Reject
              </Button>
            )}

            {/* Convert to Purchase Order Button (APPROVED only) */}
            {canConvert && (
              <Button
                size="sm"
                onClick={handleConvertToPo}
                disabled={actionLoading}
                className="bg-purple-600 hover:bg-purple-700 text-white"
              >
                Convert to Purchase Order
              </Button>
            )}

            {/* Cancel Requisition Button: Active for SUBMITTED & APPROVED, Disabled for DRAFT & others */}
            {canCancel ? (
              <Button
                size="sm"
                variant="outline"
                onClick={() => setShowCancelModal(true)}
                disabled={actionLoading}
                className="border-amber-300 text-amber-700 hover:bg-amber-50 dark:border-amber-500/30 dark:text-amber-400 cursor-pointer"
              >
                Cancel Requisition
              </Button>
            ) : (
              <span
                className="inline-flex items-center justify-center gap-2 rounded-lg border border-gray-200 bg-white text-gray-400 dark:border-white/[0.05] dark:bg-gray-800 dark:text-gray-600 px-4 py-3 text-sm cursor-not-allowed opacity-50 font-medium"
                title={
                  isDraft
                    ? "Draft requisitions cannot be cancelled (delete directly instead)"
                    : `Cannot cancel: Requisition in ${requisition.status} status cannot be cancelled`
                }
              >
                Cancel Requisition
              </span>
            )}
          </div>
        </div>

        {/* Sophisticated Colorful Horizontal Workflow Pipeline */}
        <div className="mt-8 pt-6 border-t border-gray-100 dark:border-white/[0.07]">
          <div className="flex items-center justify-between mb-3">
            <div className="flex items-center gap-2">
              <span className="inline-flex size-2 rounded-full bg-brand-500 animate-pulse" />
              <h4 className="text-xs font-bold uppercase tracking-wider text-gray-700 dark:text-gray-200">
                Procurement Pipeline Progress
              </h4>
            </div>
            <span className="text-[11px] font-semibold text-gray-500 dark:text-gray-400">
              {isCancelled
                ? "Requisition Cancelled"
                : isConverted
                ? "4 of 4 Stages Completed"
                : isApproved
                  ? "3 of 4 Stages Completed"
                  : isPending
                    ? "2 of 4 Stages Completed"
                    : isRejected
                      ? "Stage 3 Rejected"
                      : "1 of 4 Stages Completed"}
            </span>
          </div>

          {/* Visual Track Progress Bar */}
          <div className="relative mb-5 hidden md:block">
            <div className="h-1.5 w-full rounded-full bg-gray-100 dark:bg-white/[0.08] overflow-hidden">
              <div
                className={`h-full transition-all duration-500 rounded-full ${isCancelled
                    ? "bg-gradient-to-r from-gray-400 to-slate-500"
                    : isRejected
                    ? "bg-gradient-to-r from-emerald-500 via-emerald-400 to-red-500"
                    : "bg-gradient-to-r from-emerald-500 via-teal-500 to-purple-600"
                  }`}
                style={{
                  width: isCancelled
                    ? "100%"
                    : isConverted
                    ? "100%"
                    : isApproved
                      ? "75%"
                      : isPending
                        ? "50%"
                        : isRejected
                          ? "75%"
                          : "25%",
                }}
              />
            </div>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-4 gap-3 relative">
            {/* ================= STAGE 1: DRAFT CREATED ================= */}
            <div className="relative rounded-2xl border border-emerald-200/90 bg-gradient-to-br from-emerald-50/90 to-teal-50/30 p-4 shadow-sm dark:border-emerald-500/30 dark:from-emerald-950/30 dark:to-teal-900/10 transition-all hover:shadow-md">
              <div className="flex items-center justify-between mb-2">
                <div className="size-8 rounded-xl bg-emerald-500 text-white flex items-center justify-center shadow-sm shadow-emerald-500/40">
                  <svg className="size-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2.5} d="M5 13l4 4L19 7" />
                  </svg>
                </div>
                <span className="inline-flex items-center px-2 py-0.5 rounded-full text-[10px] font-bold bg-emerald-100 text-emerald-800 dark:bg-emerald-500/20 dark:text-emerald-300">
                  Completed
                </span>
              </div>
              <div className="flex flex-col">
                <span className="text-[11px] font-bold uppercase tracking-wide text-emerald-800 dark:text-emerald-300">
                  1. Created (Draft)
                </span>
                <span className="text-sm font-extrabold text-gray-900 dark:text-white mt-0.5">
                  {requisition.createdAt
                    ? new Date(requisition.createdAt).toLocaleDateString()
                    : "—"}
                </span>
                <span className="text-[11px] text-gray-500 dark:text-gray-400 mt-1 truncate">
                  By: {requisition.createdBy || requisition.requesterName}
                </span>
              </div>

              {/* Stage Connector Arrow */}
              <div className="hidden md:flex absolute -right-3 top-1/2 -translate-y-1/2 z-10 size-6 items-center justify-center rounded-full border border-emerald-200 bg-white shadow-xs dark:border-emerald-500/30 dark:bg-gray-800 text-emerald-600 dark:text-emerald-400">
                <svg className="size-3" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2.5} d="M9 5l7 7-7 7" />
                </svg>
              </div>
            </div>

            {/* ================= STAGE 2: SUBMITTED ================= */}
            <div
              className={`relative rounded-2xl border p-4 shadow-sm transition-all hover:shadow-md ${isDraft
                  ? "border-brand-300 bg-gradient-to-br from-brand-50/90 to-blue-50/40 ring-2 ring-brand-500/20 dark:border-brand-500/40 dark:from-brand-950/40 dark:to-blue-900/20"
                  : "border-emerald-200/90 bg-gradient-to-br from-emerald-50/90 to-teal-50/30 dark:border-emerald-500/30 dark:from-emerald-950/30 dark:to-teal-900/10"
                }`}
            >
              <div className="flex items-center justify-between mb-2">
                <div
                  className={`size-8 rounded-xl flex items-center justify-center text-white shadow-sm ${isDraft
                      ? "bg-brand-500 shadow-brand-500/40 ring-4 ring-brand-500/20 animate-pulse"
                      : "bg-emerald-500 shadow-emerald-500/40"
                    }`}
                >
                  {isDraft ? (
                    <svg className="size-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 19l9 2-9-18-9 18 9-2zm0 0v-8" />
                    </svg>
                  ) : (
                    <svg className="size-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2.5} d="M5 13l4 4L19 7" />
                    </svg>
                  )}
                </div>
                <span
                  className={`inline-flex items-center px-2 py-0.5 rounded-full text-[10px] font-bold ${isDraft
                      ? "bg-amber-100 text-amber-800 dark:bg-amber-500/20 dark:text-amber-300 animate-pulse"
                      : "bg-emerald-100 text-emerald-800 dark:bg-emerald-500/20 dark:text-emerald-300"
                    }`}
                >
                  {isDraft ? "Action Required" : "Submitted"}
                </span>
              </div>
              <div className="flex flex-col">
                <span
                  className={`text-[11px] font-bold uppercase tracking-wide ${isDraft
                      ? "text-brand-700 dark:text-brand-300"
                      : "text-emerald-800 dark:text-emerald-300"
                    }`}
                >
                  2. Submission
                </span>
                <span className="text-sm font-extrabold text-gray-900 dark:text-white mt-0.5">
                  {requisition.submittedDate || (isDraft ? "Awaiting Submission" : "—")}
                </span>
                <span className="text-[11px] text-gray-500 dark:text-gray-400 mt-1">
                  {isDraft ? "Submit for management review" : "In review queue"}
                </span>
              </div>

              {/* Stage Connector Arrow */}
              <div className="hidden md:flex absolute -right-3 top-1/2 -translate-y-1/2 z-10 size-6 items-center justify-center rounded-full border border-gray-200 bg-white shadow-xs dark:border-white/[0.1] dark:bg-gray-800 text-gray-400">
                <svg className="size-3" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2.5} d="M9 5l7 7-7 7" />
                </svg>
              </div>
            </div>

            {/* ================= STAGE 3: MANAGER REVIEW ================= */}
            <div
              className={`relative rounded-2xl border p-4 shadow-sm transition-all hover:shadow-md ${isApproved
                  ? "border-emerald-200/90 bg-gradient-to-br from-emerald-50/90 to-teal-50/30 dark:border-emerald-500/30 dark:from-emerald-950/30 dark:to-teal-900/10"
                  : isRejected
                    ? "border-red-300 bg-gradient-to-br from-red-50/90 to-rose-50/40 ring-2 ring-red-500/20 dark:border-red-500/40 dark:from-red-950/40 dark:to-rose-900/20"
                    : isPending
                      ? "border-amber-300 bg-gradient-to-br from-amber-50/90 to-orange-50/40 ring-2 ring-amber-500/20 dark:border-amber-500/40 dark:from-amber-950/40 dark:to-orange-900/20"
                      : "border-gray-200/70 bg-gray-50/60 dark:border-white/[0.06] dark:bg-white/[0.02] opacity-70"
                }`}
            >
              <div className="flex items-center justify-between mb-2">
                <div
                  className={`size-8 rounded-xl flex items-center justify-center text-white shadow-sm ${isApproved
                      ? "bg-emerald-500 shadow-emerald-500/40"
                      : isRejected
                        ? "bg-red-500 shadow-red-500/40"
                        : isPending
                          ? "bg-amber-500 shadow-amber-500/40 ring-4 ring-amber-500/20 animate-pulse"
                          : "bg-gray-300 dark:bg-gray-700 text-gray-500 dark:text-gray-400"
                    }`}
                >
                  {isApproved ? (
                    <svg className="size-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2.5} d="M5 13l4 4L19 7" />
                    </svg>
                  ) : isRejected ? (
                    <svg className="size-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2.5} d="M6 18L18 6M6 6l12 12" />
                    </svg>
                  ) : isPending ? (
                    <svg className="size-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2.5} d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
                    </svg>
                  ) : (
                    <svg className="size-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                    </svg>
                  )}
                </div>
                <span
                  className={`inline-flex items-center px-2 py-0.5 rounded-full text-[10px] font-bold ${isApproved
                      ? "bg-emerald-100 text-emerald-800 dark:bg-emerald-500/20 dark:text-emerald-300"
                      : isRejected
                        ? "bg-red-100 text-red-800 dark:bg-red-500/20 dark:text-red-300"
                        : isPending
                          ? "bg-amber-100 text-amber-800 dark:bg-amber-500/20 dark:text-amber-300 animate-pulse"
                          : "bg-gray-100 text-gray-500 dark:bg-gray-800 dark:text-gray-400"
                    }`}
                >
                  {isApproved
                    ? "Approved"
                    : isRejected
                      ? "Rejected"
                      : isPending
                        ? "Under Review"
                        : "Upcoming"}
                </span>
              </div>
              <div className="flex flex-col">
                <span
                  className={`text-[11px] font-bold uppercase tracking-wide ${isApproved
                      ? "text-emerald-800 dark:text-emerald-300"
                      : isRejected
                        ? "text-red-800 dark:text-red-300"
                        : isPending
                          ? "text-amber-800 dark:text-amber-300"
                          : "text-gray-500 dark:text-gray-400"
                    }`}
                >
                  3. Manager Review
                </span>
                <span className="text-sm font-extrabold text-gray-900 dark:text-white mt-0.5 truncate">
                  {isApproved
                    ? requisition.approvedDate || "Approved"
                    : isRejected
                      ? "Decision: Rejected"
                      : isPending
                        ? "Pending Decision"
                        : "Pending Submission"}
                </span>
                <span className="text-[11px] text-gray-500 dark:text-gray-400 mt-1 truncate">
                  {requisition.approverName
                    ? `By: ${requisition.approverName}`
                    : isPending
                      ? "Requires approval"
                      : "Not yet reviewed"}
                </span>
              </div>

              {/* Stage Connector Arrow */}
              <div className="hidden md:flex absolute -right-3 top-1/2 -translate-y-1/2 z-10 size-6 items-center justify-center rounded-full border border-gray-200 bg-white shadow-xs dark:border-white/[0.1] dark:bg-gray-800 text-gray-400">
                <svg className="size-3" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2.5} d="M9 5l7 7-7 7" />
                </svg>
              </div>
            </div>

            {/* ================= STAGE 4: PURCHASE ORDER CONVERSION ================= */}
            <div
              className={`relative rounded-2xl border p-4 shadow-sm transition-all hover:shadow-md ${isConverted
                  ? "border-purple-300 bg-gradient-to-br from-purple-50/90 to-indigo-50/50 ring-2 ring-purple-500/20 dark:border-purple-500/40 dark:from-purple-950/40 dark:to-indigo-900/20"
                  : isApproved
                    ? "border-purple-200 bg-purple-50/40 dark:border-purple-500/20 dark:bg-purple-950/10"
                    : "border-gray-200/70 bg-gray-50/60 dark:border-white/[0.06] dark:bg-white/[0.02] opacity-70"
                }`}
            >
              <div className="flex items-center justify-between mb-2">
                <div
                  className={`size-8 rounded-xl flex items-center justify-center text-white shadow-sm ${isConverted
                      ? "bg-gradient-to-r from-purple-600 to-indigo-600 shadow-purple-500/40"
                      : isApproved
                        ? "bg-purple-500 shadow-purple-500/40 ring-4 ring-purple-500/20 animate-pulse"
                        : "bg-gray-300 dark:bg-gray-700 text-gray-500 dark:text-gray-400"
                    }`}
                >
                  {isConverted ? (
                    <svg className="size-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M16 11V7a4 4 0 00-8 0v4M5 9h14l1 12H4L5 9z" />
                    </svg>
                  ) : isApproved ? (
                    <svg className="size-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v16m8-8H4" />
                    </svg>
                  ) : (
                    <svg className="size-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" />
                    </svg>
                  )}
                </div>
                <span
                  className={`inline-flex items-center px-2 py-0.5 rounded-full text-[10px] font-bold ${isConverted
                      ? "bg-purple-100 text-purple-800 dark:bg-purple-500/20 dark:text-purple-300"
                      : isApproved
                        ? "bg-purple-100 text-purple-700 dark:bg-purple-500/20 dark:text-purple-300 animate-pulse"
                        : "bg-gray-100 text-gray-500 dark:bg-gray-800 dark:text-gray-400"
                    }`}
                >
                  {isConverted
                    ? "Converted"
                    : isApproved
                      ? "Ready to Convert"
                      : "Not Converted"}
                </span>
              </div>
              <div className="flex flex-col">
                <span
                  className={`text-[11px] font-bold uppercase tracking-wide ${isConverted
                      ? "text-purple-800 dark:text-purple-300"
                      : isApproved
                        ? "text-purple-700 dark:text-purple-300"
                        : "text-gray-500 dark:text-gray-400"
                    }`}
                >
                  4. Purchase Order
                </span>
                <span className="text-sm font-extrabold text-gray-900 dark:text-white mt-0.5 truncate">
                  {isConverted && requisition.purchaseOrderCode
                    ? requisition.purchaseOrderCode
                    : isApproved
                      ? "Ready for PO"
                      : "Pending Approval"}
                </span>
                <span className="text-[11px] text-gray-500 dark:text-gray-400 mt-1 truncate">
                  {requisition.convertedDate
                    ? `Date: ${requisition.convertedDate}`
                    : isApproved
                      ? "1-click conversion available"
                      : "Locked"}
                </span>
              </div>
            </div>
          </div>

          {/* Rejection reason banner if rejected */}
          {isRejected && requisition.rejectionReason && (
            <div className="mt-4 p-4 rounded-2xl bg-gradient-to-r from-red-50 to-rose-50/60 text-red-900 border border-red-200 text-xs dark:bg-red-500/15 dark:text-red-300 dark:border-red-500/30 flex items-start gap-3">
              <div className="size-6 rounded-lg bg-red-500 text-white flex items-center justify-center flex-shrink-0 mt-0.5">
                <svg className="size-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2.5} d="M6 18L18 6M6 6l12 12" />
                </svg>
              </div>
              <div>
                <strong className="font-bold text-red-950 dark:text-red-200">Rejection Reason:</strong>{" "}
                <span>{requisition.rejectionReason}</span>
              </div>
            </div>
          )}

          {/* Cancellation details banner if cancelled */}
          {isCancelled && (
            <div className="mt-4 p-4 rounded-2xl bg-gradient-to-r from-gray-100 to-slate-100 text-gray-800 border border-gray-300 text-xs dark:bg-gray-800/60 dark:text-gray-200 dark:border-gray-700 flex items-start gap-3">
              <div className="size-6 rounded-lg bg-gray-600 text-white flex items-center justify-center flex-shrink-0 mt-0.5">
                <svg className="size-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M18.364 18.364A9 9 0 005.636 5.636m12.728 12.728A9 9 0 015.636 5.636m12.728 12.728L5.636 5.636" />
                </svg>
              </div>
              <div className="flex flex-col gap-0.5">
                <div>
                  <strong className="font-bold text-gray-900 dark:text-white">Requisition Cancelled:</strong>{" "}
                  <span>{requisition.cancellationReason || "No cancellation reason provided."}</span>
                </div>
                {requisition.cancelledDate && (
                  <span className="text-[11px] text-gray-500 dark:text-gray-400">
                    Cancelled on: {requisition.cancelledDate}
                  </span>
                )}
              </div>
            </div>
          )}

          {/* Approval notes if present */}
          {requisition.approvalNotes && (
            <div className="mt-4 p-4 rounded-2xl bg-gradient-to-r from-emerald-50 to-teal-50/60 text-emerald-900 border border-emerald-200 text-xs dark:bg-emerald-500/15 dark:text-emerald-300 dark:border-emerald-500/30 flex items-start gap-3">
              <div className="size-6 rounded-lg bg-emerald-500 text-white flex items-center justify-center flex-shrink-0 mt-0.5">
                <svg className="size-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2.5} d="M5 13l4 4L19 7" />
                </svg>
              </div>
              <div>
                <strong className="font-bold text-emerald-950 dark:text-emerald-200">Approval Notes:</strong>{" "}
                <span>{requisition.approvalNotes}</span>
              </div>
            </div>
          )}
        </div>
      </div>

      {/* Summary KPI Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        {/* Total Value */}
        <div className="rounded-2xl border border-gray-200 bg-white p-5 shadow-sm dark:border-white/[0.07] dark:bg-gray-900">
          <span className="text-xs font-medium text-gray-500 dark:text-gray-400">
            Total Estimated Amount
          </span>
          <div className="mt-2 text-xl font-bold text-gray-900 dark:text-white">
            {formatAmount(requisition.totalAmount, requisition.currencyCode)}
          </div>
          <span className="text-[11px] text-gray-400 mt-1 block">
            Currency: {requisition.currencyCode || "MAD"}
          </span>
        </div>

        {/* Line Items Count */}
        <div className="rounded-2xl border border-gray-200 bg-white p-5 shadow-sm dark:border-white/[0.07] dark:bg-gray-900">
          <span className="text-xs font-medium text-gray-500 dark:text-gray-400">
            Total Line Items
          </span>
          <div className="mt-2 text-xl font-bold text-gray-900 dark:text-white">
            {requisition.lines?.length || 0}
          </div>
          <span className="text-[11px] text-gray-400 mt-1 block">
            Procurement lines requested
          </span>
        </div>

        {/* Requester */}
        <div className="rounded-2xl border border-gray-200 bg-white p-5 shadow-sm dark:border-white/[0.07] dark:bg-gray-900">
          <span className="text-xs font-medium text-gray-500 dark:text-gray-400">
            Requested By
          </span>
          <div className="mt-2 text-base font-bold text-gray-900 dark:text-white truncate">
            {requisition.requesterName}
          </div>
          <span className="text-[11px] text-gray-400 mt-1 block">
            ID: {requisition.requesterId}
          </span>
        </div>

        {/* Need-By Date */}
        <div className="rounded-2xl border border-gray-200 bg-white p-5 shadow-sm dark:border-white/[0.07] dark:bg-gray-900">
          <span className="text-xs font-medium text-gray-500 dark:text-gray-400">
            Required Date
          </span>
          <div className="mt-2 text-base font-bold text-gray-900 dark:text-white">
            {requisition.requiredDate || "Not specified"}
          </div>
          <span className="text-[11px] text-gray-400 mt-1 block">
            Target delivery date
          </span>
        </div>
      </div>

      {/* Business Justification & Requisition Scope Section */}
      <div className="rounded-2xl border border-gray-200 bg-white p-6 shadow-sm dark:border-white/[0.07] dark:bg-gray-900">
        <div className="flex items-center gap-2.5 mb-4 pb-3 border-b border-gray-100 dark:border-white/[0.07]">
          <div className="size-8 rounded-xl bg-brand-50 text-brand-600 dark:bg-brand-500/15 dark:text-brand-400 flex items-center justify-center">
            <svg className="size-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
            </svg>
          </div>
          <div>
            <h3 className="text-sm font-bold text-gray-900 dark:text-white">
              Business Justification & Operational Context
            </h3>
            <p className="text-xs text-gray-400">
              Procurement rationale, project alignment, and requirements
            </p>
          </div>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {/* Business Justification Box */}
          <div className="rounded-xl border border-brand-200/80 bg-brand-50/40 p-4 dark:border-brand-500/20 dark:bg-brand-500/5">
            <div className="flex items-center gap-1.5 text-xs font-bold uppercase tracking-wider text-brand-700 dark:text-brand-300 mb-2">
              <svg className="size-4 text-brand-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
              Business Justification
            </div>
            {requisition.justification ? (
              <p className="text-xs text-gray-800 dark:text-gray-200 leading-relaxed whitespace-pre-wrap font-medium">
                {requisition.justification}
              </p>
            ) : (
              <p className="text-xs text-gray-400 italic">
                No business justification recorded for this requisition.
              </p>
            )}
          </div>

          {/* Description Box */}
          <div className="rounded-xl border border-gray-200/70 bg-gray-50/60 p-4 dark:border-white/[0.05] dark:bg-white/[0.02]">
            <div className="flex items-center gap-1.5 text-xs font-bold uppercase tracking-wider text-gray-700 dark:text-gray-300 mb-2">
              <svg className="size-4 text-gray-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 6h16M4 12h16M4 18h7" />
              </svg>
              Requisition Description
            </div>
            {requisition.description ? (
              <p className="text-xs text-gray-800 dark:text-gray-200 leading-relaxed whitespace-pre-wrap">
                {requisition.description}
              </p>
            ) : (
              <p className="text-xs text-gray-400 italic">
                No additional description provided.
              </p>
            )}
          </div>
        </div>
      </div>

      {/* Line Items Table Card */}
      <div className="rounded-2xl border border-gray-200 bg-white shadow-sm dark:border-white/[0.07] dark:bg-gray-900 overflow-hidden">
        <div className="p-5 border-b border-gray-100 dark:border-white/[0.07] flex items-center justify-between">
          <div>
            <h3 className="text-sm font-bold text-gray-900 dark:text-white">
              Line Items Breakdown
            </h3>
            <p className="text-xs text-gray-400">
              Detailed specifications, quantities, and pricing for each item
            </p>
          </div>
          <span className="text-xs font-semibold px-2.5 py-1 rounded-full bg-gray-100 text-gray-700 dark:bg-gray-800 dark:text-gray-300">
            {requisition.lines?.length || 0} items
          </span>
        </div>

        <div className="overflow-x-auto">
          <Table>
            <TableHeader className="bg-gray-50/75 dark:bg-white/[0.02]">
              <TableRow className="border-b border-gray-100 dark:border-white/[0.05]">
                <TableCell isHeader className="px-4 py-3 text-xs font-semibold text-gray-600 dark:text-gray-300">
                  #
                </TableCell>
                <TableCell isHeader className="px-4 py-3 text-xs font-semibold text-gray-600 dark:text-gray-300">
                  Material
                </TableCell>
                <TableCell isHeader className="px-4 py-3 text-xs font-semibold text-gray-600 dark:text-gray-300">
                  Quantity & UOM
                </TableCell>
                <TableCell isHeader className="px-4 py-3 text-xs font-semibold text-gray-600 dark:text-gray-300">
                  Est. Unit Price
                </TableCell>
                <TableCell isHeader className="px-4 py-3 text-xs font-semibold text-gray-600 dark:text-gray-300">
                  Line Total
                </TableCell>
                <TableCell isHeader className="px-4 py-3 text-xs font-semibold text-gray-600 dark:text-gray-300">
                  Supplier / Terms
                </TableCell>
                <TableCell isHeader className="px-4 py-3 text-xs font-semibold text-gray-600 dark:text-gray-300">
                  Storage Location
                </TableCell>
              </TableRow>
            </TableHeader>

            <TableBody>
              {(!requisition.lines || requisition.lines.length === 0) ? (
                <TableRow>
                  <TableCell className="px-4 py-8 text-center text-xs text-gray-500" colSpan={7}>
                    No line items attached to this purchase requisition.
                  </TableCell>
                </TableRow>
              ) : (
                requisition.lines.map((line, idx) => (
                  <TableRow
                    key={line.id || idx}
                    className="border-b border-gray-100 hover:bg-gray-50/50 dark:border-white/[0.05] dark:hover:bg-white/[0.02]"
                  >
                    <TableCell className="px-4 py-3.5 text-xs text-gray-400 font-mono">
                      {line.lineNumber || idx + 1}
                    </TableCell>
                    <TableCell className="px-4 py-3.5">
                      <div className="flex flex-col">
                        <span className="text-xs font-bold text-brand-600 dark:text-brand-400">
                          {line.materialCode}
                        </span>
                        <span className="text-xs font-medium text-gray-900 dark:text-white">
                          {line.materialName}
                        </span>
                        {line.materialDescription && (
                          <span className="text-[10px] text-gray-400 max-w-sm line-clamp-1">
                            {line.materialDescription}
                          </span>
                        )}
                        {line.notes && (
                          <span className="text-[10px] text-amber-600 dark:text-amber-400 italic mt-0.5">
                            Note: {line.notes}
                          </span>
                        )}
                      </div>
                    </TableCell>
                    <TableCell className="px-4 py-3.5">
                      <span className="text-xs font-semibold text-gray-900 dark:text-white">
                        {line.quantity} {line.unitOfMeasure || "PCS"}
                      </span>
                    </TableCell>
                    <TableCell className="px-4 py-3.5 text-xs text-gray-600 dark:text-gray-300">
                      {formatAmount(line.unitPrice, line.currencyCodeLine || line.currencyCode || requisition.currencyCode)}
                    </TableCell>
                    <TableCell className="px-4 py-3.5 text-xs font-bold text-gray-900 dark:text-white">
                      {formatAmount(line.lineTotal, line.currencyCodeLine || line.currencyCode || requisition.currencyCode)}
                    </TableCell>
                    <TableCell className="px-4 py-3.5">
                      <div className="flex flex-col text-xs text-gray-600 dark:text-gray-400">
                        <span>{line.supplierName || line.supplierCode || "Not specified"}</span>
                        {line.deliveryTerms && (
                          <span className="text-[10px] text-gray-400">
                            Terms: {line.deliveryTerms}
                          </span>
                        )}
                      </div>
                    </TableCell>
                    <TableCell className="px-4 py-3.5 text-xs text-gray-600 dark:text-gray-400">
                      {line.storageLocation || "—"}
                    </TableCell>
                  </TableRow>
                ))
              )}
            </TableBody>
          </Table>
        </div>
      </div>

      {/* Approval / Rejection Modal */}
      {modalState.isOpen && (
        <RequisitionApprovalModal
          isOpen={modalState.isOpen}
          mode={modalState.mode}
          requisition={requisition}
          onClose={() => setModalState({ isOpen: false, mode: "APPROVE" })}
          onConfirm={handleApprovalDecision}
          isLoading={actionLoading}
        />
      )}

      {/* Delete Confirmation Modal */}
      {showDeleteModal && (
        <DeleteConfirmModal
          isOpen={showDeleteModal}
          onClose={() => setShowDeleteModal(false)}
          onConfirm={handleDeleteConfirm}
          title="Delete Purchase Requisition"
          message={`Are you sure you want to delete purchase requisition ${requisition.requisitionCode} ("${requisition.title}")? This action cannot be undone.`}
          isDeleting={isDeleting}
        />
      )}

      {/* Cancel Confirmation Modal */}
      {showCancelModal && (
        <RequisitionCancelModal
          isOpen={showCancelModal}
          onClose={() => setShowCancelModal(false)}
          requisition={requisition}
          onConfirm={handleCancelRequisition}
          isLoading={actionLoading}
        />
      )}
    </div>
  );
}
