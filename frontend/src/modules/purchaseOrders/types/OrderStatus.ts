/**
 * Order status enum and utilities matching backend OrderStatus.java
 */
export type OrderStatus =
  | 'DRAFT'
  | 'SUBMITTED'
  | 'CONFIRMED'
  | 'READY_FOR_RECEIPT'
  | 'PARTIALLY_RECEIVED'
  | 'COMPLETED'
  | 'CANCELLED'
  | 'REJECTED';

export const OrderStatusEnum = {
  DRAFT: 'DRAFT',
  SUBMITTED: 'SUBMITTED',
  CONFIRMED: 'CONFIRMED',
  READY_FOR_RECEIPT: 'READY_FOR_RECEIPT',
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
    label: 'Draft',
    description: 'Order being prepared',
    color: '#94a3b8',
  },
  SUBMITTED: {
    code: 'SUBMITTED',
    label: 'Submitted',
    description: 'Sent to the supplier',
    color: '#f59e0b',
  },
  CONFIRMED: {
    code: 'CONFIRMED',
    label: 'Confirmed',
    description: 'Confirmed by the supplier',
    color: '#3b82f6',
  },
  READY_FOR_RECEIPT: {
    code: 'READY_FOR_RECEIPT',
    label: 'Ready for Receipt',
    description: 'Assigned to a receiver',
    color: '#0ea5e9',
  },
  PARTIALLY_RECEIVED: {
    code: 'PARTIALLY_RECEIVED',
    label: 'Partly Received',
    description: 'Part of the order was delivered',
    color: '#f59e0b',
  },
  COMPLETED: {
    code: 'COMPLETED',
    label: 'Completed',
    description: 'Order fully delivered',
    color: '#22c55e',
  },
  CANCELLED: {
    code: 'CANCELLED',
    label: 'Cancelled',
    description: 'Order cancelled',
    color: '#ef4444',
  },
  REJECTED: {
    code: 'REJECTED',
    label: 'Rejected',
    description: 'Rejected by the supplier',
    color: '#dc2626',
  },
};

export const isOrderStatusActive = (status: OrderStatus): boolean =>
  status !== 'CANCELLED' && status !== 'REJECTED' && status !== 'COMPLETED';

export const isOrderStatusModifiable = (status: OrderStatus): boolean =>
  status === 'DRAFT' || status === 'SUBMITTED';

export const isOrderStatusCancellable = (status: OrderStatus): boolean =>
  isOrderStatusActive(status) && status !== 'COMPLETED';
