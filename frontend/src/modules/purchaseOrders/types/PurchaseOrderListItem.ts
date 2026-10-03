import type { OrderStatus } from './OrderStatus';
import type { DeliveryStatus } from './DeliveryStatus';

/**
 * Lightweight row representation for purchase order tables / lists.
 */
export interface PurchaseOrderListItem {
  id: string;
  orderCode: string;
  requisitionId?: string | null;
  requisitionCode?: string | null;
  status: OrderStatus;
  deliveryStatus: DeliveryStatus;
  supplierId: string;
  supplierName: string;
  supplierCode?: string | null;
  orderDate: string;
  expectedDeliveryDate?: string | null;
  currencyCode: string;
  totalAmount: string | number;
  grandTotal: string | number;
  orderedByName?: string | null;
  assignedToName?: string | null;
  linesCount: number;
  createdAt: string;
}
