package com.materia.backend.contexts.purchaseRequisition.infrastructure.adapters.out.persistence.repositories;

import com.materia.backend.contexts.purchaseRequisition.domain.enums.RequisitionStatus;
import com.materia.backend.contexts.purchaseRequisition.infrastructure.adapters.out.persistence.entities.RequisitionJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for purchase requisitions.
 */
@Repository
public interface SpringDataRequisitionRepository extends JpaRepository<RequisitionJpaEntity, UUID>, JpaSpecificationExecutor<RequisitionJpaEntity> {

    Optional<RequisitionJpaEntity> findByRequisitionCode(String requisitionCode);

    boolean existsByRequisitionCode(String requisitionCode);

    List<RequisitionJpaEntity> findByStatus(RequisitionStatus status);

    List<RequisitionJpaEntity> findByRequesterId(String requesterId);

    List<RequisitionJpaEntity> findByApproverId(String approverId);

    List<RequisitionJpaEntity> findByRequiredDateBetween(LocalDate startDate, LocalDate endDate);

    List<RequisitionJpaEntity> findBySubmittedDateBetween(LocalDate startDate, LocalDate endDate);

    @Query("""
            SELECT r
            FROM RequisitionJpaEntity r
            WHERE LOWER(r.requisitionCode) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(r.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(COALESCE(r.description, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(COALESCE(r.justification, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(COALESCE(r.requesterName, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
            """)
    List<RequisitionJpaEntity> search(@Param("keyword") String keyword);
}
