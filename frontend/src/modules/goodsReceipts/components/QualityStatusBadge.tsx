import Badge from "../../../shared/components/ui/badge/Badge";
import {
  QUALITY_STATUS_LABELS,
  RECEIPT_STATUS_LABELS,
  type QualityStatus,
  type ReceiptStatus,
} from "../types/goodsReceipt.types";

type BadgeColor = "primary" | "success" | "error" | "warning" | "info" | "light" | "dark";

const QUALITY_COLORS: Record<QualityStatus, BadgeColor> = {
  ACCEPTED: "success",
  REJECTED: "error",
  UNDER_REVIEW: "warning",
  PARTIAL: "warning",
};

const RECEIPT_COLORS: Record<ReceiptStatus, BadgeColor> = {
  DRAFT: "light",
  IN_PROGRESS: "info",
  COMPLETED: "success",
  PARTIAL: "warning",
  CANCELLED: "error",
};

export default function QualityStatusBadge({ status }: { status?: QualityStatus | null }) {
  if (!status) return <span className="text-theme-xs text-gray-400">—</span>;
  return (
    <Badge size="sm" color={QUALITY_COLORS[status]}>
      {QUALITY_STATUS_LABELS[status]}
    </Badge>
  );
}

export function ReceiptStatusBadge({ status, size = "sm" }: { status: ReceiptStatus; size?: "sm" | "md" }) {
  return (
    <Badge size={size} color={RECEIPT_COLORS[status]}>
      {RECEIPT_STATUS_LABELS[status]}
    </Badge>
  );
}
