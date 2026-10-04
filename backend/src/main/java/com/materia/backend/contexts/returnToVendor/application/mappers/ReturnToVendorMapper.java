package com.materia.backend.contexts.returnToVendor.application.mappers;

import com.materia.backend.contexts.returnToVendor.application.dtos.ReturnToVendorLineOutput;
import com.materia.backend.contexts.returnToVendor.application.dtos.ReturnToVendorOutput;
import com.materia.backend.contexts.returnToVendor.domain.entities.ReturnToVendor;
import com.materia.backend.contexts.returnToVendor.domain.entities.ReturnToVendorLine;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Turns returns into outputs. Returns are built by the service from their goods receipt, never mapped from
 * client input, so there is no input-to-entity mapping here.
 */
@Component
public class ReturnToVendorMapper {

    public ReturnToVendorOutput toResponse(ReturnToVendor entity) {
        if (entity == null) {
            return null;
        }
        ReturnToVendorOutput output = new ReturnToVendorOutput();
        output.setId(entity.getId());
        output.setReturnCode(entity.getReturnCode() != null ? entity.getReturnCode().getValue() : null);
        output.setGoodsReceiptId(entity.getGoodsReceiptId());
        output.setGoodsReceiptCode(entity.getGoodsReceiptCode());
        output.setPurchaseOrderId(entity.getPurchaseOrderId());
        output.setPurchaseOrderCode(entity.getPurchaseOrderCode());
        output.setSupplierId(entity.getSupplierId());
        output.setSupplierName(entity.getSupplierName());
        output.setSupplierCode(entity.getSupplierCode());
        output.setCurrencyCode(entity.getCurrencyCode());
        output.setTotalValue(entity.totalValue());
        output.setTotalQuantity(entity.totalQuantity());
        output.setStatus(entity.getStatus() != null ? entity.getStatus().name() : null);
        output.setResolutionType(entity.getResolutionType() != null ? entity.getResolutionType().name() : null);
        output.setReturnDate(entity.getReturnDate());
        output.setResolutionDate(entity.getResolutionDate());
        output.setReturnReason(entity.getReturnReason());
        output.setSupplierResponse(entity.getSupplierResponse());
        output.setRejectionSummary(entity.getRejectionSummary());
        output.setCreditNoteReference(entity.getCreditNoteReference());
        output.setCreditNoteAmount(entity.getCreditNoteAmount());
        output.setReplacementPurchaseOrderReference(entity.getReplacementPurchaseOrderReference());
        output.setReplacementPurchaseOrderCode(entity.getReplacementPurchaseOrderCode());
        output.setNotes(entity.getNotes());
        output.setInternalNotes(entity.getInternalNotes());
        output.setCreatedBy(entity.getCreatedBy());
        output.setCreatedAt(entity.getCreatedAt());
        output.setUpdatedBy(entity.getUpdatedBy());
        output.setUpdatedAt(entity.getUpdatedAt());
        output.setLines(toLineOutputs(entity.getLines()));
        return output;
    }

    public List<ReturnToVendorOutput> toResponseList(List<ReturnToVendor> entities) {
        if (entities == null) {
            return new ArrayList<>();
        }
        return entities.stream().map(this::toResponse).filter(Objects::nonNull).collect(Collectors.toList());
    }

    public ReturnToVendorLineOutput toLineOutput(ReturnToVendorLine line) {
        if (line == null) {
            return null;
        }
        ReturnToVendorLineOutput output = new ReturnToVendorLineOutput();
        output.setId(line.getId());
        output.setLineNumber(line.getLineNumber());
        output.setGoodsReceiptLineId(line.getGoodsReceiptLineId());
        output.setPurchaseOrderLineId(line.getPurchaseOrderLineId());
        output.setMaterialId(line.getMaterialId());
        output.setMaterialCode(line.getMaterialCode());
        output.setMaterialName(line.getMaterialName());
        output.setUnitOfMeasure(line.getUnitOfMeasure());
        output.setRejectedQuantity(line.getRejectedQuantity());
        output.setQuantityToReturn(line.getQuantityToReturn());
        output.setQuantityAlreadyReturned(line.getQuantityAlreadyReturned());
        output.setRemainingQuantity(line.getRemainingQuantity());
        output.setUnitPrice(line.getUnitPrice());
        output.setLineValue(line.lineValue());
        output.setRejectionReason(line.getRejectionReason());
        output.setQualityNotes(line.getQualityNotes());
        output.setDefectDescription(line.getDefectDescription());
        output.setReplaced(line.isReplaced());
        output.setCreditNote(line.isCreditNote());
        output.setNotes(line.getNotes());
        return output;
    }

    private List<ReturnToVendorLineOutput> toLineOutputs(List<ReturnToVendorLine> lines) {
        if (lines == null) {
            return new ArrayList<>();
        }
        return lines.stream().map(this::toLineOutput).filter(Objects::nonNull).collect(Collectors.toCollection(ArrayList::new));
    }
}
