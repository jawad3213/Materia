import { useEffect, useState } from "react";
import { Link, useLocation, useNavigate, useParams } from "react-router-dom";
import goodsReceiptService from "../services/goodsReceiptService";
import type { GoodsReceipt } from "../types/goodsReceipt.types";
import QualityStatusBadge, { ReceiptStatusBadge } from "./QualityStatusBadge";
import useGoodsReceiptPermissions from "../hooks/useGoodsReceipt";
import Button from "../../../shared/components/ui/button/Button";
import { Modal } from "../../../shared/components/ui/modal";
import { Table, TableBody, TableCell, TableHeader, TableRow } from "../../../shared/components/ui/table";
import { getApiErrorMessage } from "../../../shared/utils/apiError";

const HEAD = "px-4 py-3 text-left text-[11px] font-semibold uppercase tracking-wider text-gray-500 dark:text-gray-400";
const CELL = "px-4 py-3 text-xs text-gray-700 dark:text-gray-300";

type Feedback = { type: "success" | "error"; text: string };

export default function GoodsReceiptDetail() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const location = useLocation();
  const permissions = useGoodsReceiptPermissions();

  const [receipt, setReceipt] = useState<GoodsReceipt | null>(null);
  const [loading, setLoading] = useState(true);
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
        if (!cancelled) {
          setFeedback({ type: "error", text: getApiErrorMessage(err, "Réception introuvable.") });
        }
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
    } catch (err) {
      setFeedback({ type: "error", text: getApiErrorMessage(err, "L'action a échoué.") });
    } finally {
      setActionLoading(false);
    }
  };

  const handleComplete = () => {
    if (!receipt) return;
    runAction(
      () => goodsReceiptService.complete(receipt.id),
      "Réception validée : le stock et la commande ont été mis à jour."
    );
  };

  const handleCancel = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!receipt || !cancelReason.trim()) return;
    await runAction(() => goodsReceiptService.cancel(receipt.id, cancelReason.trim()), "Réception annulée.");
    setShowCancelModal(false);
    setCancelReason("");
  };

  const handleDelete = async () => {
    if (!receipt || !window.confirm(`Supprimer la réception ${receipt.receiptCode} ?`)) return;
    try {
      setActionLoading(true);
      await goodsReceiptService.delete(receipt.id);
      navigate("/goods-receipts");
    } catch (err) {
      setFeedback({ type: "error", text: getApiErrorMessage(err, "La suppression a échoué.") });
      setActionLoading(false);
    }
  };

  if (loading) {
    return <div className="p-6 text-xs text-gray-500">Chargement de la réception...</div>;
  }

  if (!receipt) {
    return (
      <div className="p-6 space-y-3">
        <p className="text-xs text-red-600">{feedback?.text || "Réception introuvable."}</p>
        <Link to="/goods-receipts" className="text-xs text-brand-600 underline">
          Retour aux réceptions
        </Link>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {feedback && (
        <div
          className={`p-4 rounded-xl text-xs border flex justify-between gap-3 ${
            feedback.type === "success"
              ? "bg-emerald-50 text-emerald-700 border-emerald-200 dark:bg-emerald-500/10 dark:text-emerald-400 dark:border-emerald-500/20"
              : "bg-red-50 text-red-700 border-red-200 dark:bg-red-500/10 dark:text-red-400 dark:border-red-500/20"
          }`}
        >
          <span>{feedback.text}</span>
          <button type="button" onClick={() => setFeedback(null)} className="hover:opacity-75">
            ✕
          </button>
        </div>
      )}

      <div className="flex flex-col lg:flex-row lg:items-center justify-between gap-4 bg-white dark:bg-gray-900 p-6 rounded-2xl border border-gray-200 dark:border-white/[0.07] shadow-sm">
        <div className="space-y-1">
          <div className="flex items-center gap-3">
            <h2 className="text-xl font-bold font-mono text-gray-900 dark:text-white">{receipt.receiptCode}</h2>
            <ReceiptStatusBadge status={receipt.status} />
          </div>
          <p className="text-xs text-gray-500 dark:text-gray-400">
            Commande{" "}
            <Link
              to={`/purchase-orders/${receipt.purchaseOrderId}`}
              className="font-mono font-semibold text-brand-600 dark:text-brand-400 hover:underline"
            >
              {receipt.purchaseOrderCode || receipt.purchaseOrderId}
            </Link>
            {receipt.supplierName && ` • ${receipt.supplierName}`}
            {` • Réceptionnaire : ${receipt.receivedByName || receipt.receivedBy}`}
            {receipt.receiptDate && ` • Le ${receipt.receiptDate}`}
          </p>
        </div>

        <div className="flex flex-wrap items-center gap-2">
          {permissions.canComplete(receipt) && (
            <Button size="sm" onClick={handleComplete} disabled={actionLoading}>
              Valider la réception
            </Button>
          )}
          {permissions.canCancel(receipt) && (
            <Button
              size="sm"
              variant="outline"
              onClick={() => setShowCancelModal(true)}
              disabled={actionLoading}
              className="text-rose-600 border-rose-200 hover:bg-rose-50 dark:border-rose-500/20"
            >
              Annuler
            </Button>
          )}
          {permissions.canDelete(receipt) && (
            <Button size="sm" variant="outline" onClick={handleDelete} disabled={actionLoading}>
              Supprimer
            </Button>
          )}
        </div>
      </div>

      <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
        {[
          ["Attendu", receipt.totalQuantityOrdered],
          ["Reçu", receipt.totalQuantityReceived],
          ["Accepté", receipt.totalQuantityAccepted],
          ["Rejeté", receipt.totalQuantityRejected],
        ].map(([label, value]) => (
          <div
            key={label}
            className="bg-white dark:bg-gray-900 p-4 rounded-2xl border border-gray-200 dark:border-white/[0.07] shadow-sm"
          >
            <p className="text-[11px] uppercase tracking-wider text-gray-500">{label}</p>
            <p className="text-lg font-bold text-gray-900 dark:text-white">{value ?? 0}</p>
          </div>
        ))}
      </div>

      <div className="bg-white dark:bg-gray-900 rounded-2xl border border-gray-200 dark:border-white/[0.07] overflow-x-auto shadow-sm">
        <Table>
          <TableHeader className="border-b border-gray-100 dark:border-white/[0.05]">
            <TableRow>
              <TableCell isHeader className={HEAD}>#</TableCell>
              <TableCell isHeader className={HEAD}>Article</TableCell>
              <TableCell isHeader className={HEAD}>Reçu</TableCell>
              <TableCell isHeader className={HEAD}>Rejeté</TableCell>
              <TableCell isHeader className={HEAD}>Qualité</TableCell>
              <TableCell isHeader className={HEAD}>Lot / Emplacement</TableCell>
              <TableCell isHeader className={HEAD}>Stock avant → après</TableCell>
            </TableRow>
          </TableHeader>
          <TableBody className="divide-y divide-gray-100 dark:divide-white/[0.05]">
            {receipt.lines.map((l) => (
              <TableRow key={l.id}>
                <TableCell className={CELL}>{l.lineNumber}</TableCell>
                <TableCell className={CELL}>
                  <span className="font-mono font-semibold">{l.materialCode}</span>
                  {l.materialName && <span className="block text-gray-500">{l.materialName}</span>}
                  {l.rejectionReason && (
                    <span className="block text-rose-600 dark:text-rose-400">Motif : {l.rejectionReason}</span>
                  )}
                </TableCell>
                <TableCell className={CELL}>
                  {l.quantityReceived ?? 0} {l.unitOfMeasure}
                </TableCell>
                <TableCell className={CELL}>{l.quantityRejected ?? 0}</TableCell>
                <TableCell className={CELL}>
                  <QualityStatusBadge status={l.qualityStatus} />
                </TableCell>
                <TableCell className={CELL}>
                  {[l.batchNumber, l.storageLocation].filter(Boolean).join(" / ") || "-"}
                </TableCell>
                <TableCell className={CELL}>
                  {l.stockBefore != null && l.stockAfter != null ? `${l.stockBefore} → ${l.stockAfter}` : "-"}
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </div>

      {(receipt.notes || receipt.discrepancyNotes) && (
        <div className="bg-white dark:bg-gray-900 p-5 rounded-2xl border border-gray-200 dark:border-white/[0.07] shadow-sm text-xs space-y-2">
          {receipt.notes && <p className="text-gray-700 dark:text-gray-300">{receipt.notes}</p>}
          {receipt.discrepancyNotes && <p className="text-amber-700 dark:text-amber-400">{receipt.discrepancyNotes}</p>}
        </div>
      )}

      {showCancelModal && (
        <Modal isOpen={showCancelModal} onClose={() => setShowCancelModal(false)} className="max-w-md p-6">
          <form onSubmit={handleCancel} className="space-y-4">
            <h3 className="text-base font-bold text-gray-900 dark:text-white">Annuler la réception</h3>
            <label className="block text-xs font-semibold text-gray-700 dark:text-gray-300 space-y-1">
              <span>
                Motif <span className="text-red-500">*</span>
              </span>
              <textarea
                required
                rows={3}
                maxLength={1000}
                value={cancelReason}
                onChange={(e) => setCancelReason(e.target.value)}
                className="w-full rounded-xl border border-gray-200 dark:border-white/[0.1] bg-white dark:bg-gray-800 px-3 py-2 text-xs text-gray-800 dark:text-white focus:border-brand-500 focus:outline-none"
              />
            </label>
            <div className="flex justify-end gap-2 pt-3 border-t border-gray-100 dark:border-white/[0.05]">
              <Button variant="outline" size="sm" onClick={() => setShowCancelModal(false)}>
                Retour
              </Button>
              <Button size="sm" type="submit" disabled={actionLoading || !cancelReason.trim()}>
                Confirmer l'annulation
              </Button>
            </div>
          </form>
        </Modal>
      )}
    </div>
  );
}
