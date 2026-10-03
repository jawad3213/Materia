import { afterEach, describe, expect, it, vi } from "vitest";
import { renderHook } from "@testing-library/react";
import usePurchaseOrderPermissions from "../usePurchaseOrder";
import { isAllowed, roles, statuses } from "../../../../test/actionMatrix";
import { signInAs } from "../../../../test/mockAuth";
import type { OrderStatus } from "../../types";

/**
 * [T056] The purchase order screens offer exactly the actions the shared matrix allows, for every
 * role and every status (US7-1, US7-2, SC-002a). The backend's ActionMatrixContractTest checks the
 * same file, so the screens and the API cannot drift apart unnoticed.
 */
const HOOK_FOR_ACTION = {
  edit: "canEdit",
  delete: "canDelete",
  submit: "canSubmit",
  confirm: "canConfirm",
  reject: "canReject",
  assignReceiver: "canAssignReceiver",
  trackDelivery: "canTrackDelivery",
  receive: "canReceive",
  closeShort: "canCloseShort",
  cancel: "canCancel",
} as const;

describe("usePurchaseOrderPermissions agrees with the action matrix", () => {
  afterEach(() => vi.restoreAllMocks());

  for (const role of roles) {
    for (const status of statuses("purchaseOrder")) {
      for (const [action, hook] of Object.entries(HOOK_FOR_ACTION)) {
        for (const assigned of [true, false]) {
          it(`rule: ${role} ${assigned ? "as" : "not as"} assignee is ${
            isAllowed("purchaseOrder", role, status, action, assigned) ? "offered" : "not offered"
          } ${action} on a ${status} order`, () => {
            const userId = signInAs(role);
            const order = { status: status as OrderStatus, assignedTo: assigned ? userId : "someone-else" };

            const { result } = renderHook(() => usePurchaseOrderPermissions());

            expect(result.current[hook](order)).toBe(isAllowed("purchaseOrder", role, status, action, assigned));
          });
        }
      }
    }
  }

  it("rule: only a purchaser or admin may start a new order", () => {
    for (const role of roles) {
      signInAs(role);
      const { result } = renderHook(() => usePurchaseOrderPermissions());
      expect(result.current.canCreate).toBe(role !== "RECEIVER");
      vi.restoreAllMocks();
    }
  });

  it("rule: a receiver viewing an order assigned to someone else is offered no receipt action (US7-2)", () => {
    signInAs("RECEIVER", "receiver-me");
    const { result } = renderHook(() => usePurchaseOrderPermissions());

    expect(result.current.canReceive({ status: "READY_FOR_RECEIPT", assignedTo: "receiver-other" })).toBe(false);
    expect(result.current.canReceive({ status: "READY_FOR_RECEIPT", assignedTo: "receiver-me" })).toBe(true);
  });
});
