import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import Button from "../../../shared/components/ui/button/Button";
import { supplierApi } from "../../suppliers/services/supplierApi";
import type { SupplierListItem } from "../../suppliers/types/SupplierListItem";
import type { Requisition } from "../types";

export interface RequisitionConvertToPoModalProps {
  isOpen: boolean;
  onClose: () => void;
  onConfirm: (selectedSupplier?: { id: string; name: string; code?: string }) => void;
  requisition: Requisition | null;
  isConverting: boolean;
}

export default function RequisitionConvertToPoModal({
  isOpen,
  onClose,
  onConfirm,
  requisition,
  isConverting,
}: RequisitionConvertToPoModalProps) {
  const navigate = useNavigate();
  const [suppliers, setSuppliers] = useState<SupplierListItem[]>([]);
  const [selectedSupplierId, setSelectedSupplierId] = useState<string>("");
  const [loadingSuppliers, setLoadingSuppliers] = useState(false);

  // Check if requisition already has a supplier in its line items
  const detectedSupplier = requisition?.lines?.find(
    (l) => l.supplierId && l.supplierName
  );

  useEffect(() => {
    if (isOpen && !detectedSupplier) {
      loadSuppliers();
    }
  }, [isOpen, detectedSupplier]);

  const loadSuppliers = async () => {
    try {
      setLoadingSuppliers(true);
      const res = await supplierApi.getAllUnpaginated();
      if (res.data) {
        setSuppliers(res.data);
        if (res.data.length > 0 && !selectedSupplierId) {
          setSelectedSupplierId(res.data[0].id);
        }
      }
    } catch (err) {
      console.error("Failed to load suppliers:", err);
    } finally {
      setLoadingSuppliers(false);
    }
  };

  if (!isOpen || !requisition) return null;

  const handleDirectConfirm = () => {
    if (detectedSupplier && detectedSupplier.supplierId) {
      onConfirm({
        id: detectedSupplier.supplierId,
        name: detectedSupplier.supplierName || "Fournisseur",
        code: detectedSupplier.supplierCode,
      });
    } else if (selectedSupplierId) {
      const found = suppliers.find((s) => s.id === selectedSupplierId);
      onConfirm(
        found
          ? { id: found.id, name: found.name, code: found.code }
          : undefined
      );
    } else {
      onConfirm();
    }
  };

  const handleOpenInForm = () => {
    onClose();
    navigate(`/purchase-orders/create?fromRequisition=${requisition.id}`);
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center overflow-y-auto overflow-x-hidden bg-black/60 backdrop-blur-sm p-4">
      <div className="relative w-full max-w-lg rounded-2xl bg-white p-6 shadow-2xl dark:bg-gray-900 border border-gray-100 dark:border-white/[0.08]">
        {/* Icon & Title */}
        <div className="flex items-center gap-3.5 mb-4">
          <div className="flex size-11 items-center justify-center rounded-xl bg-purple-50 text-purple-600 dark:bg-purple-500/15 dark:text-purple-300">
            <svg className="size-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M16 11V7a4 4 0 00-8 0v4M5 9h14l1 12H4L5 9z" />
            </svg>
          </div>
          <div>
            <h3 className="text-base font-bold text-gray-900 dark:text-white">
              Convertir en Bon de Commande (PO)
            </h3>
            <p className="text-xs text-gray-500 dark:text-gray-400">
              Génère un bon de commande officiel à partir de cette demande d'achat approuvée.
            </p>
          </div>
        </div>

        {/* Requisition Details Box */}
        <div className="mb-4 rounded-xl bg-purple-50/50 p-4 border border-purple-100 dark:bg-purple-500/10 dark:border-purple-500/20 text-xs text-gray-700 dark:text-gray-300 space-y-2">
          <div className="flex justify-between">
            <span className="text-gray-500 dark:text-gray-400">Demande d'achat:</span>
            <span className="font-bold text-purple-700 dark:text-purple-300">
              {requisition.requisitionCode}
            </span>
          </div>
          <div className="flex justify-between">
            <span className="text-gray-500 dark:text-gray-400">Intitulé:</span>
            <span className="font-medium text-gray-900 dark:text-white truncate max-w-[220px]">
              {requisition.title}
            </span>
          </div>
          <div className="flex justify-between">
            <span className="text-gray-500 dark:text-gray-400">Nombre d'articles:</span>
            <span className="font-semibold text-gray-900 dark:text-white">
              {requisition.lines?.length || 0} ligne(s)
            </span>
          </div>
          <div className="flex justify-between">
            <span className="text-gray-500 dark:text-gray-400">Montant total:</span>
            <span className="font-bold text-gray-900 dark:text-white">
              {requisition.totalAmount} {requisition.currencyCode || "MAD"}
            </span>
          </div>
        </div>

        {/* Supplier selection if not detected */}
        {detectedSupplier ? (
          <div className="mb-4 p-3 rounded-xl bg-gray-50 dark:bg-gray-800/60 border border-gray-200 dark:border-gray-700 text-xs flex items-center justify-between">
            <span className="text-gray-500 dark:text-gray-400">Fournisseur pré-assigné:</span>
            <span className="font-semibold text-gray-900 dark:text-white">
              {detectedSupplier.supplierName} ({detectedSupplier.supplierCode || "N/A"})
            </span>
          </div>
        ) : (
          <div className="mb-4 space-y-1.5">
            <label className="block text-xs font-semibold text-gray-700 dark:text-gray-300">
              Sélectionner le Fournisseur pour la commande <span className="text-red-500">*</span>
            </label>
            {loadingSuppliers ? (
              <div className="text-xs text-gray-400 py-2">Chargement des fournisseurs...</div>
            ) : (
              <select
                value={selectedSupplierId}
                onChange={(e) => setSelectedSupplierId(e.target.value)}
                className="w-full px-3 py-2 text-xs rounded-xl border border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-800 text-gray-900 dark:text-white focus:outline-none focus:ring-2 focus:ring-purple-500"
              >
                {suppliers.map((s) => (
                  <option key={s.id} value={s.id}>
                    {s.name} ({s.code || "Sans code"})
                  </option>
                ))}
              </select>
            )}
          </div>
        )}

        {/* Informative text */}
        <p className="text-xs text-gray-600 dark:text-gray-400 mb-6 leading-relaxed">
          La conversion enregistrera un nouveau bon de commande en base de données et mettra à jour cette demande vers le statut <span className="font-semibold text-purple-600 dark:text-purple-400">CONVERTED</span>.
        </p>

        {/* Action Buttons */}
        <div className="flex flex-col sm:flex-row items-center justify-between gap-3 pt-2 border-t border-gray-100 dark:border-gray-800">
          <button
            type="button"
            onClick={handleOpenInForm}
            disabled={isConverting}
            className="w-full sm:w-auto text-xs font-medium text-purple-600 dark:text-purple-400 hover:underline flex items-center gap-1"
          >
            <span>Ouvrir dans le formulaire pour personnaliser</span>
            <svg className="size-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M10 6H6a2 2 0 00-2 2v10a2 2 0 002 2h10a2 2 0 002-2v-4M14 4h6m0 0v6m0-6L10 14" />
            </svg>
          </button>

          <div className="flex items-center gap-2 w-full sm:w-auto justify-end">
            <Button
              type="button"
              variant="outline"
              size="sm"
              onClick={onClose}
              disabled={isConverting}
            >
              Annuler
            </Button>
            <button
              type="button"
              onClick={handleDirectConfirm}
              disabled={isConverting || (!detectedSupplier && !selectedSupplierId && suppliers.length > 0)}
              className="inline-flex items-center gap-1.5 px-4 py-2 text-xs font-semibold rounded-lg bg-purple-600 text-white hover:bg-purple-700 active:bg-purple-800 disabled:opacity-50 transition-colors shadow-sm"
            >
              {isConverting ? (
                <>
                  <div className="size-3.5 animate-spin rounded-full border-2 border-white border-t-transparent" />
                  Conversion en cours...
                </>
              ) : (
                <>
                  <svg className="size-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                  </svg>
                  Confirmer la Conversion
                </>
              )}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
