import axiosClient from '../../../shared/api/axiosClient';
import type { CreateMaterialRequest } from '../types/CreateMaterialRequest';
import type { UpdateMaterialRequest } from '../types/UpdateMaterialRequest';
import type { MaterialListItem } from '../types/MaterialListItem';
import type { MaterialSearchRequest } from '../types/MaterialSearchRequest';
import type { MaterialFilterRequest } from '../types/MaterialFilterRequest';
// Import Material related interfaces as they are built out
// import { Material } from '../types/Material';

const BASE_URL = '/masterdata/materials';

export const materialApi = {
  // ---- Core CRUD ----
  create: (data: CreateMaterialRequest) => axiosClient.post(BASE_URL, data),
  getById: (id: string) => axiosClient.get(`${BASE_URL}/${id}`),
  getByCode: (code: string) => axiosClient.get(`${BASE_URL}/code/${code}`),
  getAll: (page = 0, size = 10) => axiosClient.get(`${BASE_URL}/list`, { params: { page, size } }),
  update: (id: string, data: UpdateMaterialRequest) => axiosClient.put(`${BASE_URL}/${id}`, data),
  delete: (id: string) => axiosClient.delete(`${BASE_URL}/${id}`),
  
  // ---- Filtering ----
  getByCategory: (categoryId: string) => axiosClient.get(`${BASE_URL}/category/${categoryId}`),
  getBySupplier: (supplierId: string) => axiosClient.get(`${BASE_URL}/supplier/${supplierId}`),
  getByStatus: (status: string) => axiosClient.get(`${BASE_URL}/status/${status}`),
  getByMaterialType: (materialType: string) => axiosClient.get(`${BASE_URL}/type/${materialType}`),
  
  // ---- Stock Management ----
  getLowStock: () => axiosClient.get(`${BASE_URL}/stock/low`),
  getAvailableStock: () => axiosClient.get(`${BASE_URL}/stock/available`),
  getOutOfStock: () => axiosClient.get(`${BASE_URL}/stock/out`),
  increaseStock: (id: string, quantity: number) => 
    axiosClient.patch(`${BASE_URL}/${id}/stock/increase`, null, { params: { quantity } }),
  decreaseStock: (id: string, quantity: number) => 
    axiosClient.patch(`${BASE_URL}/${id}/stock/decrease`, null, { params: { quantity } }),
  
  // ---- Search ----
  searchAdvancedList: (criteria: MaterialSearchRequest, page = 0, size = 10) => 
    axiosClient.post(`${BASE_URL}/search/list`, criteria, { params: { page, size } }),
  filterList: (criteria: MaterialFilterRequest, page = 0, size = 10) => 
    axiosClient.post(`${BASE_URL}/filter/list`, criteria, { params: { page, size } }),
};
