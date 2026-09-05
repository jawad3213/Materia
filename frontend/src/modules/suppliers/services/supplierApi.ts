import axiosClient from '../../../shared/api/axiosClient';
import type { Supplier } from '../types/Supplier';
import type { SupplierListItem } from '../types/SupplierListItem';
import type { CreateSupplierRequest } from '../types/CreateSupplierRequest';
import type { UpdateSupplierRequest } from '../types/UpdateSupplierRequest';
import type { SupplierFilterRequest } from '../types/SupplierFilterRequest';
import type { SupplierSearchRequest } from '../types/SupplierSearchRequest';

const BASE_URL = '/masterdata/suppliers';

export const supplierApi = {
  // ---- Core CRUD ----
  create: (data: CreateSupplierRequest) => axiosClient.post<Supplier>(BASE_URL, data),
  getById: (id: string) => axiosClient.get<Supplier>(`${BASE_URL}/${id}`),
  getByCode: (code: string) => axiosClient.get<Supplier>(`${BASE_URL}/code/${code}`),
  getAll: (page = 0, size = 10) =>
    axiosClient.get<{ content: SupplierListItem[]; totalPages: number; totalElements: number; pageNumber: number; pageSize: number; last: boolean }>(`${BASE_URL}/list`, { params: { page, size } }),
  getAllList: (page = 0, size = 10) =>
    axiosClient.get<{ content: SupplierListItem[]; totalPages: number; totalElements: number; pageNumber: number; pageSize: number; last: boolean }>(`${BASE_URL}/list`, { params: { page, size } }),
  getAllUnpaginated: (params?: SupplierFilterRequest) => axiosClient.get<SupplierListItem[]>(BASE_URL, { params }),
  update: (id: string, data: UpdateSupplierRequest) => axiosClient.put<Supplier>(`${BASE_URL}/${id}`, data),
  delete: (id: string) => axiosClient.delete(`${BASE_URL}/${id}`),
  
  // ---- Custom Endpoints ----
  search: (keyword: string) => axiosClient.get<Supplier[]>(`${BASE_URL}/search`, { params: { keyword } }),
  filterList: (criteria: SupplierFilterRequest, page = 0, size = 10) =>
    axiosClient.post<{ content: SupplierListItem[]; totalPages: number; totalElements: number; pageNumber: number; pageSize: number; last: boolean }>(`${BASE_URL}/filter/list`, criteria, { params: { page, size } }),
  searchAdvancedList: (criteria: SupplierSearchRequest, page = 0, size = 10) =>
    axiosClient.post<{ content: Supplier[]; totalPages: number; totalElements: number; pageNumber: number; pageSize: number; last: boolean }>(`${BASE_URL}/search/list`, criteria, { params: { page, size } }),
};

