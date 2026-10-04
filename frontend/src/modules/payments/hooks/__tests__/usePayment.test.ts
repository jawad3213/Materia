import { afterEach, describe, expect, it, vi } from "vitest";
import { renderHook } from "@testing-library/react";
import * as useAuthModule from "../../../auth/hooks/useAuth";
import usePaymentPermissions from "../usePayment";
import type { Payment, PaymentStatus } from "../../types/payment.types";

/** Payment actions follow Role.java (only `payment:write` manages payments) and the backend statuses. */
type Role = "ADMIN" | "PURCHASER" | "RECEIVER";

const PERMISSIONS: Record<Role, string[]> = {
  ADMIN: ["payment:read", "payment:write"],
  PURCHASER: ["payment:read"],
  RECEIVER: [],
};

const STATUSES: PaymentStatus[] = ["DRAFT", "PENDING", "COMPLETED", "CANCELLED"];

const ADMIN_ACTIONS: Record<string, PaymentStatus[]> = {
  canPrepare: ["DRAFT"],
  canComplete: ["DRAFT", "PENDING"],
  canCancel: ["DRAFT", "PENDING"],
  canDelete: ["DRAFT"],
};

function signInAs(role: Role) {
  vi.spyOn(useAuthModule, "default").mockReturnValue({
    user: { id: `user-${role}`, name: role, email: `${role}@materia.test`, role, permissions: PERMISSIONS[role] },
    isAuthenticated: true,
    hasPermission: (p: string) => role === "ADMIN" || PERMISSIONS[role].includes(p),
  } as unknown as ReturnType<typeof useAuthModule.default>);
}

const payment = (status: PaymentStatus) =>
  ({ id: "p-1", paymentCode: "PAY-1", status, lines: [] }) as unknown as Payment;

describe("usePaymentPermissions follows Role.java and the payment workflow", () => {
  afterEach(() => vi.restoreAllMocks());

  for (const role of Object.keys(PERMISSIONS) as Role[]) {
    const manages = role === "ADMIN";

    it(`rule: ${role} ${manages ? "can" : "cannot"} create payments`, () => {
      signInAs(role);
      const { result } = renderHook(() => usePaymentPermissions());
      expect(result.current.canCreate).toBe(manages);
    });

    for (const [action, statuses] of Object.entries(ADMIN_ACTIONS)) {
      for (const status of STATUSES) {
        const offered = manages && statuses.includes(status);
        it(`rule: ${role} on a ${status} payment is ${offered ? "offered" : "not offered"} ${action}`, () => {
          signInAs(role);
          const { result } = renderHook(() => usePaymentPermissions());
          const check = result.current[action as keyof typeof result.current] as (p: Payment) => boolean;
          expect(check(payment(status))).toBe(offered);
        });
      }
    }
  }
});
