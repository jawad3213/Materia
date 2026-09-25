package com.materia.backend.common.infrastructure.adapters.out.exchange;

import com.materia.backend.common.domain.enums.CurrencyCode;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory thread-safe cache for exchange rates with TTL (Time-To-Live).
 */
@Component
public class ExchangeRateCache {

    private final ExchangeRateProperties properties;
    private final Map<CurrencyCode, CachedRates> cache = new ConcurrentHashMap<>();

    public ExchangeRateCache(ExchangeRateProperties properties) {
        this.properties = properties;
    }

    public Map<CurrencyCode, BigDecimal> get(CurrencyCode base) {
        CachedRates cached = cache.get(base);
        if (cached == null || cached.isExpired(properties.getCacheTtlMinutes())) {
            return null;
        }
        return cached.rates;
    }

    public BigDecimal getRate(CurrencyCode from, CurrencyCode to) {
        if (from == null || to == null) {
            return null;
        }
        if (from.equals(to)) {
            return BigDecimal.ONE;
        }
        Map<CurrencyCode, BigDecimal> rates = get(from);
        return rates != null ? rates.get(to) : null;
    }

    public void put(CurrencyCode base, Map<CurrencyCode, BigDecimal> rates) {
        if (base != null && rates != null) {
            cache.put(base, new CachedRates(rates, Instant.now()));
        }
    }

    public void putRate(CurrencyCode from, CurrencyCode to, BigDecimal rate) {
        if (from != null && to != null && rate != null) {
            CachedRates cached = cache.get(from);
            if (cached == null || cached.isExpired(properties.getCacheTtlMinutes())) {
                Map<CurrencyCode, BigDecimal> map = new ConcurrentHashMap<>();
                map.put(from, BigDecimal.ONE);
                map.put(to, rate);
                cache.put(from, new CachedRates(map, Instant.now()));
            } else {
                cached.rates.put(to, rate);
            }
        }
    }

    public void clear() {
        cache.clear();
    }

    private static class CachedRates {
        private final Map<CurrencyCode, BigDecimal> rates;
        private final Instant timestamp;

        public CachedRates(Map<CurrencyCode, BigDecimal> rates, Instant timestamp) {
            this.rates = new ConcurrentHashMap<>(rates);
            this.timestamp = timestamp;
        }

        public boolean isExpired(int ttlMinutes) {
            return Instant.now().isAfter(timestamp.plusSeconds((long) ttlMinutes * 60));
        }
    }
}
