import axiosClient from '../../../shared/api/axiosClient';
import type {
  UserItem,
  CreateUserRequest,
  OnboardUserRequest,
  UpdateUserRequest,
  OffboardUserRequest,
  UserFilterRequest,
} from '../types';

const BASE_URL = '/employees';

export const userApi = {
  // ---- Core CRUD & Query ----
  getAll: (filters?: UserFilterRequest) =>
    axiosClient.get<UserItem[]>(BASE_URL, {
      params: {
        status: filters?.status || undefined,
      },
    }),

  getById: (id: string) =>
    axiosClient.get<UserItem>(`${BASE_URL}/${id}`),

  getByCode: (code: string) =>
    axiosClient.get<UserItem>(`${BASE_URL}/code/${code}`),

  getByEmail: (email: string) =>
    axiosClient.get<UserItem>(`${BASE_URL}/email/${encodeURIComponent(email)}`),

  create: (data: CreateUserRequest) =>
    axiosClient.post<UserItem>(BASE_URL, data),

  update: (id: string, data: UpdateUserRequest) =>
    axiosClient.put<UserItem>(`${BASE_URL}/${id}`, data),

  delete: (id: string) =>
    axiosClient.delete<void>(`${BASE_URL}/${id}`),

  // ---- Advanced Workflows ----
  onboard: (data: OnboardUserRequest) =>
    axiosClient.post<UserItem>(`${BASE_URL}/onboard`, data),

  offboard: (data: OffboardUserRequest) =>
    axiosClient.post<UserItem>(`${BASE_URL}/offboard`, data),
};

export default userApi;
