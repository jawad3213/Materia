package com.materia.backend.contexts.payement.infrastructure.adapters.out.persistence.repositories;

import com.materia.backend.contexts.payement.domain.enums.PaymentStatus;
import com.materia.backend.contexts.payement.infrastructure.adapters.out.persistence.entities.PaymentJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentSpringDataRepository extends JpaRepository<PaymentJpaEntity, UUID> {

    Optional<PaymentJpaEntity> findByPaymentCode(String paymentCode);

    boolean existsByPaymentCode(String paymentCode);

    List<PaymentJpaEntity> findByStatus(PaymentStatus status);

    List<PaymentJpaEntity> findBySupplierId(String supplierId);

    @Query("SELECT p FROM PaymentJpaEntity p WHERE " +
            "LOWER(p.paymentCode) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.supplierName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.notes) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<PaymentJpaEntity> searchByKeyword(@Param("keyword") String keyword);
}
