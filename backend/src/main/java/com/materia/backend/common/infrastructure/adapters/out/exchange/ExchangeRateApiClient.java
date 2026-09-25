package com.materia.backend.common.infrastructure.adapters.out.exchange;

import com.materia.backend.common.domain.enums.CurrencyCode;
import com.materia.backend.common.domain.exceptions.ExchangeRateException;
import com.materia.backend.common.domain.services.ExchangeRateService;
import com.materia.backend.common.domain.valueObjects.ExchangeRate;
import com.materia.backend.common.domain.valueObjects.Money;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.util.EnumMap;
import java.util.Map;

/**
 * REST Client Adapter implementing ExchangeRateService.
 * Calls public/free exchange rate API with caching and fallback rates.
 */
@Service
public class ExchangeRateApiClient implements ExchangeRateService {

    private static final Logger log = LoggerFactory.getLogger(ExchangeRateApiClient.class);

    // Hardcoded fallback rates relative to 1 EUR in case external API is unreachable or offline
    private static final Map<CurrencyCode, BigDecimal> DEFAULT_EUR_RATES = Map.of(
            CurrencyCode.EUR, BigDecimal.ONE,
            CurrencyCode.USD, new BigDecimal("1.08"),
            CurrencyCode.MAD, new BigDecimal("10.85")
    );

    private final WebClient webClient;
    private final ExchangeRateCache cache;
    private final ExchangeRateProperties properties;

    public ExchangeRateApiClient(WebClient.Builder webClientBuilder,
                                 ExchangeRateCache cache,
                                 ExchangeRateProperties properties) {
        this.webClient = webClientBuilder.baseUrl(properties.getBaseUrl()).build();
        this.cache = cache;
        this.properties = properties;
    }

    @Override
    public ExchangeRate getExchangeRate(CurrencyCode from, CurrencyCode to) {
        if (from == null || to == null) {
            throw new IllegalArgumentException("From and To currencies must not be null");
        }
        if (from.equals(to)) {
            return ExchangeRate.identity(from);
        }

        // 1. Check pair in cache
        BigDecimal cachedRate = cache.getRate(from, to);
        if (cachedRate != null) {
            return ExchangeRate.of(from, to, cachedRate, LocalDate.now());
        }

        // 2. If API key is available, call ExchangeRate-API pair endpoint: /{key}/pair/{from}/{to}
        if (hasApiKey()) {
            BigDecimal apiRate = fetchPairFromApi(from, to);
            if (apiRate != null) {
                cache.putRate(from, to, apiRate);
                return ExchangeRate.of(from, to, apiRate, LocalDate.now());
            }
        }

        // 3. Try to get rate from base rates collection (cached or fetched)
        Map<CurrencyCode, BigDecimal> rates = getLatestRates(from);
        BigDecimal rate = rates.get(to);

        if (rate != null) {
            return ExchangeRate.of(from, to, rate, LocalDate.now());
        }

        // 4. Fallback calculation if not found
        Map<CurrencyCode, BigDecimal> fallback = computeFallbackRates(from);
        BigDecimal fallbackRate = fallback.get(to);
        if (fallbackRate != null) {
            return ExchangeRate.of(from, to, fallbackRate, LocalDate.now());
        }

        throw new ExchangeRateException(from, to, "No exchange rate found for target currency");
    }

    @Override
    public Money convert(Money money, CurrencyCode targetCurrency) {
        if (money == null) {
            throw new IllegalArgumentException("Money amount cannot be null");
        }
        if (targetCurrency == null) {
            throw new IllegalArgumentException("Target currency cannot be null");
        }
        if (money.getCurrency().equals(targetCurrency)) {
            return money;
        }

        ExchangeRate rate = getExchangeRate(money.getCurrency(), targetCurrency);
        return rate.convert(money);
    }

    @Override
    public Map<CurrencyCode, BigDecimal> getLatestRates(CurrencyCode base) {
        if (base == null) {
            throw new IllegalArgumentException("Base currency cannot be null");
        }

        // 1. Check in-memory cache
        Map<CurrencyCode, BigDecimal> cachedRates = cache.get(base);
        if (cachedRates != null && !cachedRates.isEmpty()) {
            return cachedRates;
        }

        // 2. Fetch from ExchangeRate-API latest endpoint: /{key}/latest/{base}
        if (hasApiKey()) {
            Map<CurrencyCode, BigDecimal> fetchedRates = fetchLatestFromApi(base);
            if (fetchedRates != null && !fetchedRates.isEmpty()) {
                cache.put(base, fetchedRates);
                return fetchedRates;
            }
        } else {
            log.info("No EXCHANGE_API_KEY configured for ExchangeRate-API. Utilizing fallback rates for base: {}", base);
        }

        // 3. Fallback to resilient offline estimate if remote API fails or key is missing
        log.warn("Falling back to local static rates for base currency: {}", base);
        Map<CurrencyCode, BigDecimal> fallback = computeFallbackRates(base);
        cache.put(base, fallback);
        return fallback;
    }

    /**
     * Calls ExchangeRate-API pair endpoint: /{key}/pair/{from}/{to}
     */
    private BigDecimal fetchPairFromApi(CurrencyCode from, CurrencyCode to) {
        try {
            String uri = "/" + properties.getApiKey() + "/pair/" + from.getCode() + "/" + to.getCode();
            log.debug("Fetching exchange rate pair from ExchangeRate-API: {}/pair/{}/{}", 
                    properties.getBaseUrl(), from.getCode(), to.getCode());

            ExchangeRateResponse response = webClient.get()
                    .uri(uri)
                    .retrieve()
                    .bodyToMono(ExchangeRateResponse.class)
                    .timeout(Duration.ofMillis(properties.getTimeoutMs()))
                    .block();

            if (response != null && response.isSuccess() && response.getConversionRate() != null) {
                log.info("Retrieved live exchange rate {} -> {}: {}", from, to, response.getConversionRate());
                return response.getConversionRate();
            } else if (response != null && response.getErrorType() != null) {
                log.warn("ExchangeRate-API pair error: {}", response.getErrorType());
            }
        } catch (Exception ex) {
            log.error("Failed to fetch pair rate for {} -> {} from ExchangeRate-API: {}", 
                    from, to, ex.getMessage());
        }
        return null;
    }

    /**
     * Calls ExchangeRate-API latest endpoint: /{key}/latest/{base}
     */
    private Map<CurrencyCode, BigDecimal> fetchLatestFromApi(CurrencyCode base) {
        try {
            String uri = "/" + properties.getApiKey() + "/latest/" + base.getCode();
            log.debug("Fetching latest rates from ExchangeRate-API: {}/latest/{}", 
                    properties.getBaseUrl(), base.getCode());

            ExchangeRateResponse response = webClient.get()
                    .uri(uri)
                    .retrieve()
                    .bodyToMono(ExchangeRateResponse.class)
                    .timeout(Duration.ofMillis(properties.getTimeoutMs()))
                    .block();

            if (response != null && response.isSuccess()) {
                Map<String, BigDecimal> apiRates = response.getEffectiveRates();
                if (apiRates != null && !apiRates.isEmpty()) {
                    Map<CurrencyCode, BigDecimal> rates = new EnumMap<>(CurrencyCode.class);
                    rates.put(base, BigDecimal.ONE);

                    for (CurrencyCode c : CurrencyCode.values()) {
                        BigDecimal val = apiRates.get(c.getCode());
                        if (val != null) {
                            rates.put(c, val);
                        }
                    }
                    log.info("Retrieved live exchange rates for base currency {}: {} supported currencies", 
                            base, rates.size());
                    return rates;
                }
            } else if (response != null && response.getErrorType() != null) {
                log.warn("ExchangeRate-API latest error: {}", response.getErrorType());
            }
        } catch (Exception ex) {
            log.error("Failed to fetch exchange rates for {} from ExchangeRate-API: {}", 
                    base, ex.getMessage());
        }
        return null;
    }

    private Map<CurrencyCode, BigDecimal> computeFallbackRates(CurrencyCode base) {
        Map<CurrencyCode, BigDecimal> result = new EnumMap<>(CurrencyCode.class);
        BigDecimal baseInEur = DEFAULT_EUR_RATES.get(base);

        if (baseInEur == null || baseInEur.compareTo(BigDecimal.ZERO) == 0) {
            baseInEur = BigDecimal.ONE;
        }

        for (CurrencyCode target : CurrencyCode.values()) {
            if (target.equals(base)) {
                result.put(target, BigDecimal.ONE);
            } else {
                BigDecimal targetInEur = DEFAULT_EUR_RATES.get(target);
                if (targetInEur != null) {
                    BigDecimal rate = targetInEur.divide(baseInEur, ExchangeRate.RATE_SCALE, RoundingMode.HALF_UP);
                    result.put(target, rate);
                }
            }
        }
        return result;
    }

    private boolean hasApiKey() {
        return properties.getApiKey() != null && !properties.getApiKey().trim().isEmpty();
    }
}
