package com.materia.backend.contexts.purchaseRequisition.infrastructure.adapters.in.metrics;

import com.materia.backend.common.application.metrics.RecentDocument;
import com.materia.backend.contexts.purchaseRequisition.domain.enums.RequisitionStatus;
import com.materia.backend.contexts.purchaseRequisition.domain.ports.in.RequisitionMetricsUseCase;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static com.materia.backend.common.infrastructure.metrics.ReportingJpql.*;

/** This context's reporting figures, computed by the database on this context's own tables. */
@Component
@Transactional(readOnly = true)
@SuppressWarnings("unused")
public class JpaRequisitionMetrics implements RequisitionMetricsUseCase {

    @PersistenceContext
    private EntityManager em;

    @Override
    public long countInStatus(String status) {
        return em.createQuery("select count(r) from RequisitionJpaEntity r where r.status = :status", Long.class)
                .setParameter("status", RequisitionStatus.valueOf(status)).getSingleResult();
    }

    @Override
    public List<RecentDocument> latest(int limit) {
        return em.createQuery("select d.id, d.requisitionCode, d.title, d.status, d.createdAt from RequisitionJpaEntity d "
                        + "where d.createdAt is not null order by d.createdAt desc", Object[].class)
                .setMaxResults(limit)
                .getResultList().stream()
                .map(r -> new RecentDocument((UUID) r[0], (String) r[1], (String) r[2],
                        r[3] != null ? r[3].toString() : null, (LocalDateTime) r[4]))
                .toList();
    }
}
