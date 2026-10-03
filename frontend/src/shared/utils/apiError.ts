import { isAxiosError } from "axios";

/** Returns the backend's error message when there is one, otherwise the given fallback. */
export function getApiErrorMessage(error: unknown, fallback: string): string {
  if (isAxiosError<{ message?: string }>(error)) {
    return error.response?.data?.message || fallback;
  }
  return fallback;
}
