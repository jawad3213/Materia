package com.materia.backend.contexts.invoice.infrastructure.adapters.in.metrics;

import com.materia.backend.common.application.metrics.MonthAmount;
import com.materia.backend.common.application.metrics.RecentDocument;
import com.materia.backend.common.application.metrics.ReportingRates;
import com.materia.backend.contexts.invoice.application.dtos.OutstandingByDueDate;
import com.materia.backend.contexts.invoice.domain.enums.InvoiceStatus;
import com.materia.backend.contexts.invoice.domain.enums.InvoiceType;
import com.materia.backend.contexts.invoice.domain.ports.in.InvoiceMetricsUseCase;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static com.materia.backend.common.infrastructure.metrics.ReportingJpql.*;

/** This context's reporting figures, computed by the database on this context's own tables. */
@Component
@Transactional(readOnly = true)
@SuppressWarnings("unused")
public class JpaInvoiceMetrics implements InvoiceMetricsUseCase {

    @PersistenceContext
    private EntityManager em;

    /** What remains to be paid on an invoice. */
    private static final String OUTSTANDING = "(i.totalAmountWithTax - coalesce(i.paidAmount, 0))";

    @Override
    public Map<String, Long> documentsByCurrency() {
        return countByCurrency(em, "InvoiceJpaEntity");
    }

    @Override
    public Map<String, Long> valuedAtOwnDate(String reportingCurrency) {
        return countValuedAtOwnDate(em, "InvoiceJpaEntity", reportingCurrency);
    }

    @Override
    public List<OutstandingByDueDate> outstandingByDueDate(String typeName, Set<String> statusNames, ReportingRates rates) {
        InvoiceType type = InvoiceType.valueOf(typeName);
        Set<InvoiceStatus> statuses = enums(InvoiceStatus.class, statusNames);
        if (statuses.isEmpty()) return List.of();
        String reporting = upper(rates.reportingCurrency());
        return em.createQuery("select i.dueDate, count(i), coalesce(sum(" + converted("i", OUTSTANDING, rates) + "), 0) "
                        + "from InvoiceJpaEntity i where i.invoiceType = :type and i.status in :statuses "
                        + "and " + OUTSTANDING + " > 0 group by i.dueDate", Object[].class)
                .setParameter("type", type).setParameter("statuses", statuses)
                .getResultList().stream()
                .map(r -> new OutstandingByDueDate((LocalDate) r[0], reporting, (Long) r[1], amount(r[2])))
                .toList();
    }

    @Override
    public List<MonthAmount> invoicedByMonth(Set<String> excludedNames, ReportingRates rates, LocalDate from, LocalDate to) {
        Set<InvoiceStatus> excluded = enums(InvoiceStatus.class, excludedNames);
        String signedTotal = "(case when i.invoiceType = :credit then 0 - i.totalAmountWithTax else i.totalAmountWithTax end)";
        return months(em.createQuery("select extract(year from i.invoiceDate), extract(month from i.invoiceDate), "
                        + "coalesce(sum(" + converted("i", signedTotal, rates) + "), 0) from InvoiceJpaEntity i "
                        + "where i.status not in :excluded and i.invoiceDate between :from and :to "
                        + "group by extract(year from i.invoiceDate), extract(month from i.invoiceDate)", Object[].class)
                .setParameter("credit", InvoiceType.CREDIT_NOTE).setParameter("excluded", excluded)
                .setParameter("from", from).setParameter("to", to));
    }

    @Override
    public List<RecentDocument> latest(int limit) {
        return em.createQuery("select d.id, d.invoiceCode, d.supplierName, d.status, d.createdAt from InvoiceJpaEntity d "
                        + "where d.createdAt is not null order by d.createdAt desc", Object[].class)
                .setMaxResults(limit)
                .getResultList().stream()
                .map(r -> new RecentDocument((UUID) r[0], (String) r[1], (String) r[2],
                        r[3] != null ? r[3].toString() : null, (LocalDateTime) r[4]))
                .toList();
    }
}
