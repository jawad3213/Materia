package com.materia.backend.contexts.returnToVendor.infrastructure.adapters.out.persistence.mappers;

import com.materia.backend.contexts.returnToVendor.domain.entities.ReturnToVendor;
import com.materia.backend.contexts.returnToVendor.domain.entities.ReturnToVendorLine;
import com.materia.backend.contexts.returnToVendor.domain.enums.ResolutionType;
import com.materia.backend.contexts.returnToVendor.domain.enums.ReturnStatus;
import com.materia.backend.contexts.returnToVendor.infrastructure.adapters.out.persistence.entities.ReturnToVendorJpaEntity;
import com.materia.backend.contexts.returnToVendor.infrastructure.adapters.out.persistence.entities.ReturnToVendorLineJpaEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ReturnToVendorPersistenceMapper {

    public ReturnToVendorJpaEntity toJpaEntity(ReturnToVendor domain) {
        if (domain == null) {
            return null;
        }
        
        ReturnToVendorJpaEntity jpa = new ReturnToVendorJpaEntity();
        jpa.setId(domain.getId());
        jpa.setCreatedAt(domain.getCreatedAt());
        jpa.setUpdatedAt(domain.getUpdatedAt());
        jpa.setCreatedBy(domain.getCreatedBy());
        jpa.setUpdatedBy(domain.getUpdatedBy());
        
        jpa.setReturnCode(domain.getReturnCode() != null ? domain.getReturnCode().getValue() : null);
        jpa.setGoodsReceiptId(domain.getGoodsReceiptId());
        jpa.setGoodsReceiptCode(domain.getGoodsReceiptCode());
        jpa.setPurchaseOrderId(domain.getPurchaseOrderId());
        jpa.setPurchaseOrderCode(domain.getPurchaseOrderCode());
        jpa.setSupplierId(domain.getSupplierId());
        jpa.setSupplierName(domain.getSupplierName());
        jpa.setSupplierCode(domain.getSupplierCode());
        jpa.setStatus(domain.getStatus() != null ? domain.getStatus().name() : null);
        jpa.setResolutionType(domain.getResolutionType() != null ? domain.getResolutionType().name() : null);
        jpa.setReturnDate(domain.getReturnDate());
        jpa.setResolutionDate(domain.getResolutionDate());
        jpa.setReturnReason(domain.getReturnReason());
        jpa.setSupplierResponse(domain.getSupplierResponse());
        jpa.setRejectionSummary(domain.getRejectionSummary());
        jpa.setCreditNoteReference(domain.getCreditNoteReference());
        jpa.setCreditNoteAmount(domain.getCreditNoteAmount());
        jpa.setReplacementPurchaseOrderReference(domain.getReplacementPurchaseOrderReference());
        jpa.setReplacementPurchaseOrderCode(domain.getReplacementPurchaseOrderCode());
        jpa.setNotes(domain.getNotes());
        jpa.setInternalNotes(domain.getInternalNotes());
        
        if (domain.getLines() != null) {
            List<ReturnToVendorLineJpaEntity> lineJpas = domain.getLines().stream()
                .map(this::toLineJpaEntity)
                .collect(Collectors.toList());
            lineJpas.forEach(l -> l.setReturnToVendor(jpa));
            jpa.setLines(lineJpas);
        } else {
            jpa.setLines(new ArrayList<>());
        }
        
        return jpa;
    }

    public ReturnToVendorLineJpaEntity toLineJpaEntity(ReturnToVendorLine domain) {
        if (domain == null) {
            return null;
        }
        
        ReturnToVendorLineJpaEntity jpa = new ReturnToVendorLineJpaEntity();
        jpa.setId(domain.getId());
        jpa.setCreatedAt(domain.getCreatedAt());
        jpa.setUpdatedAt(domain.getUpdatedAt());
        jpa.setCreatedBy(domain.getCreatedBy());
        jpa.setUpdatedBy(domain.getUpdatedBy());
        
        jpa.setLineNumber(domain.getLineNumber());
        jpa.setGoodsReceiptLineId(domain.getGoodsReceiptLineId());
        jpa.setMaterialCode(domain.getMaterialCode());
        jpa.setMaterialName(domain.getMaterialName());
        jpa.setUnitOfMeasure(domain.getUnitOfMeasure());
        jpa.setRejectedQuantity(domain.getRejectedQuantity());
        jpa.setQuantityToReturn(domain.getQuantityToReturn());
        jpa.setQuantityAlreadyReturned(domain.getQuantityAlreadyReturned());
        jpa.setRejectionReason(domain.getRejectionReason());
        jpa.setQualityNotes(domain.getQualityNotes());
        jpa.setDefectDescription(domain.getDefectDescription());
        jpa.setReplaced(domain.isReplaced());
        jpa.setCreditNote(domain.isCreditNote());
        jpa.setNotes(domain.getNotes());
        
        return jpa;
    }

    public ReturnToVendor toDomain(ReturnToVendorJpaEntity jpa) {
        if (jpa == null) {
            return null;
        }
        
        ReturnToVendor domain = ReturnToVendor.builder()
                .id(jpa.getId())
                .returnCode(jpa.getReturnCode())
                .goodsReceiptId(jpa.getGoodsReceiptId())
                .goodsReceiptCode(jpa.getGoodsReceiptCode())
                .purchaseOrderId(jpa.getPurchaseOrderId())
                .purchaseOrderCode(jpa.getPurchaseOrderCode())
                .supplierId(jpa.getSupplierId())
                .supplierName(jpa.getSupplierName())
                .supplierCode(jpa.getSupplierCode())
                .status(jpa.getStatus() != null ? ReturnStatus.valueOf(jpa.getStatus()) : null)
                .resolutionType(jpa.getResolutionType() != null ? ResolutionType.valueOf(jpa.getResolutionType()) : null)
                .returnDate(jpa.getReturnDate())
                .resolutionDate(jpa.getResolutionDate())
                .returnReason(jpa.getReturnReason())
                .supplierResponse(jpa.getSupplierResponse())
                .rejectionSummary(jpa.getRejectionSummary())
                .creditNoteReference(jpa.getCreditNoteReference())
                .creditNoteAmount(jpa.getCreditNoteAmount())
                .replacementPurchaseOrderReference(jpa.getReplacementPurchaseOrderReference())
                .replacementPurchaseOrderCode(jpa.getReplacementPurchaseOrderCode())
                .notes(jpa.getNotes())
                .internalNotes(jpa.getInternalNotes())
                .createdAt(jpa.getCreatedAt())
                .updatedAt(jpa.getUpdatedAt())
                .createdBy(jpa.getCreatedBy())
                .build();
        
        domain.setUpdatedBy(jpa.getUpdatedBy());
                
        if (jpa.getLines() != null) {
            domain.setLines(jpa.getLines().stream().map(this::toLineDomain).collect(Collectors.toList()));
        }
        return domain;
    }

    public ReturnToVendorLine toLineDomain(ReturnToVendorLineJpaEntity jpa) {
        if (jpa == null) {
            return null;
        }
        
        return ReturnToVendorLine.builder()
                .id(jpa.getId())
                .lineNumber(jpa.getLineNumber())
                .goodsReceiptLineId(jpa.getGoodsReceiptLineId())
                .materialCode(jpa.getMaterialCode())
                .materialName(jpa.getMaterialName())
                .unitOfMeasure(jpa.getUnitOfMeasure())
                .rejectedQuantity(jpa.getRejectedQuantity())
                .quantityToReturn(jpa.getQuantityToReturn())
                .quantityAlreadyReturned(jpa.getQuantityAlreadyReturned())
                .rejectionReason(jpa.getRejectionReason())
                .qualityNotes(jpa.getQualityNotes())
                .defectDescription(jpa.getDefectDescription())
                .isReplaced(jpa.isReplaced())
                .isCreditNote(jpa.isCreditNote())
                .notes(jpa.getNotes())
                .createdAt(jpa.getCreatedAt())
                .updatedAt(jpa.getUpdatedAt())
                .createdBy(jpa.getCreatedBy())
                .build();
    }
}
