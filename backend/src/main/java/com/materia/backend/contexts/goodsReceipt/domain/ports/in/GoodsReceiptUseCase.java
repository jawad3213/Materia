package com.materia.backend.contexts.goodsReceipt.domain.ports.in;

import com.materia.backend.common.domain.BaseUseCase;
import com.materia.backend.contexts.goodsReceipt.application.dtos.CreateGoodsReceiptInput;
import com.materia.backend.contexts.goodsReceipt.application.dtos.GoodsReceiptLineInput;
import com.materia.backend.contexts.goodsReceipt.application.dtos.GoodsReceiptOutput;
import com.materia.backend.contexts.goodsReceipt.application.dtos.UpdateGoodsReceiptInput;
import com.materia.backend.contexts.goodsReceipt.domain.enums.ReceiptStatus;

import java.util.List;
import java.util.UUID;

/** Input port for goods receipt use cases. */
public interface GoodsReceiptUseCase
        extends BaseUseCase<CreateGoodsReceiptInput, GoodsReceiptOutput, UUID> {

    GoodsReceiptOutput update(UUID id, UpdateGoodsReceiptInput request);

    void delete(UUID id, String userId);

    List<GoodsReceiptOutput> getByPurchaseOrderId(String purchaseOrderId);

    List<GoodsReceiptOutput> getByStatus(ReceiptStatus status);

    List<GoodsReceiptOutput> getByReceiverId(String receiverId);

    List<GoodsReceiptOutput> search(String keyword);

    GoodsReceiptOutput addLine(UUID id, GoodsReceiptLineInput line, String userId);

    GoodsReceiptOutput removeLine(UUID id, int lineIndex, String userId);

    GoodsReceiptOutput complete(UUID id, String userId);

    GoodsReceiptOutput cancel(UUID id, String userId, String reason);
}
