import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import Button from "../../../shared/components/ui/button/Button";
import { Modal } from "../../../shared/components/ui/modal";
import Label from "../../../shared/components/form/Label";
import { supplierApi } from "../../suppliers/services/supplierApi";
import type { SupplierListItem } from "../../suppliers/types/SupplierListItem";
import type { Requisition } from "../types";
import { supplierFromLines, type OrderSupplier } from "../utils/convertToPurchaseOrder";
import { formatAmount } from "../../invoices/utils/invoiceLine";
import { SELECT_CLASS } from "../../../shared/components/page/pageStyles";

export interface RequisitionConvertToPoModalProps {
  isOpen: boolean;
  onClose: () => void;
  onConfirm: (supplier: OrderSupplier) => void;
  requisition: Requisition | null;
  isConverting: boolean;
}

/**
 * Turns an approved requisition into a purchase order. The supplier is the one chosen on the requisition lines,
 * or one the buyer picks here; it is never guessed. The full order form stays available to adjust terms first.
 */
export default function RequisitionConvertToPoModal({ isOpen, onClose, onConfirm, requisition, isConverting }: RequisitionConvertToPoModalProps) {
  const navigate = useNavigate();
  // null until loaded: the supplier list is fetched once, the first time it is needed.
  const [suppliers, setSuppliers] = useState<SupplierListItem[] | null>(null);
  const [selectedId, setSelectedId] = useState("");
  const preassigned = requisition ? supplierFromLines(requisition) : null;
  const needsList = isOpen && !preassigned;
  const loading = needsList && suppliers === null;

  useEffect(() => {
    if (!needsList || suppliers !== null) return;
    let cancelled = false;
    supplierApi
      .getAllUnpaginated()
      .then((res) => {
        if (!cancelled) setSuppliers(Array.isArray(res.data) ? res.data : []);
      })
      .catch(() => {
        if (!cancelled) setSuppliers([]);
      });
    return () => {
      cancelled = true;
    };
  }, [needsList, suppliers]);

  if (!isOpen || !requisition) return null;

  const chosen: OrderSupplier | null =
    preassigned ??
    (() => {
      const found = suppliers?.find((s) => s.id === selectedId);
      return found ? { id: found.id, name: found.name, code: found.code || undefined } : null;
    })();

  return (
    <Modal isOpen={isOpen} onClose={onClose} className="max-w-md p-6">
      <div className="space-y-4">
        <div>
          <h3 className="text-lg font-semibold text-gray-800 dark:text-white/90">Create Purchase Order</h3>
          <p className="mt-1 text-sm text-gray-500 dark:text-gray-400">
            Orders every line of the approved requisition; the requisition becomes "Ordered".
          </p>
        </div>

        <dl className="grid grid-cols-2 gap-x-4 gap-y-3 rounded-xl border border-gray-200 bg-gray-50/60 p-4 text-sm dark:border-gray-800 dark:bg-white/[0.02]">
          <div>
            <dt className="text-theme-xs text-gray-500 dark:text-gray-400">Requisition</dt>
            <dd className="font-mono font-medium text-gray-800 dark:text-white/90">{requisition.requisitionCode}</dd>
          </div>
          <div>
            <dt className="text-theme-xs text-gray-500 dark:text-gray-400">Lines</dt>
            <dd className="font-medium text-gray-800 dark:text-white/90">{requisition.lines?.length || 0}</dd>
          </div>
          <div className="col-span-2">
            <dt className="text-theme-xs text-gray-500 dark:text-gray-400">Title</dt>
            <dd className="font-medium text-gray-800 dark:text-white/90">{requisition.title}</dd>
          </div>
          <div className="col-span-2">
            <dt className="text-theme-xs text-gray-500 dark:text-gray-400">Estimated Total</dt>
            <dd className="font-medium text-gray-800 dark:text-white/90">{formatAmount(requisition.totalAmount, requisition.currencyCode)}</dd>
          </div>
        </dl>

        {preassigned ? (
          <p className="text-sm text-gray-600 dark:text-gray-400">
            Supplier from the requisition: <span className="font-medium text-gray-800 dark:text-white/90">{preassigned.name}</span>
            {preassigned.code && ` (${preassigned.code})`}
          </p>
        ) : (
          <div>
            <Label>Supplier *</Label>
            <select value={selectedId} onChange={(e) => setSelectedId(e.target.value)} disabled={loading} className={SELECT_CLASS}>
              <option value="">{loading ? "Loading suppliers..." : "Select a supplier..."}</option>
              {(suppliers ?? []).map((s) => (
                <option key={s.id} value={s.id}>
                  {s.name} {s.code ? `(${s.code})` : ""}
                </option>
              ))}
            </select>
          </div>
        )}

        <div className="flex flex-col-reverse gap-3 border-t border-gray-100 pt-3 sm:flex-row sm:items-center sm:justify-between dark:border-white/[0.05]">
          <button
            type="button"
            disabled={isConverting}
            onClick={() => {
              onClose();
              navigate(`/purchase-orders/create?fromRequisition=${requisition.id}`);
            }}
            className="text-theme-sm font-medium text-brand-500 hover:underline"
          >
            Open the full order form
          </button>
          <div className="flex justify-end gap-2">
            <Button variant="outline" size="sm" onClick={onClose} disabled={isConverting}>
              Back
            </Button>
            <Button size="sm" onClick={() => chosen && onConfirm(chosen)} disabled={isConverting || !chosen}>
              {isConverting ? "Creating..." : "Create Order"}
            </Button>
          </div>
        </div>
      </div>
    </Modal>
  );
}
