package com.materia.backend.contexts.purchaseRequisition.infrastructure.adapters.out.persistence.repositories;

import com.materia.backend.contexts.purchaseRequisition.infrastructure.adapters.out.persistence.entities.RequisitionCodeSequenceJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data repository for requisition code sequences.
 */
@Repository
public interface SpringDataRequisitionCodeSequenceRepository extends JpaRepository<RequisitionCodeSequenceJpaEntity, UUID> {

    @Query("SELECT s FROM RequisitionCodeSequenceJpaEntity s WHERE s.prefix = :prefix")
    Optional<RequisitionCodeSequenceJpaEntity> findByPrefixForUpdate(@Param("prefix") String prefix);
}
