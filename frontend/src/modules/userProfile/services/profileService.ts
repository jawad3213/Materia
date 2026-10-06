import axiosClient from '../../../shared/api/axiosClient';
import type { Profile, UpdateProfilePayload } from '../types/profile.types';

const BASE_URL = '/profile';

/** The signed-in user's own account; the backend takes the user from the access token. */
export const profileService = {
  async getProfile(): Promise<Profile> {
    const { data } = await axiosClient.get<Profile>(BASE_URL);
    return data;
  },

  async updateProfile(payload: UpdateProfilePayload): Promise<Profile> {
    const { data } = await axiosClient.put<Profile>(BASE_URL, {
      firstName: payload.firstName.trim(),
      lastName: payload.lastName.trim(),
      phone: payload.phone.trim(),
    });
    return data;
  },
};
