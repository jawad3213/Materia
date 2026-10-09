package com.materia.backend.common.infrastructure.adapters.out.fx;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/** One stored exchange rate: a day and a currency pair. */
@Entity
@Table(name = "exchange_rates", uniqueConstraints = @UniqueConstraint(name = "uk_exchange_rates_day_pair",
        columnNames = {"rate_date", "from_currency", "to_currency"}))
public class ExchangeRateJpaEntity {

    @Id
    private UUID id;

    @Column(name = "rate_date", nullable = false)
    private LocalDate rateDate;

    @Column(name = "from_currency", nullable = false, length = 3)
    private String fromCurrency;

    @Column(name = "to_currency", nullable = false, length = 3)
    private String toCurrency;

    @Column(name = "rate", nullable = false, precision = 19, scale = 8)
    private BigDecimal rate;

    @Column(name = "source", nullable = false, length = 20)
    private String source;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected ExchangeRateJpaEntity() {
    }

    public ExchangeRateJpaEntity(LocalDate rateDate, String fromCurrency, String toCurrency, BigDecimal rate, String source) {
        this.id = UUID.randomUUID();
        this.rateDate = rateDate;
        this.fromCurrency = fromCurrency;
        this.toCurrency = toCurrency;
        this.rate = rate;
        this.source = source;
        this.createdAt = LocalDateTime.now();
    }

    public UUID getId() { return id; }
    public LocalDate getRateDate() { return rateDate; }
    public String getFromCurrency() { return fromCurrency; }
    public String getToCurrency() { return toCurrency; }
    public BigDecimal getRate() { return rate; }
    public String getSource() { return source; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void replace(BigDecimal rate, String source) {
        this.rate = rate;
        this.source = source;
    }
}
