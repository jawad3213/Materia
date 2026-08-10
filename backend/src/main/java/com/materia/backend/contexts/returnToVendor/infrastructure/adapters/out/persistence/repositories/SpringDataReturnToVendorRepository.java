package com.materia.backend.contexts.returnToVendor.infrastructure.adapters.out.persistence.repositories;

import com.materia.backend.contexts.returnToVendor.infrastructure.adapters.out.persistence.entities.ReturnToVendorJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataReturnToVendorRepository extends JpaRepository<ReturnToVendorJpaEntity, UUID> {
    Optional<ReturnToVendorJpaEntity> findByReturnCode(String returnCode);
    boolean existsByReturnCode(String returnCode);
    List<ReturnToVendorJpaEntity> findByGoodsReceiptId(String goodsReceiptId);
    List<ReturnToVendorJpaEntity> findByPurchaseOrderId(String purchaseOrderId);
    List<ReturnToVendorJpaEntity> findBySupplierId(String supplierId);
    List<ReturnToVendorJpaEntity> findByStatus(String status);
    List<ReturnToVendorJpaEntity> findByResolutionType(String resolutionType);
    List<ReturnToVendorJpaEntity> findByReturnDateBetween(LocalDate startDate, LocalDate endDate);
    List<ReturnToVendorJpaEntity> findByResolutionDateBetween(LocalDate startDate, LocalDate endDate);
    
    @Query("SELECT r FROM ReturnToVendorJpaEntity r WHERE LOWER(r.returnCode) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(r.supplierName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(r.supplierCode) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<ReturnToVendorJpaEntity> search(@Param("keyword") String keyword);
}
