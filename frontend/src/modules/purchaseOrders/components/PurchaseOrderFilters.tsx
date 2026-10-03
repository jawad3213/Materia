import React from "react";
import { Dropdown } from "../../../shared/components/ui/dropdown/Dropdown";
import Button from "../../../shared/components/ui/button/Button";

interface PurchaseOrderFiltersProps {
  isOpen: boolean;
  onClose: () => void;
  filterStatus: string;
  setFilterStatus: (val: string) => void;
  filterDeliveryStatus: string;
  setFilterDeliveryStatus: (val: string) => void;
  filterDateFrom: string;
  setFilterDateFrom: (val: string) => void;
  filterDateTo: string;
  setFilterDateTo: (val: string) => void;
  onApply: () => void;
  onClear: () => void;
}

const statusOptions = [
  { value: "", label: "Tous les statuts" },
  { value: "DRAFT", label: "Brouillon (DRAFT)" },
  { value: "SUBMITTED", label: "Soumise (SUBMITTED)" },
  { value: "CONFIRMED", label: "Confirmée (CONFIRMED)" },
  { value: "READY_FOR_RECEIPT", label: "Prête pour réception" },
  { value: "RECEIVED", label: "Reçue (RECEIVED)" },
  { value: "PARTIALLY_RECEIVED", label: "Partiellement reçue" },
  { value: "COMPLETED", label: "Terminée (COMPLETED)" },
  { value: "CANCELLED", label: "Annulée (CANCELLED)" },
  { value: "REJECTED", label: "Rejetée (REJECTED)" },
];

const deliveryStatusOptions = [
  { value: "", label: "Tous les statuts de livraison" },
  { value: "NOT_SHIPPED", label: "Non expédié (NOT_SHIPPED)" },
  { value: "SHIPPED", label: "Expédié (SHIPPED)" },
  { value: "IN_TRANSIT", label: "En transit (IN_TRANSIT)" },
  { value: "PARTIAL", label: "Partielle (PARTIAL)" },
  { value: "DELIVERED", label: "Livrée (DELIVERED)" },
  { value: "DELAYED", label: "Retardée (DELAYED)" },
];

export default function PurchaseOrderFilters({
  isOpen,
  onClose,
  filterStatus,
  setFilterStatus,
  filterDeliveryStatus,
  setFilterDeliveryStatus,
  filterDateFrom,
  setFilterDateFrom,
  filterDateTo,
  setFilterDateTo,
  onApply,
  onClear,
}: PurchaseOrderFiltersProps) {
  const activeCount = [
    filterStatus,
    filterDeliveryStatus,
    filterDateFrom,
    filterDateTo,
  ].filter(Boolean).length;

  return (
    <Dropdown
      isOpen={isOpen}
      onClose={onClose}
      className="w-[320px] sm:w-[460px] p-5 top-full right-0 mt-2 z-50 shadow-xl border border-gray-200 dark:border-white/[0.1] bg-white dark:bg-gray-900 rounded-2xl"
    >
      <div className="flex items-center justify-between pb-3 mb-4 border-b border-gray-100 dark:border-white/[0.05]">
        <div className="flex items-center gap-2">
          <svg
            className="size-4 text-brand-500"
            fill="none"
            viewBox="0 0 24 24"
            stroke="currentColor"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M3 4a1 1 0 011-1h16a1 1 0 011 1v2.586a1 1 0 01-.293.707l-6.414 6.414a1 1 0 00-.293.707V17l-4 4v-6.586a1 1 0 00-.293-.707L3.293 7.293A1 1 0 013 6.586V4z"
            />
          </svg>
          <h4 className="text-sm font-semibold text-gray-800 dark:text-white/90">
            Filtrer les Bons de Commande
          </h4>
        </div>
        {activeCount > 0 && (
          <button
            type="button"
            onClick={onClear}
            className="text-xs font-medium text-brand-500 hover:text-brand-600 dark:text-brand-400 transition-colors"
          >
            Réinitialiser ({activeCount})
          </button>
        )}
      </div>

      <div className="space-y-4 mb-5">
        <div>
          <label className="block mb-1.5 text-xs font-medium text-gray-700 dark:text-gray-300">
            Statut de la Commande
          </label>
          <select
            value={filterStatus}
            onChange={(e) => setFilterStatus(e.target.value)}
            className="w-full rounded-xl border border-gray-200 dark:border-white/[0.1] bg-white dark:bg-gray-800 px-3 py-2 text-xs text-gray-800 dark:text-white focus:border-brand-500 focus:outline-none focus:ring-1 focus:ring-brand-500"
          >
            {statusOptions.map((opt) => (
              <option key={opt.value} value={opt.value}>
                {opt.label}
              </option>
            ))}
          </select>
        </div>

        <div>
          <label className="block mb-1.5 text-xs font-medium text-gray-700 dark:text-gray-300">
            Statut de Livraison
          </label>
          <select
            value={filterDeliveryStatus}
            onChange={(e) => setFilterDeliveryStatus(e.target.value)}
            className="w-full rounded-xl border border-gray-200 dark:border-white/[0.1] bg-white dark:bg-gray-800 px-3 py-2 text-xs text-gray-800 dark:text-white focus:border-brand-500 focus:outline-none focus:ring-1 focus:ring-brand-500"
          >
            {deliveryStatusOptions.map((opt) => (
              <option key={opt.value} value={opt.value}>
                {opt.label}
              </option>
            ))}
          </select>
        </div>

        <div>
          <label className="block mb-1.5 text-xs font-medium text-gray-700 dark:text-gray-300">
            Période de Commande
          </label>
          <div className="grid grid-cols-2 gap-2">
            <div>
              <span className="text-[10px] text-gray-400 block mb-1">Du</span>
              <input
                type="date"
                value={filterDateFrom}
                onChange={(e) => setFilterDateFrom(e.target.value)}
                className="w-full rounded-xl border border-gray-200 dark:border-white/[0.1] bg-white dark:bg-gray-800 px-2.5 py-1.5 text-xs text-gray-800 dark:text-white focus:border-brand-500 focus:outline-none focus:ring-1 focus:ring-brand-500"
              />
            </div>
            <div>
              <span className="text-[10px] text-gray-400 block mb-1">Au</span>
              <input
                type="date"
                value={filterDateTo}
                onChange={(e) => setFilterDateTo(e.target.value)}
                className="w-full rounded-xl border border-gray-200 dark:border-white/[0.1] bg-white dark:bg-gray-800 px-2.5 py-1.5 text-xs text-gray-800 dark:text-white focus:border-brand-500 focus:outline-none focus:ring-1 focus:ring-brand-500"
              />
            </div>
          </div>
        </div>
      </div>

      <div className="flex items-center justify-end gap-2 pt-3 border-t border-gray-100 dark:border-white/[0.05]">
        <Button variant="outline" size="sm" onClick={onClose}>
          Fermer
        </Button>
        <Button
          size="sm"
          onClick={() => {
            onApply();
            onClose();
          }}
        >
          Appliquer ({activeCount})
        </Button>
      </div>
    </Dropdown>
  );
}
