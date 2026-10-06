import { useCallback, useEffect, useState } from 'react';
import useAuth from '../../auth/hooks/useAuth';
import { getApiErrorMessage } from '../../../shared/utils/apiError';
import { profileService } from '../services/profileService';
import type { Profile, UpdateProfilePayload } from '../types/profile.types';

interface LoadState {
  key: number;
  profile: Profile | null;
  error: string | null;
}

/**
 * Loads the signed-in user's profile and saves changes to it. After a save the header and menus
 * (which read the auth context) show the new name straight away.
 */
export function useProfile() {
  const { updateUser } = useAuth();
  const [requestKey, setRequestKey] = useState(0);
  const [state, setState] = useState<LoadState>({ key: -1, profile: null, error: null });

  useEffect(() => {
    let cancelled = false;
    profileService
      .getProfile()
      .then((profile) => {
        if (!cancelled) setState({ key: requestKey, profile, error: null });
      })
      .catch((err) => {
        if (!cancelled) setState({ key: requestKey, profile: null, error: getApiErrorMessage(err, 'Your profile could not be loaded') });
      });
    return () => {
      cancelled = true;
    };
  }, [requestKey]);

  const reload = useCallback(() => setRequestKey((key) => key + 1), []);

  const save = useCallback(
    async (payload: UpdateProfilePayload) => {
      const profile = await profileService.updateProfile(payload);
      setState((current) => ({ ...current, profile }));
      updateUser({ name: profile.fullName ?? `${profile.firstName ?? ''} ${profile.lastName ?? ''}`.trim() });
      return profile;
    },
    [updateUser]
  );

  return {
    profile: state.profile,
    error: state.error,
    loading: state.key !== requestKey,
    reload,
    save,
  };
}
