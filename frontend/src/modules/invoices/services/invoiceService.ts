import axiosClient from "../../../shared/api/axiosClient";
import type { CreateInvoiceRequest, Invoice } from "../types/invoice.types";

const BASE_URL = "/invoices";

/**
 * Workflow actions send no user details: the backend takes the acting user from the session and
 * names them from their account.
 */
export const invoiceService = {
  getAll: () => axiosClient.get<Invoice[]>(BASE_URL),

  getById: (id: string) => axiosClient.get<Invoice>(`${BASE_URL}/${id}`),

  getByPurchaseOrderId: (purchaseOrderId: string) =>
    axiosClient.get<Invoice[]>(`${BASE_URL}/purchase-order/${purchaseOrderId}`),

  create: (data: CreateInvoiceRequest) => axiosClient.post<Invoice>(BASE_URL, data),

  submit: (id: string) => axiosClient.patch<Invoice>(`${BASE_URL}/${id}/submit`, {}),

  verify: (id: string) => axiosClient.patch<Invoice>(`${BASE_URL}/${id}/verify`, {}),

  /** Records a payment; payments accumulate until the invoice total is reached. */
  pay: (id: string, amount: number) => axiosClient.patch<Invoice>(`${BASE_URL}/${id}/pay`, { amount }),

  cancel: (id: string, reason: string) => axiosClient.patch<Invoice>(`${BASE_URL}/${id}/cancel`, { reason }),

  delete: (id: string) => axiosClient.delete<void>(`${BASE_URL}/${id}`),
};

export default invoiceService;
