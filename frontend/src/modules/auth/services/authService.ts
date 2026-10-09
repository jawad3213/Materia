import axiosClient, {
  setAccessToken,
  getAccessToken,
  setRefreshToken,
  getRefreshToken,
  setRememberMe,
  isRememberMe,
  clearSessionState,
  refreshSession,
  USER_STORAGE_KEY,
} from '../../../shared/api/axiosClient';
import type {
  AuthResponse,
  LoginCredentials,
  ChangePasswordCredentials,
  ResetPasswordCredentials,
  ConfirmResetPasswordCredentials,
  User,
  UserRole,
} from '../types/auth.types';

class AuthService {
  /**
   * Helper to normalize backend auth responses into the frontend AuthResponse structure
   */
  private normalizeAuthResponse(data: any): AuthResponse {
    const backendUser = data.user ?? {};

    const rawRole: UserRole =
      backendUser.role ??
      (typeof data.role === 'object' ? data.role?.code : data.role) ??
      'PURCHASER';
    const email = backendUser.email || data.email || '';
    const userId = backendUser.id || backendUser.userId || data.userId || '';

    // The login/refresh response is flat (firstName, lastName, fullName at the top level).
    const firstName = backendUser.firstName ?? data.firstName;
    const lastName = backendUser.lastName ?? data.lastName;
    const fullName = (backendUser.fullName ?? data.fullName ?? [firstName, lastName].filter(Boolean).join(' '))
      .trim();
    const name =
      (backendUser.name && backendUser.name.trim()) ||
      fullName ||
      (email ? email.split('@')[0] : 'User');

    const mustChangePassword = !!(backendUser.mustChangePassword ?? data.mustChangePassword);
    const permissions: string[] = Array.isArray(data.permissions)
      ? data.permissions
      : Array.isArray(backendUser.permissions)
      ? backendUser.permissions
      : [];

    const user: User = {
      id: userId,
      name,
      email,
      role: rawRole,
      permissions,
      createdAt: new Date().toISOString(),
      mustChangePassword,
    };

    return {
      accessToken: data.accessToken,
      refreshToken: data.refreshToken,
      user,
    };
  }

  /**
   * Log in user with email, password & rememberMe flag
   */
  async login(credentials: LoginCredentials): Promise<AuthResponse> {
    const remember = credentials.rememberMe !== false;
    const response = await axiosClient.post<any>('/auth/login', {
      email: credentials.email.trim(),
      password: credentials.password,
      rememberMe: remember,
    });
    const authData = this.normalizeAuthResponse(response.data);
    this.setSession(authData, remember);
    return authData;
  }

  /**
   * Perform silent refresh using the stored refresh token or HttpOnly cookie
   */
  async refreshToken(): Promise<AuthResponse> {
    const data = await refreshSession();
    const authData = this.normalizeAuthResponse(data);
    this.setSession(authData);
    return authData;
  }

  /**
   * Request a password reset link for the given email
   */
  async resetPassword(data: ResetPasswordCredentials): Promise<{ message: string }> {
    const response = await axiosClient.post<{ message: string }>('/auth/forgot-password', {
      email: data.email.trim(),
    });
    return response.data;
  }

  /**
   * Confirm password reset with new password
   */
  async confirmPasswordReset(data: ConfirmResetPasswordCredentials): Promise<{ message: string }> {
    const response = await axiosClient.post<{ message: string }>('/auth/reset-password', {
      email: data.email ? data.email.trim() : undefined,
      token: data.token,
      password: data.password,
      confirmPassword: data.confirmPassword,
    });
    return response.data;
  }

  /**
   * Change the signed-in user's password (used during first-login or from the profile page).
   * The server ends every session on a password change and returns a fresh one, which is stored here.
   */
  async changePassword(data: ChangePasswordCredentials): Promise<{ message: string }> {
    const response = await axiosClient.post<any>('/auth/change-password', {
      currentPassword: data.currentPassword,
      newPassword: data.newPassword,
      confirmPassword: data.confirmPassword,
    });

    const authData = this.normalizeAuthResponse(response.data);
    // Keep the cached profile (name, avatar...) and only clear the first-login flag.
    const currentUser = this.getStoredUser();
    this.setSession({ ...authData, user: { ...(currentUser ?? authData.user), mustChangePassword: false } });

    return { message: 'Password successfully updated.' };
  }

  /**
   * Log out user and clear stored tokens
   */
  async logout(): Promise<void> {
    try {
      const refreshToken = getRefreshToken();
      await axiosClient.post('/auth/logout', refreshToken ? { refreshToken } : {});
    } catch {
      // Silent error ignore on logout
    } finally {
      this.clearSession();
    }
  }

  /**
   * Store access token, refresh token, and user profile according to rememberMe preference
   */
  setSession(authData: AuthResponse, rememberMe?: boolean): void {
    if (rememberMe !== undefined) {
      setRememberMe(rememberMe);
    }
    if (authData.accessToken) {
      setAccessToken(authData.accessToken);
    }
    if (authData.refreshToken) {
      setRefreshToken(authData.refreshToken);
    }
    if (authData.user) {
      this.storeUser(authData.user);
    }
  }

  /**
   * Persist the cached user profile in the storage matching the rememberMe preference
   */
  storeUser(user: User): void {
    const storage = isRememberMe() ? localStorage : sessionStorage;
    storage.setItem(USER_STORAGE_KEY, JSON.stringify(user));
    // Clean opposite storage to avoid stale user data
    if (isRememberMe()) {
      sessionStorage.removeItem(USER_STORAGE_KEY);
    } else {
      localStorage.removeItem(USER_STORAGE_KEY);
    }
  }

  /**
   * Clear session data from memory and storages
   */
  clearSession(): void {
    clearSessionState();
  }

  /**
   * Retrieve active access token (memory or storage fallback)
   */
  getStoredToken(): string | null {
    return getAccessToken();
  }

  /**
   * Retrieve stored refresh token
   */
  getRefreshToken(): string | null {
    return getRefreshToken();
  }

  /**
   * Retrieve stored user object
   */
  getStoredUser(): User | null {
    const raw = isRememberMe()
      ? (localStorage.getItem(USER_STORAGE_KEY) || sessionStorage.getItem(USER_STORAGE_KEY))
      : (sessionStorage.getItem(USER_STORAGE_KEY) || localStorage.getItem(USER_STORAGE_KEY));
    if (!raw) return null;
    try {
      return JSON.parse(raw) as User;
    } catch {
      return null;
    }
  }

  /**
   * Check if active session exists
   */
  isAuthenticated(): boolean {
    return !!this.getStoredUser() && (!!this.getStoredToken() || !!this.getRefreshToken());
  }

  /**
   * Get remembered email for login
   */
  getRememberedEmail(): string | null {
    return localStorage.getItem('materia_remembered_email');
  }

  /**
   * Save or clear remembered email
   */
  setRememberedEmail(email: string | null): void {
    if (email && email.trim()) {
      localStorage.setItem('materia_remembered_email', email.trim());
    } else {
      localStorage.removeItem('materia_remembered_email');
    }
  }
}

export const authService = new AuthService();
export default authService;
