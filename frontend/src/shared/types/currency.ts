export type CurrencyCode = 'MAD' | 'EUR' | 'USD';

export interface CurrencyInfo {
  code: CurrencyCode;
  displayName: string;
  symbol: string;
  decimalPlaces: number;
  isDefault: boolean;
}

export interface ExchangeRatePair {
  fromCurrency: CurrencyCode;
  toCurrency: CurrencyCode;
  rate: number;
  rateDate: string;
}

export interface MoneyConversion {
  originalAmount: number;
  originalCurrency: CurrencyCode;
  convertedAmount: number;
  targetCurrency: CurrencyCode;
  rate: number;
  rateDate: string;
}
