/**
 * Requisition status enum matching backend RequisitionStatus.java
 */
export type RequisitionStatus =
  | 'DRAFT'
  | 'SUBMITTED'
  | 'UNDER_REVIEW'
  | 'APPROVED'
  | 'REJECTED'
  | 'CANCELLED'
  | 'CONVERTED';

export const RequisitionStatusEnum = {
  DRAFT: 'DRAFT',
  SUBMITTED: 'SUBMITTED',
  UNDER_REVIEW: 'UNDER_REVIEW',
  APPROVED: 'APPROVED',
  REJECTED: 'REJECTED',
  CANCELLED: 'CANCELLED',
  CONVERTED: 'CONVERTED',
} as const;

export interface RequisitionStatusInfo {
  code: RequisitionStatus;
  label: string;
  description: string;
  color: string;
}

export const REQUISITION_STATUS_INFO: Record<RequisitionStatus, RequisitionStatusInfo> = {
  DRAFT: {
    code: 'DRAFT',
    label: 'Draft',
    description: 'Requisition is being prepared',
    color: '#94a3b8',
  },
  SUBMITTED: {
    code: 'SUBMITTED',
    label: 'Submitted',
    description: 'Requisition submitted for processing',
    color: '#2563eb',
  },
  UNDER_REVIEW: {
    code: 'UNDER_REVIEW',
    label: 'Under Review',
    description: 'Requisition is under review',
    color: '#3b82f6',
  },
  APPROVED: {
    code: 'APPROVED',
    label: 'Approved',
    description: 'Requisition approved for conversion',
    color: '#22c55e',
  },
  REJECTED: {
    code: 'REJECTED',
    label: 'Rejected',
    description: 'Requisition rejected',
    color: '#ef4444',
  },
  CANCELLED: {
    code: 'CANCELLED',
    label: 'Cancelled',
    description: 'Requisition cancelled',
    color: '#6b7280',
  },
  CONVERTED: {
    code: 'CONVERTED',
    label: 'Converted',
    description: 'Requisition converted to purchase order',
    color: '#8b5cf6',
  },
};
