import { describe, expect, it } from "vitest";
import { displayValue, fieldLabel } from "../auditFormat";

describe("audit history formatting", () => {
  it("turns field names into labels", () => {
    expect(fieldLabel("unitPrice")).toBe("Unit price");
    expect(fieldLabel("status")).toBe("Status");
    expect(fieldLabel("taxRate2")).toBe("Tax rate2");
  });

  it("shows absent values as a dash and booleans as words", () => {
    expect(displayValue(null)).toBe("—");
    expect(displayValue(undefined)).toBe("—");
    expect(displayValue("")).toBe("—");
    expect(displayValue(true)).toBe("Yes");
    expect(displayValue(12.5)).toBe("12.5");
  });
});
