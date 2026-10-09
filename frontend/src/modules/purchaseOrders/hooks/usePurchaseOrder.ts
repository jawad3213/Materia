import useAuth from "../../auth/hooks/useAuth";
import type { OrderStatus } from "../types";

/**
 * Lifecycle rules mirrored from backend PurchaseOrder.java / OrderStatus.java.
 * Keep these in step with the backend so the UI never offers an action the API refuses.
 */
export const EDITABLE_STATUSES: OrderStatus[] = ["DRAFT", "SUBMITTED"];
export const CANCELLABLE_STATUSES: OrderStatus[] = [
  "DRAFT",
  "SUBMITTED",
  "CONFIRMED",
  "READY_FOR_RECEIPT",
];
export const DELIVERY_TRACKING_STATUSES: OrderStatus[] = [
  "CONFIRMED",
  "READY_FOR_RECEIPT",
  "PARTIALLY_RECEIVED",
];
export const RECEIVABLE_STATUSES: OrderStatus[] = ["READY_FOR_RECEIPT", "PARTIALLY_RECEIVED"];

interface OrderLike {
  status: OrderStatus;
  assignedTo?: string | null;
}

/** Permission-aware action availability for purchase orders, matching the backend's @PreAuthorize rules. */
export default function usePurchaseOrderPermissions() {
  const { user, hasPermission } = useAuth();

  const canWrite = hasPermission("order:write");
  const canValidate = hasPermission("order:validate");
  const canCancelOrders = hasPermission("order:cancel");
  const canRecordReceipts = hasPermission("receipt:write");
  // Deletion additionally requires the ADMIN role on the backend.
  const canDeleteOrders = canWrite && user?.role === "ADMIN";

  const isAssignedReceiver = (order: OrderLike) =>
    !!user?.id && user.id === order.assignedTo;

  return {
    canCreate: canWrite,
    canEdit: (order: OrderLike) => canWrite && EDITABLE_STATUSES.includes(order.status),
    canDelete: (order: OrderLike) => canDeleteOrders && EDITABLE_STATUSES.includes(order.status),
    canSubmit: (order: OrderLike) => canWrite && order.status === "DRAFT",
    canConfirm: (order: OrderLike) => canValidate && order.status === "SUBMITTED",
    canReject: (order: OrderLike) => canValidate && order.status === "SUBMITTED",
    canAssignReceiver: (order: OrderLike) => canWrite && order.status === "CONFIRMED",
    canTrackDelivery: (order: OrderLike) =>
      canWrite && DELIVERY_TRACKING_STATUSES.includes(order.status),
    canReceive: (order: OrderLike) =>
      canRecordReceipts && isAssignedReceiver(order) && RECEIVABLE_STATUSES.includes(order.status),
    canCloseShort: (order: OrderLike) => canWrite && order.status === "PARTIALLY_RECEIVED",
    canCancel: (order: OrderLike) =>
      canCancelOrders && CANCELLABLE_STATUSES.includes(order.status),
  };
}
