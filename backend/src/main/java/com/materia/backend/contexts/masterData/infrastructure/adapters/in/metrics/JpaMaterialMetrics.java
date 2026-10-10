package com.materia.backend.contexts.masterData.infrastructure.adapters.in.metrics;

import com.materia.backend.contexts.masterData.application.dtos.material.StockAlert;
import com.materia.backend.contexts.masterData.domain.enums.MaterialStatus;
import com.materia.backend.contexts.masterData.domain.ports.in.MaterialMetricsUseCase;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.materia.backend.common.infrastructure.metrics.ReportingJpql.*;

/** This context's reporting figures, computed by the database on this context's own tables. */
@Component
@Transactional(readOnly = true)
@SuppressWarnings("unused")
public class JpaMaterialMetrics implements MaterialMetricsUseCase {

    @PersistenceContext
    private EntityManager em;

    /** An active material at or below its reorder level (out of stock, below safety stock or reorder point). */
    private static final String STOCK_ALERT = "m.status = :active and m.currentStock is not null and (m.currentStock <= 0 "
            + "or (m.safetyStock is not null and m.currentStock <= m.safetyStock) "
            + "or (m.reorderPoint is not null and m.currentStock <= m.reorderPoint))";

    @Override
    public long countStockAlerts(boolean outOfStockOnly) {
        return em.createQuery("select count(m) from MaterialJpaEntity m where " + STOCK_ALERT
                        + (outOfStockOnly ? " and m.currentStock <= 0" : ""), Long.class)
                .setParameter("active", MaterialStatus.ACTIVE).getSingleResult();
    }

    @Override
    public List<StockAlert> stockAlerts(int limit) {
        return em.createQuery("select m.id, m.code, m.name, m.currentStock, m.reorderPoint, m.safetyStock, "
                        + "coalesce(m.stockOnOrder, 0), m.unitOfMeasure from MaterialJpaEntity m where " + STOCK_ALERT
                        + " order by case when m.currentStock <= 0 then 0 "
                        + "when m.safetyStock is not null and m.currentStock <= m.safetyStock then 1 else 2 end, "
                        + "m.currentStock - coalesce(m.reorderPoint, 0)", Object[].class)
                .setParameter("active", MaterialStatus.ACTIVE)
                .setMaxResults(limit)
                .getResultList().stream()
                .map(r -> new StockAlert((UUID) r[0], (String) r[1], (String) r[2], (Integer) r[3], (Integer) r[4],
                        (Integer) r[5], (Integer) r[6], r[7] != null ? r[7].toString() : null))
                .toList();
    }

    @Override
    public Map<UUID, String> categoryNamesById(Collection<UUID> materialIds) {
        Map<UUID, String> names = new HashMap<>();
        if (materialIds == null || materialIds.isEmpty()) return names;
        em.createQuery("select m.id, m.categoryName from MaterialJpaEntity m where m.id in :ids", Object[].class)
                .setParameter("ids", materialIds)
                .getResultList()
                .forEach(r -> names.put((UUID) r[0], (String) r[1]));
        return names;
    }

    @Override
    public Map<String, String> categoryNamesByCode(Collection<String> materialCodes) {
        Map<String, String> names = new HashMap<>();
        if (materialCodes == null || materialCodes.isEmpty()) return names;
        em.createQuery("select m.code, m.categoryName from MaterialJpaEntity m where m.code in :codes", Object[].class)
                .setParameter("codes", materialCodes)
                .getResultList()
                .forEach(r -> names.put(String.valueOf(r[0]), (String) r[1]));
        return names;
    }
}
