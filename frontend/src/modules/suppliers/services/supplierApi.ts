import axiosClient from '../../../shared/api/axiosClient';
import type { Supplier } from '../types/Supplier';
import type { SupplierListItem } from '../types/SupplierListItem';
import type { CreateSupplierRequest } from '../types/CreateSupplierRequest';
import type { UpdateSupplierRequest } from '../types/UpdateSupplierRequest';

const BASE_URL = '/masterdata/suppliers';

export const supplierApi = {
  // ---- Core CRUD ----
  create: (data: CreateSupplierRequest) => axiosClient.post<Supplier>(BASE_URL, data),
  getById: (id: string) => axiosClient.get<Supplier>(`${BASE_URL}/${id}`),
  getByCode: (code: string) => axiosClient.get<Supplier>(`${BASE_URL}/code/${code}`),
  getAll: () => axiosClient.get<SupplierListItem[]>(BASE_URL),
  update: (id: string, data: UpdateSupplierRequest) => axiosClient.put<Supplier>(`${BASE_URL}/${id}`, data),
  delete: (id: string) => axiosClient.delete(`${BASE_URL}/${id}`),
  
  // ---- Custom Endpoints ----
  search: (keyword: string) => axiosClient.get<SupplierListItem[]>(`${BASE_URL}/search`, { params: { keyword } }),
};
