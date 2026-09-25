import type { RequisitionStatus } from './RequisitionStatus';
import type { RequisitionLine } from './RequisitionLine';

/**
 * Purchase requisition model.
 * Matches backend RequisitionWebResponse.
 */
export interface Requisition {
  id: string;
  requisitionCode: string;
  title: string;
  description?: string;
  justification?: string;
  status: RequisitionStatus;
  requesterId: string;
  requesterName: string;
  requiredDate?: string;
  submittedDate?: string;
  approvedDate?: string;
  convertedDate?: string;
  cancelledDate?: string;
  totalAmount: string | number;
  currencyCode: string;
  approverId?: string;
  approverName?: string;
  rejectionReason?: string;
  approvalNotes?: string;
  cancellationReason?: string;
  purchaseOrderId?: string;
  purchaseOrderCode?: string;
  lines: RequisitionLine[];
  createdBy: string;
  createdAt: string;
  updatedBy?: string;
  updatedAt?: string;
}

// Backward-compatible alias
export type RequisitionWebResponse = Requisition;
