package com.materia.backend.contexts.goodsReceipt.application.mappers;

import com.materia.backend.common.application.BaseMapper;
import com.materia.backend.contexts.goodsReceipt.application.dtos.CreateGoodsReceiptInput;
import com.materia.backend.contexts.goodsReceipt.application.dtos.GoodsReceiptLineInput;
import com.materia.backend.contexts.goodsReceipt.application.dtos.GoodsReceiptLineOutput;
import com.materia.backend.contexts.goodsReceipt.application.dtos.GoodsReceiptOutput;
import com.materia.backend.contexts.goodsReceipt.application.dtos.UpdateGoodsReceiptInput;
import com.materia.backend.contexts.goodsReceipt.domain.entities.GoodsReceipt;
import com.materia.backend.contexts.goodsReceipt.domain.entities.GoodsReceiptLine;
import com.materia.backend.contexts.goodsReceipt.domain.enums.QualityStatus;
import com.materia.backend.contexts.goodsReceipt.domain.enums.ReceiptStatus;
import com.materia.backend.contexts.goodsReceipt.domain.valueObjects.ReceiptCode;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/** Maps the goods receipt aggregate between application DTOs and domain objects. */
@Component
public class GoodsReceiptMapper implements BaseMapper<GoodsReceipt, CreateGoodsReceiptInput, UpdateGoodsReceiptInput, GoodsReceiptOutput> {

    @Override
    public GoodsReceipt toEntity(CreateGoodsReceiptInput request) {
        if (request == null) {
            return null;
        }

        GoodsReceipt.Builder builder = GoodsReceipt.builder()
                .purchaseOrderId(request.getPurchaseOrderId())
                .purchaseOrderCode(request.getPurchaseOrderCode())
                .receiptDate(request.getReceiptDate())
                .expectedDeliveryDate(request.getExpectedDeliveryDate())
                .receivedBy(request.getReceivedBy())
                .receivedByName(request.getReceivedByName())
                .notes(request.getNotes())
                .supplierId(request.getSupplierId())
                .supplierName(request.getSupplierName())
                .discrepancyNotes(request.getDiscrepancyNotes())
                .lines(toLineEntities(request.getLines()))
                .createdBy(request.getUserId());

        if (request.getReceiptCode() != null && !request.getReceiptCode().isBlank()) {
            builder.receiptCode(request.getReceiptCode());
        }

        return builder.build();
    }

    @Override
    public void updateEntity(GoodsReceipt entity, UpdateGoodsReceiptInput request) {
        if (entity == null || request == null) {
            return;
        }

        if (request.getReceiptCode() != null && !request.getReceiptCode().isBlank()) {
            entity.setReceiptCode(ReceiptCode.of(request.getReceiptCode()));
        }
        if (request.getPurchaseOrderId() != null) entity.setPurchaseOrderId(request.getPurchaseOrderId());
        if (request.getPurchaseOrderCode() != null) entity.setPurchaseOrderCode(request.getPurchaseOrderCode());
        if (request.getReceiptDate() != null) entity.setReceiptDate(request.getReceiptDate());
        if (request.getExpectedDeliveryDate() != null) entity.setExpectedDeliveryDate(request.getExpectedDeliveryDate());
        if (request.getReceivedBy() != null) entity.setReceivedBy(request.getReceivedBy());
        if (request.getReceivedByName() != null) entity.setReceivedByName(request.getReceivedByName());
        if (request.getNotes() != null) entity.setNotes(request.getNotes());
        if (request.getSupplierId() != null) entity.setSupplierId(request.getSupplierId());
        if (request.getSupplierName() != null) entity.setSupplierName(request.getSupplierName());
        if (request.getDiscrepancyNotes() != null) entity.setDiscrepancyNotes(request.getDiscrepancyNotes());
        if (request.getLines() != null && !request.getLines().isEmpty()) {
            entity.setLines(toLineEntities(request.getLines()));
            entity.recalculateTotals();
        }
        if (request.getUserId() != null) entity.updateAudit(request.getUserId());
    }

    @Override
    public GoodsReceiptOutput toResponse(GoodsReceipt entity) {
        if (entity == null) {
            return null;
        }

        GoodsReceiptOutput output = new GoodsReceiptOutput();
        output.setId(entity.getId());
        output.setReceiptCode(entity.getReceiptCode() != null ? entity.getReceiptCode().getValue() : null);
        output.setPurchaseOrderId(entity.getPurchaseOrderId());
        output.setPurchaseOrderCode(entity.getPurchaseOrderCode());
        output.setStatus(entity.getStatus() != null ? entity.getStatus().name() : null);
        output.setReceiptDate(entity.getReceiptDate());
        output.setExpectedDeliveryDate(entity.getExpectedDeliveryDate());
        output.setReceivedBy(entity.getReceivedBy());
        output.setReceivedByName(entity.getReceivedByName());
        output.setNotes(entity.getNotes());
        output.setSupplierId(entity.getSupplierId());
        output.setSupplierName(entity.getSupplierName());
        output.setTotalQuantityOrdered(entity.getTotalQuantityOrdered());
        output.setTotalQuantityReceived(entity.getTotalQuantityReceived());
        output.setTotalQuantityRejected(entity.getTotalQuantityRejected());
        output.setTotalQuantityAccepted(entity.getTotalQuantityAccepted());
        output.setHasDiscrepancy(entity.isHasDiscrepancy());
        output.setDiscrepancyNotes(entity.getDiscrepancyNotes());
        output.setCreatedBy(entity.getCreatedBy());
        output.setCreatedAt(entity.getCreatedAt());
        output.setUpdatedBy(entity.getUpdatedBy());
        output.setUpdatedAt(entity.getUpdatedAt());
        output.setLines(toLineOutputs(entity.getLines()));
        return output;
    }

    public ReceiptStatus toReceiptStatus(String status) {
        return status != null && !status.isBlank() ? ReceiptStatus.fromCode(status) : null;
    }

    public GoodsReceiptLine toLineEntity(GoodsReceiptLineInput line) {
        if (line == null) {
            return null;
        }

        return GoodsReceiptLine.builder()
                .id(line.getId())
                .lineNumber(line.getLineNumber())
                .purchaseOrderLineId(line.getPurchaseOrderLineId())
                .materialCode(line.getMaterialCode())
                .materialId(line.getMaterialId())
                .materialName(line.getMaterialName())
                .unitOfMeasure(line.getUnitOfMeasure())
                // The purchase order is the only source of the ordered quantity.
                .quantityOrdered(null)
                .quantityReceived(line.getQuantityReceived())
                .quantityRejected(line.getQuantityRejected())
                .qualityStatus(toQualityStatus(line.getQualityStatus()))
                .qualityNotes(line.getQualityNotes())
                .rejectionReason(line.getRejectionReason())
                .stockBefore(line.getStockBefore())
                .stockAfter(line.getStockAfter())
                .unitPrice(line.getUnitPrice())
                .supplierId(line.getSupplierId())
                .supplierName(line.getSupplierName())
                .batchNumber(line.getBatchNumber())
                .expiryDate(line.getExpiryDate())
                .storageLocation(line.getStorageLocation())
                .notes(line.getNotes())
                .build();
    }

    public GoodsReceiptLineOutput toLineOutput(GoodsReceiptLine line) {
        if (line == null) {
            return null;
        }

        GoodsReceiptLineOutput output = new GoodsReceiptLineOutput();
        output.setId(line.getId());
        output.setLineNumber(line.getLineNumber());
        output.setPurchaseOrderLineId(line.getPurchaseOrderLineId());
        output.setMaterialCode(line.getMaterialCode());
        output.setMaterialId(line.getMaterialId());
        output.setMaterialName(line.getMaterialName());
        output.setUnitOfMeasure(line.getUnitOfMeasure());
        output.setQuantityOrdered(line.getQuantityOrdered());
        output.setQuantityReceived(line.getQuantityReceived());
        output.setQuantityRejected(line.getQuantityRejected());
        output.setQuantityAccepted(line.getQuantityAccepted());
        output.setQuantityPending(line.getQuantityPending());
        output.setQualityStatus(line.getQualityStatus() != null ? line.getQualityStatus().name() : null);
        output.setQualityNotes(line.getQualityNotes());
        output.setRejectionReason(line.getRejectionReason());
        output.setStockBefore(line.getStockBefore());
        output.setStockAfter(line.getStockAfter());
        output.setUnitPrice(line.getUnitPrice());
        output.setLineTotal(line.getLineTotal());
        output.setSupplierId(line.getSupplierId());
        output.setSupplierName(line.getSupplierName());
        output.setBatchNumber(line.getBatchNumber());
        output.setExpiryDate(line.getExpiryDate());
        output.setStorageLocation(line.getStorageLocation());
        output.setNotes(line.getNotes());
        return output;
    }

    private QualityStatus toQualityStatus(String qualityStatus) {
        return qualityStatus != null && !qualityStatus.isBlank()
                ? QualityStatus.fromCode(qualityStatus)
                : null;
    }

    private List<GoodsReceiptLine> toLineEntities(List<GoodsReceiptLineInput> lines) {
        if (lines == null) {
            return new ArrayList<>();
        }
        return lines.stream()
                .map(this::toLineEntity)
                .filter(line -> line != null)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private List<GoodsReceiptLineOutput> toLineOutputs(List<GoodsReceiptLine> lines) {
        if (lines == null) {
            return new ArrayList<>();
        }
        return lines.stream()
                .map(this::toLineOutput)
                .filter(line -> line != null)
                .collect(Collectors.toCollection(ArrayList::new));
    }
}
