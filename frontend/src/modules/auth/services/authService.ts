import axiosClient, {
  setAccessToken,
  getAccessToken,
  refreshSession,
  USER_STORAGE_KEY,
} from '../../../shared/api/axiosClient';
import type {
  AuthResponse,
  LoginCredentials,
  ResetPasswordCredentials,
  ConfirmResetPasswordCredentials,
  User,
} from '../types/auth.types';

class AuthService {
  /**
   * Helper to normalize backend auth responses into the frontend AuthResponse structure
   */
  private normalizeAuthResponse(data: any): AuthResponse {
    // The backend sends a ready-made `user` object alongside the flat fields.
    // Prefer it so the real profile name/role reach the UI instead of a value
    // derived from the email local part.
    const backendUser = data.user ?? {};

    const rawRole =
      backendUser.role ??
      (typeof data.role === 'object' ? data.role?.code : data.role) ??
      'PURCHASER';
    const email = backendUser.email || data.email || '';
    const userId = backendUser.id || backendUser.userId || data.userId || '';

    const fullName = [backendUser.firstName, backendUser.lastName]
      .filter(Boolean)
      .join(' ')
      .trim();
    const name =
      (backendUser.name && backendUser.name.trim()) ||
      fullName ||
      (email ? email.split('@')[0] : 'User');

    const user: User = {
      id: userId,
      name,
      email,
      role: rawRole,
      createdAt: new Date().toISOString(),
    };

    return {
      accessToken: data.accessToken,
      refreshToken: data.refreshToken,
      user,
    };
  }

  /**
   * Log in user with email & password strictly
   */
  async login(credentials: LoginCredentials): Promise<AuthResponse> {
    const response = await axiosClient.post<any>('/auth/login', {
      email: credentials.email.trim(),
      password: credentials.password,
    });
    const authData = this.normalizeAuthResponse(response.data);
    this.setSession(authData);
    return authData;
  }

  /**
   * Perform silent refresh using the HttpOnly refresh_token cookie
   */
  async refreshToken(): Promise<AuthResponse> {
    // Shared single-flight call: rotation makes concurrent refreshes fatal.
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
   * Log out user and clear stored tokens
   */
  async logout(): Promise<void> {
    try {
      await axiosClient.post('/auth/logout', {});
    } catch {
      // Silent error ignore on logout
    } finally {
      this.clearSession();
    }
  }

  /**
   * Keep access token in memory and cache user profile in localStorage
   */
  setSession(authData: AuthResponse): void {
    if (authData.accessToken) {
      setAccessToken(authData.accessToken);
    }
    if (authData.user) {
      localStorage.setItem(USER_STORAGE_KEY, JSON.stringify(authData.user));
    }
  }

  /**
   * Clear session data
   */
  clearSession(): void {
    setAccessToken(null);
    localStorage.removeItem(USER_STORAGE_KEY);
  }

  /**
   * Retrieve in-memory access token
   */
  getStoredToken(): string | null {
    return getAccessToken();
  }

  /**
   * Retrieve stored user object
   */
  getStoredUser(): User | null {
    const raw = localStorage.getItem(USER_STORAGE_KEY);
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
    return !!this.getStoredToken();
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
