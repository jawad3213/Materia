export const CurrencyCode = {
  MAD: 'MAD',
  EUR: 'EUR',
  USD: 'USD',
} as const;

export type CurrencyCodeValue = typeof CurrencyCode[keyof typeof CurrencyCode];
