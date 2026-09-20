import axiosClient from './axiosClient';
import type { CurrencyInfo, ExchangeRatePair, MoneyConversion, CurrencyCode } from '../types/currency';

const BASE_URL = '/currencies';

export const currencyApi = {
  /**
   * Get all supported currencies with symbols and metadata
   */
  getCurrencies: () =>
    axiosClient.get<CurrencyInfo[]>(BASE_URL),

  /**
   * Get latest exchange rates for a base currency
   */
  getRates: (base: string = 'EUR') =>
    axiosClient.get<Record<string, number>>(`${BASE_URL}/rates`, {
      params: { base },
    }),

  /**
   * Get exchange rate between two specific currencies
   */
  getPairRate: (from: string, to: string) =>
    axiosClient.get<ExchangeRatePair>(`${BASE_URL}/pair`, {
      params: { from, to },
    }),

  /**
   * Convert an amount directly from one currency to another
   */
  convert: (amount: number, from: string, to: string) =>
    axiosClient.get<MoneyConversion>(`${BASE_URL}/convert`, {
      params: { amount, from, to },
    }),
};
