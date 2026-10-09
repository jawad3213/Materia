import type { UserRole } from '../../auth/types/auth.types';

export interface UserProfile {
  id: string;
  email: string;
  firstName: string | null;
  lastName: string | null;
  fullName: string | null;
  phone: string | null;
  department: string | null;
  roleCode: UserRole;
  statusCode: string | null;
  createdAt: string | null;
}

export interface UpdateProfilePayload {
  firstName: string;
  lastName: string;
  phone: string;
}
