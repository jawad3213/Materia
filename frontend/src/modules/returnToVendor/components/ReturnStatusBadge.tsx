import Badge from "../../../shared/components/ui/badge/Badge";
import {
  RESOLUTION_TYPE_LABELS,
  RETURN_STATUS_LABELS,
  type ResolutionType,
  type ReturnStatus,
} from "../types/returnToVendor.types";

type BadgeColor = "primary" | "success" | "error" | "warning" | "info" | "light" | "dark";

const STATUS_COLORS: Record<ReturnStatus, BadgeColor> = {
  DRAFT: "light",
  PENDING: "warning",
  RESOLVED: "success",
  CANCELLED: "error",
};

export default function ReturnStatusBadge({ status, size = "sm" }: { status: ReturnStatus; size?: "sm" | "md" }) {
  return (
    <Badge size={size} color={STATUS_COLORS[status]}>
      {RETURN_STATUS_LABELS[status]}
    </Badge>
  );
}

export function ResolutionBadge({ type }: { type: ResolutionType }) {
  return (
    <Badge size="sm" color={type === "REPLACEMENT" ? "info" : "primary"}>
      {RESOLUTION_TYPE_LABELS[type]}
    </Badge>
  );
}
