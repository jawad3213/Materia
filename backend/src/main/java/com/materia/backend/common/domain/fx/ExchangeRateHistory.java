package com.materia.backend.common.domain.fx;

import com.materia.backend.common.domain.enums.CurrencyCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

/**
 * 🔹 EXCHANGE RATE HISTORY (OUTPUT PORT)
 *
 * One stored rate per day and currency pair.
 */
public interface ExchangeRateHistory {

    record StoredRate(LocalDate rateDate, CurrencyCode from, CurrencyCode to, BigDecimal rate, String source) {
    }

    Optional<StoredRate> latestOnOrBefore(CurrencyCode from, CurrencyCode to, LocalDate date);

    Optional<StoredRate> earliestAfter(CurrencyCode from, CurrencyCode to, LocalDate date);

    /** Creates or replaces the rate of that day and pair. */
    void save(StoredRate rate);
}
