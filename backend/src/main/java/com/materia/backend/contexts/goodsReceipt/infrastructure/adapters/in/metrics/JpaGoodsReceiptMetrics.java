package com.materia.backend.contexts.goodsReceipt.infrastructure.adapters.in.metrics;

import com.materia.backend.common.application.metrics.RecentDocument;
import com.materia.backend.contexts.goodsReceipt.application.dtos.ReceiptTotals;
import com.materia.backend.contexts.goodsReceipt.domain.enums.ReceiptStatus;
import com.materia.backend.contexts.goodsReceipt.domain.ports.in.GoodsReceiptMetricsUseCase;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static com.materia.backend.common.infrastructure.metrics.ReportingJpql.*;

/** This context's reporting figures, computed by the database on this context's own tables. */
@Component
@Transactional(readOnly = true)
@SuppressWarnings("unused")
public class JpaGoodsReceiptMetrics implements GoodsReceiptMetricsUseCase {

    @PersistenceContext
    private EntityManager em;

    private static final String RECEIPT_TOTALS = "count(r), coalesce(sum(r.totalQuantityReceived), 0), "
            + "coalesce(sum(r.totalQuantityAccepted), 0), coalesce(sum(r.totalQuantityRejected), 0), "
            + "coalesce(sum(case when r.receiptDate is not null and r.expectedDeliveryDate is not null then 1 else 0 end), 0), "
            + "coalesce(sum(case when r.receiptDate is not null and r.expectedDeliveryDate is not null "
            + "and r.receiptDate <= r.expectedDeliveryDate then 1 else 0 end), 0)";

    @Override
    public long count(Set<String> statusNames, LocalDate from, LocalDate to, boolean withRejectionOnly) {
        Set<ReceiptStatus> statuses = enums(ReceiptStatus.class, statusNames);
        if (statuses.isEmpty()) return 0;
        return em.createQuery("select count(r) from GoodsReceiptJpaEntity r where r.status in :statuses "
                        + "and r.receiptDate between :from and :to"
                        + (withRejectionOnly ? " and r.totalQuantityRejected > 0" : ""), Long.class)
                .setParameter("statuses", statuses).setParameter("from", from).setParameter("to", to)
                .getSingleResult();
    }

    @Override
    public ReceiptTotals totals(Set<String> statusNames) {
        Set<ReceiptStatus> statuses = enums(ReceiptStatus.class, statusNames);
        if (statuses.isEmpty()) return ReceiptTotals.NONE;
        Object[] r = em.createQuery("select " + RECEIPT_TOTALS + " from GoodsReceiptJpaEntity r where r.status in :statuses",
                        Object[].class)
                .setParameter("statuses", statuses).getSingleResult();
        return totals(r, 0);
    }

    @Override
    public Map<UUID, ReceiptTotals> totalsBySupplier(Set<String> statusNames) {
        Set<ReceiptStatus> statuses = enums(ReceiptStatus.class, statusNames);
        Map<UUID, ReceiptTotals> result = new HashMap<>();
        if (statuses.isEmpty()) return result;
        em.createQuery("select r.supplierId, " + RECEIPT_TOTALS + " from GoodsReceiptJpaEntity r "
                        + "where r.status in :statuses and r.supplierId is not null group by r.supplierId", Object[].class)
                .setParameter("statuses", statuses)
                .getResultList()
                .forEach(r -> result.merge((UUID) r[0], totals(r, 1), ReceiptTotals::plus));
        return result;
    }

    private static ReceiptTotals totals(Object[] r, int offset) {
        return new ReceiptTotals(number(r[offset]), number(r[offset + 1]), number(r[offset + 2]), number(r[offset + 3]),
                number(r[offset + 4]), number(r[offset + 5]));
    }

    @Override
    public List<RecentDocument> latest(int limit) {
        return em.createQuery("select d.id, d.receiptCode, d.supplierName, d.status, d.createdAt from GoodsReceiptJpaEntity d "
                        + "where d.createdAt is not null order by d.createdAt desc", Object[].class)
                .setMaxResults(limit)
                .getResultList().stream()
                .map(r -> new RecentDocument((UUID) r[0], (String) r[1], (String) r[2],
                        r[3] != null ? r[3].toString() : null, (LocalDateTime) r[4]))
                .toList();
    }
}
