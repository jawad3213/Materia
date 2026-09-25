package com.materia.backend.common.domain.valueObjects;

import com.materia.backend.common.domain.enums.CurrencyCode;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Objects;

/**
 * ExchangeRate Value Object
 * Represents a conversion rate between a source and a target currency at a given date.
 */
public class ExchangeRate {

    public static final int RATE_SCALE = 6;
    public static final RoundingMode DEFAULT_ROUNDING = RoundingMode.HALF_UP;

    private final CurrencyCode fromCurrency;
    private final CurrencyCode toCurrency;
    private final BigDecimal rate;
    private final LocalDate rateDate;

    public ExchangeRate(CurrencyCode fromCurrency, CurrencyCode toCurrency, BigDecimal rate, LocalDate rateDate) {
        if (fromCurrency == null) {
            throw new IllegalArgumentException("Source currency (fromCurrency) is required");
        }
        if (toCurrency == null) {
            throw new IllegalArgumentException("Target currency (toCurrency) is required");
        }
        if (rate == null) {
            throw new IllegalArgumentException("Exchange rate is required");
        }
        if (rate.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Exchange rate must be positive: " + rate);
        }
        this.fromCurrency = fromCurrency;
        this.toCurrency = toCurrency;
        this.rate = rate.setScale(RATE_SCALE, DEFAULT_ROUNDING);
        this.rateDate = rateDate != null ? rateDate : LocalDate.now();
    }

    public static ExchangeRate of(CurrencyCode from, CurrencyCode to, BigDecimal rate) {
        return new ExchangeRate(from, to, rate, LocalDate.now());
    }

    public static ExchangeRate of(CurrencyCode from, CurrencyCode to, BigDecimal rate, LocalDate date) {
        return new ExchangeRate(from, to, rate, date);
    }

    public static ExchangeRate of(CurrencyCode from, CurrencyCode to, double rate) {
        return new ExchangeRate(from, to, BigDecimal.valueOf(rate), LocalDate.now());
    }

    public static ExchangeRate identity(CurrencyCode currency) {
        return new ExchangeRate(currency, currency, BigDecimal.ONE, LocalDate.now());
    }

    /**
     * Converts a Money amount in fromCurrency to toCurrency using this rate
     */
    public Money convert(Money money) {
        if (money == null) {
            throw new IllegalArgumentException("Money to convert cannot be null");
        }
        if (!money.getCurrency().equals(fromCurrency)) {
            throw new IllegalArgumentException(
                    "Cannot convert money of currency " + money.getCurrency() + " using exchange rate for " + fromCurrency
            );
        }
        if (fromCurrency.equals(toCurrency)) {
            return money;
        }
        BigDecimal convertedAmount = money.getAmount().multiply(rate).setScale(Money.DEFAULT_SCALE, Money.DEFAULT_ROUNDING);
        return Money.of(convertedAmount, toCurrency);
    }

    /**
     * Inverts the exchange rate (e.g., EUR -> USD becomes USD -> EUR)
     */
    public ExchangeRate invert() {
        if (fromCurrency.equals(toCurrency)) {
            return this;
        }
        BigDecimal invertedRate = BigDecimal.ONE.divide(rate, RATE_SCALE, DEFAULT_ROUNDING);
        return new ExchangeRate(toCurrency, fromCurrency, invertedRate, rateDate);
    }

    public CurrencyCode getFromCurrency() {
        return fromCurrency;
    }

    public CurrencyCode getToCurrency() {
        return toCurrency;
    }

    public BigDecimal getRate() {
        return rate;
    }

    public LocalDate getRateDate() {
        return rateDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExchangeRate that = (ExchangeRate) o;
        return fromCurrency == that.fromCurrency &&
                toCurrency == that.toCurrency &&
                rate.compareTo(that.rate) == 0 &&
                Objects.equals(rateDate, that.rateDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fromCurrency, toCurrency, rate, rateDate);
    }

    @Override
    public String toString() {
        return String.format("ExchangeRate[1 %s = %s %s on %s]", fromCurrency, rate, toCurrency, rateDate);
    }
}
