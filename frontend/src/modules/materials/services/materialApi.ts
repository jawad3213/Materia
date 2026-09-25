import axiosClient from '../../../shared/api/axiosClient';
import type { CreateMaterialRequest } from '../types/CreateMaterialRequest';
import type { UpdateMaterialRequest } from '../types/UpdateMaterialRequest';
import type { MaterialSearchRequest } from '../types/MaterialSearchRequest';
import type { MaterialFilterRequest } from '../types/MaterialFilterRequest';
import type { ReorderRecommendation, ManualReorderResponse } from '../types/Material';

const BASE_URL = '/masterdata/materials';

export const materialApi = {
  // ---- Core CRUD ----
  create: (data: CreateMaterialRequest) => axiosClient.post(BASE_URL, data),
  getById: (id: string) => axiosClient.get(`${BASE_URL}/${id}`),
  getByCode: (code: string) => axiosClient.get(`${BASE_URL}/code/${code}`),
  getAll: (page = 0, size = 10) => axiosClient.get(`${BASE_URL}/list`, { params: { page, size } }),
  update: (id: string, data: UpdateMaterialRequest) => axiosClient.put(`${BASE_URL}/${id}`, data),
  delete: (id: string) => axiosClient.delete(`${BASE_URL}/${id}`),
  
// ---- Search ----
  searchAdvancedList: (criteria: MaterialSearchRequest, page = 0, size = 10) => 
    axiosClient.post(`${BASE_URL}/search/list`, criteria, { params: { page, size } }),

  //---- Filter ----
  filterList: (criteria: MaterialFilterRequest, page = 0, size = 10) => 
    axiosClient.post(`${BASE_URL}/filter/list`, criteria, { params: { page, size } }),
  


  // ---- Stock Management & Thresholds ----
  getOutOfStock: () => axiosClient.get(`${BASE_URL}/stock/out-of-stock`),
  getReorderNeeded: () => axiosClient.get(`${BASE_URL}/stock/reorder-needed`),
  getCriticalStock: () => axiosClient.get(`${BASE_URL}/stock/critical`),
  increaseStock: (id: string, quantity: number) => 
    axiosClient.patch(`${BASE_URL}/${id}/stock/increase`, null, { params: { quantity } }),
  decreaseStock: (id: string, quantity: number) => 
    axiosClient.patch(`${BASE_URL}/${id}/stock/decrease`, null, { params: { quantity } }),
  
  // ---- Reorder & EOQ Recommendation (1-Click) ----
  getReorderRecommendation: (id: string) => 
    axiosClient.get<ReorderRecommendation>(`${BASE_URL}/${id}/reorder-recommendation`),
  triggerManualReorder: (id: string, data?: { quantity?: number; reason?: string }) => 
    axiosClient.post<ManualReorderResponse>(`${BASE_URL}/${id}/reorder`, data),

};
