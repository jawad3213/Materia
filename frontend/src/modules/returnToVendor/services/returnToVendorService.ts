import axiosClient from "../../../shared/api/axiosClient";
import type { CreateReturnRequest, ResolveReturnRequest, ReturnToVendor } from "../types/returnToVendor.types";

const BASE_URL = "/return-to-vendors";

/** Workflow actions send no user details: the backend takes the acting user from the session. */
export const returnToVendorService = {
  getAll: () => axiosClient.get<ReturnToVendor[]>(BASE_URL),

  getById: (id: string) => axiosClient.get<ReturnToVendor>(`${BASE_URL}/${id}`),

  getByGoodsReceiptId: (goodsReceiptId: string) =>
    axiosClient.get<ReturnToVendor[]>(`${BASE_URL}/goods-receipt/${goodsReceiptId}`),

  create: (data: CreateReturnRequest) => axiosClient.post<ReturnToVendor>(BASE_URL, data),

  /** The goods were shipped back to the supplier. */
  submit: (id: string) => axiosClient.patch<ReturnToVendor>(`${BASE_URL}/${id}/submit`),

  /** A replacement reopens the purchase order; a credit note records its number and amount. */
  resolve: (id: string, data: ResolveReturnRequest) => axiosClient.patch<ReturnToVendor>(`${BASE_URL}/${id}/resolve`, data),

  cancel: (id: string, reason: string) => axiosClient.patch<ReturnToVendor>(`${BASE_URL}/${id}/cancel`, { reason }),

  delete: (id: string) => axiosClient.delete<void>(`${BASE_URL}/${id}`),
};

export default returnToVendorService;
