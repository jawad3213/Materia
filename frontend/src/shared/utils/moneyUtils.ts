/** Parses an amount the backend may send as a number or as a formatted string such as "1200.50 MAD". */
export function parseAmount(value: string | number | null | undefined): number {
  if (value === null || value === undefined) return 0;
  if (typeof value === "number") return value;
  const parsed = parseFloat(value.replace(/[^0-9.-]/g, ""));
  return Number.isNaN(parsed) ? 0 : parsed;
}
