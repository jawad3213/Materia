package com.materia.backend.contexts.payment.domain.ports.in;

import com.materia.backend.common.application.metrics.MonthAmount;
import com.materia.backend.common.application.metrics.RecentDocument;
import com.materia.backend.common.application.metrics.ReportingRates;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/** Reporting figures of the payment context, computed on its own data (status names as in PaymentStatus). */
public interface PaymentMetricsUseCase {

    /** How many documents use each currency. */
    Map<String, Long> documentsByCurrency();

    /** Documents in another currency valued at the rate of their own date, by currency. */
    Map<String, Long> valuedAtOwnDate(String reportingCurrency);

    long countInStatus(String status);

    /** Totals per month of confirmation, between {@code from} and {@code to} inclusive, in the reporting currency. */
    List<MonthAmount> byMonth(String status, ReportingRates rates, LocalDate from, LocalDate to);

    /** The latest documents, newest first. */
    List<RecentDocument> latest(int limit);
}
