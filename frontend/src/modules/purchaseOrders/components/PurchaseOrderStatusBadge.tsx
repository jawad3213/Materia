import Badge from "../../../shared/components/ui/badge/Badge";
import { ORDER_STATUS_INFO, type OrderStatus } from "../types/OrderStatus";
import { DELIVERY_STATUS_INFO, type DeliveryStatus } from "../types/DeliveryStatus";

type BadgeColor = "primary" | "success" | "error" | "warning" | "info" | "light" | "dark";

const ORDER_COLORS: Record<OrderStatus, BadgeColor> = {
  DRAFT: "light",
  SUBMITTED: "warning",
  CONFIRMED: "info",
  READY_FOR_RECEIPT: "primary",
  RECEIVED: "success",
  PARTIALLY_RECEIVED: "warning",
  COMPLETED: "success",
  CANCELLED: "error",
  REJECTED: "error",
};

const DELIVERY_COLORS: Record<DeliveryStatus, BadgeColor> = {
  NOT_SHIPPED: "light",
  SHIPPED: "info",
  IN_TRANSIT: "primary",
  PARTIAL: "warning",
  DELIVERED: "success",
  DELAYED: "error",
};

interface BadgeProps<T extends string> {
  status: T | string | null | undefined;
  size?: "sm" | "md";
}

export default function PurchaseOrderStatusBadge({ status, size = "sm" }: BadgeProps<OrderStatus>) {
  const key = (status || "").toUpperCase() as OrderStatus;
  return (
    <Badge size={size} color={ORDER_COLORS[key] ?? "light"}>
      {ORDER_STATUS_INFO[key]?.label ?? (status || "Unknown")}
    </Badge>
  );
}

export function PurchaseOrderDeliveryStatusBadge({ status, size = "sm" }: BadgeProps<DeliveryStatus>) {
  const key = (status || "").toUpperCase() as DeliveryStatus;
  return (
    <Badge size={size} color={DELIVERY_COLORS[key] ?? "light"}>
      {DELIVERY_STATUS_INFO[key]?.label ?? (status || "Unknown")}
    </Badge>
  );
}
