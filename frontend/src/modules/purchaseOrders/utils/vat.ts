/** VAT rate a new order line starts with, in percent (backend app.tax.default-rate). */
export const DEFAULT_VAT_RATE = 20;

const toCents = (value: number) => Math.round(value * 100) / 100;

/** The VAT of a line: quantity x unit price at the rate, rounded to the cent like the backend. */
export function lineVat(quantity: number, unitPrice: number, ratePercent: number): number {
  return toCents(toCents((Number(quantity) || 0) * (Number(unitPrice) || 0)) * (Number(ratePercent) || 0) / 100);
}

/**
 * The rate an order without VAT per line effectively used (its tax over its subtotal), so editing it keeps its VAT;
 * the default rate when it had no tax amount.
 */
export function effectiveVatRate(taxAmount: number | null | undefined, subtotal: number): number {
  if (taxAmount === null || taxAmount === undefined || !(subtotal > 0)) return DEFAULT_VAT_RATE;
  return Math.round((taxAmount / subtotal) * 10000) / 100;
}
