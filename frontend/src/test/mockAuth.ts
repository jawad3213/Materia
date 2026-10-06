import { vi } from "vitest";
import * as useAuthModule from "../modules/auth/hooks/useAuth";
import type { UserRole } from "../modules/auth/types/auth.types";
import { matrix, type MatrixRole } from "./actionMatrix";

/**
 * Signs a test in as a user of the given role, holding exactly that role's permissions from the
 * shared action matrix. `hasPermission` mirrors AuthContext: ADMIN passes every check.
 */
export function signInAs(role: MatrixRole, userId = `user-${role.toLowerCase()}`) {
  const permissions = matrix.roles[role].permissions;
  vi.spyOn(useAuthModule, "default").mockReturnValue({
    user: { id: userId, name: `${role} user`, email: `${userId}@materia.test`, role: role as UserRole, permissions },
    isAuthenticated: true,
    isLoading: false,
    error: null,
    hasRole: vi.fn((roles: UserRole | UserRole[]) => (Array.isArray(roles) ? roles : [roles]).includes(role as UserRole)),
    hasPermission: vi.fn((p: string) => matrix.roles[role].passesEveryPermissionCheck || permissions.includes(p)),
    login: vi.fn(),
    logout: vi.fn(),
    changePassword: vi.fn(),
    clearMustChangePassword: vi.fn(),
    updateUser: vi.fn(),
    resetPassword: vi.fn(),
    confirmPasswordReset: vi.fn(),
    clearError: vi.fn(),
  } as unknown as ReturnType<typeof useAuthModule.default>);
  return userId;
}
