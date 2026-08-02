package com.materia.backend.contexts.masterdata.infrastructure.adapters.out.persistence.repositories;

import com.materia.backend.contexts.masterdata.infrastructure.adapters.out.persistence.entities.SupplierJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA Repository for SupplierJpaEntity
 */
@Repository
public interface SpringDataSupplierRepository extends JpaRepository<SupplierJpaEntity, UUID> {

    Optional<SupplierJpaEntity> findByCode(String code);



    List<SupplierJpaEntity> findByStatus(String status);

    List<SupplierJpaEntity> findByCountry(String country);

    List<SupplierJpaEntity> findByCity(String city);

    boolean existsByCode(String code);

    @Query("SELECT s FROM SupplierJpaEntity s WHERE " +
           "LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(s.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(s.contactPerson) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<SupplierJpaEntity> search(@Param("keyword") String keyword);
}
