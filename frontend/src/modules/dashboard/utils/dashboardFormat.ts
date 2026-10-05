import type { MonthlyTrend } from "../types/dashboard.types";

/** "1,234.50 MAD"; large amounts in the compact form used on chart axes when `compact` ("12.4K MAD"). */
export function formatMoney(value: number, currency: string, compact = false): string {
  const number = new Intl.NumberFormat("en-US", compact
    ? { notation: "compact", maximumFractionDigits: 1 }
    : { minimumFractionDigits: 2, maximumFractionDigits: 2 }).format(value || 0);
  return `${number} ${currency}`;
}

/** "Oct 26" from "2026-10". */
export function monthLabel(month: string): string {
  const [year, m] = month.split("-").map(Number);
  return new Date(year, (m || 1) - 1, 1).toLocaleDateString("en-US", { month: "short", year: "2-digit" });
}

/** The monthly series summed by calendar quarter ("Q4 26"), oldest first. */
export function toQuarters(trend: MonthlyTrend): MonthlyTrend {
  const keys: string[] = [];
  const index = new Map<string, number>();
  trend.months.forEach((month) => {
    const [year, m] = month.split("-").map(Number);
    const key = `Q${Math.ceil(m / 3)} ${String(year).slice(2)}`;
    if (!index.has(key)) {
      index.set(key, keys.length);
      keys.push(key);
    }
  });
  const sum = (values: number[]) => {
    const totals = keys.map(() => 0);
    values.forEach((value, i) => {
      const [year, m] = trend.months[i].split("-").map(Number);
      totals[index.get(`Q${Math.ceil(m / 3)} ${String(year).slice(2)}`)!] += value || 0;
    });
    return totals.map((t) => Math.round(t * 100) / 100);
  };
  return { months: keys, ordered: sum(trend.ordered), invoiced: sum(trend.invoiced), paid: sum(trend.paid) };
}

/** Sum of a series. */
export const total = (values: number[]) => Math.round(values.reduce((a, b) => a + (b || 0), 0) * 100) / 100;

/** "3 days ago", "just now". */
export function timeAgo(iso: string, now: Date = new Date()): string {
  const seconds = Math.max(0, Math.round((now.getTime() - new Date(iso).getTime()) / 1000));
  if (seconds < 60) return "just now";
  const minutes = Math.round(seconds / 60);
  if (minutes < 60) return `${minutes} min ago`;
  const hours = Math.round(minutes / 60);
  if (hours < 24) return `${hours} h ago`;
  const days = Math.round(hours / 24);
  if (days < 30) return `${days} day${days > 1 ? "s" : ""} ago`;
  return new Date(iso).toLocaleDateString();
}
