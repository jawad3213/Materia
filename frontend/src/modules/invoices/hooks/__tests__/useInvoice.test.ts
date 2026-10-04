import { afterEach, describe, expect, it, vi } from "vitest";
import { renderHook } from "@testing-library/react";
import * as useAuthModule from "../../../auth/hooks/useAuth";
import useInvoicePermissions from "../useInvoice";
import type { Invoice, InvoiceStatus } from "../../types/invoice.types";

/**
 * The invoice screens offer each action only to the roles Role.java grants it to, and only in the
 * statuses the backend Invoice entity allows it from.
 */
type Role = "ADMIN" | "PURCHASER" | "RECEIVER";

/** Invoice-related permissions per role, as in Role.java. ADMIN passes every check. */
const PERMISSIONS: Record<Role, string[]> = {
  ADMIN: ["invoice:read", "invoice:write", "invoice:validate", "payment:read", "payment:write"],
  PURCHASER: ["invoice:read", "invoice:write", "payment:read"],
  RECEIVER: [],
};

const STATUSES: InvoiceStatus[] = ["DRAFT", "SUBMITTED", "VERIFIED", "PAID", "CANCELLED"];

/** Expected availability: role → action → statuses where it is offered. */
const EXPECTED: Record<Role, Record<string, InvoiceStatus[]>> = {
  ADMIN: {
    canSubmit: ["DRAFT"],
    canVerify: ["SUBMITTED"],
    canPay: ["VERIFIED"],
    canCancel: ["DRAFT", "SUBMITTED", "VERIFIED"],
    canDelete: ["DRAFT"],
  },
  PURCHASER: {
    canSubmit: ["DRAFT"],
    canVerify: [],
    canPay: [],
    canCancel: ["DRAFT", "SUBMITTED"],
    canDelete: ["DRAFT"],
  },
  RECEIVER: { canSubmit: [], canVerify: [], canPay: [], canCancel: [], canDelete: [] },
};

function signInAs(role: Role) {
  vi.spyOn(useAuthModule, "default").mockReturnValue({
    user: { id: `user-${role}`, name: role, email: `${role}@materia.test`, role, permissions: PERMISSIONS[role] },
    isAuthenticated: true,
    hasPermission: (p: string) => role === "ADMIN" || PERMISSIONS[role].includes(p),
  } as unknown as ReturnType<typeof useAuthModule.default>);
}

const invoice = (status: InvoiceStatus) =>
  ({ id: "inv-1", invoiceCode: "INV-1", status, invoiceType: "STANDARD", lines: [] }) as unknown as Invoice;

describe("useInvoicePermissions follows Role.java and the invoice workflow", () => {
  afterEach(() => vi.restoreAllMocks());

  it("rule: a partly paid verified invoice is still offered payment but no longer cancellation", () => {
    signInAs("ADMIN");
    const { result } = renderHook(() => useInvoicePermissions());
    const partlyPaid = { ...invoice("VERIFIED"), paidAmount: 10 } as Invoice;

    expect(result.current.canPay(partlyPaid)).toBe(true);
    expect(result.current.canCancel(partlyPaid)).toBe(false);
  });

  for (const role of Object.keys(EXPECTED) as Role[]) {
    it(`rule: ${role} ${role === "ADMIN" ? "can" : "cannot"} record invoices`, () => {
      signInAs(role);
      const { result } = renderHook(() => useInvoicePermissions());
      expect(result.current.canRecord).toBe(role === "ADMIN");
    });

    for (const [action, allowed] of Object.entries(EXPECTED[role])) {
      for (const status of STATUSES) {
        const offered = allowed.includes(status);
        it(`rule: ${role} on a ${status} invoice is ${offered ? "offered" : "not offered"} ${action}`, () => {
          signInAs(role);
          const { result } = renderHook(() => useInvoicePermissions());
          const check = result.current[action as keyof typeof result.current] as (i: Invoice) => boolean;
          expect(check(invoice(status))).toBe(offered);
        });
      }
    }
  }
});
