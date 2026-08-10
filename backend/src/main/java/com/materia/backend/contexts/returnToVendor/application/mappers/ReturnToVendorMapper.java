package com.materia.backend.contexts.returnToVendor.application.mappers;

import com.materia.backend.common.application.BaseMapper;
import com.materia.backend.contexts.returnToVendor.application.dtos.CreateReturnToVendorInput;
import com.materia.backend.contexts.returnToVendor.application.dtos.ReturnToVendorLineInput;
import com.materia.backend.contexts.returnToVendor.application.dtos.ReturnToVendorLineOutput;
import com.materia.backend.contexts.returnToVendor.application.dtos.ReturnToVendorOutput;
import com.materia.backend.contexts.returnToVendor.application.dtos.UpdateReturnToVendorInput;
import com.materia.backend.contexts.returnToVendor.domain.entities.ReturnToVendor;
import com.materia.backend.contexts.returnToVendor.domain.entities.ReturnToVendorLine;
import com.materia.backend.contexts.returnToVendor.domain.valueObjects.ReturnCode;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ReturnToVendorMapper implements BaseMapper<ReturnToVendor, CreateReturnToVendorInput, UpdateReturnToVendorInput, ReturnToVendorOutput> {

    @Override
    public ReturnToVendor toEntity(CreateReturnToVendorInput request) {
        if (request == null) {
            return null;
        }

        ReturnToVendor.Builder builder = ReturnToVendor.builder()
                .goodsReceiptId(request.getGoodsReceiptId())
                .goodsReceiptCode(request.getGoodsReceiptCode())
                .purchaseOrderId(request.getPurchaseOrderId())
                .purchaseOrderCode(request.getPurchaseOrderCode())
                .supplierId(request.getSupplierId())
                .supplierName(request.getSupplierName())
                .supplierCode(request.getSupplierCode())
                .returnDate(request.getReturnDate())
                .returnReason(request.getReturnReason())
                .rejectionSummary(request.getRejectionSummary())
                .notes(request.getNotes())
                .internalNotes(request.getInternalNotes())
                .lines(toLineEntities(request.getLines()))
                .createdBy(request.getUserId());

        if (request.getReturnCode() != null && !request.getReturnCode().isBlank()) {
            builder.returnCode(request.getReturnCode());
        }

        return builder.build();
    }

    @Override
    public void updateEntity(ReturnToVendor entity, UpdateReturnToVendorInput request) {
        if (entity == null || request == null) {
            return;
        }

        if (request.getReturnDate() != null) entity.setReturnDate(request.getReturnDate());
        if (request.getReturnReason() != null) entity.setReturnReason(request.getReturnReason());
        if (request.getSupplierResponse() != null) entity.setSupplierResponse(request.getSupplierResponse());
        if (request.getRejectionSummary() != null) entity.setRejectionSummary(request.getRejectionSummary());
        if (request.getCreditNoteReference() != null) entity.setCreditNoteReference(request.getCreditNoteReference());
        if (request.getCreditNoteAmount() != null) entity.setCreditNoteAmount(request.getCreditNoteAmount());
        if (request.getReplacementPurchaseOrderReference() != null) {
            entity.setReplacementPurchaseOrderReference(request.getReplacementPurchaseOrderReference());
        }
        if (request.getReplacementPurchaseOrderCode() != null) {
            entity.setReplacementPurchaseOrderCode(request.getReplacementPurchaseOrderCode());
        }
        if (request.getNotes() != null) entity.setNotes(request.getNotes());
        if (request.getInternalNotes() != null) entity.setInternalNotes(request.getInternalNotes());
        if (request.getLines() != null && !request.getLines().isEmpty()) {
            entity.setLines(toLineEntities(request.getLines()));
        }
        if (request.getUserId() != null) {
            entity.updateAudit(request.getUserId());
        }
    }

    @Override
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

    public void updateReturnCode(ReturnToVendor entity, String returnCode) {
        if (entity != null && returnCode != null && !returnCode.isBlank()) {
            entity.setReturnCode(ReturnCode.of(returnCode));
        }
    }

    public ReturnToVendorLine toLineEntity(ReturnToVendorLineInput line) {
        if (line == null) {
            return null;
        }

        return ReturnToVendorLine.builder()
                .id(line.getId())
                .lineNumber(line.getLineNumber())
                .goodsReceiptLineId(line.getGoodsReceiptLineId())
                .materialCode(line.getMaterialCode())
                .materialName(line.getMaterialName())
                .unitOfMeasure(line.getUnitOfMeasure())
                .rejectedQuantity(line.getRejectedQuantity())
                .quantityToReturn(line.getQuantityToReturn())
                .quantityAlreadyReturned(line.getQuantityAlreadyReturned())
                .rejectionReason(line.getRejectionReason())
                .qualityNotes(line.getQualityNotes())
                .defectDescription(line.getDefectDescription())
                .isReplaced(line.isReplaced())
                .isCreditNote(line.isCreditNote())
                .notes(line.getNotes())
                .createdBy(line.getUserId())
                .build();
    }

    public ReturnToVendorLineOutput toLineOutput(ReturnToVendorLine line) {
        if (line == null) {
            return null;
        }

        ReturnToVendorLineOutput output = new ReturnToVendorLineOutput();
        output.setId(line.getId());
        output.setLineNumber(line.getLineNumber());
        output.setGoodsReceiptLineId(line.getGoodsReceiptLineId());
        output.setMaterialCode(line.getMaterialCode());
        output.setMaterialName(line.getMaterialName());
        output.setUnitOfMeasure(line.getUnitOfMeasure());
        output.setRejectedQuantity(line.getRejectedQuantity());
        output.setQuantityToReturn(line.getQuantityToReturn());
        output.setQuantityAlreadyReturned(line.getQuantityAlreadyReturned());
        output.setRemainingQuantity(line.getRemainingQuantity());
        output.setRejectionReason(line.getRejectionReason());
        output.setQualityNotes(line.getQualityNotes());
        output.setDefectDescription(line.getDefectDescription());
        output.setReplaced(line.isReplaced());
        output.setCreditNote(line.isCreditNote());
        output.setNotes(line.getNotes());
        return output;
    }

    public List<ReturnToVendorLine> toLineEntities(List<ReturnToVendorLineInput> lines) {
        if (lines == null) {
            return new ArrayList<>();
        }
        return lines.stream()
                .map(this::toLineEntity)
                .filter(line -> line != null)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private List<ReturnToVendorLineOutput> toLineOutputs(List<ReturnToVendorLine> lines) {
        if (lines == null) {
            return new ArrayList<>();
        }
        return lines.stream()
                .map(this::toLineOutput)
                .filter(line -> line != null)
                .collect(Collectors.toCollection(ArrayList::new));
    }
}
