/**
 * Order status enum and utilities matching backend OrderStatus.java
 */
export type OrderStatus =
  | 'DRAFT'
  | 'SUBMITTED'
  | 'CONFIRMED'
  | 'READY_FOR_RECEIPT'
  | 'RECEIVED'
  | 'PARTIALLY_RECEIVED'
  | 'COMPLETED'
  | 'CANCELLED'
  | 'REJECTED';

export const OrderStatusEnum = {
  DRAFT: 'DRAFT',
  SUBMITTED: 'SUBMITTED',
  CONFIRMED: 'CONFIRMED',
  READY_FOR_RECEIPT: 'READY_FOR_RECEIPT',
  RECEIVED: 'RECEIVED',
  PARTIALLY_RECEIVED: 'PARTIALLY_RECEIVED',
  COMPLETED: 'COMPLETED',
  CANCELLED: 'CANCELLED',
  REJECTED: 'REJECTED',
} as const;

export interface OrderStatusInfo {
  code: OrderStatus;
  label: string;
  description: string;
  color: string;
}

export const ORDER_STATUS_INFO: Record<OrderStatus, OrderStatusInfo> = {
  DRAFT: {
    code: 'DRAFT',
    label: 'Brouillon',
    description: 'Commande en cours de saisie',
    color: '#94a3b8',
  },
  SUBMITTED: {
    code: 'SUBMITTED',
    label: 'Soumise',
    description: 'Commande soumise au fournisseur',
    color: '#f59e0b',
  },
  CONFIRMED: {
    code: 'CONFIRMED',
    label: 'Confirmée',
    description: 'Commande confirmée par le fournisseur',
    color: '#3b82f6',
  },
  READY_FOR_RECEIPT: {
    code: 'READY_FOR_RECEIPT',
    label: 'Prête pour réception',
    description: 'Commande assignée pour réception',
    color: '#0ea5e9',
  },
  RECEIVED: {
    code: 'RECEIVED',
    label: 'Reçue',
    description: 'Commande réceptionnée',
    color: '#10b981',
  },
  PARTIALLY_RECEIVED: {
    code: 'PARTIALLY_RECEIVED',
    label: 'Partiellement reçue',
    description: 'Commande partiellement livrée',
    color: '#f59e0b',
  },
  COMPLETED: {
    code: 'COMPLETED',
    label: 'Terminée',
    description: 'Commande complètement livrée',
    color: '#22c55e',
  },
  CANCELLED: {
    code: 'CANCELLED',
    label: 'Annulée',
    description: 'Commande annulée',
    color: '#ef4444',
  },
  REJECTED: {
    code: 'REJECTED',
    label: 'Rejetée',
    description: 'Commande rejetée par le fournisseur',
    color: '#dc2626',
  },
};

export const isOrderStatusActive = (status: OrderStatus): boolean =>
  status !== 'CANCELLED' && status !== 'REJECTED' && status !== 'COMPLETED' && status !== 'RECEIVED';

export const isOrderStatusModifiable = (status: OrderStatus): boolean =>
  status === 'DRAFT' || status === 'SUBMITTED';

export const isOrderStatusCancellable = (status: OrderStatus): boolean =>
  isOrderStatusActive(status) && status !== 'COMPLETED' && status !== 'RECEIVED';
