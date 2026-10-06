import { createContext } from 'react';
import type {
  User,
  UserRole,
  LoginCredentials,
  ChangePasswordCredentials,
  ResetPasswordCredentials,
  ConfirmResetPasswordCredentials,
} from '../types/auth.types';

export interface AuthContextType {
  user: User | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  error: string | null;
  login: (credentials: LoginCredentials) => Promise<void>;
  logout: () => Promise<void>;
  changePassword: (data: ChangePasswordCredentials) => Promise<string>;
  clearMustChangePassword: () => void;
  /** Applies changes to the signed-in user (e.g. a new name after a profile edit) and keeps them across reloads. */
  updateUser: (changes: Partial<User>) => void;
  resetPassword: (data: ResetPasswordCredentials) => Promise<string>;
  confirmPasswordReset: (data: ConfirmResetPasswordCredentials) => Promise<string>;
  hasRole: (roles: UserRole | UserRole[]) => boolean;
  hasPermission: (permission: string) => boolean;
  clearError: () => void;
}

export const AuthContext = createContext<AuthContextType | undefined>(undefined);
