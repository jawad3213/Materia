package com.materia.backend.contexts.masterdata.infrastructure.adapters.out.persistence.repositories;

import com.materia.backend.contexts.masterdata.domain.enums.MaterialStatus;
import com.materia.backend.contexts.masterdata.infrastructure.adapters.out.persistence.entities.MaterialJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA Repository for MaterialJpaEntity
 */
@Repository
public interface SpringDataMaterialRepository extends JpaRepository<MaterialJpaEntity, UUID> {

    Optional<MaterialJpaEntity> findByCode(String code);

    List<MaterialJpaEntity> findByCategoryId(String categoryId);

    List<MaterialJpaEntity> findBySupplierId(String supplierId);

    List<MaterialJpaEntity> findByStatus(MaterialStatus status);

    boolean existsByCode(String code);

    @Query("SELECT m FROM MaterialJpaEntity m WHERE " +
           "LOWER(m.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(m.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(m.searchKeywords) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<MaterialJpaEntity> search(@Param("keyword") String keyword);

    @Query("SELECT m FROM MaterialJpaEntity m WHERE m.currentStock < m.minimumStock")
    List<MaterialJpaEntity> findBelowMinimumStock();

    @Query("SELECT m FROM MaterialJpaEntity m WHERE m.currentStock < m.reorderPoint")
    List<MaterialJpaEntity> findBelowReorderPoint();

    @Query("SELECT m FROM MaterialJpaEntity m WHERE m.availableStock > 0")
    List<MaterialJpaEntity> findAvailableStock();

    @Query("SELECT m FROM MaterialJpaEntity m WHERE m.availableStock <= 0")
    List<MaterialJpaEntity> findOutOfStock();
}
