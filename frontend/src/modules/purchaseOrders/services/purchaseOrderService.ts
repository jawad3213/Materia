import axiosClient from '../../../shared/api/axiosClient';
import type {
  PurchaseOrder,
  CreatePurchaseOrderRequest,
  UpdatePurchaseOrderRequest,
  PurchaseOrderSubmitRequest,
  PurchaseOrderConfirmRequest,
  PurchaseOrderAssignReceiverRequest,
  PurchaseOrderConfirmReceiptRequest,
  PurchaseOrderCancelRequest,
  PurchaseOrderRejectRequest,
  AssignableReceiver,
  PurchaseOrderCompleteRequest,
  UpdatePurchaseOrderDeliveryStatusRequest,
  OrderStatus,
  DeliveryStatus,
} from '../types';

const BASE_URL = '/purchase-orders';

export const purchaseOrderService = {
  // ---- Core CRUD ----
  getAll: () =>
    axiosClient.get<PurchaseOrder[]>(BASE_URL),

  getById: (id: string) =>
    axiosClient.get<PurchaseOrder>(`${BASE_URL}/${id}`),

  getByCode: (code: string) =>
    axiosClient.get<PurchaseOrder>(`${BASE_URL}/code/${code}`),

  create: (data: CreatePurchaseOrderRequest) =>
    axiosClient.post<PurchaseOrder>(BASE_URL, data),

  update: (id: string, data: UpdatePurchaseOrderRequest) =>
    axiosClient.put<PurchaseOrder>(`${BASE_URL}/${id}`, data),

  delete: (id: string) =>
    axiosClient.delete<void>(`${BASE_URL}/${id}`),

  // ---- Query & Filter Endpoints ----
  getByStatus: (status: OrderStatus | string) =>
    axiosClient.get<PurchaseOrder[]>(`${BASE_URL}/status/${status}`),

  getByDeliveryStatus: (deliveryStatus: DeliveryStatus | string) =>
    axiosClient.get<PurchaseOrder[]>(`${BASE_URL}/delivery-status/${deliveryStatus}`),

  getBySupplierId: (supplierId: string) =>
    axiosClient.get<PurchaseOrder[]>(`${BASE_URL}/supplier/${supplierId}`),

  getByRequisitionId: (requisitionId: string) =>
    axiosClient.get<PurchaseOrder[]>(`${BASE_URL}/requisition/${requisitionId}`),

  searchByKeyword: (keyword: string) =>
    axiosClient.get<PurchaseOrder[]>(`${BASE_URL}/search/keyword`, {
      params: { keyword },
    }),

  // ---- Lifecycle & Workflow Mutations ----
  submit: (id: string, data: PurchaseOrderSubmitRequest) =>
    axiosClient.patch<PurchaseOrder>(`${BASE_URL}/${id}/submit`, data),

  confirm: (id: string, data: PurchaseOrderConfirmRequest) =>
    axiosClient.patch<PurchaseOrder>(`${BASE_URL}/${id}/confirm`, data),

  assignReceiver: (id: string, data: PurchaseOrderAssignReceiverRequest) =>
    axiosClient.patch<PurchaseOrder>(`${BASE_URL}/${id}/assign-receiver`, data),

  confirmReceipt: (id: string, data: PurchaseOrderConfirmReceiptRequest) =>
    axiosClient.patch<PurchaseOrder>(`${BASE_URL}/${id}/confirm-receipt`, data),

  reject: (id: string, data: PurchaseOrderRejectRequest) =>
    axiosClient.patch<PurchaseOrder>(`${BASE_URL}/${id}/reject`, data),

  getAssignableReceivers: () =>
    axiosClient.get<AssignableReceiver[]>(`${BASE_URL}/assignable-receivers`),

  cancel: (id: string, data: PurchaseOrderCancelRequest) =>
    axiosClient.patch<PurchaseOrder>(`${BASE_URL}/${id}/cancel`, data),

  complete: (id: string, data: PurchaseOrderCompleteRequest) =>
    axiosClient.patch<PurchaseOrder>(`${BASE_URL}/${id}/complete`, data),

  updateDeliveryStatus: (
    id: string,
    data: UpdatePurchaseOrderDeliveryStatusRequest
  ) =>
    axiosClient.patch<PurchaseOrder>(`${BASE_URL}/${id}/delivery-status`, data),
};

export default purchaseOrderService;
