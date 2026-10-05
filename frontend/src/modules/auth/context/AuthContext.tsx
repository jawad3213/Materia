import { useState, useEffect, type ReactNode } from 'react';
import { AuthContext, type AuthContextType } from './authContextValue';
import type {
  User,
  UserRole,
  LoginCredentials,
  ChangePasswordCredentials,
  ResetPasswordCredentials,
  ConfirmResetPasswordCredentials,
} from '../types/auth.types';
import authService from '../services/authService';
import { SESSION_EXPIRED_EVENT } from '../../../shared/api/axiosClient';

import { getApiErrorMessage, getApiErrorStatus } from '../../../shared/utils/apiError';

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
      } catch (err) {
        const status = getApiErrorStatus(err);
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
    } catch (err) {
      const message = getApiErrorMessage(err, 'Login failed');
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
    } catch (err) {
      const message = getApiErrorMessage(err, 'Password reset failed');
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
    } catch (err) {
      const message = getApiErrorMessage(err, 'Password update failed');
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
    } catch (err) {
      const message = getApiErrorMessage(err, 'Password update failed');
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

  const hasRole = (roles: UserRole | UserRole[]): boolean => {
    if (!user) return false;
    const roleList = Array.isArray(roles) ? roles : [roles];
    return roleList.includes(user.role);
  };

  const hasPermission = (permission: string): boolean => {
    if (!user) return false;
    if (user.role === 'ADMIN') return true;
    if (user.permissions && user.permissions.includes(permission)) return true;
    
    // Canonical permission matrix fallback
    const rolePermissions: Record<UserRole, string[]> = {
      ADMIN: ['*'],
      PURCHASER: [
        'category:read', 'category:write',
        'material:read', 'supplier:read', 'supplier:write',
        'requisition:read', 'requisition:write', 'requisition:validate', 'requisition:convert',
        'order:read', 'order:write', 'order:cancel', 'receipt:read',
        'invoice:read', 'invoice:write', 'payment:read',
        'return:read', 'return:write', 'dashboard:read'
      ],
      RECEIVER: [
        'category:read', 'material:read', 'material:stock:read', 'material:stock:write',
        'supplier:read', 'requisition:read',
        'order:read', 'receipt:read', 'receipt:write', 'receipt:quality',
        'return:read', 'return:write', 'dashboard:read'
      ]
    };
    return rolePermissions[user.role]?.includes(permission) ?? false;
  };

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
    hasRole,
    hasPermission,
    clearError,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export default AuthProvider;
