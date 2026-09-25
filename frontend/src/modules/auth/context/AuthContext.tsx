import { createContext, useState, useEffect, type ReactNode } from 'react';
import type {
  User,
  LoginCredentials,
  ChangePasswordCredentials,
  ResetPasswordCredentials,
  ConfirmResetPasswordCredentials,
} from '../types/auth.types';
import authService from '../services/authService';
import { SESSION_EXPIRED_EVENT } from '../../../shared/api/axiosClient';

export interface AuthContextType {
  user: User | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  error: string | null;
  login: (credentials: LoginCredentials) => Promise<void>;
  logout: () => Promise<void>;
  changePassword: (data: ChangePasswordCredentials) => Promise<string>;
  clearMustChangePassword: () => void;
  resetPassword: (data: ResetPasswordCredentials) => Promise<string>;
  confirmPasswordReset: (data: ConfirmResetPasswordCredentials) => Promise<string>;
  clearError: () => void;
}

export const AuthContext = createContext<AuthContextType | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(() => authService.getStoredUser());
  const [isLoading, setIsLoading] = useState<boolean>(() => !authService.getStoredUser());
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    // Restore session on mount via silent token refresh (HttpOnly cookie)
    let isMounted = true;

    const initAuth = async () => {
      if (!authService.getStoredUser() && !authService.getStoredToken() && !authService.getRefreshToken()) {
        if (isMounted) {
          setIsLoading(false);
        }
        return;
      }

      try {
        const authData = await authService.refreshToken();
        if (isMounted) {
          setUser(authData.user);
        }
      } catch (err: any) {
        const status = err?.response?.status;
        if (status === 401 || status === 403) {
          if (isMounted) {
            setUser(null);
            authService.clearSession();
          }
        }
      } finally {
        if (isMounted) {
          setIsLoading(false);
        }
      }
    };

    initAuth();

    // The axios interceptor clears the tokens when a refresh fails mid-session.
    // Mirror that here so protected routes stop rendering immediately instead
    // of waiting for the next reload.
    const handleSessionExpired = () => {
      if (isMounted) {
        setUser(null);
      }
    };
    window.addEventListener(SESSION_EXPIRED_EVENT, handleSessionExpired);

    return () => {
      isMounted = false;
      window.removeEventListener(SESSION_EXPIRED_EVENT, handleSessionExpired);
    };
  }, []);

  const login = async (credentials: LoginCredentials) => {
    setIsLoading(true);
    setError(null);
    try {
      const response = await authService.login(credentials);
      setUser(response.user);
    } catch (err: any) {
      const message =
        err.response?.data?.message ||
        err.response?.data?.detail ||
        err.response?.data?.error ||
        err.message ||
        'Login failed';
      setError(message);
      throw err;
    } finally {
      setIsLoading(false);
    }
  };

  const logout = async () => {
    setIsLoading(true);
    try {
      await authService.logout();
      setUser(null);
    } finally {
      setIsLoading(false);
    }
  };

  const resetPassword = async (data: ResetPasswordCredentials) => {
    setIsLoading(true);
    setError(null);
    try {
      const res = await authService.resetPassword(data);
      return res.message;
    } catch (err: any) {
      const message = err.response?.data?.message || err.message || 'Password reset failed';
      setError(message);
      throw err;
    } finally {
      setIsLoading(false);
    }
  };

  const confirmPasswordReset = async (data: ConfirmResetPasswordCredentials) => {
    setIsLoading(true);
    setError(null);
    try {
      const res = await authService.confirmPasswordReset(data);
      return res.message;
    } catch (err: any) {
      const message = err.response?.data?.message || err.message || 'Password update failed';
      setError(message);
      throw err;
    } finally {
      setIsLoading(false);
    }
  };

  const changePassword = async (data: ChangePasswordCredentials) => {
    setIsLoading(true);
    setError(null);
    try {
      const res = await authService.changePassword(data);
      if (user) {
        setUser({ ...user, mustChangePassword: false });
      }
      return res.message;
    } catch (err: any) {
      const message =
        err.response?.data?.message ||
        err.response?.data?.detail ||
        err.message ||
        'Password update failed';
      setError(message);
      throw err;
    } finally {
      setIsLoading(false);
    }
  };

  const clearMustChangePassword = () => {
    if (user) {
      setUser({ ...user, mustChangePassword: false });
    }
  };

  const clearError = () => setError(null);

  const value: AuthContextType = {
    user,
    isAuthenticated: !!user,
    isLoading,
    error,
    login,
    logout,
    changePassword,
    clearMustChangePassword,
    resetPassword,
    confirmPasswordReset,
    clearError,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export default AuthContext;
