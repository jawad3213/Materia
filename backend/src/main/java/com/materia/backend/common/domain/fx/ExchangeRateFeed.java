package com.materia.backend.common.domain.fx;

import com.materia.backend.common.domain.enums.CurrencyCode;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 🔹 EXCHANGE RATE FEED (OUTPUT PORT)
 *
 * Today's rates from the exchange-rate service, saying whether they are live or the built-in fallback.
 */
public interface ExchangeRateFeed {

    /**
     * @param rates  units of each currency for one unit of {@code base}
     * @param source {@link FxValuation#LIVE} or {@link FxValuation#FALLBACK}
     */
    record RateQuote(Map<CurrencyCode, BigDecimal> rates, String source) {
    }

    RateQuote latest(CurrencyCode base);
}
