package com.materia.backend.contexts.purchaseOrder.domain.ports.in;

import com.materia.backend.common.application.metrics.CountedAmount;
import com.materia.backend.common.application.metrics.MonthAmount;
import com.materia.backend.common.application.metrics.RecentDocument;
import com.materia.backend.common.application.metrics.ReportingRates;
import com.materia.backend.contexts.purchaseOrder.application.dtos.MaterialSpend;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Reporting figures of the purchase-order context, computed on its own data (status names as in OrderStatus). */
public interface PurchaseOrderMetricsUseCase {

    /** How many documents use each currency. */
    Map<String, Long> documentsByCurrency();

    /** Documents in another currency valued at the rate of their own date, by currency. */
    Map<String, Long> valuedAtOwnDate(String reportingCurrency);

    long count(Set<String> statuses);

    long countExpectedBefore(Set<String> statuses, LocalDate date);

    BigDecimal sum(Set<String> statuses, ReportingRates rates);

    /** Grand totals per month of order date, between {@code from} and {@code to} inclusive. */
    List<MonthAmount> byMonth(Set<String> statuses, ReportingRates rates, LocalDate from, LocalDate to);

    /** Orders per status name, in lifecycle order; statuses with no order are left out. */
    Map<String, Long> countByStatus();

    /** Order-line value per material (the master data context knows each material's category). */
    List<MaterialSpend> spendByMaterial(Set<String> statuses, ReportingRates rates);

    /** Grand totals per supplier; key = supplier id, count = orders. */
    List<CountedAmount> spendBySupplier(Set<String> statuses, ReportingRates rates);

    /** The latest documents, newest first. */
    List<RecentDocument> latest(int limit);
}
