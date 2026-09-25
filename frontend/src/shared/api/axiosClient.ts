import axios, { type AxiosError, type InternalAxiosRequestConfig } from 'axios';

export const USER_STORAGE_KEY = 'materia_auth_user';

/**
 * Dispatched on `window` when the refresh token is no longer usable and the
 * session has been cleared. AuthProvider listens for it so React state stays
 * in sync with the cleared token instead of rendering a stale logged-in UI.
 */
export const SESSION_EXPIRED_EVENT = 'materia:session-expired';

// In-memory access token storage (secure from XSS localStorage attacks)
let inMemoryAccessToken: string | null = null;

export const setAccessToken = (token: string | null): void => {
  inMemoryAccessToken = token;
};

export const getAccessToken = (): string | null => {
  return inMemoryAccessToken;
};

// Create Axios instance with cookie credentials enabled
const axiosClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1',
  withCredentials: true, // Send HttpOnly refresh_token cookie automatically
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor to attach in-memory JWT Bearer token
axiosClient.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    if (inMemoryAccessToken && config.headers) {
      config.headers.Authorization = `Bearer ${inMemoryAccessToken}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

const clearSessionState = (): void => {
  setAccessToken(null);
  localStorage.removeItem(USER_STORAGE_KEY);
};

// Single-flight refresh state
let refreshPromise: Promise<any> | null = null;

/**
 * Refresh the access token using the HttpOnly refresh_token cookie.
 *
 * The backend rotates the refresh token on every call and revokes the previous
 * one, so two concurrent calls sharing the same cookie would destroy the
 * session: the first rotates, the second replays a revoked token and gets 401.
 * All callers therefore share a single in-flight request.
 *
 * Resolves with the full auth payload so callers that need the user object
 * (AuthService) and callers that only need the token (the 401 retry below) can
 * both use it.
 */
export const refreshSession = (): Promise<any> => {
  if (!refreshPromise) {
    refreshPromise = axiosClient
      .post<any>('/auth/refresh', {})
      .then((response) => {
        const accessToken = response.data?.accessToken;
        if (!accessToken) {
          throw new Error('No access token returned from refresh');
        }
        setAccessToken(accessToken);
        return response.data;
      })
      .finally(() => {
        refreshPromise = null;
      });
  }
  return refreshPromise;
};

// Response interceptor with silent token refresh on 401
axiosClient.interceptors.response.use(
  (response) => response,
  async (error: AxiosError) => {
    const originalRequest = error.config as InternalAxiosRequestConfig & { _retry?: boolean };

    // Do not attempt refresh for login, refresh itself, or already retried requests
    const isAuthRequest = originalRequest?.url?.includes('/auth/login') ||
                          originalRequest?.url?.includes('/auth/refresh') ||
                          originalRequest?.url?.includes('/auth/logout') ||
                          originalRequest?.url?.includes('/auth/reset-password');

    if (error.response?.status === 401 && originalRequest && !originalRequest._retry && !isAuthRequest) {
      originalRequest._retry = true;

      try {
        // HttpOnly refresh_token cookie is automatically sent by browser
        const authData = await refreshSession();
        if (originalRequest.headers) {
          originalRequest.headers.Authorization = `Bearer ${authData.accessToken}`;
        }
        return axiosClient(originalRequest);
      } catch (refreshErr) {
        clearSessionState();
        window.dispatchEvent(new Event(SESSION_EXPIRED_EVENT));
        return Promise.reject(refreshErr);
      }
    }

    return Promise.reject(error);
  }
);

export default axiosClient;
