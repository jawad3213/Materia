import { describe, it, expect, beforeEach, vi } from 'vitest';
import axiosClient, {
  setAccessToken,
  getAccessToken,
  setRefreshToken,
  getRefreshToken,
  clearSessionState,
  SESSION_EXPIRED_EVENT,
} from '../axiosClient';

describe('axiosClient & Token Management', () => {
  beforeEach(() => {
    clearSessionState();
    localStorage.clear();
    sessionStorage.clear();
    vi.clearAllMocks();
  });

  it('correctly stores and retrieves in-memory and session access token', () => {
    expect(getAccessToken()).toBeNull();
    setAccessToken('test-access-token-123');
    expect(getAccessToken()).toBe('test-access-token-123');
  });

  it('correctly stores and retrieves refresh token', () => {
    expect(getRefreshToken()).toBeNull();
    setRefreshToken('test-refresh-token-456');
    expect(getRefreshToken()).toBe('test-refresh-token-456');
  });

  it('clearSessionState removes all tokens from memory and storages', () => {
    setAccessToken('access-to-clear');
    setRefreshToken('refresh-to-clear');
    clearSessionState();

    expect(getAccessToken()).toBeNull();
    expect(getRefreshToken()).toBeNull();
  });

  it('attaches Bearer token in request headers when available', async () => {
    setAccessToken('bearer-jwt-token');

    // Test request interceptor directly
    const interceptor = (axiosClient.interceptors.request as any).handlers[0]?.fulfilled;
    expect(interceptor).toBeDefined();

    const config = { headers: {} as Record<string, string>, url: '/materials' };
    const modifiedConfig = interceptor(config);

    expect(modifiedConfig.headers.Authorization).toBe('Bearer bearer-jwt-token');
  });

  it('does not attach Authorization header to /auth/refresh to avoid invalid credentials', async () => {
    setAccessToken('expired-access-token');

    const interceptor = (axiosClient.interceptors.request as any).handlers[0]?.fulfilled;
    const config = { headers: {} as Record<string, string>, url: '/auth/refresh' };
    const modifiedConfig = interceptor(config);

    expect(modifiedConfig.headers.Authorization).toBeUndefined();
  });

  it('dispatches SESSION_EXPIRED_EVENT when session refresh fails', async () => {
    const eventListener = vi.fn();
    window.addEventListener(SESSION_EXPIRED_EVENT, eventListener);

    // Call response error handler on 401
    const errorHandler = (axiosClient.interceptors.response as any).handlers[0]?.rejected;
    expect(errorHandler).toBeDefined();

    const mockError = {
      response: { status: 401 },
      config: { url: '/materials', headers: {} },
    };

    // Mock axiosClient.post on /auth/refresh to reject
    const originalPost = axiosClient.post;
    axiosClient.post = vi.fn().mockRejectedValue(new Error('Refresh token expired'));

    await expect(errorHandler(mockError)).rejects.toThrow();

    expect(eventListener).toHaveBeenCalled();
    expect(getAccessToken()).toBeNull();

    window.removeEventListener(SESSION_EXPIRED_EVENT, eventListener);
    axiosClient.post = originalPost;
  });
});
