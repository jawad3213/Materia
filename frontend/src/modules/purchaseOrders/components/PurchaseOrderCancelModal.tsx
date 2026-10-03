import React, { useState } from "react";
import { Modal } from "../../../shared/components/ui/modal";
import Button from "../../../shared/components/ui/button/Button";
import type { PurchaseOrder } from "../types/PurchaseOrder";
import type { PurchaseOrderListItem } from "../types/PurchaseOrderListItem";

interface PurchaseOrderCancelModalProps {
  isOpen: boolean;
  onClose: () => void;
  order: PurchaseOrder | PurchaseOrderListItem | null;
  onConfirm: (reason: string) => Promise<void>;
  isLoading?: boolean;
}

export default function PurchaseOrderCancelModal({
  isOpen,
  onClose,
  order,
  onConfirm,
  isLoading = false,
}: PurchaseOrderCancelModalProps) {
  const [reason, setReason] = useState("");
  const [internalSubmitting, setInternalSubmitting] = useState(false);
  const isSubmitting = isLoading || internalSubmitting;
  const [error, setError] = useState("");

  if (!order) return null;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!reason.trim()) {
      setError("Le motif d'annulation est obligatoire.");
      return;
    }
    try {
      setInternalSubmitting(true);
      setError("");
      await onConfirm(reason.trim());
      setReason("");
      onClose();
    } catch (err: any) {
      setError(
        err?.response?.data?.message ||
          err?.message ||
          "Échec de l'annulation de la commande."
      );
    } finally {
      setInternalSubmitting(false);
    }
  };

  return (
    <Modal isOpen={isOpen} onClose={onClose} className="max-w-lg p-6">
      <div className="space-y-4">
        {/* Header */}
        <div className="flex items-start gap-3">
          <div className="p-3 rounded-xl flex-shrink-0 bg-rose-50 text-rose-600 dark:bg-rose-500/15 dark:text-rose-400">
            <svg
              className="size-6"
              fill="none"
              viewBox="0 0 24 24"
              stroke="currentColor"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"
              />
            </svg>
          </div>
          <div>
            <h3 className="text-base font-bold text-gray-900 dark:text-white">
              Annuler le Bon de Commande
            </h3>
            <p className="text-xs text-gray-500 dark:text-gray-400 mt-1">
              Êtes-vous sûr de vouloir annuler la commande{" "}
              <span className="font-semibold text-brand-600 dark:text-brand-400">
                {order.orderCode}
              </span>{" "}
              auprès de{" "}
              <span className="font-semibold text-gray-800 dark:text-white">
                {order.supplierName}
              </span>
              ? Cette action est irréversible.
            </p>
          </div>
        </div>

        {error && (
          <div className="p-3 rounded-xl bg-red-50 text-red-700 dark:bg-red-500/10 dark:text-red-400 text-xs border border-red-200 dark:border-red-500/20">
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block mb-1.5 text-xs font-semibold text-gray-700 dark:text-gray-300">
              Motif de l'annulation <span className="text-red-500">*</span>
            </label>
            <textarea
              rows={3}
              value={reason}
              onChange={(e) => setReason(e.target.value)}
              placeholder="Précisez la raison de l'annulation (fournisseur indisponible, erreur de saisie, etc.)..."
              required
              className="w-full rounded-xl border border-gray-200 dark:border-white/[0.1] bg-white dark:bg-gray-800 p-3 text-xs text-gray-800 dark:text-white placeholder:text-gray-400 focus:border-brand-500 focus:outline-none focus:ring-1 focus:ring-brand-500"
            />
          </div>

          <div className="flex items-center justify-end gap-2.5 pt-2">
            <Button
              type="button"
              variant="outline"
              size="sm"
              onClick={onClose}
              disabled={isSubmitting}
            >
              Fermer
            </Button>
            <Button
              type="submit"
              size="sm"
              disabled={isSubmitting || !reason.trim()}
              className="bg-rose-600 hover:bg-rose-700 text-white"
            >
              {isSubmitting ? "Annulation en cours..." : "Confirmer l'annulation"}
            </Button>
          </div>
        </form>
      </div>
    </Modal>
  );
}
