package com.materia.backend.common.domain.fx;

import com.materia.backend.common.domain.enums.CurrencyCode;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

/**
 * 🔹 REPORTING CURRENCY (INPUT PORT)
 *
 * Every amount shown across documents is expressed in one configured reporting currency. A committed document keeps
 * the rate of its own date; anything not yet committed is converted at the latest stored rate.
 */
public interface ReportingFx {

    CurrencyCode reportingCurrency();

    /** Rate of {@code date} (or the closest earlier stored day) from {@code from} into the reporting currency. */
    FxValuation valuationOn(CurrencyCode from, LocalDate date);

    /** Latest stored rate of every other currency into the reporting currency; a currency with no rate is absent. */
    Map<CurrencyCode, FxValuation> currentRates();

    /** Stores today's rates of every other currency into the reporting currency. */
    void refreshDailyRates();

    /** Copies the rate of the document's date onto a committed document; does nothing if it is not committed. */
    void valueDocument(FxDocument kind, UUID id);

    /** Values committed documents that have no copy (or one made for another reporting currency); returns how many. */
    int backfill();
}
