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
    label: 'Non expédié',
    description: 'Commande non encore expédiée',
    color: '#94a3b8',
  },
  SHIPPED: {
    code: 'SHIPPED',
    label: 'Expédié',
    description: 'Commande expédiée',
    color: '#3b82f6',
  },
  IN_TRANSIT: {
    code: 'IN_TRANSIT',
    label: 'En transit',
    description: 'Commande en cours de transport',
    color: '#8b5cf6',
  },
  PARTIAL: {
    code: 'PARTIAL',
    label: 'Partielle',
    description: 'Commande partiellement livrée',
    color: '#f59e0b',
  },
  DELIVERED: {
    code: 'DELIVERED',
    label: 'Livrée',
    description: 'Commande complètement livrée',
    color: '#22c55e',
  },
  DELAYED: {
    code: 'DELAYED',
    label: 'Retardée',
    description: 'Commande en retard',
    color: '#ef4444',
  },
};
