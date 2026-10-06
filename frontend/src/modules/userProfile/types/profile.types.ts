import type { UserRole } from '../../auth/types/auth.types';

/** The signed-in user's own account, as returned by GET /profile. */
export interface Profile {
  id: string;
  email: string;
  firstName: string | null;
  lastName: string | null;
  fullName: string | null;
  phone: string | null;
  department: string | null;
  role: UserRole | null;
  roleLabel: string | null;
  status: string | null;
  mustChangePassword: boolean;
  permissions: string[];
  createdAt: string | null;
  updatedAt: string | null;
}

/** What users may change about themselves (PUT /profile). */
export interface UpdateProfilePayload {
  firstName: string;
  lastName: string;
  phone: string;
}

export interface ChangePasswordPayload {
  currentPassword: string;
  newPassword: string;
  confirmPassword: string;
}
