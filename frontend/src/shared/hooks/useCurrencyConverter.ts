import { useState, useEffect, useCallback } from 'react';
import { currencyApi } from '../api/currencyApi';
import type { CurrencyInfo, CurrencyCode } from '../types/currency';

// Default live fallback rates relative to 1 EUR
const DEFAULT_EUR_RATES: Record<string, number> = {
  EUR: 1.0,
  USD: 1.16,
  MAD: 10.85,
};

/**
 * Universal Currency Conversion Formula for MAD, EUR, USD
 * Converts amount from any currency (from) to any currency (to).
 */
export function convertCurrency(
  amount: number | string | undefined | null,
  from: string | undefined | null,
  to: string | undefined | null,
  eurRates: Record<string, number> = DEFAULT_EUR_RATES
): { converted: number; rate: number; isConverted: boolean } {
  const parseNum = (val: unknown): number => {
    if (val === undefined || val === null) return 0;
    if (typeof val === 'number') return isNaN(val) ? 0 : val;
    let str = String(val).replace(/[^0-9.,-]+/g, '');
    if (str.includes(',') && str.includes('.')) {
      if (str.indexOf(',') < str.indexOf('.')) {
        str = str.replace(/,/g, '');
      } else {
        str = str.replace(/\./g, '').replace(/,/g, '.');
      }
    } else if (str.includes(',')) {
      str = str.replace(/,/g, '.');
    }
    const n = parseFloat(str);
    return isNaN(n) ? 0 : n;
  };

  const numAmount = parseNum(amount);

  if (numAmount === 0 || isNaN(numAmount)) {
    return { converted: 0, rate: 1, isConverted: false };
  }

  const normalizeCurrency = (code: string | undefined | null): string => {
    if (!code) return 'MAD';
    const clean = String(code).toUpperCase().trim();
    if (clean === 'DH' || clean.includes('DIRHAM') || clean.includes('MAD')) return 'MAD';
    if (clean === '€' || clean.includes('EUR') || clean.includes('â‚¬') || clean.includes('\u20AC')) return 'EUR';
    if (clean === '$' || clean.includes('USD')) return 'USD';
    return clean;
  };

  const cleanFrom = normalizeCurrency(from);
  const cleanTo = normalizeCurrency(to);

  if (cleanFrom === cleanTo) {
    return { converted: numAmount, rate: 1, isConverted: false };
  }

  const eurToFrom = eurRates[cleanFrom] ?? DEFAULT_EUR_RATES[cleanFrom] ?? 1.0;
  const eurToTarget = eurRates[cleanTo] ?? DEFAULT_EUR_RATES[cleanTo] ?? 1.0;

  if (!eurToFrom || eurToFrom <= 0 || isNaN(eurToFrom)) {
    return { converted: numAmount, rate: 1, isConverted: false };
  }
  if (!eurToTarget || eurToTarget <= 0 || isNaN(eurToTarget)) {
    return { converted: numAmount, rate: 1, isConverted: false };
  }

  // 1 unit of 'from' in 'to'
  const unitRate = eurToTarget / eurToFrom;
  const rawConverted = numAmount * unitRate;
  const converted = isNaN(rawConverted)
    ? numAmount
    : Math.round(rawConverted * 100) / 100;

  return {
    converted: isNaN(converted) ? numAmount : converted,
    rate: isNaN(unitRate) ? 1 : Math.round(unitRate * 10000) / 10000,
    isConverted: true,
  };
}

export function useCurrencyConverter(targetCurrency: string = 'MAD') {
  const [eurRates, setEurRates] = useState<Record<string, number>>(DEFAULT_EUR_RATES);
  const [currencies, setCurrencies] = useState<CurrencyInfo[]>([]);
  const [isLoading, setIsLoading] = useState(false);

  // Load supported currencies & latest EUR rates on mount
  useEffect(() => {
    currencyApi
      .getCurrencies()
      .then((res) => {
        if (Array.isArray(res.data)) {
          setCurrencies(res.data);
        }
      })
      .catch((err) => {
        console.warn('Could not load currencies list:', err);
      });

    setIsLoading(true);
    // Fetch live rates relative to EUR
    currencyApi
      .getRates('EUR')
      .then((res) => {
        if (res.data && Object.keys(res.data).length > 0) {
          setEurRates((prev) => ({
            ...prev,
            ...res.data,
            EUR: 1.0,
          }));
        }
      })
      .catch((err) => {
        console.warn('Using default fallback rates for EUR:', err);
      })
      .finally(() => {
        setIsLoading(false);
      });
  }, []);

  /**
   * Converts an amount from source currency into target currency.
   */
  const convertToTarget = useCallback(
    (amount: number | string | undefined | null, fromCurrency: string | undefined | null, customTarget?: string) => {
      const target = customTarget || targetCurrency || 'MAD';
      return convertCurrency(amount, fromCurrency, target, eurRates);
    },
    [eurRates, targetCurrency]
  );

  return {
    rates: eurRates,
    currencies,
    isLoadingRates: isLoading,
    convertToTarget,
    convertCurrency: (amount: number | string | undefined | null, from: string | undefined | null, to: string | undefined | null) =>
      convertCurrency(amount, from, to, eurRates),
  };
}
