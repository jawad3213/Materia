package com.materia.backend.common.domain.services;

import com.materia.backend.common.domain.enums.CurrencyCode;
import com.materia.backend.common.domain.valueObjects.ExchangeRate;
import com.materia.backend.common.domain.valueObjects.Money;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Domain Service Port for retrieving exchange rates and converting monetary values.
 */
public interface ExchangeRateService {

    /**
     * Gets the exchange rate between two currencies.
     *
     * @param from Source currency
     * @param to   Target currency
     * @return The exchange rate
     */
    ExchangeRate getExchangeRate(CurrencyCode from, CurrencyCode to);

    /**
     * Converts a Money value to a target currency using current market rates.
     *
     * @param money          The money value to convert
     * @param targetCurrency The target currency
     * @return Converted money
     */
    Money convert(Money money, CurrencyCode targetCurrency);

    /**
     * Gets all exchange rates for a base currency.
     *
     * @param base The base currency
     * @return Map of target CurrencyCode to rate BigDecimal
     */
    Map<CurrencyCode, BigDecimal> getLatestRates(CurrencyCode base);
}
