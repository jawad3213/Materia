package com.materia.backend.common.infrastructure.metrics;

import com.materia.backend.common.application.metrics.MonthAmount;
import com.materia.backend.common.application.metrics.ReportingRates;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * JPQL helpers for a context's own reporting queries. They name no entity: each context passes its own entity and
 * aliases, so every context keeps computing its figures on its own data once it is a separate service.
 */
public final class ReportingJpql {

    private ReportingJpql() {
    }

    /**
     * {@code amount} in the reporting currency: times the document's own rate copy when it was made for this reporting
     * currency, otherwise times the latest rate of the document's currency. Without any rate the product is null and
     * SUM leaves the document out. Currency codes come from CurrencyCode and rates are numbers, never user input.
     */
    public static String converted(String alias, String amount, ReportingRates rates) {
        String reporting = upper(rates.reportingCurrency());
        StringBuilder latest = new StringBuilder("case upper(").append(alias).append(".currencyCode)");
        latest.append(" when '").append(reporting).append("' then 1bd");
        rates.current().forEach((currency, rate) -> {
            if (rate != null && !reporting.equalsIgnoreCase(currency) && currency.matches("[A-Za-z]{3}")) {
                latest.append(" when '").append(currency.toUpperCase()).append("' then ")
                        .append(rate.toPlainString()).append("bd");
            }
        });
        latest.append(" else null end");
        return "((" + amount + ") * (case when " + alias + ".fxReportingCurrency = '" + reporting + "' and "
                + alias + ".fxRateToReporting is not null then " + alias + ".fxRateToReporting else " + latest + " end))";
    }

    /** How many documents of {@code entity} use each currency. */
    public static Map<String, Long> countByCurrency(EntityManager em, String entity) {
        Map<String, Long> counts = new LinkedHashMap<>();
        em.createQuery("select upper(d.currencyCode), count(d) from " + entity + " d "
                        + "where d.currencyCode is not null group by upper(d.currencyCode)", Object[].class)
                .getResultList()
                .forEach(r -> counts.merge((String) r[0], (Long) r[1], Long::sum));
        return counts;
    }

    /** Documents of {@code entity} in another currency that carry a rate copy for this reporting currency, by currency. */
    public static Map<String, Long> countValuedAtOwnDate(EntityManager em, String entity, String reportingCurrency) {
        Map<String, Long> counts = new LinkedHashMap<>();
        em.createQuery("select upper(d.currencyCode), count(d) from " + entity + " d where d.currencyCode is not null "
                        + "and upper(d.currencyCode) <> :reporting and d.fxReportingCurrency = :reporting "
                        + "and d.fxRateToReporting is not null group by upper(d.currencyCode)", Object[].class)
                .setParameter("reporting", upper(reportingCurrency))
                .getResultList()
                .forEach(r -> counts.merge((String) r[0], (Long) r[1], Long::sum));
        return counts;
    }

    /** Rows of (year, month, amount). */
    public static List<MonthAmount> months(TypedQuery<Object[]> query) {
        return query.getResultList().stream()
                .map(r -> new MonthAmount(YearMonth.of(((Number) r[0]).intValue(), ((Number) r[1]).intValue()), amount(r[2])))
                .toList();
    }

    public static BigDecimal amount(Object value) {
        if (value == null) return BigDecimal.ZERO;
        return value instanceof BigDecimal decimal ? decimal : new BigDecimal(value.toString());
    }

    public static long number(Object value) {
        return value != null ? ((Number) value).longValue() : 0L;
    }

    public static String upper(String currency) {
        return currency != null ? currency.toUpperCase() : null;
    }

    /** The enum constants named, for a query parameter; an unknown name is a programming error and fails fast. */
    public static <E extends Enum<E>> Set<E> enums(Class<E> type, Set<String> names) {
        Set<E> values = EnumSet.noneOf(type);
        names.forEach(name -> values.add(Enum.valueOf(type, name)));
        return values;
    }
}
