export * from './RequisitionStatus';
export * from './RequisitionLine';
export * from './CreateRequisitionRequest';
export * from './UpdateRequisitionRequest';
export * from './RequisitionSearchRequest';
export * from './Requisition';
export * from './RequisitionListItem';

export type RequisitionFilterTab = 'ALL' | 'PENDING' | 'APPROVED' | 'CONVERTED' | 'DRAFT';

export interface RequisitionStats {
  total: number;
  pendingReview: number;
  readyForPo: number;
  converted: number;
  totalEstimatedValue: number;
  currency: string;
}
