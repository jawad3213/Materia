package com.materia.backend.common.infrastructure.adapters.out.fx;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataExchangeRateRepository extends JpaRepository<ExchangeRateJpaEntity, UUID> {

    Optional<ExchangeRateJpaEntity> findFirstByFromCurrencyAndToCurrencyAndRateDateLessThanEqualOrderByRateDateDesc(
            String fromCurrency, String toCurrency, LocalDate date);

    Optional<ExchangeRateJpaEntity> findFirstByFromCurrencyAndToCurrencyAndRateDateGreaterThanOrderByRateDateAsc(
            String fromCurrency, String toCurrency, LocalDate date);

    Optional<ExchangeRateJpaEntity> findByRateDateAndFromCurrencyAndToCurrency(
            LocalDate rateDate, String fromCurrency, String toCurrency);
}
