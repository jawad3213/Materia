import { afterEach, describe, expect, it, vi } from "vitest";
import { renderHook } from "@testing-library/react";
import React from "react";
import { AuthProvider, AuthContext } from "../AuthContext";
import authService from "../../services/authService";
import { matrix } from "../../../../test/actionMatrix";

/**
 * [T059] When a user carries no permission list, AuthContext falls back to a built-in matrix.
 * Its order and receipt permissions must match Role.java, as recorded in the shared action matrix,
 * or the screens would hide or offer actions wrongly for such users.
 */
const ORDER_AND_RECEIPT_PERMISSIONS = [
  "order:read", "order:write", "order:validate", "order:cancel",
  "receipt:read", "receipt:write", "receipt:quality",
];

const wrapper = ({ children }: { children: React.ReactNode }) => <AuthProvider>{children}</AuthProvider>;

describe("AuthContext fallback permissions agree with the action matrix", () => {
  afterEach(() => vi.restoreAllMocks());

  for (const role of ["PURCHASER", "RECEIVER"] as const) {
    it(`rule: a ${role} without a permission list holds exactly the matrix's order and receipt permissions`, () => {
      vi.spyOn(authService, "getStoredUser").mockReturnValue({
        id: `${role}-1`,
        name: role,
        email: `${role.toLowerCase()}@materia.test`,
        role,
      });

      const { result } = renderHook(() => React.useContext(AuthContext)!, { wrapper });

      for (const permission of ORDER_AND_RECEIPT_PERMISSIONS) {
        expect(result.current.hasPermission(permission), `${role} ${permission}`).toBe(
          matrix.roles[role].permissions.includes(permission)
        );
      }
    });
  }
});
