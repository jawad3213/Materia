import axiosClient from "../../../shared/api/axiosClient";
import type { CreateGoodsReceiptRequest, GoodsReceipt } from "../types/goodsReceipt.types";

const BASE_URL = "/goods-receipts";

export const goodsReceiptService = {
  getAll: () => axiosClient.get<GoodsReceipt[]>(BASE_URL),

  getById: (id: string) => axiosClient.get<GoodsReceipt>(`${BASE_URL}/${id}`),

  getByPurchaseOrderId: (purchaseOrderId: string) =>
    axiosClient.get<GoodsReceipt[]>(`${BASE_URL}/purchase-order/${purchaseOrderId}`),

  create: (data: CreateGoodsReceiptRequest) => axiosClient.post<GoodsReceipt>(BASE_URL, data),

  complete: (id: string) => axiosClient.patch<GoodsReceipt>(`${BASE_URL}/${id}/complete`, {}),

  cancel: (id: string, reason: string) =>
    axiosClient.patch<GoodsReceipt>(`${BASE_URL}/${id}/cancel`, { reason }),

  delete: (id: string) => axiosClient.delete<void>(`${BASE_URL}/${id}`),
};

export default goodsReceiptService;
