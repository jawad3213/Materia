package com.materia.backend.common.domain.fx;

import com.materia.backend.common.domain.enums.CurrencyCode;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

/**
 * 🔹 FX VALUATION
 *
 * The rate that converts an amount in {@code from} into the reporting currency (amount × rate), the day that rate
 * belongs to and where it came from: {@link #SAME} (no conversion), {@link #LIVE} (exchange-rate API) or
 * {@link #FALLBACK} (built-in rates used when the API is unavailable).
 */
public record FxValuation(CurrencyCode from, CurrencyCode reporting, BigDecimal rate, LocalDate rateDate, String source) {

    public static final String SAME = "SAME";
    public static final String LIVE = "LIVE";
    public static final String FALLBACK = "FALLBACK";
    public static final int RATE_SCALE = 8;

    public static FxValuation identity(CurrencyCode currency, LocalDate date) {
        return new FxValuation(currency, currency, BigDecimal.ONE, date, SAME);
    }

    /** The amount in the reporting currency, rounded to cents. */
    public BigDecimal convert(BigDecimal amount) {
        if (amount == null) {
            return null;
        }
        return amount.multiply(rate).setScale(2, RoundingMode.HALF_UP);
    }

    public boolean isFallback() {
        return FALLBACK.equals(source);
    }
}
