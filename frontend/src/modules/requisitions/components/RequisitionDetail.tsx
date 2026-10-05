import { useEffect, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import { requisitionApi } from "../services/requisitionApi";
import purchaseOrderService from "../../purchaseOrders/services/purchaseOrderService";
import useAuth from "../../auth/hooks/useAuth";
import { isOwnRequisition } from "../utils/requisitionOwnership";
import { toPurchaseOrderRequest, type OrderSupplier } from "../utils/convertToPurchaseOrder";
import type { Requisition } from "../types";
import RequisitionStatusBadge from "./RequisitionStatusBadge";
import RequisitionApprovalModal from "./RequisitionApprovalModal";
import RequisitionCancelModal from "./RequisitionCancelModal";
import RequisitionConvertToPoModal from "./RequisitionConvertToPoModal";
import { formatAmount } from "../../invoices/utils/invoiceLine";
import Button from "../../../shared/components/ui/button/Button";
import DeleteConfirmModal from "../../../shared/components/ui/modal/DeleteConfirmModal";
import { Table, TableBody, TableCell, TableHeader, TableRow } from "../../../shared/components/ui/table";
import {
  BackToListButton,
  DetailCard,
  DetailField,
  DetailGrid,
  DetailHeader,
  FloatingToast,
  PageLoader,
  PageNotFound,
} from "../../../shared/components/page/DetailParts";
import { InitialsAvatar, StackedCell } from "../../../shared/components/page/ListParts";
import { SectionIcons } from "../../../shared/components/page/pageIcons";
import { BODY_CELL, HEAD_CELL } from "../../../shared/components/page/pageStyles";
import { getApiErrorMessage } from "../../../shared/utils/apiError";

type Feedback = { type: "success" | "error"; text: string };
type StepState = "done" | "current" | "failed" | "todo";

const formatDate = (value?: string | null) => (value ? new Date(value).toLocaleDateString() : null);
const formatDateTime = (value?: string | null) => (value ? new Date(value).toLocaleString() : "—");

const STEP_STYLES: Record<StepState, string> = {
  done: "bg-success-500 text-white",
  current: "bg-brand-500 text-white ring-4 ring-brand-500/20",
  failed: "bg-error-500 text-white",
  todo: "bg-gray-200 text-gray-500 dark:bg-gray-800 dark:text-gray-400",
};

/** Created, submitted, reviewed, ordered: where the requisition is in its lifecycle. */
function Progress({ requisition }: { requisition: Requisition }) {
  const s = requisition.status;
  const reviewed = s === "APPROVED" || s === "CONVERTED";
  const steps: { label: string; date: string | null; state: StepState; note?: string }[] = [
    { label: "Created", date: formatDate(requisition.createdAt), state: "done" },
    {
      label: "Submitted",
      date: formatDate(requisition.submittedDate),
      state: s === "DRAFT" ? "current" : requisition.submittedDate || s !== "CANCELLED" ? "done" : "todo",
    },
    {
      label: s === "REJECTED" ? "Rejected" : "Approved",
      date: formatDate(requisition.approvedDate),
      state: reviewed ? "done" : s === "REJECTED" ? "failed" : s === "SUBMITTED" || s === "UNDER_REVIEW" ? "current" : "todo",
      note: requisition.approverName || undefined,
    },
    {
      label: "Ordered",
      date: formatDate(requisition.convertedDate),
      state: s === "CONVERTED" ? "done" : s === "APPROVED" ? "current" : "todo",
      note: requisition.purchaseOrderCode || undefined,
    },
  ];

  return (
    <DetailCard title="Progress" icon={SectionIcons.check} tone={s === "REJECTED" || s === "CANCELLED" ? "error" : "brand"}>
      {s === "CANCELLED" && (
        <p className="mb-4 text-sm text-error-500">
          Cancelled{requisition.cancelledDate ? ` on ${formatDate(requisition.cancelledDate)}` : ""}
          {requisition.cancellationReason ? `: ${requisition.cancellationReason}` : "."}
        </p>
      )}
      <ol className="grid grid-cols-2 gap-4 sm:grid-cols-4">
        {steps.map((step, i) => (
          <li key={step.label} className="flex items-start gap-3">
            <span className={`flex h-8 w-8 shrink-0 items-center justify-center rounded-full text-sm font-semibold ${STEP_STYLES[step.state]}`}>
              {step.state === "done" ? "✓" : step.state === "failed" ? "✕" : i + 1}
            </span>
            <div>
              <p className="text-sm font-medium text-gray-800 dark:text-white/90">{step.label}</p>
              <p className="text-theme-xs text-gray-500 dark:text-gray-400">{step.date ?? (step.state === "current" ? "In progress" : "—")}</p>
              {step.note && <p className="text-theme-xs text-gray-500 dark:text-gray-400">{step.note}</p>}
            </div>
          </li>
        ))}
      </ol>
    </DetailCard>
  );
}

export default function RequisitionDetail() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { user, hasPermission } = useAuth();
  const canWrite = hasPermission("requisition:write");
  const canValidate = hasPermission("requisition:validate");
  const canConvertPermission = hasPermission("requisition:convert");

  const [requisition, setRequisition] = useState<Requisition | null>(null);
  const [loading, setLoading] = useState(true);
  const [loadError, setLoadError] = useState<string | null>(null);
  const [actionLoading, setActionLoading] = useState(false);
  const [feedback, setFeedback] = useState<Feedback | null>(null);
  const [decision, setDecision] = useState<"APPROVE" | "REJECT" | null>(null);
  const [showCancel, setShowCancel] = useState(false);
  const [showConvert, setShowConvert] = useState(false);
  const [showDelete, setShowDelete] = useState(false);
  const [isDeleting, setIsDeleting] = useState(false);

  useEffect(() => {
    if (!id) return;
    let cancelled = false;
    requisitionApi
      .getById(id)
      .then((res) => {
        if (!cancelled) setRequisition(res.data);
      })
      .catch((err) => {
        if (!cancelled) setLoadError(getApiErrorMessage(err, "The requisition you requested does not exist."));
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });
    return () => {
      cancelled = true;
    };
  }, [id]);

  const runAction = async (action: () => Promise<{ data: Requisition }>, success: (r: Requisition) => string, failure: string) => {
    try {
      setActionLoading(true);
      const res = await action();
      setRequisition(res.data);
      setFeedback({ type: "success", text: success(res.data) });
      return true;
    } catch (err) {
      setFeedback({ type: "error", text: getApiErrorMessage(err, failure) });
      return false;
    } finally {
      setActionLoading(false);
    }
  };

  const approverId = user?.id || "";
  const approverName = user?.name || user?.email || "";

  const handleSubmit = () =>
    requisition &&
    runAction(() => requisitionApi.submit(requisition.id), (r) => `Requisition ${r.requisitionCode} was submitted for approval.`, "Submitting the requisition failed.");

  const handleDecision = async (notesOrReason: string) => {
    if (!requisition || !decision) return;
    const approve = decision === "APPROVE";
    const done = await runAction(
      () =>
        approve
          ? requisitionApi.approve(requisition.id, approverId, approverName, notesOrReason)
          : requisitionApi.reject(requisition.id, notesOrReason, approverId, approverName),
      (r) => `Requisition ${r.requisitionCode} was ${approve ? "approved" : "rejected"}.`,
      approve ? "Approving the requisition failed." : "Rejecting the requisition failed."
    );
    if (done) setDecision(null);
  };

  const handleCancel = async (reason: string) => {
    if (!requisition) return;
    const res = await requisitionApi.cancel(requisition.id, undefined, reason);
    setRequisition(res.data);
    setFeedback({ type: "success", text: `Requisition ${res.data.requisitionCode} was cancelled.` });
  };

  const handleConvert = async (supplier: OrderSupplier) => {
    if (!requisition) return;
    try {
      setActionLoading(true);
      const created = await purchaseOrderService.create(toPurchaseOrderRequest(requisition, supplier, user));
      setRequisition((await requisitionApi.getById(requisition.id)).data);
      setShowConvert(false);
      setFeedback({ type: "success", text: `Purchase order ${created.data.orderCode} was created from this requisition.` });
    } catch (err) {
      setFeedback({ type: "error", text: getApiErrorMessage(err, "Creating the purchase order failed.") });
    } finally {
      setActionLoading(false);
    }
  };

  const handleDelete = async () => {
    if (!requisition) return;
    try {
      setIsDeleting(true);
      await requisitionApi.delete(requisition.id);
      navigate("/requisitions");
    } catch (err) {
      setFeedback({ type: "error", text: getApiErrorMessage(err, "Deleting the requisition failed.") });
      setShowDelete(false);
      setIsDeleting(false);
    }
  };

  if (loading) return <PageLoader message="Loading requisition..." />;
  if (!requisition) {
    return (
      <PageNotFound
        title="Requisition Not Found"
        message={loadError || "The requisition you requested does not exist."}
        backTo="/requisitions"
        backLabel="Back to Requisitions"
      />
    );
  }

  const s = requisition.status;
  const isPending = s === "SUBMITTED" || s === "UNDER_REVIEW";
  const own = isOwnRequisition(requisition, user?.id);
  const canEdit = canWrite && (s === "DRAFT" || isPending);
  const canDelete = canWrite && (s === "DRAFT" || s === "REJECTED" || s === "CANCELLED");
  const canSubmit = canWrite && s === "DRAFT";
  const canDecide = canValidate && isPending && !own;
  const canConvert = canConvertPermission && s === "APPROVED";
  const canCancel = canWrite && (isPending || s === "APPROVED");
  const lines = requisition.lines || [];
  const currency = requisition.currencyCode;

  return (
    <>
      <FloatingToast feedback={feedback} onClose={() => setFeedback(null)} />

      <DetailHeader title={requisition.requisitionCode} parentName="Requisitions" parentUrl="/requisitions">
        <BackToListButton to="/requisitions" />
        <Button size="sm" variant="outline" onClick={() => window.print()}>
          Print
        </Button>
        {canDelete && (
          <Button size="sm" variant="outline" onClick={() => setShowDelete(true)} disabled={actionLoading}>
            Delete
          </Button>
        )}
        {canCancel && (
          <Button size="sm" variant="outline" onClick={() => setShowCancel(true)} disabled={actionLoading}>
            Cancel Requisition
          </Button>
        )}
        {canEdit && (
          <Link to={`/requisitions/edit/${requisition.id}`}>
            <Button size="sm" variant="outline" disabled={actionLoading}>
              Edit
            </Button>
          </Link>
        )}
        {canDecide && (
          <Button size="sm" variant="outline" onClick={() => setDecision("REJECT")} disabled={actionLoading}>
            Reject
          </Button>
        )}
        {canDecide && (
          <Button size="sm" onClick={() => setDecision("APPROVE")} disabled={actionLoading}>
            Approve
          </Button>
        )}
        {canSubmit && (
          <Button size="sm" onClick={handleSubmit} disabled={actionLoading}>
            Submit for Approval
          </Button>
        )}
        {canConvert && (
          <Button size="sm" onClick={() => setShowConvert(true)} disabled={actionLoading}>
            Create Purchase Order
          </Button>
        )}
      </DetailHeader>

      {canValidate && isPending && own && (
        <p className="mb-6 rounded-xl border border-warning-200 bg-warning-50 px-4 py-3 text-sm text-warning-700 dark:border-warning-500/30 dark:bg-warning-500/10 dark:text-orange-300">
          You requested this purchase, so another approver must review it.
        </p>
      )}

      <div className="grid grid-cols-1 gap-6 lg:grid-cols-3">
        <div className="flex flex-col gap-6 lg:col-span-1">
          <DetailCard>
            <div className="mb-6 flex items-center justify-between">
              <InitialsAvatar name={requisition.requesterName} size="lg" />
              <RequisitionStatusBadge status={s} size="md" />
            </div>
            <h3 className="mb-1 font-mono text-xl font-bold text-gray-900 dark:text-white">{requisition.requisitionCode}</h3>
            <p className="mb-6 text-sm font-medium text-gray-500 dark:text-gray-400">{requisition.title}</p>
            <div className="space-y-4">
              <DetailField label="Requester">{requisition.requesterName}</DetailField>
              <DetailField label="Needed By">{requisition.requiredDate || "—"}</DetailField>
              <DetailField label="Estimated Total">
                <span className="font-semibold">{formatAmount(requisition.totalAmount, currency)}</span>
              </DetailField>
              <DetailField label="Purchase Order">
                {requisition.purchaseOrderId ? (
                  <Link to={`/purchase-orders/${requisition.purchaseOrderId}`} className="font-mono text-brand-500 hover:underline">
                    {requisition.purchaseOrderCode || requisition.purchaseOrderId}
                  </Link>
                ) : (
                  "—"
                )}
              </DetailField>
            </div>
          </DetailCard>

          {(requisition.description || requisition.justification) && (
            <DetailCard title="Purpose" icon={SectionIcons.note}>
              <div className="space-y-4">
                {requisition.description && <DetailField label="Description">{requisition.description}</DetailField>}
                {requisition.justification && <DetailField label="Justification">{requisition.justification}</DetailField>}
              </div>
            </DetailCard>
          )}
        </div>

        <div className="flex flex-col gap-6 lg:col-span-2">
          <Progress requisition={requisition} />

          <DetailCard title={`Requested Lines (${lines.length})`} icon={SectionIcons.list} tone="brand" padded={false}>
            <div className="max-w-full overflow-x-auto">
              <Table>
                <TableHeader className="border-b border-gray-100 bg-gray-50/50 dark:border-white/[0.05] dark:bg-gray-900/50">
                  <TableRow>
                    <TableCell isHeader className={HEAD_CELL}>Material</TableCell>
                    <TableCell isHeader className={HEAD_CELL}>Quantity</TableCell>
                    <TableCell isHeader className={HEAD_CELL}>Unit Price</TableCell>
                    <TableCell isHeader className={HEAD_CELL}>Line Total</TableCell>
                    <TableCell isHeader className={HEAD_CELL}>Supplier</TableCell>
                    <TableCell isHeader className={HEAD_CELL}>Needed By</TableCell>
                  </TableRow>
                </TableHeader>
                <TableBody className="divide-y divide-gray-100 dark:divide-white/[0.05]">
                  {lines.map((l, i) => (
                    <TableRow key={l.id || i}>
                      <TableCell className={BODY_CELL}>
                        <StackedCell main={<span className="font-mono">{l.materialCode}</span>} sub={l.materialName} />
                      </TableCell>
                      <TableCell className={BODY_CELL}>{`${l.quantity} ${l.unitOfMeasure ?? ""}`.trim()}</TableCell>
                      <TableCell className={`${BODY_CELL} whitespace-nowrap`}>{formatAmount(l.unitPrice, l.currencyCode || currency)}</TableCell>
                      <TableCell className={`${BODY_CELL} whitespace-nowrap`}>{formatAmount(l.lineTotal, l.currencyCode || currency)}</TableCell>
                      <TableCell className={BODY_CELL}>{l.supplierName || "—"}</TableCell>
                      <TableCell className={BODY_CELL}>{l.requiredDate || requisition.requiredDate || "—"}</TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </div>
          </DetailCard>

          <DetailCard title="Review" icon={SectionIcons.info}>
            <DetailGrid>
              <DetailField label="Approver">{requisition.approverName || "Not reviewed yet"}</DetailField>
              <DetailField label="Reviewed On">{formatDate(requisition.approvedDate) ?? "—"}</DetailField>
              {requisition.approvalNotes && (
                <DetailField label="Approval Notes" wide>
                  {requisition.approvalNotes}
                </DetailField>
              )}
              {requisition.rejectionReason && (
                <DetailField label="Rejection Reason" wide>
                  <span className="text-error-500">{requisition.rejectionReason}</span>
                </DetailField>
              )}
            </DetailGrid>
          </DetailCard>

          <DetailCard title="System Information" icon={SectionIcons.info}>
            <DetailGrid>
              <DetailField label="Created">{formatDateTime(requisition.createdAt)}</DetailField>
              <DetailField label="Created By">{requisition.createdBy || "—"}</DetailField>
              <DetailField label="Last Updated">{formatDateTime(requisition.updatedAt)}</DetailField>
              <DetailField label="Updated By">{requisition.updatedBy || "—"}</DetailField>
            </DetailGrid>
          </DetailCard>
        </div>
      </div>

      {decision && (
        <RequisitionApprovalModal
          isOpen
          mode={decision}
          requisition={requisition}
          onClose={() => setDecision(null)}
          onConfirm={handleDecision}
          isLoading={actionLoading}
        />
      )}

      {showCancel && (
        <RequisitionCancelModal isOpen onClose={() => setShowCancel(false)} requisition={requisition} onConfirm={handleCancel} isLoading={actionLoading} />
      )}

      <RequisitionConvertToPoModal
        isOpen={showConvert}
        onClose={() => setShowConvert(false)}
        onConfirm={handleConvert}
        requisition={requisition}
        isConverting={actionLoading}
      />

      {showDelete && (
        <DeleteConfirmModal
          isOpen
          onClose={() => setShowDelete(false)}
          onConfirm={handleDelete}
          title="Delete Requisition"
          message={`Delete requisition ${requisition.requisitionCode} permanently?`}
          isDeleting={isDeleting}
        />
      )}
    </>
  );
}
