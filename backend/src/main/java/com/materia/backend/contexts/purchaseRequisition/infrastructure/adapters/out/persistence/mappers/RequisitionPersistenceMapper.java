package com.materia.backend.contexts.purchaseRequisition.infrastructure.adapters.out.persistence.mappers;

import com.materia.backend.common.domain.enums.CurrencyCode;
import com.materia.backend.common.domain.valueObjects.Money;
import com.materia.backend.contexts.purchaseRequisition.domain.entities.Requisition;
import com.materia.backend.contexts.purchaseRequisition.domain.entities.RequisitionLine;
import com.materia.backend.contexts.purchaseRequisition.domain.valueObjects.RequisitionCode;
import com.materia.backend.contexts.purchaseRequisition.infrastructure.adapters.out.persistence.entities.RequisitionJpaEntity;
import com.materia.backend.contexts.purchaseRequisition.infrastructure.adapters.out.persistence.entities.RequisitionLineJpaEntity;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper between requisition domain entities and JPA entities.
 */
@Component
public class RequisitionPersistenceMapper {

    public RequisitionJpaEntity toJpaEntity(Requisition domain) {
        if (domain == null) {
            return null;
        }

        RequisitionJpaEntity jpa = new RequisitionJpaEntity();
        jpa.setId(domain.getId());
        jpa.setRequisitionCode(domain.getRequisitionCode() != null ? domain.getRequisitionCode().getValue() : null);
        jpa.setTitle(domain.getTitle());
        jpa.setDescription(domain.getDescription());
        jpa.setJustification(domain.getJustification());
        jpa.setStatus(domain.getStatus());
        jpa.setRequesterId(domain.getRequesterId());
        jpa.setRequesterName(domain.getRequesterName());
        jpa.setRequiredDate(domain.getRequiredDate());
        jpa.setSubmittedDate(domain.getSubmittedDate());
        jpa.setApprovedDate(domain.getApprovedDate());
        jpa.setConvertedDate(domain.getConvertedDate());
        jpa.setCancelledDate(domain.getCancelledDate());
        jpa.setTotalAmount(domain.getTotalAmount() != null ? domain.getTotalAmount().getAmount() : null);
        jpa.setCurrencyCode(domain.getCurrencyCode());
        jpa.setApproverId(domain.getApproverId());
        jpa.setApproverName(domain.getApproverName());
        jpa.setRejectionReason(domain.getRejectionReason());
        jpa.setApprovalNotes(domain.getApprovalNotes());
        jpa.setCancellationReason(domain.getCancellationReason());
        jpa.setPurchaseOrderId(domain.getPurchaseOrderId());
        jpa.setPurchaseOrderCode(domain.getPurchaseOrderCode());
        jpa.setCreatedAt(domain.getCreatedAt());
        jpa.setUpdatedAt(domain.getUpdatedAt());
        jpa.setVersion(domain.getVersion());
        jpa.setCreatedBy(domain.getCreatedBy());
        jpa.setUpdatedBy(domain.getUpdatedBy());

        List<RequisitionLineJpaEntity> lineEntities = toLineJpaEntities(domain.getLines(), jpa);
        jpa.setLines(lineEntities);

        return jpa;
    }

    public Requisition toDomainEntity(RequisitionJpaEntity jpa) {
        if (jpa == null) {
            return null;
        }

        Requisition domain = new Requisition();
        domain.setId(jpa.getId());
        if (jpa.getRequisitionCode() != null) {
            domain.setRequisitionCode(RequisitionCode.of(jpa.getRequisitionCode()));
        }
        domain.setTitle(jpa.getTitle());
        domain.setDescription(jpa.getDescription());
        domain.setJustification(jpa.getJustification());
        domain.setStatus(jpa.getStatus());
        domain.setRequesterId(jpa.getRequesterId());
        domain.setRequesterName(jpa.getRequesterName());
        domain.setRequiredDate(jpa.getRequiredDate());
        domain.setSubmittedDate(jpa.getSubmittedDate());
        domain.setApprovedDate(jpa.getApprovedDate());
        domain.setConvertedDate(jpa.getConvertedDate());
        domain.setCancelledDate(jpa.getCancelledDate());
        domain.setTotalAmount(toMoney(jpa.getTotalAmount(), jpa.getCurrencyCode()));
        domain.setCurrencyCode(jpa.getCurrencyCode());
        domain.setApproverId(jpa.getApproverId());
        domain.setApproverName(jpa.getApproverName());
        domain.setRejectionReason(jpa.getRejectionReason());
        domain.setApprovalNotes(jpa.getApprovalNotes());
        domain.setCancellationReason(jpa.getCancellationReason());
        domain.setPurchaseOrderId(jpa.getPurchaseOrderId());
        domain.setPurchaseOrderCode(jpa.getPurchaseOrderCode());
        domain.setLines(toDomainLines(jpa.getLines()));
        domain.setCreatedAt(jpa.getCreatedAt());
        domain.setUpdatedAt(jpa.getUpdatedAt());
        domain.setVersion(jpa.getVersion());
        domain.setCreatedBy(jpa.getCreatedBy());
        domain.setUpdatedBy(jpa.getUpdatedBy());
        return domain;
    }

    private List<RequisitionLineJpaEntity> toLineJpaEntities(List<RequisitionLine> lines, RequisitionJpaEntity parent) {
        if (lines == null) {
            return new ArrayList<>();
        }

        return lines.stream()
                .map(line -> toLineJpaEntity(line, parent))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private RequisitionLineJpaEntity toLineJpaEntity(RequisitionLine domain, RequisitionJpaEntity parent) {
        RequisitionLineJpaEntity jpa = new RequisitionLineJpaEntity();
        jpa.setId(domain.getId());
        jpa.setRequisition(parent);
        jpa.setLineNumber(domain.getLineNumber());
        jpa.setMaterialCode(domain.getMaterialCode());
        jpa.setMaterialId(domain.getMaterialId());
        jpa.setMaterialName(domain.getMaterialName());
        jpa.setMaterialDescription(domain.getMaterialDescription());
        jpa.setUnitOfMeasure(domain.getUnitOfMeasure());
        jpa.setStandardPrice(domain.getStandardPrice() != null ? domain.getStandardPrice().getAmount() : null);
        jpa.setUnitPrice(domain.getUnitPrice() != null ? domain.getUnitPrice().getAmount() : null);
        jpa.setCurrencyCode(domain.getCurrencyCode());
        jpa.setQuantity(domain.getQuantity());
        jpa.setQuantityReceived(domain.getQuantityReceived());
        jpa.setQuantityRejected(domain.getQuantityRejected());
        jpa.setRequiredDate(domain.getRequiredDate());
        jpa.setLineTotal(domain.getLineTotal() != null ? domain.getLineTotal().getAmount() : null);
        jpa.setCurrencyCodeLine(domain.getCurrencyCodeLine());
        jpa.setSupplierId(domain.getSupplierId());
        jpa.setSupplierName(domain.getSupplierName());
        jpa.setSupplierCode(domain.getSupplierCode());
        jpa.setNotes(domain.getNotes());
        jpa.setDeliveryTerms(domain.getDeliveryTerms());
        jpa.setStorageLocation(domain.getStorageLocation());
        jpa.setBatchNumber(domain.getBatchNumber());
        jpa.setExpiryDate(domain.getExpiryDate());
        return jpa;
    }

    private List<RequisitionLine> toDomainLines(List<RequisitionLineJpaEntity> lines) {
        if (lines == null) {
            return new ArrayList<>();
        }

        return lines.stream()
                .map(this::toDomainLine)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private RequisitionLine toDomainLine(RequisitionLineJpaEntity jpa) {
        RequisitionLine domain = new RequisitionLine();
        domain.setId(jpa.getId());
        domain.setLineNumber(jpa.getLineNumber());
        domain.setMaterialCode(jpa.getMaterialCode());
        domain.setMaterialId(jpa.getMaterialId());
        domain.setMaterialName(jpa.getMaterialName());
        domain.setMaterialDescription(jpa.getMaterialDescription());
        domain.setUnitOfMeasure(jpa.getUnitOfMeasure());
        domain.setStandardPrice(toMoney(jpa.getStandardPrice(), jpa.getCurrencyCode()));
        domain.setUnitPrice(toMoney(jpa.getUnitPrice(), jpa.getCurrencyCode()));
        domain.setCurrencyCode(jpa.getCurrencyCode());
        domain.setQuantity(jpa.getQuantity());
        domain.setQuantityReceived(jpa.getQuantityReceived());
        domain.setQuantityRejected(jpa.getQuantityRejected());
        domain.setRequiredDate(jpa.getRequiredDate());
        domain.setLineTotal(toMoney(jpa.getLineTotal(), jpa.getCurrencyCodeLine()));
        domain.setCurrencyCodeLine(jpa.getCurrencyCodeLine());
        domain.setSupplierId(jpa.getSupplierId());
        domain.setSupplierName(jpa.getSupplierName());
        domain.setSupplierCode(jpa.getSupplierCode());
        domain.setNotes(jpa.getNotes());
        domain.setDeliveryTerms(jpa.getDeliveryTerms());
        domain.setStorageLocation(jpa.getStorageLocation());
        domain.setBatchNumber(jpa.getBatchNumber());
        domain.setExpiryDate(jpa.getExpiryDate());
        return domain;
    }

    public void updateJpaEntity(RequisitionJpaEntity jpa, Requisition domain) {
        if (jpa == null || domain == null) {
            return;
        }
        jpa.setTitle(domain.getTitle());
        jpa.setDescription(domain.getDescription());
        jpa.setJustification(domain.getJustification());
        jpa.setStatus(domain.getStatus());
        jpa.setRequesterId(domain.getRequesterId());
        jpa.setRequesterName(domain.getRequesterName());
        jpa.setRequiredDate(domain.getRequiredDate());
        jpa.setSubmittedDate(domain.getSubmittedDate());
        jpa.setApprovedDate(domain.getApprovedDate());
        jpa.setConvertedDate(domain.getConvertedDate());
        jpa.setCancelledDate(domain.getCancelledDate());
        jpa.setTotalAmount(domain.getTotalAmount() != null ? domain.getTotalAmount().getAmount() : null);
        jpa.setCurrencyCode(domain.getCurrencyCode());
        jpa.setApproverId(domain.getApproverId());
        jpa.setApproverName(domain.getApproverName());
        jpa.setRejectionReason(domain.getRejectionReason());
        jpa.setApprovalNotes(domain.getApprovalNotes());
        jpa.setCancellationReason(domain.getCancellationReason());
        jpa.setPurchaseOrderId(domain.getPurchaseOrderId());
        jpa.setPurchaseOrderCode(domain.getPurchaseOrderCode());
        jpa.setUpdatedAt(domain.getUpdatedAt() != null ? domain.getUpdatedAt() : java.time.LocalDateTime.now());
        jpa.setUpdatedBy(domain.getUpdatedBy());
    }

    private Money toMoney(BigDecimal amount, String currencyCode) {
        if (amount == null) {
            return null;
        }

        CurrencyCode currency = currencyCode != null && !currencyCode.isBlank()
                ? CurrencyCode.fromCode(currencyCode)
                : CurrencyCode.MAD;
        return Money.of(amount, currency);
    }
}
