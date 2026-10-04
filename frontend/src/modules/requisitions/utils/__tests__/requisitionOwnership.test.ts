import { describe, expect, it } from "vitest";
import { isOwnRequisition } from "../requisitionOwnership";

describe("isOwnRequisition (no self-approval, as the backend enforces)", () => {
  it("rule: the requester's own requisition is theirs", () => {
    expect(isOwnRequisition({ requesterId: "u-1", createdBy: "u-9" }, "u-1")).toBe(true);
  });

  it("rule: a requisition created on someone else's behalf belongs to its creator too", () => {
    expect(isOwnRequisition({ requesterId: "Employee name", createdBy: "u-2" }, "u-2")).toBe(true);
  });

  it("rule: anyone else may approve; without a signed-in user nothing is considered own", () => {
    expect(isOwnRequisition({ requesterId: "u-1", createdBy: "u-2" }, "u-3")).toBe(false);
    expect(isOwnRequisition({ requesterId: "u-1", createdBy: null }, undefined)).toBe(false);
  });
});
