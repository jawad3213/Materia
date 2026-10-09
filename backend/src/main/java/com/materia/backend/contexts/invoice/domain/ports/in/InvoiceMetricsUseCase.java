package com.materia.backend.contexts.invoice.domain.ports.in;

import com.materia.backend.common.application.metrics.MonthAmount;
import com.materia.backend.common.application.metrics.RecentDocument;
import com.materia.backend.common.application.metrics.ReportingRates;
import com.materia.backend.contexts.invoice.application.dtos.OutstandingByDueDate;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Reporting figures of the invoice context, computed on its own data (names as in InvoiceStatus / InvoiceType). */
public interface InvoiceMetricsUseCase {

    /** How many documents use each currency. */
    Map<String, Long> documentsByCurrency();

    /** Documents in another currency valued at the rate of their own date, by currency. */
    Map<String, Long> valuedAtOwnDate(String reportingCurrency);

    /** Invoices of the type and statuses that still have something to pay, by due date, in the reporting currency. */
    List<OutstandingByDueDate> outstandingByDueDate(String type, Set<String> statuses, ReportingRates rates);

    /** Totals with tax per month of invoice date, credit notes subtracted, statuses in {@code excluded} left out. */
    List<MonthAmount> invoicedByMonth(Set<String> excluded, ReportingRates rates, LocalDate from, LocalDate to);

    /** The latest documents, newest first. */
    List<RecentDocument> latest(int limit);
}
