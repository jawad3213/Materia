import Badge from "../../../shared/components/ui/badge/Badge";
import { INVOICE_STATUS_LABELS, INVOICE_TYPE_LABELS, type InvoiceStatus, type InvoiceType } from "../types/invoice.types";

type BadgeColor = "primary" | "success" | "error" | "warning" | "info" | "light" | "dark";

const STATUS_COLORS: Record<InvoiceStatus, BadgeColor> = {
  DRAFT: "light",
  SUBMITTED: "info",
  VERIFIED: "primary",
  PAID: "success",
  CANCELLED: "error",
};

export default function InvoiceStatusBadge({ status, size = "sm" }: { status: InvoiceStatus; size?: "sm" | "md" }) {
  return (
    <Badge size={size} color={STATUS_COLORS[status]}>
      {INVOICE_STATUS_LABELS[status]}
    </Badge>
  );
}

export function InvoiceTypeBadge({ type }: { type: InvoiceType }) {
  return (
    <Badge size="sm" color={type === "CREDIT_NOTE" ? "warning" : "light"}>
      {INVOICE_TYPE_LABELS[type]}
    </Badge>
  );
}
