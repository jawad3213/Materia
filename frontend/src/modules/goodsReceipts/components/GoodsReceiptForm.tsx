import { useEffect, useState } from "react";
import { Link, useNavigate, useSearchParams } from "react-router-dom";
import goodsReceiptService from "../services/goodsReceiptService";
import purchaseOrderService from "../../purchaseOrders/services/purchaseOrderService";
import { RECEIVABLE_STATUSES } from "../../purchaseOrders/hooks/usePurchaseOrder";
import type { PurchaseOrder } from "../../purchaseOrders/types";
import GoodsReceiptLine from "./GoodsReceiptLine";
import { deriveQualityStatus, lineError, toDrafts, type ReceiptLineDraft } from "../utils/receiptLine";
import Button from "../../../shared/components/ui/button/Button";
import useAuth from "../../auth/hooks/useAuth";
import { getApiErrorMessage } from "../../../shared/utils/apiError";

export default function GoodsReceiptForm() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const { user } = useAuth();
  const initialOrderId = searchParams.get("purchaseOrderId") || "";

  const [assignedOrders, setAssignedOrders] = useState<PurchaseOrder[]>([]);
  const [selectedOrderId, setSelectedOrderId] = useState(initialOrderId);
  const [order, setOrder] = useState<PurchaseOrder | null>(null);
  const [lines, setLines] = useState<ReceiptLineDraft[]>([]);
  const [notes, setNotes] = useState("");
  const [loadingOrder, setLoadingOrder] = useState(!!initialOrderId);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // Orders the current user is assigned to receive.
  useEffect(() => {
    let cancelled = false;
    purchaseOrderService
      .getAll()
      .then((res) => {
        if (cancelled) return;
        setAssignedOrders(
          res.data.filter((o) => o.assignedTo === user?.id && RECEIVABLE_STATUSES.includes(o.status))
        );
      })
      .catch((err) => console.error("Failed to load assigned purchase orders:", err));
    return () => {
      cancelled = true;
    };
  }, [user?.id]);

  useEffect(() => {
    if (!selectedOrderId) return;
    let cancelled = false;
    Promise.all([
      purchaseOrderService.getById(selectedOrderId),
      goodsReceiptService.getByPurchaseOrderId(selectedOrderId),
    ])
      .then(([orderRes, receiptsRes]) => {
        if (cancelled) return;
        const loaded = orderRes.data;
        if (!RECEIVABLE_STATUSES.includes(loaded.status)) {
          setError("Cette commande n'est pas prête pour la réception.");
          return;
        }
        if (loaded.assignedTo !== user?.id) {
          setError("Vous n'êtes pas assigné à la réception de cette commande.");
          return;
        }
        setOrder(loaded);
        setLines(toDrafts(loaded, receiptsRes.data));
      })
      .catch((err) => {
        if (!cancelled) setError(getApiErrorMessage(err, "Impossible de charger la commande."));
      })
      .finally(() => {
        if (!cancelled) setLoadingOrder(false);
      });
    return () => {
      cancelled = true;
    };
  }, [selectedOrderId, user?.id]);

  const handleSelectOrder = (orderId: string) => {
    setError(null);
    setOrder(null);
    setLines([]);
    setLoadingOrder(!!orderId);
    setSelectedOrderId(orderId);
  };

  const updateLine = (index: number, line: ReceiptLineDraft) =>
    setLines((prev) => prev.map((l, i) => (i === index ? line : l)));

  const submit = async (validate: boolean) => {
    if (!order) return;
    setError(null);

    const received = lines.filter((l) => l.received > 0);
    if (received.length === 0) {
      setError("Saisissez au moins une quantité reçue.");
      return;
    }
    if (lines.some((l) => lineError(l))) {
      setError("Corrigez les lignes signalées avant d'enregistrer.");
      return;
    }

    try {
      setSubmitting(true);
      const created = await goodsReceiptService.create({
        purchaseOrderId: order.id,
        notes: notes.trim() || undefined,
        lines: received.map((l) => ({
          purchaseOrderLineId: l.purchaseOrderLineId,
          materialCode: l.materialCode,
          quantityReceived: l.received,
          quantityRejected: l.rejected,
          qualityStatus: deriveQualityStatus(l),
          rejectionReason: l.rejected > 0 ? l.rejectionReason.trim() : undefined,
          batchNumber: l.batchNumber.trim() || undefined,
          storageLocation: l.storageLocation.trim() || undefined,
        })),
      });
      if (validate) {
        try {
          await goodsReceiptService.complete(created.data.id);
        } catch (completeErr) {
          // The draft exists; send the user to it so validation can be retried there.
          navigate(`/goods-receipts/${created.data.id}`, {
            state: {
              error: getApiErrorMessage(
                completeErr,
                "La réception a été enregistrée en brouillon mais n'a pas pu être validée."
              ),
            },
          });
          return;
        }
      }
      navigate(`/goods-receipts/${created.data.id}`);
    } catch (err) {
      console.error("Failed to save goods receipt:", err);
      setError(getApiErrorMessage(err, "Échec de l'enregistrement de la réception."));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="max-w-4xl mx-auto space-y-6">
      <div className="flex items-center justify-between bg-white dark:bg-gray-900 p-6 rounded-2xl border border-gray-200 dark:border-white/[0.07] shadow-sm">
        <div>
          <h2 className="text-xl font-bold text-gray-900 dark:text-white">Nouvelle réception</h2>
          <p className="text-xs text-gray-500 dark:text-gray-400 mt-1">
            Saisissez les quantités reçues et rejetées. La validation met à jour le stock et la commande.
          </p>
        </div>
        <Link to={order ? `/purchase-orders/${order.id}` : "/goods-receipts"}>
          <Button variant="outline" size="sm">
            Annuler
          </Button>
        </Link>
      </div>

      {error && (
        <div className="p-4 rounded-xl bg-red-50 text-red-700 dark:bg-red-500/10 dark:text-red-400 text-xs border border-red-200 dark:border-red-500/20">
          {error}
        </div>
      )}

      <div className="bg-white dark:bg-gray-900 p-6 rounded-2xl border border-gray-200 dark:border-white/[0.07] shadow-sm space-y-4">
        <label className="block text-xs font-semibold text-gray-700 dark:text-gray-300 space-y-1.5">
          <span>Commande à réceptionner</span>
          <select
            value={selectedOrderId}
            onChange={(e) => handleSelectOrder(e.target.value)}
            className="w-full rounded-xl border border-gray-200 dark:border-white/[0.1] bg-white dark:bg-gray-800 px-3 py-2 text-xs text-gray-800 dark:text-white focus:border-brand-500 focus:outline-none"
          >
            <option value="">Sélectionnez une commande qui vous est assignée</option>
            {assignedOrders.map((o) => (
              <option key={o.id} value={o.id}>
                {o.orderCode} — {o.supplierName}
              </option>
            ))}
            {selectedOrderId && !assignedOrders.some((o) => o.id === selectedOrderId) && order && (
              <option value={order.id}>
                {order.orderCode} — {order.supplierName}
              </option>
            )}
          </select>
        </label>

        {loadingOrder && <p className="text-xs text-gray-500">Chargement de la commande...</p>}

        {order && lines.length === 0 && (
          <p className="text-xs text-gray-500">Toutes les quantités de cette commande ont déjà été reçues.</p>
        )}

        {order && lines.length > 0 && (
          <div className="space-y-3">
            {lines.map((line, index) => (
              <GoodsReceiptLine
                key={line.purchaseOrderLineId}
                line={line}
                onChange={(updated) => updateLine(index, updated)}
              />
            ))}

            <label className="block text-xs font-semibold text-gray-700 dark:text-gray-300 space-y-1.5">
              <span>Observations</span>
              <textarea
                rows={2}
                maxLength={1000}
                value={notes}
                onChange={(e) => setNotes(e.target.value)}
                className="w-full rounded-xl border border-gray-200 dark:border-white/[0.1] bg-white dark:bg-gray-800 px-3 py-2 text-xs text-gray-800 dark:text-white focus:border-brand-500 focus:outline-none"
              />
            </label>

            <div className="flex justify-end gap-2 pt-3 border-t border-gray-100 dark:border-white/[0.05]">
              <Button variant="outline" size="sm" onClick={() => submit(false)} disabled={submitting}>
                Enregistrer en brouillon
              </Button>
              <Button size="sm" onClick={() => submit(true)} disabled={submitting}>
                {submitting ? "Enregistrement..." : "Valider la réception"}
              </Button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
