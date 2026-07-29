package com.materia.backend.contexts.masterdata.infrastructure.adapters.out.persistence.repositories;

import com.materia.backend.contexts.masterdata.infrastructure.adapters.out.persistence.entities.CategoryJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA Repository for CategoryJpaEntity
 * Infrastructure Layer - Provides database access via Spring Data
 */
@Repository
public interface SpringDataCategoryRepository extends JpaRepository<CategoryJpaEntity, UUID> {

    Optional<CategoryJpaEntity> findByCode(String code);

    List<CategoryJpaEntity> findByParentIdIsNull();

    List<CategoryJpaEntity> findByParentId(String parentId);

    List<CategoryJpaEntity> findByCategoryType(com.materia.backend.contexts.masterdata.domain.enums.CategoryType categoryType);

    List<CategoryJpaEntity> findByStatus(String status);

    boolean existsByCode(String code);

    @Query("SELECT c FROM CategoryJpaEntity c WHERE " +
           "LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<CategoryJpaEntity> search(@Param("keyword") String keyword);
}
