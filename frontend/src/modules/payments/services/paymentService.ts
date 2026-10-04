import axiosClient from "../../../shared/api/axiosClient";
import type { CompletePaymentRequest, CreatePaymentRequest, Payment } from "../types/payment.types";

const BASE_URL = "/payments";

/** Workflow actions send no user details: the backend takes the acting user from the session. */
export const paymentService = {
  getAll: () => axiosClient.get<Payment[]>(BASE_URL),

  getById: (id: string) => axiosClient.get<Payment>(`${BASE_URL}/${id}`),

  getBySupplierId: (supplierId: string) => axiosClient.get<Payment[]>(`${BASE_URL}/supplier/${supplierId}`),

  create: (data: CreatePaymentRequest) => axiosClient.post<Payment>(BASE_URL, data),

  prepare: (id: string) => axiosClient.patch<Payment>(`${BASE_URL}/${id}/prepare`, {}),

  /** Records the payment on every invoice it covers; all or nothing. */
  complete: (id: string, data: CompletePaymentRequest) => axiosClient.patch<Payment>(`${BASE_URL}/${id}/complete`, data),

  cancel: (id: string, reason: string) => axiosClient.patch<Payment>(`${BASE_URL}/${id}/cancel`, { reason }),

  delete: (id: string) => axiosClient.delete<void>(`${BASE_URL}/${id}`),
};

export default paymentService;
