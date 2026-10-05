import Badge from "../../../shared/components/ui/badge/Badge";
import { REQUISITION_STATUS_INFO, type RequisitionStatus } from "../types/RequisitionStatus";

type BadgeColor = "primary" | "success" | "error" | "warning" | "info" | "light" | "dark";

const STATUS_COLORS: Record<RequisitionStatus, BadgeColor> = {
  DRAFT: "light",
  SUBMITTED: "info",
  UNDER_REVIEW: "warning",
  APPROVED: "success",
  REJECTED: "error",
  CANCELLED: "dark",
  CONVERTED: "primary",
};

export default function RequisitionStatusBadge({ status, size = "sm" }: { status: RequisitionStatus | string; size?: "sm" | "md" }) {
  const key = (status || "").toUpperCase() as RequisitionStatus;
  return (
    <Badge size={size} color={STATUS_COLORS[key] ?? "light"}>
      {key === "CONVERTED" ? "Ordered" : REQUISITION_STATUS_INFO[key]?.label ?? (status || "Unknown")}
    </Badge>
  );
}
