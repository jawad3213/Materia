import { isAxiosError } from "axios";

type ApiErrorBody = { message?: string; detail?: string; error?: string };

/** Returns the backend's error message when there is one, otherwise the given fallback. */
export function getApiErrorMessage(error: unknown, fallback: string): string {
  if (isAxiosError<ApiErrorBody>(error)) {
    const body = error.response?.data;
    return body?.message || body?.detail || body?.error || fallback;
  }
  return fallback;
}

/** The HTTP status of a failed API call, if the error came from one. */
export function getApiErrorStatus(error: unknown): number | undefined {
  return isAxiosError(error) ? error.response?.status : undefined;
}
