import useAuth from "../../auth/hooks/useAuth";
import { OPEN_PAYMENT_STATUSES, type Payment } from "../types/payment.types";

/**
 * Action availability for payments, following Role.java: administrators manage payments
 * (`payment:write`); purchasers only consult them (`payment:read`). The statuses mirror the backend
 * Payment entity: prepare a draft, complete or cancel an open payment, delete only a draft.
 */
export default function usePaymentPermissions() {
  const { hasPermission } = useAuth();
  const canManage = hasPermission("payment:write");

  return {
    canCreate: canManage,
    canPrepare: (payment: Payment) => canManage && payment.status === "DRAFT",
    canComplete: (payment: Payment) => canManage && OPEN_PAYMENT_STATUSES.includes(payment.status),
    canCancel: (payment: Payment) => canManage && OPEN_PAYMENT_STATUSES.includes(payment.status),
    canDelete: (payment: Payment) => canManage && payment.status === "DRAFT",
  };
}
