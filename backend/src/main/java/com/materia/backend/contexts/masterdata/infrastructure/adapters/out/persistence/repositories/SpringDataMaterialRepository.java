package com.materia.backend.contexts.masterData.infrastructure.adapters.out.persistence.repositories;

import com.materia.backend.contexts.masterData.domain.enums.MaterialStatus;
import com.materia.backend.contexts.masterData.infrastructure.adapters.out.persistence.entities.MaterialJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA Repository for MaterialJpaEntity
 */
@Repository
public interface SpringDataMaterialRepository extends JpaRepository<MaterialJpaEntity, UUID>, JpaSpecificationExecutor<MaterialJpaEntity> {

    Optional<MaterialJpaEntity> findByCode(String code);


    List<MaterialJpaEntity> findByCategoryId(UUID categoryId);

    boolean existsByCategoryId(UUID categoryId);

    long countByCategoryId(UUID categoryId);

    List<MaterialJpaEntity> findBySupplierId(UUID supplierId);

    boolean existsBySupplierId(UUID supplierId);

    List<MaterialJpaEntity> findByStatus(MaterialStatus status);

    List<MaterialJpaEntity> findByMaterialType(com.materia.backend.contexts.masterData.domain.enums.MaterialType materialType);

    boolean existsByCode(String code);



    @Query("SELECT m FROM MaterialJpaEntity m WHERE m.currentStock < m.reorderPoint")
    List<MaterialJpaEntity> findBelowReorderPoint();

    @Query("SELECT m FROM MaterialJpaEntity m WHERE m.availableStock <= 0")
    List<MaterialJpaEntity> findOutOfStock();

    @Query("SELECT m.categoryId, COUNT(m) FROM MaterialJpaEntity m WHERE m.categoryId IN :categoryIds GROUP BY m.categoryId")
    List<Object[]> countMaterialsByCategoryIds(@Param("categoryIds") List<UUID> categoryIds);

}
