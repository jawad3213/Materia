package com.materia.backend.contexts.goodsReceipt.domain.ports.out;

import com.materia.backend.common.domain.BaseRepository;
import com.materia.backend.contexts.goodsReceipt.domain.entities.GoodsReceipt;
import com.materia.backend.contexts.goodsReceipt.domain.enums.ReceiptStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Outbound persistence port for goods receipts.
 * Implementations may use JPA, another database, or an external service.
 */
public interface GoodsReceiptRepository extends BaseRepository<GoodsReceipt> {

    Optional<GoodsReceipt> findByReceiptCode(String receiptCode);

    boolean existsByReceiptCode(String receiptCode);

    List<GoodsReceipt> findByPurchaseOrderId(String purchaseOrderId);

    List<GoodsReceipt> findByStatus(ReceiptStatus status);

    List<GoodsReceipt> findByReceivedBy(String receiverId);

    List<GoodsReceipt> findBySupplierId(String supplierId);

    List<GoodsReceipt> findByReceiptDateBetween(LocalDate startDate, LocalDate endDate);

    List<GoodsReceipt> search(String keyword);
}
