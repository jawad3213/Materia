/**
 * Delivery status enum and metadata matching backend DeliveryStatus.java
 */
export type DeliveryStatus =
  | 'NOT_SHIPPED'
  | 'SHIPPED'
  | 'IN_TRANSIT'
  | 'PARTIAL'
  | 'DELIVERED'
  | 'DELAYED';

export const DeliveryStatusEnum = {
  NOT_SHIPPED: 'NOT_SHIPPED',
  SHIPPED: 'SHIPPED',
  IN_TRANSIT: 'IN_TRANSIT',
  PARTIAL: 'PARTIAL',
  DELIVERED: 'DELIVERED',
  DELAYED: 'DELAYED',
} as const;

export interface DeliveryStatusInfo {
  code: DeliveryStatus;
  label: string;
  description: string;
  color: string;
}

export const DELIVERY_STATUS_INFO: Record<DeliveryStatus, DeliveryStatusInfo> = {
  NOT_SHIPPED: {
    code: 'NOT_SHIPPED',
    label: 'Not Shipped',
    description: 'Not shipped yet',
    color: '#94a3b8',
  },
  SHIPPED: {
    code: 'SHIPPED',
    label: 'Shipped',
    description: 'Shipped by the supplier',
    color: '#3b82f6',
  },
  IN_TRANSIT: {
    code: 'IN_TRANSIT',
    label: 'In Transit',
    description: 'On its way',
    color: '#8b5cf6',
  },
  PARTIAL: {
    code: 'PARTIAL',
    label: 'Partial',
    description: 'Partly delivered',
    color: '#f59e0b',
  },
  DELIVERED: {
    code: 'DELIVERED',
    label: 'Delivered',
    description: 'Fully delivered',
    color: '#22c55e',
  },
  DELAYED: {
    code: 'DELAYED',
    label: 'Delayed',
    description: 'Behind schedule',
    color: '#ef4444',
  },
};
