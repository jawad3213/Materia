/**
 * Request payload for advanced requisition search.
 */
export interface RequisitionSearchRequest {
  keyword?: string;
  requesterId?: string;
  approverId?: string;
  status?: string;
  currencyCode?: string;
  requiredDateFrom?: string;
  requiredDateTo?: string;
  submittedDateFrom?: string;
  submittedDateTo?: string;
}

// Backward-compatible alias
export type RequisitionSearchWebRequest = RequisitionSearchRequest;
