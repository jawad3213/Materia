package com.materia.backend.contexts.payment.infrastructure.adapters.in.metrics;

import com.materia.backend.common.application.metrics.MonthAmount;
import com.materia.backend.common.application.metrics.RecentDocument;
import com.materia.backend.common.application.metrics.ReportingRates;
import com.materia.backend.contexts.payment.domain.enums.PaymentStatus;
import com.materia.backend.contexts.payment.domain.ports.in.PaymentMetricsUseCase;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.materia.backend.common.infrastructure.metrics.ReportingJpql.*;

/** This context's reporting figures, computed by the database on this context's own tables. */
@Component
@Transactional(readOnly = true)
@SuppressWarnings("unused")
public class JpaPaymentMetrics implements PaymentMetricsUseCase {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Map<String, Long> documentsByCurrency() {
        return countByCurrency(em, "PaymentJpaEntity");
    }

    @Override
    public Map<String, Long> valuedAtOwnDate(String reportingCurrency) {
        return countValuedAtOwnDate(em, "PaymentJpaEntity", reportingCurrency);
    }

    @Override
    public long countInStatus(String status) {
        return em.createQuery("select count(p) from PaymentJpaEntity p where p.status = :status", Long.class)
                .setParameter("status", PaymentStatus.valueOf(status)).getSingleResult();
    }

    @Override
    public List<MonthAmount> byMonth(String statusName, ReportingRates rates, LocalDate from, LocalDate to) {
        PaymentStatus status = PaymentStatus.valueOf(statusName);
        return months(em.createQuery("select extract(year from p.confirmedDate), extract(month from p.confirmedDate), "
                        + "coalesce(sum(" + converted("p", "p.totalAmount", rates) + "), 0) from PaymentJpaEntity p "
                        + "where p.status = :status and p.confirmedDate >= :from and p.confirmedDate < :to "
                        + "group by extract(year from p.confirmedDate), extract(month from p.confirmedDate)", Object[].class)
                .setParameter("status", status)
                .setParameter("from", from.atStartOfDay()).setParameter("to", to.plusDays(1).atStartOfDay()));
    }

    @Override
    public List<RecentDocument> latest(int limit) {
        return em.createQuery("select d.id, d.paymentCode, d.supplierName, d.status, d.createdAt from PaymentJpaEntity d "
                        + "where d.createdAt is not null order by d.createdAt desc", Object[].class)
                .setMaxResults(limit)
                .getResultList().stream()
                .map(r -> new RecentDocument((UUID) r[0], (String) r[1], (String) r[2],
                        r[3] != null ? r[3].toString() : null, (LocalDateTime) r[4]))
                .toList();
    }
}
