import Badge from "../../../shared/components/ui/badge/Badge";
import { PAYMENT_STATUS_LABELS, type PaymentStatus } from "../types/payment.types";

type BadgeColor = "primary" | "success" | "error" | "warning" | "info" | "light" | "dark";

const STATUS_COLORS: Record<PaymentStatus, BadgeColor> = {
  DRAFT: "light",
  PENDING: "info",
  COMPLETED: "success",
  CANCELLED: "error",
};

export default function PaymentStatusBadge({ status, size = "sm" }: { status: PaymentStatus; size?: "sm" | "md" }) {
  return (
    <Badge size={size} color={STATUS_COLORS[status]}>
      {PAYMENT_STATUS_LABELS[status]}
    </Badge>
  );
}
