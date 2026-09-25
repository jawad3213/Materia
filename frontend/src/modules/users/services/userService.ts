import userApi from './userApi';
import type {
  UserItem,
  CreateUserRequest,
  OnboardUserRequest,
  UpdateUserRequest,
  OffboardUserRequest,
  UserFilterRequest,
} from '../types';

export const userService = {
  fetchUsers: async (filters?: UserFilterRequest): Promise<UserItem[]> => {
    const response = await userApi.getAll(filters);
    return response.data;
  },

  getUserById: async (id: string): Promise<UserItem> => {
    const response = await userApi.getById(id);
    return response.data;
  },

  getUserByCode: async (code: string): Promise<UserItem> => {
    const response = await userApi.getByCode(code);
    return response.data;
  },

  createUser: async (payload: CreateUserRequest): Promise<UserItem> => {
    const response = await userApi.create(payload);
    return response.data;
  },

  onboardUser: async (payload: OnboardUserRequest): Promise<UserItem> => {
    const response = await userApi.onboard(payload);
    return response.data;
  },

  updateUser: async (id: string, payload: UpdateUserRequest): Promise<UserItem> => {
    const response = await userApi.update(id, payload);
    return response.data;
  },

  offboardUser: async (payload: OffboardUserRequest): Promise<UserItem> => {
    const response = await userApi.offboard(payload);
    return response.data;
  },

  deleteUser: async (id: string): Promise<void> => {
    await userApi.delete(id);
  },
};

export default userService;
