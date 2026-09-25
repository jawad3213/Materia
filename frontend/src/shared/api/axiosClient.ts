import axios, { type AxiosError, type InternalAxiosRequestConfig } from 'axios';

export const USER_STORAGE_KEY = 'materia_auth_user';
export const ACCESS_TOKEN_KEY = 'materia_access_token';
export const REFRESH_TOKEN_KEY = 'materia_refresh_token';
export const REMEMBER_ME_KEY = 'materia_remember_me';

/**
 * Dispatched on `window` when the refresh token is no longer usable and the
 * session has been cleared. AuthProvider listens for it so React state stays
 * in sync with the cleared token instead of rendering a stale logged-in UI.
 */
export const SESSION_EXPIRED_EVENT = 'materia:session-expired';

// In-memory access token storage
let inMemoryAccessToken: string | null = null;

export const isRememberMe = (): boolean => {
  return localStorage.getItem(REMEMBER_ME_KEY) === 'true';
};

export const setRememberMe = (remember: boolean): void => {
  localStorage.setItem(REMEMBER_ME_KEY, remember ? 'true' : 'false');
};

export const setAccessToken = (token: string | null): void => {
  inMemoryAccessToken = token;
  if (token) {
    if (isRememberMe()) {
      localStorage.setItem(ACCESS_TOKEN_KEY, token);
      sessionStorage.removeItem(ACCESS_TOKEN_KEY);
    } else {
      sessionStorage.setItem(ACCESS_TOKEN_KEY, token);
      localStorage.removeItem(ACCESS_TOKEN_KEY);
    }
  } else {
    localStorage.removeItem(ACCESS_TOKEN_KEY);
    sessionStorage.removeItem(ACCESS_TOKEN_KEY);
  }
};

export const getAccessToken = (): string | null => {
  if (inMemoryAccessToken) return inMemoryAccessToken;
  const stored = isRememberMe()
    ? localStorage.getItem(ACCESS_TOKEN_KEY)
    : (sessionStorage.getItem(ACCESS_TOKEN_KEY) || localStorage.getItem(ACCESS_TOKEN_KEY));
  if (stored) {
    inMemoryAccessToken = stored;
  }
  return inMemoryAccessToken;
};

export const setRefreshToken = (token: string | null): void => {
  if (token) {
    if (isRememberMe()) {
      localStorage.setItem(REFRESH_TOKEN_KEY, token);
      sessionStorage.removeItem(REFRESH_TOKEN_KEY);
    } else {
      sessionStorage.setItem(REFRESH_TOKEN_KEY, token);
      localStorage.removeItem(REFRESH_TOKEN_KEY);
    }
  } else {
    localStorage.removeItem(REFRESH_TOKEN_KEY);
    sessionStorage.removeItem(REFRESH_TOKEN_KEY);
  }
};

export const getRefreshToken = (): string | null => {
  return isRememberMe()
    ? localStorage.getItem(REFRESH_TOKEN_KEY)
    : (sessionStorage.getItem(REFRESH_TOKEN_KEY) || localStorage.getItem(REFRESH_TOKEN_KEY));
};

// Create Axios instance with cookie credentials enabled
const axiosClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1',
  withCredentials: true, // Send HttpOnly refresh_token cookie automatically
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor to attach JWT Bearer token
axiosClient.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = getAccessToken();
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

export const clearSessionState = (): void => {
  setAccessToken(null);
  setRefreshToken(null);
  localStorage.removeItem(USER_STORAGE_KEY);
  sessionStorage.removeItem(USER_STORAGE_KEY);
};

// Single-flight refresh state
let refreshPromise: Promise<any> | null = null;

/**
 * Refresh the access token using either the stored refresh token or the HttpOnly cookie.
 *
 * All callers share a single in-flight request to avoid race conditions.
 */
export const refreshSession = (): Promise<any> => {
  if (!refreshPromise) {
    const token = getRefreshToken();
    refreshPromise = axiosClient
      .post<any>('/auth/refresh', token ? { refreshToken: token } : {})
      .then((response) => {
        const accessToken = response.data?.accessToken;
        if (!accessToken) {
          throw new Error('No access token returned from refresh');
        }
        setAccessToken(accessToken);
        if (response.data?.refreshToken) {
          setRefreshToken(response.data.refreshToken);
        }
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

    // Do not attempt refresh for login, refresh itself, logout, or reset-password
    const isAuthRequest = originalRequest?.url?.includes('/auth/login') ||
                          originalRequest?.url?.includes('/auth/refresh') ||
                          originalRequest?.url?.includes('/auth/logout') ||
                          originalRequest?.url?.includes('/auth/reset-password');

    if (error.response?.status === 401 && originalRequest && !originalRequest._retry && !isAuthRequest) {
      originalRequest._retry = true;

      try {
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
