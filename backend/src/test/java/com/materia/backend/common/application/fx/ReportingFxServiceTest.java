package com.materia.backend.common.application.fx;

import com.materia.backend.common.domain.enums.CurrencyCode;
import com.materia.backend.common.domain.exceptions.ExchangeRateException;
import com.materia.backend.common.domain.fx.DocumentFxStore;
import com.materia.backend.common.domain.fx.DocumentFxStore.DocumentAmount;
import com.materia.backend.common.domain.fx.ExchangeRateFeed;
import com.materia.backend.common.domain.fx.ExchangeRateFeed.RateQuote;
import com.materia.backend.common.domain.fx.ExchangeRateHistory;
import com.materia.backend.common.domain.fx.ExchangeRateHistory.StoredRate;
import com.materia.backend.common.domain.fx.FxDocument;
import com.materia.backend.common.domain.fx.FxValuation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/** Rates for the reporting currency: stored history first, the feed only when a day is missing, and the copy on documents. */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ReportingFxServiceTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 10, 6);

    @Mock private ExchangeRateHistory history;
    @Mock private ExchangeRateFeed feed;
    @Mock private DocumentFxStore documents;
    private ReportingFxService service;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(TODAY.atTime(9, 0).atZone(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());
        service = new ReportingFxService(CurrencyCode.MAD, history, feed, documents, clock);
    }

    private static StoredRate eur(LocalDate day, String rate, String source) {
        return new StoredRate(day, CurrencyCode.EUR, CurrencyCode.MAD, new BigDecimal(rate), source);
    }

    @Test
    @DisplayName("rule: the reporting currency converts at 1 without any lookup")
    void sameCurrency_identity() {
        FxValuation valuation = service.valuationOn(CurrencyCode.MAD, TODAY.minusDays(3));

        assertThat(valuation.rate()).isEqualByComparingTo("1");
        assertThat(valuation.source()).isEqualTo(FxValuation.SAME);
        verifyNoInteractions(history, feed);
    }

    @Test
    @DisplayName("rule: a document date uses the stored rate of that day or the closest earlier one, without calling the feed")
    void storedRate_closestEarlierDay() {
        LocalDate orderDate = TODAY.minusDays(10);
        when(history.latestOnOrBefore(CurrencyCode.EUR, CurrencyCode.MAD, orderDate))
                .thenReturn(Optional.of(eur(orderDate.minusDays(2), "10.91", FxValuation.LIVE)));

        FxValuation valuation = service.valuationOn(CurrencyCode.EUR, orderDate);

        assertThat(valuation.rate()).isEqualByComparingTo("10.91");
        assertThat(valuation.rateDate()).isEqualTo(orderDate.minusDays(2));
        assertThat(valuation.reporting()).isEqualTo(CurrencyCode.MAD);
        verifyNoInteractions(feed);
    }

    @Test
    @DisplayName("rule: with no earlier stored day, today's rate is fetched and the earliest stored rate is used, with its own date")
    void noHistory_fetchesAndUsesEarliest() {
        LocalDate oldInvoice = LocalDate.of(2025, 1, 15);
        when(history.latestOnOrBefore(eq(CurrencyCode.EUR), eq(CurrencyCode.MAD), any())).thenReturn(Optional.empty());
        when(history.earliestAfter(CurrencyCode.EUR, CurrencyCode.MAD, oldInvoice))
                .thenReturn(Optional.of(eur(TODAY, "10.80", FxValuation.LIVE)));
        when(feed.latest(any())).thenReturn(new RateQuote(Map.of(CurrencyCode.MAD, new BigDecimal("10.80")), FxValuation.LIVE));

        FxValuation valuation = service.valuationOn(CurrencyCode.EUR, oldInvoice);

        assertThat(valuation.rateDate()).isEqualTo(TODAY);
        verify(history).save(eur(TODAY, "10.80000000", FxValuation.LIVE));
    }

    @Test
    @DisplayName("rule: without any rate a valuation fails clearly")
    void noRateAtAll_fails() {
        when(history.latestOnOrBefore(any(), any(), any())).thenReturn(Optional.empty());
        when(history.earliestAfter(any(), any(), any())).thenReturn(Optional.empty());
        when(feed.latest(any())).thenReturn(new RateQuote(Map.of(), FxValuation.FALLBACK));

        assertThatThrownBy(() -> service.valuationOn(CurrencyCode.USD, TODAY)).isInstanceOf(ExchangeRateException.class);
    }

    @Test
    @DisplayName("refresh: today's rate of every other currency is stored with its source; a failing feed is skipped")
    void refresh_storesEveryCurrency() {
        when(feed.latest(CurrencyCode.EUR)).thenReturn(new RateQuote(Map.of(CurrencyCode.MAD, new BigDecimal("10.85")), FxValuation.FALLBACK));
        when(feed.latest(CurrencyCode.USD)).thenThrow(new IllegalStateException("offline"));

        service.refreshDailyRates();

        verify(history).save(eur(TODAY, "10.85000000", FxValuation.FALLBACK));
        verify(history, times(1)).save(any());
        verify(feed, never()).latest(CurrencyCode.MAD);
    }

    @Test
    @DisplayName("current rates: the latest stored rate per currency; the feed is asked once when one is missing")
    void currentRates_latestStored() {
        when(history.latestOnOrBefore(CurrencyCode.EUR, CurrencyCode.MAD, TODAY))
                .thenReturn(Optional.of(eur(TODAY.minusDays(1), "10.82", FxValuation.LIVE)));
        when(history.latestOnOrBefore(CurrencyCode.USD, CurrencyCode.MAD, TODAY)).thenReturn(Optional.empty());
        when(feed.latest(any())).thenReturn(new RateQuote(Map.of(), FxValuation.FALLBACK));

        Map<CurrencyCode, FxValuation> rates = service.currentRates();

        assertThat(rates).containsOnlyKeys(CurrencyCode.EUR);
        assertThat(rates.get(CurrencyCode.EUR).rate()).isEqualByComparingTo("10.82");
    }

    @Test
    @DisplayName("documents: a committed EUR document gets the rate of its date and the amount in MAD")
    void valueDocument_committed() {
        UUID id = UUID.randomUUID();
        LocalDate orderDate = TODAY.minusDays(4);
        when(documents.find(FxDocument.PURCHASE_ORDER, id)).thenReturn(Optional.of(
                new DocumentAmount(id, "EUR", new BigDecimal("80.00"), orderDate, "CONFIRMED", null)));
        when(history.latestOnOrBefore(CurrencyCode.EUR, CurrencyCode.MAD, orderDate))
                .thenReturn(Optional.of(eur(orderDate, "11.00", FxValuation.LIVE)));

        service.valueDocument(FxDocument.PURCHASE_ORDER, id);

        ArgumentCaptor<FxValuation> valuation = ArgumentCaptor.forClass(FxValuation.class);
        verify(documents).saveValuation(eq(FxDocument.PURCHASE_ORDER), eq(id), valuation.capture(), eq(new BigDecimal("880.00")));
        assertThat(valuation.getValue().rateDate()).isEqualTo(orderDate);
    }

    @Test
    @DisplayName("documents: drafts, documents already valued in MAD, and documents with no rate are left unchanged")
    void valueDocument_skips() {
        UUID draft = UUID.randomUUID();
        UUID valued = UUID.randomUUID();
        UUID noRate = UUID.randomUUID();
        when(documents.find(FxDocument.INVOICE, draft)).thenReturn(Optional.of(
                new DocumentAmount(draft, "EUR", BigDecimal.TEN, TODAY, "DRAFT", null)));
        when(documents.find(FxDocument.INVOICE, valued)).thenReturn(Optional.of(
                new DocumentAmount(valued, "EUR", BigDecimal.TEN, TODAY, "VERIFIED", "MAD")));
        when(documents.find(FxDocument.INVOICE, noRate)).thenReturn(Optional.of(
                new DocumentAmount(noRate, "USD", BigDecimal.TEN, TODAY, "VERIFIED", null)));
        when(history.latestOnOrBefore(any(), any(), any())).thenReturn(Optional.empty());
        when(history.earliestAfter(any(), any(), any())).thenReturn(Optional.empty());
        when(feed.latest(any())).thenReturn(new RateQuote(Map.of(), FxValuation.FALLBACK));

        service.valueDocument(FxDocument.INVOICE, draft);
        service.valueDocument(FxDocument.INVOICE, valued);
        service.valueDocument(FxDocument.INVOICE, noRate); // must not throw: verifying an invoice never fails on a rate

        verify(documents, never()).saveValuation(any(), any(), any(), any());
    }

    @Test
    @DisplayName("backfill: every committed document without a copy for the reporting currency is valued; MAD ones at 1")
    void backfill_valuesMissingCopies() {
        UUID order = UUID.randomUUID();
        UUID payment = UUID.randomUUID();
        when(documents.unvalued(any(), eq("MAD"), anyInt())).thenReturn(List.of());
        when(documents.unvalued(eq(FxDocument.PURCHASE_ORDER), eq("MAD"), anyInt())).thenReturn(List.of(
                new DocumentAmount(order, "EUR", new BigDecimal("100.00"), TODAY, "COMPLETED", "USD")));
        when(documents.unvalued(eq(FxDocument.PAYMENT), eq("MAD"), anyInt())).thenReturn(List.of(
                new DocumentAmount(payment, "MAD", new BigDecimal("250.00"), TODAY, "COMPLETED", null)));
        when(history.latestOnOrBefore(CurrencyCode.EUR, CurrencyCode.MAD, TODAY))
                .thenReturn(Optional.of(eur(TODAY, "10.80", FxValuation.LIVE)));

        assertThat(service.backfill()).isEqualTo(2);

        verify(documents).saveValuation(eq(FxDocument.PURCHASE_ORDER), eq(order), any(), eq(new BigDecimal("1080.00")));
        verify(documents).saveValuation(eq(FxDocument.PAYMENT), eq(payment), any(), eq(new BigDecimal("250.00")));
    }
}
