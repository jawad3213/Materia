import type { RequisitionStatus } from './RequisitionStatus';
import type { RequisitionLine } from './RequisitionLine';

/**
 * Lightweight projection of a Requisition for list tables and cards.
 */
export interface RequisitionListItem {
  id: string;
  requisitionCode: string;
  title: string;
  justification?: string;
  status: RequisitionStatus;
  requesterId: string;
  requesterName: string;
  requiredDate?: string;
  submittedDate?: string;
  approvedDate?: string;
  convertedDate?: string;
  totalAmount: string | number;
  currencyCode: string;
  purchaseOrderId?: string;
  purchaseOrderCode?: string;
  linesCount: number;
  lines: RequisitionLine[];
}
