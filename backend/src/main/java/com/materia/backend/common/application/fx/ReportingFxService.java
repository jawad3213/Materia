package com.materia.backend.common.application.fx;

import com.materia.backend.common.domain.enums.CurrencyCode;
import com.materia.backend.common.domain.exceptions.ExchangeRateException;
import com.materia.backend.common.domain.fx.DocumentFxStore;
import com.materia.backend.common.domain.fx.DocumentFxStore.DocumentAmount;
import com.materia.backend.common.domain.fx.ExchangeRateFeed;
import com.materia.backend.common.domain.fx.ExchangeRateHistory;
import com.materia.backend.common.domain.fx.ExchangeRateHistory.StoredRate;
import com.materia.backend.common.domain.fx.FxDocument;
import com.materia.backend.common.domain.fx.FxValuation;
import com.materia.backend.common.domain.fx.ReportingFx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * 🔹 REPORTING CURRENCY SERVICE
 *
 * <ul>
 *   <li>Rates come from the stored daily history; the exchange-rate service is only asked when a day is missing
 *       (and by the daily job), never on every dashboard load.</li>
 *   <li>A document is valued at the rate of its own date, or the closest earlier stored day. When no earlier day
 *       exists (documents older than the history), the earliest stored rate is used and its date is kept, so the
 *       copy always says which day's rate it holds.</li>
 * </ul>
 */
@Service
@Transactional(readOnly = true)
public class ReportingFxService implements ReportingFx {

    private static final Logger log = LoggerFactory.getLogger(ReportingFxService.class);
    static final int BACKFILL_BATCH = 500;

    private final CurrencyCode reporting;
    private final ExchangeRateHistory history;
    private final ExchangeRateFeed feed;
    private final DocumentFxStore documents;
    private final Clock clock;

    @Autowired
    public ReportingFxService(@Value("${app.reporting-currency:MAD}") String reportingCurrency,
                              ExchangeRateHistory history,
                              ExchangeRateFeed feed,
                              DocumentFxStore documents) {
        this(CurrencyCode.fromCode(reportingCurrency), history, feed, documents, Clock.systemDefaultZone());
    }

    public ReportingFxService(CurrencyCode reporting, ExchangeRateHistory history, ExchangeRateFeed feed,
                              DocumentFxStore documents, Clock clock) {
        this.reporting = reporting;
        this.history = history;
        this.feed = feed;
        this.documents = documents;
        this.clock = clock;
    }

    @Override
    public CurrencyCode reportingCurrency() {
        return reporting;
    }

    @Override
    @Transactional
    public FxValuation valuationOn(CurrencyCode from, LocalDate date) {
        LocalDate day = date != null ? date : today();
        if (from == reporting) {
            return FxValuation.identity(reporting, day);
        }
        Optional<StoredRate> stored = history.latestOnOrBefore(from, reporting, day);
        if (stored.isEmpty()) {
            refreshDailyRates();
            stored = history.latestOnOrBefore(from, reporting, day).or(() -> history.earliestAfter(from, reporting, day));
        }
        return stored.map(this::toValuation)
                .orElseThrow(() -> new ExchangeRateException(from, reporting, "No exchange rate available"));
    }

    @Override
    // Its own transaction: the dashboard reads in a read-only one, and a missing day may have to be stored.
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    public Map<CurrencyCode, FxValuation> currentRates() {
        Map<CurrencyCode, FxValuation> rates = new EnumMap<>(CurrencyCode.class);
        boolean refreshed = false;
        for (CurrencyCode currency : CurrencyCode.values()) {
            if (currency == reporting) continue;
            Optional<StoredRate> stored = history.latestOnOrBefore(currency, reporting, today());
            if (stored.isEmpty() && !refreshed) {
                refreshDailyRates();
                refreshed = true;
                stored = history.latestOnOrBefore(currency, reporting, today());
            }
            stored.ifPresent(rate -> rates.put(currency, toValuation(rate)));
        }
        return rates;
    }

    @Override
    @Transactional
    public void refreshDailyRates() {
        LocalDate today = today();
        for (CurrencyCode currency : CurrencyCode.values()) {
            if (currency == reporting) continue;
            try {
                ExchangeRateFeed.RateQuote quote = feed.latest(currency);
                BigDecimal rate = quote.rates() != null ? quote.rates().get(reporting) : null;
                if (rate == null || rate.signum() <= 0) {
                    log.warn("[FX] No {} -> {} rate in the feed", currency, reporting);
                    continue;
                }
                history.save(new StoredRate(today, currency, reporting,
                        rate.setScale(FxValuation.RATE_SCALE, RoundingMode.HALF_UP), quote.source()));
            } catch (RuntimeException e) {
                log.warn("[FX] Could not refresh the {} -> {} rate: {}", currency, reporting, e.getMessage());
            }
        }
    }

    @Override
    @Transactional
    public void valueDocument(FxDocument kind, UUID id) {
        if (kind == null || id == null) return;
        documents.find(kind, id).ifPresent(document -> value(kind, document));
    }

    @Override
    @Transactional
    public int backfill() {
        int valued = 0;
        for (FxDocument kind : FxDocument.values()) {
            for (DocumentAmount document : documents.unvalued(kind, reporting.getCode(), BACKFILL_BATCH)) {
                if (value(kind, document)) valued++;
            }
        }
        if (valued > 0) {
            log.info("[FX] Valued {} committed document(s) in {}", valued, reporting);
        }
        return valued;
    }

    /** Copies the rate onto a committed document not yet valued in the reporting currency; false when skipped. */
    private boolean value(FxDocument kind, DocumentAmount document) {
        if (!kind.isCommitted(document.status()) || document.currency() == null) return false;
        if (reporting.getCode().equalsIgnoreCase(document.valuedIn())) return false;
        try {
            FxValuation valuation = valuationOn(CurrencyCode.fromCode(document.currency()), document.date());
            documents.saveValuation(kind, document.id(), valuation, valuation.convert(document.amount()));
            return true;
        } catch (RuntimeException e) {
            // A missing rate must never block confirming, verifying or paying; the daily job values it later.
            log.warn("[FX] {} {} not valued yet: {}", kind, document.id(), e.getMessage());
            return false;
        }
    }

    private FxValuation toValuation(StoredRate rate) {
        return new FxValuation(rate.from(), reporting, rate.rate(), rate.rateDate(), rate.source());
    }

    private LocalDate today() {
        return LocalDate.now(clock);
    }
}
