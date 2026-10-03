import React from "react";
import type { OrderStatus } from "../types/OrderStatus";
import type { DeliveryStatus } from "../types/DeliveryStatus";

interface OrderStatusBadgeProps {
  status: OrderStatus | string;
  size?: "sm" | "md";
}

interface DeliveryStatusBadgeProps {
  status: DeliveryStatus | string;
  size?: "sm" | "md";
}

const orderStatusConfig: Record<
  string,
  { label: string; badgeClass: string; dotClass: string }
> = {
  DRAFT: {
    label: "Brouillon",
    badgeClass:
      "bg-gray-100 text-gray-700 dark:bg-gray-800 dark:text-gray-300 border border-gray-200 dark:border-gray-700",
    dotClass: "bg-gray-400 dark:bg-gray-500",
  },
  SUBMITTED: {
    label: "Soumise",
    badgeClass:
      "bg-amber-50 text-amber-700 dark:bg-amber-500/15 dark:text-amber-400 border border-amber-200/60 dark:border-amber-500/20",
    dotClass: "bg-amber-500 animate-pulse",
  },
  CONFIRMED: {
    label: "Confirmée",
    badgeClass:
      "bg-blue-50 text-blue-700 dark:bg-blue-500/15 dark:text-blue-400 border border-blue-200/60 dark:border-blue-500/20",
    dotClass: "bg-blue-500",
  },
  READY_FOR_RECEIPT: {
    label: "Prête réception",
    badgeClass:
      "bg-sky-50 text-sky-700 dark:bg-sky-500/15 dark:text-sky-400 border border-sky-200/60 dark:border-sky-500/20",
    dotClass: "bg-sky-500 animate-pulse",
  },
  RECEIVED: {
    label: "Reçue",
    badgeClass:
      "bg-teal-50 text-teal-700 dark:bg-teal-500/15 dark:text-teal-400 border border-teal-200/60 dark:border-teal-500/20",
    dotClass: "bg-teal-500",
  },
  PARTIALLY_RECEIVED: {
    label: "Partielle",
    badgeClass:
      "bg-yellow-50 text-yellow-800 dark:bg-yellow-500/15 dark:text-yellow-400 border border-yellow-200/60 dark:border-yellow-500/20",
    dotClass: "bg-yellow-500",
  },
  COMPLETED: {
    label: "Terminée",
    badgeClass:
      "bg-emerald-50 text-emerald-700 dark:bg-emerald-500/15 dark:text-emerald-400 border border-emerald-200/60 dark:border-emerald-500/20",
    dotClass: "bg-emerald-500",
  },
  CANCELLED: {
    label: "Annulée",
    badgeClass:
      "bg-red-50 text-red-700 dark:bg-red-500/15 dark:text-red-400 border border-red-200/60 dark:border-red-500/20",
    dotClass: "bg-red-500",
  },
  REJECTED: {
    label: "Rejetée",
    badgeClass:
      "bg-rose-50 text-rose-800 dark:bg-rose-500/15 dark:text-rose-400 border border-rose-200/60 dark:border-rose-500/20",
    dotClass: "bg-rose-600",
  },
};

const deliveryStatusConfig: Record<
  string,
  { label: string; badgeClass: string; dotClass: string }
> = {
  NOT_SHIPPED: {
    label: "Non expédié",
    badgeClass:
      "bg-gray-100 text-gray-600 dark:bg-gray-800 dark:text-gray-400 border border-gray-200/80 dark:border-gray-700",
    dotClass: "bg-gray-400",
  },
  SHIPPED: {
    label: "Expédié",
    badgeClass:
      "bg-blue-50 text-blue-700 dark:bg-blue-500/15 dark:text-blue-400 border border-blue-200/60 dark:border-blue-500/20",
    dotClass: "bg-blue-500",
  },
  IN_TRANSIT: {
    label: "En transit",
    badgeClass:
      "bg-indigo-50 text-indigo-700 dark:bg-indigo-500/15 dark:text-indigo-400 border border-indigo-200/60 dark:border-indigo-500/20",
    dotClass: "bg-indigo-500 animate-pulse",
  },
  PARTIAL: {
    label: "Partielle",
    badgeClass:
      "bg-amber-50 text-amber-700 dark:bg-amber-500/15 dark:text-amber-400 border border-amber-200/60 dark:border-amber-500/20",
    dotClass: "bg-amber-500",
  },
  DELIVERED: {
    label: "Livrée",
    badgeClass:
      "bg-emerald-50 text-emerald-700 dark:bg-emerald-500/15 dark:text-emerald-400 border border-emerald-200/60 dark:border-emerald-500/20",
    dotClass: "bg-emerald-500",
  },
  DELAYED: {
    label: "Retardée",
    badgeClass:
      "bg-red-50 text-red-700 dark:bg-red-500/15 dark:text-red-400 border border-red-200/60 dark:border-red-500/20",
    dotClass: "bg-red-500",
  },
};

export default function PurchaseOrderStatusBadge({
  status,
  size = "md",
}: OrderStatusBadgeProps) {
  const normalizedKey = (status || "").toUpperCase();
  const config = orderStatusConfig[normalizedKey] || {
    label: status || "Unknown",
    badgeClass:
      "bg-gray-100 text-gray-700 dark:bg-gray-800 dark:text-gray-300 border border-gray-200 dark:border-gray-700",
    dotClass: "bg-gray-400",
  };

  const sizeClasses =
    size === "sm"
      ? "px-2 py-0.5 text-xs font-medium"
      : "px-2.5 py-1 text-xs font-semibold";

  return (
    <span
      className={`inline-flex items-center gap-1.5 rounded-full transition-colors ${sizeClasses} ${config.badgeClass}`}
    >
      <span className={`h-1.5 w-1.5 rounded-full ${config.dotClass}`} />
      <span>{config.label}</span>
    </span>
  );
}

export function PurchaseOrderDeliveryStatusBadge({
  status,
  size = "md",
}: DeliveryStatusBadgeProps) {
  const normalizedKey = (status || "").toUpperCase();
  const config = deliveryStatusConfig[normalizedKey] || {
    label: status || "Unknown",
    badgeClass:
      "bg-gray-100 text-gray-600 dark:bg-gray-800 dark:text-gray-400 border border-gray-200/80 dark:border-gray-700",
    dotClass: "bg-gray-400",
  };

  const sizeClasses =
    size === "sm"
      ? "px-2 py-0.5 text-xs font-medium"
      : "px-2.5 py-1 text-xs font-semibold";

  return (
    <span
      className={`inline-flex items-center gap-1.5 rounded-full transition-colors ${sizeClasses} ${config.badgeClass}`}
    >
      <span className={`h-1.5 w-1.5 rounded-full ${config.dotClass}`} />
      <span>{config.label}</span>
    </span>
  );
}
