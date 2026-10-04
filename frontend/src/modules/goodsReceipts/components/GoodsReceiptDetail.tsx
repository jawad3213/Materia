import { useEffect, useState } from "react";
import { Link, useLocation, useNavigate, useParams } from "react-router-dom";
import goodsReceiptService from "../services/goodsReceiptService";
import type { GoodsReceipt } from "../types/goodsReceipt.types";
import QualityStatusBadge, { ReceiptStatusBadge } from "./QualityStatusBadge";
import useGoodsReceiptPermissions from "../hooks/useGoodsReceipt";
import GoodsReceiptReturns from "../../returnToVendor/components/GoodsReceiptReturns";
import Button from "../../../shared/components/ui/button/Button";
import { Modal } from "../../../shared/components/ui/modal";
import Label from "../../../shared/components/form/Label";
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
import { SectionIcons } from "../../../shared/components/page/pageIcons";
import { InitialsAvatar, StackedCell } from "../../../shared/components/page/ListParts";
import { BODY_CELL, HEAD_CELL } from "../../../shared/components/page/pageStyles";
import { getApiErrorMessage } from "../../../shared/utils/apiError";

type Feedback = { type: "success" | "error"; text: string };

export default function GoodsReceiptDetail() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const location = useLocation();
  const permissions = useGoodsReceiptPermissions();

  const [receipt, setReceipt] = useState<GoodsReceipt | null>(null);
  const [loading, setLoading] = useState(true);
  const [loadError, setLoadError] = useState<string | null>(null);
  const [actionLoading, setActionLoading] = useState(false);
  const [feedback, setFeedback] = useState<Feedback | null>(() => {
    const state = location.state as { error?: string } | null;
    return state?.error ? { type: "error", text: state.error } : null;
  });
  const [showCancelModal, setShowCancelModal] = useState(false);
  const [cancelReason, setCancelReason] = useState("");

  useEffect(() => {
    if (!id) return;
    let cancelled = false;
    goodsReceiptService
      .getById(id)
      .then((res) => {
        if (!cancelled) setReceipt(res.data);
      })
      .catch((err) => {
        if (!cancelled) setLoadError(getApiErrorMessage(err, "The goods receipt you requested does not exist."));
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });
    return () => {
      cancelled = true;
    };
  }, [id]);

  const runAction = async (action: () => Promise<{ data: GoodsReceipt }>, success: string) => {
    try {
      setActionLoading(true);
      const res = await action();
      setReceipt(res.data);
      setFeedback({ type: "success", text: success });
      return true;
    } catch (err) {
      setFeedback({ type: "error", text: getApiErrorMessage(err, "The action failed.") });
      return false;
    } finally {
      setActionLoading(false);
    }
  };

  const handleComplete = () =>
    receipt &&
    runAction(() => goodsReceiptService.complete(receipt.id), "Receipt validated: stock and purchase order updated.");

  const handleCancel = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!receipt || !cancelReason.trim()) return;
    if (await runAction(() => goodsReceiptService.cancel(receipt.id, cancelReason.trim()), "Receipt cancelled.")) {
      setShowCancelModal(false);
      setCancelReason("");
    }
  };

  const handleDelete = async () => {
    if (!receipt || !window.confirm(`Delete receipt ${receipt.receiptCode}?`)) return;
    try {
      setActionLoading(true);
      await goodsReceiptService.delete(receipt.id);
      navigate("/goods-receipts");
    } catch (err) {
      setFeedback({ type: "error", text: getApiErrorMessage(err, "The deletion failed.") });
      setActionLoading(false);
    }
  };

  if (loading) return <PageLoader message="Loading goods receipt..." />;
  if (!receipt) {
    return (
      <PageNotFound
        title="Goods Receipt Not Found"
        message={loadError || "The goods receipt you requested does not exist."}
        backTo="/goods-receipts"
        backLabel="Back to Goods Receipts"
      />
    );
  }

  return (
    <>
      <FloatingToast feedback={feedback} onClose={() => setFeedback(null)} />

      <DetailHeader title={receipt.receiptCode} parentName="Goods Receipts" parentUrl="/goods-receipts">
        <BackToListButton to="/goods-receipts" />
        {permissions.canDelete(receipt) && (
          <Button size="sm" variant="outline" onClick={handleDelete} disabled={actionLoading}>
            Delete
          </Button>
        )}
        {permissions.canCancel(receipt) && (
          <Button size="sm" variant="outline" onClick={() => setShowCancelModal(true)} disabled={actionLoading}>
            Cancel Receipt
          </Button>
        )}
        {permissions.canComplete(receipt) && (
          <Button size="sm" onClick={handleComplete} disabled={actionLoading}>
            Validate Receipt
          </Button>
        )}
      </DetailHeader>

      <div className="grid grid-cols-1 gap-6 lg:grid-cols-3">
        <div className="flex flex-col gap-6 lg:col-span-1">
          <DetailCard>
            <div className="mb-6 flex items-center justify-between">
              <InitialsAvatar name={receipt.supplierName} size="lg" />
              <ReceiptStatusBadge status={receipt.status} size="md" />
            </div>
            <h3 className="mb-1 font-mono text-xl font-bold text-gray-900 dark:text-white">{receipt.receiptCode}</h3>
            <p className="mb-6 text-sm font-medium text-gray-500 dark:text-gray-400">{receipt.supplierName || "—"}</p>
            <div className="space-y-4">
              <DetailField label="Purchase Order">
                <Link to={`/purchase-orders/${receipt.purchaseOrderId}`} className="font-mono text-brand-500 hover:underline">
                  {receipt.purchaseOrderCode || receipt.purchaseOrderId}
                </Link>
              </DetailField>
              <DetailField label="Received By">{receipt.receivedByName || receipt.receivedBy}</DetailField>
              <DetailField label="Receipt Date">{receipt.receiptDate || "Not validated yet"}</DetailField>
              <DetailField label="Expected Delivery">{receipt.expectedDeliveryDate || "—"}</DetailField>
            </div>
          </DetailCard>

          {(receipt.notes || receipt.discrepancyNotes) && (
            <DetailCard title="Notes" icon={SectionIcons.note}>
              <div className="space-y-3 text-sm">
                {receipt.notes && <p className="text-gray-700 dark:text-gray-300">{receipt.notes}</p>}
                {receipt.discrepancyNotes && <p className="text-warning-600 dark:text-orange-400">{receipt.discrepancyNotes}</p>}
              </div>
            </DetailCard>
          )}
        </div>

        <div className="flex flex-col gap-6 lg:col-span-2">
          <DetailCard title="Quantities" icon={SectionIcons.box} tone="success">
            <DetailGrid>
              <DetailField label="Expected">{receipt.totalQuantityOrdered ?? 0}</DetailField>
              <DetailField label="Received">{receipt.totalQuantityReceived ?? 0}</DetailField>
              <DetailField label="Accepted">{receipt.totalQuantityAccepted ?? 0}</DetailField>
              <DetailField label="Rejected">{receipt.totalQuantityRejected ?? 0}</DetailField>
            </DetailGrid>
          </DetailCard>

          <DetailCard title="Received Lines" icon={SectionIcons.list} tone="brand" padded={false}>
            <div className="max-w-full overflow-x-auto">
              <Table>
                <TableHeader className="border-b border-gray-100 bg-gray-50/50 dark:border-white/[0.05] dark:bg-gray-900/50">
                  <TableRow>
                    <TableCell isHeader className={HEAD_CELL}>Material</TableCell>
                    <TableCell isHeader className={HEAD_CELL}>Received</TableCell>
                    <TableCell isHeader className={HEAD_CELL}>Rejected</TableCell>
                    <TableCell isHeader className={HEAD_CELL}>Quality</TableCell>
                    <TableCell isHeader className={HEAD_CELL}>Batch / Location</TableCell>
                    <TableCell isHeader className={HEAD_CELL}>Stock Before → After</TableCell>
                  </TableRow>
                </TableHeader>
                <TableBody className="divide-y divide-gray-100 dark:divide-white/[0.05]">
                  {receipt.lines.map((l) => (
                    <TableRow key={l.id}>
                      <TableCell className={BODY_CELL}>
                        <StackedCell
                          main={<span className="font-mono">{l.materialCode}</span>}
                          sub={l.rejectionReason ? `Reason: ${l.rejectionReason}` : l.materialName}
                        />
                      </TableCell>
                      <TableCell className={BODY_CELL}>
                        {l.quantityReceived ?? 0} {l.unitOfMeasure}
                      </TableCell>
                      <TableCell className={BODY_CELL}>{l.quantityRejected ?? 0}</TableCell>
                      <TableCell className={BODY_CELL}>
                        <QualityStatusBadge status={l.qualityStatus} />
                      </TableCell>
                      <TableCell className={BODY_CELL}>
                        {[l.batchNumber, l.storageLocation].filter(Boolean).join(" / ") || "—"}
                      </TableCell>
                      <TableCell className={BODY_CELL}>
                        {l.stockBefore != null && l.stockAfter != null ? `${l.stockBefore} → ${l.stockAfter}` : "—"}
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </div>
          </DetailCard>

          <GoodsReceiptReturns receipt={receipt} />

          <DetailCard title="System Information" icon={SectionIcons.info}>
            <DetailGrid>
              <DetailField label="Created By">{receipt.createdBy || "—"}</DetailField>
              <DetailField label="Created At">{receipt.createdAt ? new Date(receipt.createdAt).toLocaleString() : "—"}</DetailField>
              <DetailField label="Last Updated By">{receipt.updatedBy || "—"}</DetailField>
              <DetailField label="Last Updated At">{receipt.updatedAt ? new Date(receipt.updatedAt).toLocaleString() : "—"}</DetailField>
            </DetailGrid>
          </DetailCard>
        </div>
      </div>

      {showCancelModal && (
        <Modal isOpen={showCancelModal} onClose={() => setShowCancelModal(false)} className="max-w-md p-6">
          <form onSubmit={handleCancel} className="space-y-4">
            <h3 className="text-lg font-semibold text-gray-800 dark:text-white/90">Cancel Receipt</h3>
            <div>
              <Label>Reason *</Label>
              <TextArea rows={3} value={cancelReason} onChange={setCancelReason} placeholder="Why is this receipt cancelled?" />
            </div>
            <div className="flex justify-end gap-2 border-t border-gray-100 pt-3 dark:border-white/[0.05]">
              <Button variant="outline" size="sm" onClick={() => setShowCancelModal(false)}>
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
