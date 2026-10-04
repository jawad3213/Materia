import { useEffect, useState } from "react";
import { Link, useLocation, useNavigate, useParams } from "react-router-dom";
import returnToVendorService from "../services/returnToVendorService";
import { RESOLUTION_TYPE_LABELS, type ResolutionType, type ReturnToVendor } from "../types/returnToVendor.types";
import ReturnStatusBadge, { ResolutionBadge } from "./ReturnStatusBadge";
import useReturnToVendorPermissions from "../hooks/useReturnToVendor";
import { formatAmount } from "../../invoices/utils/invoiceLine";
import Button from "../../../shared/components/ui/button/Button";
import Badge from "../../../shared/components/ui/badge/Badge";
import { Modal } from "../../../shared/components/ui/modal";
import Label from "../../../shared/components/form/Label";
import Input from "../../../shared/components/form/input/InputField";
import TextArea from "../../../shared/components/form/input/TextArea";
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
import { BODY_CELL, HEAD_CELL, SELECT_CLASS } from "../../../shared/components/page/pageStyles";
import { getApiErrorMessage } from "../../../shared/utils/apiError";

type Feedback = { type: "success" | "error"; text: string };

const formatDateTime = (value?: string | null) => (value ? new Date(value).toLocaleString() : "—");

/** What each resolution does to the chain, shown in the resolve dialog. */
const RESOLUTION_EFFECTS: Record<ResolutionType, string> = {
  REPLACEMENT: "The purchase order is reopened and the quantity goes back on order, so the receiver can receive the replacement on it.",
  CREDIT_NOTE: "The credit note is recorded at the value of the returned goods at purchase-order prices. The order stays closed.",
};

export default function ReturnDetail() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const location = useLocation();
  const permissions = useReturnToVendorPermissions();

  const [rtv, setRtv] = useState<ReturnToVendor | null>(null);
  const [loading, setLoading] = useState(true);
  const [loadError, setLoadError] = useState<string | null>(null);
  const [actionLoading, setActionLoading] = useState(false);
  const [feedback, setFeedback] = useState<Feedback | null>(() => {
    const state = location.state as { error?: string } | null;
    return state?.error ? { type: "error", text: state.error } : null;
  });
  const [showResolve, setShowResolve] = useState(false);
  const [resolutionType, setResolutionType] = useState<ResolutionType>("REPLACEMENT");
  const [reference, setReference] = useState("");
  const [supplierResponse, setSupplierResponse] = useState("");
  const [showCancel, setShowCancel] = useState(false);
  const [cancelReason, setCancelReason] = useState("");

  useEffect(() => {
    if (!id) return;
    let cancelled = false;
    returnToVendorService
      .getById(id)
      .then((res) => {
        if (!cancelled) setRtv(res.data);
      })
      .catch((err) => {
        if (!cancelled) setLoadError(getApiErrorMessage(err, "The return you requested does not exist."));
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });
    return () => {
      cancelled = true;
    };
  }, [id]);

  const runAction = async (action: () => Promise<{ data: ReturnToVendor }>, success: string) => {
    try {
      setActionLoading(true);
      const res = await action();
      setRtv(res.data);
      setFeedback({ type: "success", text: success });
      return true;
    } catch (err) {
      setFeedback({ type: "error", text: getApiErrorMessage(err, "The action failed.") });
      return false;
    } finally {
      setActionLoading(false);
    }
  };

  const handleSubmit = () =>
    rtv && runAction(() => returnToVendorService.submit(rtv.id), "Return marked as shipped: it now awaits the supplier.");

  const handleResolve = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!rtv || !reference.trim()) return;
    const done = await runAction(
      () =>
        returnToVendorService.resolve(rtv.id, {
          resolutionType,
          reference: reference.trim(),
          supplierResponse: supplierResponse.trim() || undefined,
        }),
      resolutionType === "REPLACEMENT"
        ? "Return resolved: the purchase order is reopened to receive the replacement."
        : "Return resolved: the credit note is recorded."
    );
    if (done) setShowResolve(false);
  };

  const handleCancel = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!rtv || !cancelReason.trim()) return;
    if (await runAction(() => returnToVendorService.cancel(rtv.id, cancelReason.trim()), "Return cancelled.")) {
      setShowCancel(false);
      setCancelReason("");
    }
  };

  const handleDelete = async () => {
    if (!rtv || !window.confirm(`Delete return ${rtv.returnCode}?`)) return;
    try {
      setActionLoading(true);
      await returnToVendorService.delete(rtv.id);
      navigate("/returns");
    } catch (err) {
      setFeedback({ type: "error", text: getApiErrorMessage(err, "The deletion failed.") });
      setActionLoading(false);
    }
  };

  if (loading) return <PageLoader message="Loading return..." />;
  if (!rtv) {
    return (
      <PageNotFound
        title="Return Not Found"
        message={loadError || "The return you requested does not exist."}
        backTo="/returns"
        backLabel="Back to Returns"
      />
    );
  }

  const currency = rtv.currencyCode;

  return (
    <>
      <FloatingToast feedback={feedback} onClose={() => setFeedback(null)} />

      <DetailHeader title={rtv.returnCode} parentName="Vendor Returns" parentUrl="/returns">
        <BackToListButton to="/returns" />
        {permissions.canDelete(rtv) && (
          <Button size="sm" variant="outline" onClick={handleDelete} disabled={actionLoading}>
            Delete
          </Button>
        )}
        {permissions.canCancel(rtv) && (
          <Button size="sm" variant="outline" onClick={() => setShowCancel(true)} disabled={actionLoading}>
            Cancel Return
          </Button>
        )}
        {permissions.canSubmit(rtv) && (
          <Button size="sm" onClick={handleSubmit} disabled={actionLoading}>
            Mark as Shipped
          </Button>
        )}
        {permissions.canResolve(rtv) && (
          <Button size="sm" onClick={() => setShowResolve(true)} disabled={actionLoading}>
            Resolve
          </Button>
        )}
      </DetailHeader>

      <div className="grid grid-cols-1 gap-6 lg:grid-cols-3">
        <div className="flex flex-col gap-6 lg:col-span-1">
          <DetailCard>
            <div className="mb-6 flex items-center justify-between">
              <InitialsAvatar name={rtv.supplierName} size="lg" />
              <div className="flex flex-col items-end gap-2">
                <ReturnStatusBadge status={rtv.status} size="md" />
                {rtv.resolutionType && <ResolutionBadge type={rtv.resolutionType} />}
              </div>
            </div>
            <h3 className="mb-1 font-mono text-xl font-bold text-gray-900 dark:text-white">{rtv.returnCode}</h3>
            <p className="mb-6 text-sm font-medium text-gray-500 dark:text-gray-400">{rtv.supplierName}</p>
            <div className="space-y-4">
              <DetailField label="Goods Receipt">
                <Link to={`/goods-receipts/${rtv.goodsReceiptId}`} className="font-mono text-brand-500 hover:underline">
                  {rtv.goodsReceiptCode || rtv.goodsReceiptId}
                </Link>
              </DetailField>
              <DetailField label="Purchase Order">
                {rtv.purchaseOrderId ? (
                  <Link to={`/purchase-orders/${rtv.purchaseOrderId}`} className="font-mono text-brand-500 hover:underline">
                    {rtv.purchaseOrderCode || rtv.purchaseOrderId}
                  </Link>
                ) : (
                  "—"
                )}
              </DetailField>
              <DetailField label="Return Reason">{rtv.returnReason}</DetailField>
            </div>
          </DetailCard>

          <DetailCard title="Dates" icon={SectionIcons.calendar}>
            <div className="space-y-4">
              <DetailField label="Return Date">{rtv.returnDate || "—"}</DetailField>
              <DetailField label="Resolved On">{rtv.resolutionDate || "—"}</DetailField>
            </div>
          </DetailCard>
        </div>

        <div className="flex flex-col gap-6 lg:col-span-2">
          <DetailCard title="Value" icon={SectionIcons.money} tone="success">
            <DetailGrid>
              <DetailField label="Units Returned">{rtv.totalQuantity ?? 0}</DetailField>
              <DetailField label="Value at Order Prices">{formatAmount(rtv.totalValue ?? 0, currency)}</DetailField>
            </DetailGrid>
          </DetailCard>

          <DetailCard
            title="Returned Lines"
            icon={SectionIcons.box}
            tone="brand"
            padded={false}
            aside={<Badge size="sm" color="light">{rtv.lines.length} line(s)</Badge>}
          >
            <div className="max-w-full overflow-x-auto">
              <Table>
                <TableHeader className="border-b border-gray-100 bg-gray-50/50 dark:border-white/[0.05] dark:bg-gray-900/50">
                  <TableRow>
                    <TableCell isHeader className={HEAD_CELL}>Material</TableCell>
                    <TableCell isHeader className={HEAD_CELL}>Rejected</TableCell>
                    <TableCell isHeader className={HEAD_CELL}>Returned</TableCell>
                    <TableCell isHeader className={HEAD_CELL}>Unit Price</TableCell>
                    <TableCell isHeader className={HEAD_CELL}>Value</TableCell>
                  </TableRow>
                </TableHeader>
                <TableBody className="divide-y divide-gray-100 dark:divide-white/[0.05]">
                  {rtv.lines.map((l) => (
                    <TableRow key={l.id}>
                      <TableCell className={BODY_CELL}>
                        <StackedCell main={<span className="font-mono">{l.materialCode}</span>} sub={`${l.materialName} — ${l.rejectionReason}`} />
                      </TableCell>
                      <TableCell className={BODY_CELL}>{l.rejectedQuantity ?? "—"}</TableCell>
                      <TableCell className={BODY_CELL}>{`${l.quantityToReturn} ${l.unitOfMeasure ?? ""}`.trim()}</TableCell>
                      <TableCell className={`${BODY_CELL} whitespace-nowrap`}>
                        {l.unitPrice != null ? formatAmount(l.unitPrice, currency) : "—"}
                      </TableCell>
                      <TableCell className={`${BODY_CELL} whitespace-nowrap`}>{formatAmount(l.lineValue ?? 0, currency)}</TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </div>
          </DetailCard>

          <DetailCard title="Resolution" icon={SectionIcons.check} tone={rtv.status === "RESOLVED" ? "success" : "gray"}>
            {rtv.status === "RESOLVED" && rtv.resolutionType ? (
              <DetailGrid>
                <DetailField label="Outcome">{RESOLUTION_TYPE_LABELS[rtv.resolutionType]}</DetailField>
                {rtv.resolutionType === "CREDIT_NOTE" ? (
                  <>
                    <DetailField label="Credit Note Number">
                      <span className="font-mono">{rtv.creditNoteReference || "—"}</span>
                    </DetailField>
                    <DetailField label="Credit Note Amount">{formatAmount(rtv.creditNoteAmount ?? 0, currency)}</DetailField>
                  </>
                ) : (
                  <DetailField label="Replacement Reference">
                    <span className="font-mono">{rtv.replacementReference || "—"}</span>
                  </DetailField>
                )}
                <DetailField label="Supplier Response" wide>
                  {rtv.supplierResponse || "—"}
                </DetailField>
              </DetailGrid>
            ) : (
              <p className="text-sm text-gray-500 dark:text-gray-400">
                {rtv.status === "DRAFT" && "Mark the return as shipped once the goods have left the warehouse."}
                {rtv.status === "PENDING" && "The goods are with the supplier. Record their replacement or credit note when it arrives."}
                {rtv.status === "CANCELLED" && "This return was cancelled; its quantities can be returned again."}
              </p>
            )}
          </DetailCard>

          <DetailCard title="System Information" icon={SectionIcons.info}>
            <DetailGrid>
              <DetailField label="Created">{formatDateTime(rtv.createdAt)}</DetailField>
              <DetailField label="Created By">{rtv.createdBy || "—"}</DetailField>
              <DetailField label="Last Updated">{formatDateTime(rtv.updatedAt)}</DetailField>
              <DetailField label="Updated By">{rtv.updatedBy || "—"}</DetailField>
            </DetailGrid>
          </DetailCard>

          {(rtv.notes || rtv.internalNotes) && (
            <DetailCard title="Notes" icon={SectionIcons.note}>
              <div className="space-y-3 whitespace-pre-line text-sm">
                {rtv.notes && <p className="text-gray-700 dark:text-gray-300">{rtv.notes}</p>}
                {rtv.internalNotes && <p className="text-gray-500">{rtv.internalNotes}</p>}
              </div>
            </DetailCard>
          )}
        </div>
      </div>

      {showResolve && (
        <Modal isOpen={showResolve} onClose={() => setShowResolve(false)} className="max-w-md p-6">
          <form onSubmit={handleResolve} className="space-y-4">
            <h3 className="text-lg font-semibold text-gray-800 dark:text-white/90">Resolve Return</h3>
            <div>
              <Label>Outcome *</Label>
              <select value={resolutionType} onChange={(e) => setResolutionType(e.target.value as ResolutionType)} className={SELECT_CLASS}>
                {(Object.keys(RESOLUTION_TYPE_LABELS) as ResolutionType[]).map((type) => (
                  <option key={type} value={type}>
                    {RESOLUTION_TYPE_LABELS[type]}
                  </option>
                ))}
              </select>
              <p className="mt-1.5 text-theme-xs text-gray-500 dark:text-gray-400">{RESOLUTION_EFFECTS[resolutionType]}</p>
            </div>
            <div>
              <Label>{resolutionType === "CREDIT_NOTE" ? "Credit Note Number *" : "Replacement Reference *"}</Label>
              <Input
                value={reference}
                onChange={(e) => setReference(e.target.value)}
                placeholder={resolutionType === "CREDIT_NOTE" ? "e.g. AV-2026-0042" : "e.g. RMA or delivery note number"}
              />
            </div>
            {resolutionType === "CREDIT_NOTE" && (
              <p className="text-sm text-gray-600 dark:text-gray-400">
                Amount: <span className="font-semibold">{formatAmount(rtv.totalValue ?? 0, currency)}</span>
              </p>
            )}
            <div>
              <Label>Supplier Response</Label>
              <TextArea rows={2} value={supplierResponse} onChange={setSupplierResponse} placeholder="Optional" />
            </div>
            <div className="flex justify-end gap-2 border-t border-gray-100 pt-3 dark:border-white/[0.05]">
              <Button variant="outline" size="sm" onClick={() => setShowResolve(false)}>
                Back
              </Button>
              <Button size="sm" type="submit" disabled={actionLoading || !reference.trim()}>
                Confirm Resolution
              </Button>
            </div>
          </form>
        </Modal>
      )}

      {showCancel && (
        <Modal isOpen={showCancel} onClose={() => setShowCancel(false)} className="max-w-md p-6">
          <form onSubmit={handleCancel} className="space-y-4">
            <h3 className="text-lg font-semibold text-gray-800 dark:text-white/90">Cancel Return</h3>
            <p className="text-sm text-gray-500 dark:text-gray-400">The quantities become returnable again on the goods receipt.</p>
            <div>
              <Label>Reason *</Label>
              <TextArea rows={3} value={cancelReason} onChange={setCancelReason} placeholder="Why is this return cancelled?" />
            </div>
            <div className="flex justify-end gap-2 border-t border-gray-100 pt-3 dark:border-white/[0.05]">
              <Button variant="outline" size="sm" onClick={() => setShowCancel(false)}>
                Back
              </Button>
              <Button size="sm" type="submit" disabled={actionLoading || !cancelReason.trim()}>
                Confirm Cancellation
              </Button>
            </div>
          </form>
        </Modal>
      )}
    </>
  );
}
