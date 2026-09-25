import type { RequisitionLineRequest } from './RequisitionLine';

/**
 * Request payload for updating a purchase requisition.
 */
export interface UpdateRequisitionRequest {
  title?: string;
  description?: string;
  justification?: string;
  requiredDate?: string;
  currencyCode?: string;
  lines?: RequisitionLineRequest[];
  updatedBy?: string;
}

// Backward-compatible alias
export type UpdateRequisitionWebRequest = UpdateRequisitionRequest;
