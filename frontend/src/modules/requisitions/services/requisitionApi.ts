import axiosClient from '../../../shared/api/axiosClient';
import type {
  CreateRequisitionRequest,
  UpdateRequisitionRequest,
  RequisitionSearchRequest,
  Requisition,
  RequisitionStatus,
} from '../types';

const BASE_URL = '/purchase-requisitions';

export interface PageResponse<T> {
  content: T[];
  pageNumber: number;
  pageSize: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
}

export const requisitionApi = {
  // ---- Core CRUD ----
  create: (data: CreateRequisitionRequest) =>
    axiosClient.post<Requisition>(BASE_URL, data),

  getById: (id: string) =>
    axiosClient.get<Requisition>(`${BASE_URL}/${id}`),

  getByCode: (code: string) =>
    axiosClient.get<Requisition>(`${BASE_URL}/code/${code}`),

  getAll: () =>
    axiosClient.get<Requisition[]>(BASE_URL),

  update: (id: string, data: UpdateRequisitionRequest) =>
    axiosClient.put<Requisition>(`${BASE_URL}/${id}`, data),

  delete: (id: string) =>
    axiosClient.delete<void>(`${BASE_URL}/${id}`),

  // ---- Query & Filter Endpoints ----
  getByStatus: (status: RequisitionStatus | string) =>
    axiosClient.get<Requisition[]>(`${BASE_URL}/status/${status}`),

  getByRequester: (requesterId: string) =>
    axiosClient.get<Requisition[]>(`${BASE_URL}/requester/${requesterId}`),

  // ---- Search ----
  searchByKeyword: (keyword: string) =>
    axiosClient.get<Requisition[]>(`${BASE_URL}/search/keyword`, {
      params: { keyword },
    }),

  searchAdvanced: (criteria: RequisitionSearchRequest = {}, page = 0, size = 10) => {
    // Strip empty strings so Jackson does not throw DateTimeParseException on empty date fields
    const cleanedCriteria: Record<string, unknown> = {};
    Object.entries(criteria || {}).forEach(([key, value]) => {
      if (value !== undefined && value !== null && value !== '') {
        cleanedCriteria[key] = value;
      }
    });

    return axiosClient.post<PageResponse<Requisition>>(`${BASE_URL}/search`, cleanedCriteria, {
      params: { page, size },
    });
  },

  // ---- Workflow Lifecycle Actions ----
  submit: (id: string, userId: string = 'current-user') =>
    axiosClient.patch<Requisition>(`${BASE_URL}/${id}/submit`, {}, {
      params: { userId },
    }),

  approve: (
    id: string,
    approverId: string = 'current-approver',
    approverName: string = 'Current Approver',
    notes?: string
  ) =>
    axiosClient.patch<Requisition>(`${BASE_URL}/${id}/approve`, {}, {
      params: { approverId, approverName, ...(notes ? { notes } : {}) },
    }),

  reject: (
    id: string,
    reason: string,
    approverId: string = 'current-approver',
    approverName: string = 'Current Approver'
  ) =>
    axiosClient.patch<Requisition>(`${BASE_URL}/${id}/reject`, {}, {
      params: { approverId, approverName, reason },
    }),

  cancel: (id: string, userId: string = 'current-user', reason?: string) =>
    axiosClient.patch<Requisition>(`${BASE_URL}/${id}/cancel`, {}, {
      params: { userId, ...(reason ? { reason } : {}) },
    }),

  convert: (
    id: string,
    purchaseOrderId: string,
    purchaseOrderCode: string,
    userId: string = 'current-user'
  ) =>
    axiosClient.patch<Requisition>(`${BASE_URL}/${id}/convert`, {}, {
      params: { purchaseOrderId, purchaseOrderCode, userId },
    }),
};
