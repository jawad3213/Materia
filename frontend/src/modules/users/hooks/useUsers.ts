import { useState, useEffect, useCallback } from 'react';
import type {
  UserItem,
  UserFilterRequest,
  OnboardUserRequest,
  UpdateUserRequest,
  OffboardUserRequest,
} from '../types';
import userService from '../services/userService';

import { getApiErrorMessage } from '../../../shared/utils/apiError';
export function useUsers(initialFilters?: UserFilterRequest) {
  const [users, setUsers] = useState<UserItem[]>([]);
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [filters, setFiltersState] = useState<UserFilterRequest>(initialFilters || {});

  /** New filters reload the list. */
  const setFilters = useCallback((next: UserFilterRequest) => {
    setIsLoading(true);
    setFiltersState(next);
  }, []);

  const fetchUsers = useCallback(async () => {
    setIsLoading(true);
    setError(null);
    try {
      const data = await userService.fetchUsers(filters);
      setUsers(data);
    } catch (err) {
      const msg = getApiErrorMessage(err, 'Failed to fetch users');
      setError(msg);
    } finally {
      setIsLoading(false);
    }
  }, [filters]);

  // Loads the list whenever the filters change; a newer request supersedes an older one.
  useEffect(() => {
    let cancelled = false;
    userService
      .fetchUsers(filters)
      .then((data) => {
        if (!cancelled) {
          setUsers(data);
          setError(null);
        }
      })
      .catch((err) => {
        if (!cancelled) setError(getApiErrorMessage(err, 'Failed to fetch users'));
      })
      .finally(() => {
        if (!cancelled) setIsLoading(false);
      });
    return () => {
      cancelled = true;
    };
  }, [filters]);

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
