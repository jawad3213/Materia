package com.materia.backend.contexts.invoice.infrastructure.adapters.out.persistence.repositories;

import com.materia.backend.contexts.invoice.domain.enums.InvoiceStatus;
import com.materia.backend.contexts.invoice.infrastructure.adapters.out.persistence.entities.InvoiceJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InvoiceSpringDataRepository extends JpaRepository<InvoiceJpaEntity, UUID> {

    Optional<InvoiceJpaEntity> findByInvoiceCode(String invoiceCode);

    List<InvoiceJpaEntity> findByStatus(InvoiceStatus status);

    List<InvoiceJpaEntity> findBySupplierId(String supplierId);

    List<InvoiceJpaEntity> findByPurchaseOrderId(String purchaseOrderId);

    boolean existsByInvoiceCode(String invoiceCode);

    @Query("SELECT i FROM InvoiceJpaEntity i WHERE " +
           "LOWER(i.invoiceCode) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(i.supplierName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(i.purchaseOrderCode) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<InvoiceJpaEntity> searchByKeyword(@Param("keyword") String keyword);

    List<InvoiceJpaEntity> findByDueDateBetween(java.time.LocalDate startDate, java.time.LocalDate endDate);

    List<InvoiceJpaEntity> findByInvoiceDateBetween(java.time.LocalDate startDate, java.time.LocalDate endDate);

    List<InvoiceJpaEntity> findByInvoiceType(com.materia.backend.contexts.invoice.domain.enums.InvoiceType type);
}
