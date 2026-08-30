import axiosClient from '../../../shared/api/axiosClient';
import type { Category } from '../types/Category';
import type { CategoryListItem } from '../types/CategoryListItem';
import type { CreateCategoryRequest } from '../types/CreateCategoryRequest';
import type { UpdateCategoryRequest } from '../types/UpdateCategoryRequest';

const BASE_URL = '/masterdata/categories';

export const categoryApi = {
  // ---- Core CRUD ----
  create: (data: CreateCategoryRequest) => axiosClient.post<Category>(BASE_URL, data),
  getById: (id: string) => axiosClient.get<Category>(`${BASE_URL}/${id}`),
  getByCode: (code: string) => axiosClient.get<Category>(`${BASE_URL}/code/${code}`),
  getAll: () => axiosClient.get<CategoryListItem[]>(BASE_URL),
  update: (id: string, data: UpdateCategoryRequest) => axiosClient.put<Category>(`${BASE_URL}/${id}`, data),
  delete: (id: string) => axiosClient.delete(`${BASE_URL}/${id}`),
  
  // ---- Custom Endpoints ----
  getRoots: () => axiosClient.get<CategoryListItem[]>(`${BASE_URL}/roots`),
  getSubCategories: (parentId: string) => axiosClient.get<CategoryListItem[]>(`${BASE_URL}/${parentId}/subcategories`),
};
