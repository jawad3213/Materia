import type { OrderStatus } from './OrderStatus';
import type { DeliveryStatus } from './DeliveryStatus';

/**
 * Filter tabs for Purchase Order list UI
 */
export type PurchaseOrderFilterTab =
  | 'ALL'
  | 'DRAFT'
  | 'SUBMITTED'
  | 'CONFIRMED'
  | 'READY_FOR_RECEIPT'
  | 'COMPLETED'
  | 'CANCELLED';

/**
 * Query and filter parameters for fetching Purchase Orders
 */
export interface PurchaseOrderFilterParams {
  status?: OrderStatus | 'ALL';
  deliveryStatus?: DeliveryStatus | 'ALL';
  supplierId?: string;
  requisitionId?: string;
  keyword?: string;
  startDate?: string;
  endDate?: string;
  page?: number;
  size?: number;
  sortBy?: string;
  sortDirection?: 'asc' | 'desc';
}

/**
 * Summary statistics for Purchase Order overview dashboards
 */
export interface PurchaseOrderStats {
  total: number;
  draft: number;
  submitted: number;
  confirmed: number;
  readyForReceipt: number;
  completed: number;
  cancelled: number;
  totalValue: number;
  currency: string;
}
