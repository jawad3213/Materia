import axiosClient from '../../../shared/api/axiosClient';
import type { UpdateProfilePayload, UserProfile } from '../types/profile.types';

export const profileService = {
  async getProfile(): Promise<UserProfile> {
    const response = await axiosClient.get<UserProfile>('/auth/me');
    return response.data;
  },

  async updateProfile(payload: UpdateProfilePayload): Promise<UserProfile> {
    const response = await axiosClient.put<UserProfile>('/auth/me', {
      firstName: payload.firstName.trim(),
      lastName: payload.lastName.trim(),
      phone: payload.phone.trim(),
    });
    return response.data;
  },
};

export default profileService;
