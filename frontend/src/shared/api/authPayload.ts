/** A user as the backend describes it in login and refresh responses; endpoints fill different fields. */
export interface BackendAuthUser {
  id?: string;
  userId?: string;
  email?: string;
  name?: string;
  firstName?: string;
  lastName?: string;
  role?: string;
  mustChangePassword?: boolean;
  permissions?: string[];
}

/** The login / refresh payload as sent by the backend, before the frontend normalizes it. */
export interface BackendAuthResponse {
  accessToken: string;
  refreshToken?: string;
  user?: BackendAuthUser;
  role?: string | { code?: string };
  email?: string;
  userId?: string;
  mustChangePassword?: boolean;
  permissions?: string[];
}
