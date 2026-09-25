import { useState, useEffect, useCallback } from 'react';
import type {
  UserItem,
  UserFilterRequest,
  OnboardUserRequest,
  UpdateUserRequest,
  OffboardUserRequest,
} from '../types';
import userService from '../services/userService';

export function useUsers(initialFilters?: UserFilterRequest) {
  const [users, setUsers] = useState<UserItem[]>([]);
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [filters, setFilters] = useState<UserFilterRequest>(initialFilters || {});

  const fetchUsers = useCallback(async () => {
    setIsLoading(true);
    setError(null);
    try {
      const data = await userService.fetchUsers(filters);
      setUsers(data);
    } catch (err: any) {
      const msg = err.response?.data?.message || err.message || 'Failed to fetch users';
      setError(msg);
    } finally {
      setIsLoading(false);
    }
  }, [filters]);

  useEffect(() => {
    fetchUsers();
  }, [fetchUsers]);

  const onboard = async (payload: OnboardUserRequest): Promise<UserItem> => {
    const created = await userService.onboardUser(payload);
    await fetchUsers();
    return created;
  };

  const update = async (id: string, payload: UpdateUserRequest): Promise<UserItem> => {
    const updated = await userService.updateUser(id, payload);
    await fetchUsers();
    return updated;
  };

  const offboard = async (payload: OffboardUserRequest): Promise<UserItem> => {
    const offboarded = await userService.offboardUser(payload);
    await fetchUsers();
    return offboarded;
  };

  const remove = async (id: string): Promise<void> => {
    await userService.deleteUser(id);
    await fetchUsers();
  };

  return {
    users,
    isLoading,
    error,
    filters,
    setFilters,
    fetchUsers,
    onboard,
    update,
    offboard,
    remove,
  };
}

export default useUsers;
