package com.materia.backend.common.infrastructure.adapters.out.fx;

import com.materia.backend.common.domain.enums.CurrencyCode;
import com.materia.backend.common.domain.fx.ExchangeRateHistory;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Optional;

/** Stored daily rates in the exchange_rates table. */
@Component
public class ExchangeRateHistoryAdapter implements ExchangeRateHistory {

    private final SpringDataExchangeRateRepository repository;

    public ExchangeRateHistoryAdapter(SpringDataExchangeRateRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<StoredRate> latestOnOrBefore(CurrencyCode from, CurrencyCode to, LocalDate date) {
        return repository.findFirstByFromCurrencyAndToCurrencyAndRateDateLessThanEqualOrderByRateDateDesc(
                from.getCode(), to.getCode(), date).map(ExchangeRateHistoryAdapter::toStored);
    }

    @Override
    public Optional<StoredRate> earliestAfter(CurrencyCode from, CurrencyCode to, LocalDate date) {
        return repository.findFirstByFromCurrencyAndToCurrencyAndRateDateGreaterThanOrderByRateDateAsc(
                from.getCode(), to.getCode(), date).map(ExchangeRateHistoryAdapter::toStored);
    }

    @Override
    public void save(StoredRate rate) {
        String from = rate.from().getCode();
        String to = rate.to().getCode();
        ExchangeRateJpaEntity entity = repository.findByRateDateAndFromCurrencyAndToCurrency(rate.rateDate(), from, to)
                .map(existing -> {
                    existing.replace(rate.rate(), rate.source());
                    return existing;
                })
                .orElseGet(() -> new ExchangeRateJpaEntity(rate.rateDate(), from, to, rate.rate(), rate.source()));
        repository.save(entity);
    }

    private static StoredRate toStored(ExchangeRateJpaEntity entity) {
        return new StoredRate(entity.getRateDate(), CurrencyCode.fromCode(entity.getFromCurrency()),
                CurrencyCode.fromCode(entity.getToCurrency()), entity.getRate(), entity.getSource());
    }
}
