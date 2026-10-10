package com.materia.backend.contexts.purchaseOrder.infrastructure.adapters.in.metrics;

import com.materia.backend.common.application.metrics.CountedAmount;
import com.materia.backend.common.application.metrics.MonthAmount;
import com.materia.backend.common.application.metrics.RecentDocument;
import com.materia.backend.common.application.metrics.ReportingRates;
import com.materia.backend.contexts.purchaseOrder.application.dtos.MaterialSpend;
import com.materia.backend.contexts.purchaseOrder.domain.enums.OrderStatus;
import com.materia.backend.contexts.purchaseOrder.domain.ports.in.PurchaseOrderMetricsUseCase;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static com.materia.backend.common.infrastructure.metrics.ReportingJpql.*;

/** This context's reporting figures, computed by the database on this context's own tables. */
@Component
@Transactional(readOnly = true)
@SuppressWarnings("unused")
public class JpaPurchaseOrderMetrics implements PurchaseOrderMetricsUseCase {

    @PersistenceContext
    private EntityManager em;

    /** Value of an order line: its total, or price × quantity when the total is missing. */
    private static final String LINE_VALUE = "coalesce(l.lineTotal, l.unitPrice * l.quantity, 0)";

    @Override
    public Map<String, Long> documentsByCurrency() {
        return countByCurrency(em, "PurchaseOrderJpaEntity");
    }

    @Override
    public Map<String, Long> valuedAtOwnDate(String reportingCurrency) {
        return countValuedAtOwnDate(em, "PurchaseOrderJpaEntity", reportingCurrency);
    }

    @Override
    public long count(Set<String> statusNames) {
        Set<OrderStatus> statuses = enums(OrderStatus.class, statusNames);
        if (statuses.isEmpty()) return 0;
        return em.createQuery("select count(o) from PurchaseOrderJpaEntity o where o.status in :statuses", Long.class)
                .setParameter("statuses", statuses).getSingleResult();
    }

    @Override
    public long countExpectedBefore(Set<String> statusNames, LocalDate date) {
        Set<OrderStatus> statuses = enums(OrderStatus.class, statusNames);
        if (statuses.isEmpty()) return 0;
        return em.createQuery("select count(o) from PurchaseOrderJpaEntity o where o.status in :statuses "
                        + "and o.expectedDeliveryDate < :date", Long.class)
                .setParameter("statuses", statuses).setParameter("date", date).getSingleResult();
    }

    @Override
    public BigDecimal sum(Set<String> statusNames, ReportingRates rates) {
        Set<OrderStatus> statuses = enums(OrderStatus.class, statusNames);
        if (statuses.isEmpty()) return BigDecimal.ZERO;
        return amount(em.createQuery("select coalesce(sum(" + converted("o", "o.grandTotal", rates) + "), 0) "
                        + "from PurchaseOrderJpaEntity o where o.status in :statuses", Object.class)
                .setParameter("statuses", statuses).getSingleResult());
    }

    @Override
    public List<MonthAmount> byMonth(Set<String> statusNames, ReportingRates rates, LocalDate from, LocalDate to) {
        Set<OrderStatus> statuses = enums(OrderStatus.class, statusNames);
        if (statuses.isEmpty()) return List.of();
        return months(em.createQuery("select extract(year from o.orderDate), extract(month from o.orderDate), "
                        + "coalesce(sum(" + converted("o", "o.grandTotal", rates) + "), 0) from PurchaseOrderJpaEntity o "
                        + "where o.status in :statuses and o.orderDate between :from and :to "
                        + "group by extract(year from o.orderDate), extract(month from o.orderDate)", Object[].class)
                .setParameter("statuses", statuses).setParameter("from", from).setParameter("to", to));
    }

    @Override
    public Map<String, Long> countByStatus() {
        Map<OrderStatus, Long> counts = new EnumMap<>(OrderStatus.class);
        em.createQuery("select o.status, count(o) from PurchaseOrderJpaEntity o where o.status is not null "
                        + "group by o.status", Object[].class)
                .getResultList()
                .forEach(r -> counts.put((OrderStatus) r[0], (Long) r[1]));
        // The EnumMap iterates in lifecycle order; keep that order for the names.
        Map<String, Long> byName = new LinkedHashMap<>();
        counts.forEach((status, count) -> byName.put(status.name(), count));
        return byName;
    }

    @Override
    public List<MaterialSpend> spendByMaterial(Set<String> statusNames, ReportingRates rates) {
        Set<OrderStatus> statuses = enums(OrderStatus.class, statusNames);
        if (statuses.isEmpty()) return List.of();
        return em.createQuery("select l.materialId, l.materialCode, count(l), "
                        + "coalesce(sum(" + converted("o", LINE_VALUE, rates) + "), 0) "
                        + "from PurchaseOrderLineJpaEntity l join l.purchaseOrder o where o.status in :statuses "
                        + "group by l.materialId, l.materialCode", Object[].class)
                .setParameter("statuses", statuses)
                .getResultList().stream()
                .map(r -> new MaterialSpend((UUID) r[0], (String) r[1], (Long) r[2], amount(r[3])))
                .toList();
    }

    @Override
    public List<CountedAmount> spendBySupplier(Set<String> statusNames, ReportingRates rates) {
        Set<OrderStatus> statuses = enums(OrderStatus.class, statusNames);
        if (statuses.isEmpty()) return List.of();
        return em.createQuery("select o.supplierId, max(o.supplierName), count(o), "
                        + "coalesce(sum(" + converted("o", "o.grandTotal", rates) + "), 0) "
                        + "from PurchaseOrderJpaEntity o where o.status in :statuses "
                        + "and o.supplierId is not null group by o.supplierId", Object[].class)
                .setParameter("statuses", statuses)
                .getResultList().stream()
                .map(r -> new CountedAmount(r[0].toString(), (String) r[1], (Long) r[2], amount(r[3])))
                .toList();
    }

    @Override
    public List<RecentDocument> latest(int limit) {
        return em.createQuery("select d.id, d.orderCode, d.supplierName, d.status, d.createdAt from PurchaseOrderJpaEntity d "
                        + "where d.createdAt is not null order by d.createdAt desc", Object[].class)
                .setMaxResults(limit)
                .getResultList().stream()
                .map(r -> new RecentDocument((UUID) r[0], (String) r[1], (String) r[2],
                        r[3] != null ? r[3].toString() : null, (LocalDateTime) r[4]))
                .toList();
    }
}
