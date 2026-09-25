import type { RequisitionLineRequest } from './RequisitionLine';

/**
 * Request payload for creating a purchase requisition.
 */
export interface CreateRequisitionRequest {
  title: string;
  description?: string;
  justification?: string;
  requesterId?: string;
  requesterName: string;
  requiredDate?: string;
  currencyCode?: string;
  lines: RequisitionLineRequest[];
  createdBy?: string;
  status?: 'DRAFT' | 'SUBMITTED' | string;
}

// Backward-compatible alias
export type CreateRequisitionWebRequest = CreateRequisitionRequest;
