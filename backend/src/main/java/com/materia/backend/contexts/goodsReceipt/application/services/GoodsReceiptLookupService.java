package com.materia.backend.contexts.goodsReceipt.application.services;

import com.materia.backend.contexts.goodsReceipt.application.dtos.GoodsReceiptView;
import com.materia.backend.contexts.goodsReceipt.domain.entities.GoodsReceipt;
import com.materia.backend.contexts.goodsReceipt.domain.enums.ReceiptStatus;
import com.materia.backend.contexts.goodsReceipt.domain.ports.in.GoodsReceiptLookupUseCase;
import com.materia.backend.contexts.goodsReceipt.domain.ports.out.GoodsReceiptRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class GoodsReceiptLookupService implements GoodsReceiptLookupUseCase {

    private static final Set<ReceiptStatus> VALIDATED = EnumSet.of(ReceiptStatus.COMPLETED, ReceiptStatus.PARTIAL);

    private final GoodsReceiptRepository goodsReceiptRepository;

    public GoodsReceiptLookupService(GoodsReceiptRepository goodsReceiptRepository) {
        this.goodsReceiptRepository = goodsReceiptRepository;
    }

    @Override
    public Optional<GoodsReceiptView> findReceipt(UUID goodsReceiptId) {
        return goodsReceiptRepository.findById(goodsReceiptId).map(GoodsReceiptLookupService::toView);
    }

    @Override
    public List<GoodsReceiptView> findReceiptsForOrder(String purchaseOrderId) {
        return goodsReceiptRepository.findByPurchaseOrderId(purchaseOrderId).stream()
                .map(GoodsReceiptLookupService::toView)
                .toList();
    }

    private static GoodsReceiptView toView(GoodsReceipt receipt) {
        return new GoodsReceiptView(
                receipt.getId(),
                receipt.getReceiptCode() != null ? receipt.getReceiptCode().getValue() : null,
                receipt.getStatus() != null ? receipt.getStatus().name() : null,
                VALIDATED.contains(receipt.getStatus()),
                receipt.getPurchaseOrderId(),
                receipt.getPurchaseOrderCode(),
                receipt.getSupplierId(),
                receipt.getSupplierName(),
                receipt.getLines().stream()
                        .map(line -> new GoodsReceiptView.Line(line.getId(), line.getPurchaseOrderLineId(),
                                line.getMaterialId(), line.getMaterialCode(), line.getMaterialName(),
                                line.getUnitOfMeasure(), line.getUnitPrice(), line.getQuantityReceived(),
                                line.getQuantityAccepted(), line.getQuantityRejected(), line.getRejectionReason(),
                                line.getQualityNotes()))
                        .toList());
    }
}
