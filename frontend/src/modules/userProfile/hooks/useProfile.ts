import { useCallback, useEffect, useState } from 'react';
import { isAxiosError } from 'axios';
import profileService from '../services/profileService';
import useAuth from '../../auth/hooks/useAuth';
import type { UpdateProfilePayload, UserProfile } from '../types/profile.types';

interface ApiError {
  message?: string;
  detail?: string;
  validationErrors?: Record<string, string>;
}

export function errorMessage(err: unknown, fallback: string): string {
  const data = isAxiosError<ApiError>(err) ? err.response?.data : undefined;
  const firstValidationError = data?.validationErrors ? Object.values(data.validationErrors)[0] : undefined;
  return firstValidationError || data?.message || data?.detail || (err instanceof Error ? err.message : '') || fallback;
}

export function useProfile() {
  const { updateUser } = useAuth();
  const [profile, setProfile] = useState<UserProfile | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchProfile = useCallback(
    () =>
      profileService
        .getProfile()
        .then(setProfile)
        .catch((err) => setError(errorMessage(err, 'Could not load your profile')))
        .finally(() => setIsLoading(false)),
    [],
  );

  useEffect(() => {
    fetchProfile();
  }, [fetchProfile]);

  const reload = () => {
    setIsLoading(true);
    setError(null);
    fetchProfile();
  };

  const save = async (payload: UpdateProfilePayload) => {
    const updated = await profileService.updateProfile(payload);
    setProfile(updated);
    // Keep the header (name and initials) in sync with the new name.
    updateUser({ name: updated.fullName || updated.email });
    return updated;
  };

  return { profile, isLoading, error, reload, save };
}

export default useProfile;
