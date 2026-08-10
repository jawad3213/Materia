package com.materia.backend.contexts.returnToVendor.domain.ports.out;

import com.materia.backend.common.domain.BaseRepository;
import com.materia.backend.contexts.returnToVendor.domain.entities.ReturnToVendor;
import com.materia.backend.contexts.returnToVendor.domain.enums.ResolutionType;
import com.materia.backend.contexts.returnToVendor.domain.enums.ReturnStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ReturnToVendorRepository extends BaseRepository<ReturnToVendor> {

    Optional<ReturnToVendor> findByReturnCode(String returnCode);

    boolean existsByReturnCode(String returnCode);

    List<ReturnToVendor> findByGoodsReceiptId(String goodsReceiptId);

    List<ReturnToVendor> findByPurchaseOrderId(String purchaseOrderId);

    List<ReturnToVendor> findBySupplierId(String supplierId);

    List<ReturnToVendor> findByStatus(ReturnStatus status);

    List<ReturnToVendor> findByResolutionType(ResolutionType resolutionType);

    List<ReturnToVendor> findByReturnDateBetween(LocalDate startDate, LocalDate endDate);

    List<ReturnToVendor> findByResolutionDateBetween(LocalDate startDate, LocalDate endDate);

    List<ReturnToVendor> search(String keyword);
}
