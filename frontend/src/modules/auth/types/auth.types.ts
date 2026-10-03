export type UserRole = 'ADMIN' | 'PURCHASER' | 'RECEIVER';

export interface User {
  id: string;
  name: string;
  email: string;
  role: UserRole;
  permissions?: string[];
  avatar?: string;
  createdAt?: string;
  mustChangePassword?: boolean;
}

export interface LoginCredentials {
  email: string;
  password: string;
  rememberMe?: boolean;
}

export interface ChangePasswordCredentials {
  currentPassword?: string;
  newPassword: string;
  confirmPassword: string;
}

export interface ResetPasswordCredentials {
  email: string;
}

export interface ConfirmResetPasswordCredentials {
  token?: string;
  email?: string;
  password: string;
  confirmPassword?: string;
}

export interface AuthResponse {
  user: User;
  accessToken: string;
  refreshToken?: string;
}

export interface AuthState {
  user: User | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  error: string | null;
}
