import { describe, expect, it } from "vitest";
import { formatMoney, monthLabel, timeAgo, toQuarters, total } from "../dashboardFormat";

describe("dashboard formatting", () => {
  it("rule: amounts carry their currency, compact on chart axes", () => {
    expect(formatMoney(1234.5, "MAD")).toBe("1,234.50 MAD");
    expect(formatMoney(12400, "EUR", true)).toBe("12.4K EUR");
    expect(formatMoney(0, "MAD")).toBe("0.00 MAD");
  });

  it("rule: months are shown as short month and year", () => {
    expect(monthLabel("2026-10")).toBe("Oct 26");
  });

  it("rule: quarters sum the months of each calendar quarter, oldest first", () => {
    const q = toQuarters({
      months: ["2026-08", "2026-09", "2026-10", "2026-11"],
      ordered: [10, 20, 30, 40],
      invoiced: [1, 2, 3, 4],
      paid: [],
    });
    expect(q.months).toEqual(["Q3 26", "Q4 26"]);
    expect(q.ordered).toEqual([30, 70]);
    expect(q.invoiced).toEqual([3, 7]);
    expect(q.paid).toEqual([0, 0]);
  });

  it("rule: totals and relative times", () => {
    expect(total([1.1, 2.2, 3.3])).toBe(6.6);
    const now = new Date("2026-10-15T12:00:00Z");
    expect(timeAgo("2026-10-15T11:59:30Z", now)).toBe("just now");
    expect(timeAgo("2026-10-15T11:30:00Z", now)).toBe("30 min ago");
    expect(timeAgo("2026-10-15T09:00:00Z", now)).toBe("3 h ago");
    expect(timeAgo("2026-10-12T12:00:00Z", now)).toBe("3 days ago");
  });
});
