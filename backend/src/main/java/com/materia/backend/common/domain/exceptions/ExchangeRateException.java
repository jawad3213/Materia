package com.materia.backend.common.domain.exceptions;

import com.materia.backend.common.domain.enums.CurrencyCode;

/**
 * Exception thrown when an exchange rate operation fails.
 */
public class ExchangeRateException extends RuntimeException {

    private final CurrencyCode fromCurrency;
    private final CurrencyCode toCurrency;

    public ExchangeRateException(String message) {
        super(message);
        this.fromCurrency = null;
        this.toCurrency = null;
    }

    public ExchangeRateException(String message, Throwable cause) {
        super(message, cause);
        this.fromCurrency = null;
        this.toCurrency = null;
    }

    public ExchangeRateException(CurrencyCode from, CurrencyCode to, String message) {
        super(String.format("Exchange rate error from %s to %s: %s", from, to, message));
        this.fromCurrency = from;
        this.toCurrency = to;
    }

    public ExchangeRateException(CurrencyCode from, CurrencyCode to, Throwable cause) {
        super(String.format("Failed to retrieve exchange rate from %s to %s", from, to), cause);
        this.fromCurrency = from;
        this.toCurrency = to;
    }

    public CurrencyCode getFromCurrency() {
        return fromCurrency;
    }

    public CurrencyCode getToCurrency() {
        return toCurrency;
    }
}
