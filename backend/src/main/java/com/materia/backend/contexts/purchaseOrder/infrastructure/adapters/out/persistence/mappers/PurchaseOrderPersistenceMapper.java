package com.materia.backend.contexts.purchaseOrder.infrastructure.adapters.out.persistence.mappers;

import com.materia.backend.common.domain.enums.CurrencyCode;
import com.materia.backend.common.domain.valueObjects.Money;
import com.materia.backend.contexts.purchaseOrder.domain.entities.PurchaseOrder;
import com.materia.backend.contexts.purchaseOrder.domain.entities.PurchaseOrderLine;
import com.materia.backend.contexts.purchaseOrder.domain.valueObjects.OrderCode;
import com.materia.backend.contexts.purchaseOrder.infrastructure.adapters.out.persistence.entities.PurchaseOrderJpaEntity;
import com.materia.backend.contexts.purchaseOrder.infrastructure.adapters.out.persistence.entities.PurchaseOrderLineJpaEntity;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper between purchase order domain entities and JPA entities.
 */
@Component
public class PurchaseOrderPersistenceMapper {

    public PurchaseOrderJpaEntity toJpaEntity(PurchaseOrder domain) {
        if (domain == null) {
            return null;
        }

        PurchaseOrderJpaEntity jpa = new PurchaseOrderJpaEntity();
        jpa.setId(domain.getId());
        jpa.setOrderCode(domain.getOrderCode() != null ? domain.getOrderCode().getValue() : null);
        jpa.setRequisitionId(domain.getRequisitionId());
        jpa.setRequisitionCode(domain.getRequisitionCode());
        jpa.setStatus(domain.getStatus());
        jpa.setDeliveryStatus(domain.getDeliveryStatus());
        jpa.setSupplierId(domain.getSupplierId());
        jpa.setSupplierName(domain.getSupplierName());
        jpa.setSupplierCode(domain.getSupplierCode());
        jpa.setOrderDate(domain.getOrderDate());
        jpa.setExpectedDeliveryDate(domain.getExpectedDeliveryDate());
        jpa.setConfirmedDeliveryDate(domain.getConfirmedDeliveryDate());
        jpa.setReceivedDate(domain.getReceivedDate());
        jpa.setPaymentTerms(domain.getPaymentTerms());
        jpa.setPaymentDelayDays(domain.getPaymentDelayDays());
        jpa.setDeliveryTerms(domain.getDeliveryTerms());
        jpa.setIncoterm(domain.getIncoterm());
        jpa.setCurrencyCode(domain.getCurrencyCode());
        jpa.setTotalAmount(toAmount(domain.getTotalAmount()));
        jpa.setTaxAmount(toAmount(domain.getTaxAmount()));
        jpa.setShippingCost(toAmount(domain.getShippingCost()));
        jpa.setGrandTotal(toAmount(domain.getGrandTotal()));
        jpa.setOrderedBy(domain.getOrderedBy());
        jpa.setOrderedByName(domain.getOrderedByName());
        jpa.setApprovedBy(domain.getApprovedBy());
        jpa.setApprovedByName(domain.getApprovedByName());
        jpa.setAssignedTo(domain.getAssignedTo());
        jpa.setAssignedToName(domain.getAssignedToName());
        jpa.setAssignedAt(domain.getAssignedAt());
        jpa.setAssignedBy(domain.getAssignedBy());
        jpa.setAssignedByName(domain.getAssignedByName());
        jpa.setNotes(domain.getNotes());
        jpa.setInternalNotes(domain.getInternalNotes());
        jpa.setObsoletedAt(domain.getObsoletedAt());
        jpa.setObsoletedBy(domain.getObsoletedBy());
        jpa.setObsoletedReason(domain.getObsoletedReason());
        jpa.setCreatedAt(domain.getCreatedAt());
        jpa.setUpdatedAt(domain.getUpdatedAt());
        jpa.setVersion(domain.getVersion());
        jpa.setCreatedBy(domain.getCreatedBy());
        jpa.setUpdatedBy(domain.getUpdatedBy());
        jpa.setLines(toLineJpaEntities(domain.getLines(), jpa));
        return jpa;
    }

    public PurchaseOrder toDomainEntity(PurchaseOrderJpaEntity jpa) {
        if (jpa == null) {
            return null;
        }

        PurchaseOrder domain = new PurchaseOrder();
        domain.setId(jpa.getId());
        if (jpa.getOrderCode() != null) {
            domain.setOrderCode(OrderCode.of(jpa.getOrderCode()));
        }
        domain.setRequisitionId(jpa.getRequisitionId());
        domain.setRequisitionCode(jpa.getRequisitionCode());
        domain.setStatus(jpa.getStatus());
        domain.setDeliveryStatus(jpa.getDeliveryStatus());
        domain.setSupplierId(jpa.getSupplierId());
        domain.setSupplierName(jpa.getSupplierName());
        domain.setSupplierCode(jpa.getSupplierCode());
        domain.setOrderDate(jpa.getOrderDate());
        domain.setExpectedDeliveryDate(jpa.getExpectedDeliveryDate());
        domain.setConfirmedDeliveryDate(jpa.getConfirmedDeliveryDate());
        domain.setReceivedDate(jpa.getReceivedDate());
        domain.setPaymentTerms(jpa.getPaymentTerms());
        domain.setPaymentDelayDays(jpa.getPaymentDelayDays());
        domain.setDeliveryTerms(jpa.getDeliveryTerms());
        domain.setIncoterm(jpa.getIncoterm());
        domain.setCurrencyCode(jpa.getCurrencyCode());
        domain.setTotalAmount(toMoney(jpa.getTotalAmount(), jpa.getCurrencyCode()));
        domain.setTaxAmount(toMoney(jpa.getTaxAmount(), jpa.getCurrencyCode()));
        domain.setShippingCost(toMoney(jpa.getShippingCost(), jpa.getCurrencyCode()));
        domain.setGrandTotal(toMoney(jpa.getGrandTotal(), jpa.getCurrencyCode()));
        domain.setOrderedBy(jpa.getOrderedBy());
        domain.setOrderedByName(jpa.getOrderedByName());
        domain.setApprovedBy(jpa.getApprovedBy());
        domain.setApprovedByName(jpa.getApprovedByName());
        domain.setAssignedTo(jpa.getAssignedTo());
        domain.setAssignedToName(jpa.getAssignedToName());
        domain.setAssignedAt(jpa.getAssignedAt());
        domain.setAssignedBy(jpa.getAssignedBy());
        domain.setAssignedByName(jpa.getAssignedByName());
        domain.setNotes(jpa.getNotes());
        domain.setInternalNotes(jpa.getInternalNotes());
        domain.setObsoletedAt(jpa.getObsoletedAt());
        domain.setObsoletedBy(jpa.getObsoletedBy());
        domain.setObsoletedReason(jpa.getObsoletedReason());
        domain.setLines(toDomainLines(jpa.getLines(), jpa.getCurrencyCode()));
        domain.setCreatedAt(jpa.getCreatedAt());
        domain.setUpdatedAt(jpa.getUpdatedAt());
        domain.setVersion(jpa.getVersion());
        domain.setCreatedBy(jpa.getCreatedBy());
        domain.setUpdatedBy(jpa.getUpdatedBy());
        return domain;
    }

    private List<PurchaseOrderLineJpaEntity> toLineJpaEntities(List<PurchaseOrderLine> lines, PurchaseOrderJpaEntity parent) {
        if (lines == null) {
            return new ArrayList<>();
        }

        return lines.stream()
                .map(line -> toLineJpaEntity(line, parent))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private PurchaseOrderLineJpaEntity toLineJpaEntity(PurchaseOrderLine domain, PurchaseOrderJpaEntity parent) {
        PurchaseOrderLineJpaEntity jpa = new PurchaseOrderLineJpaEntity();
        jpa.setId(domain.getId());
        jpa.setPurchaseOrder(parent);
        jpa.setLineNumber(domain.getLineNumber());
        jpa.setRequisitionLineId(domain.getRequisitionLineId());
        jpa.setMaterialCode(domain.getMaterialCode());
        jpa.setMaterialId(domain.getMaterialId());
        jpa.setMaterialName(domain.getMaterialName());
        jpa.setMaterialDescription(domain.getMaterialDescription());
        jpa.setUnitOfMeasure(domain.getUnitOfMeasure());
        jpa.setQuantity(domain.getQuantity());
        jpa.setUnitPrice(toAmount(domain.getUnitPrice()));
        jpa.setLineTotal(toAmount(domain.getLineTotal()));
        jpa.setCurrencyCode(domain.getCurrencyCode());
        jpa.setSupplierId(domain.getSupplierId());
        jpa.setSupplierName(domain.getSupplierName());
        jpa.setExpectedDeliveryDate(domain.getExpectedDeliveryDate());
        jpa.setNotes(domain.getNotes());
        return jpa;
    }

    private List<PurchaseOrderLine> toDomainLines(List<PurchaseOrderLineJpaEntity> lines, String defaultCurrencyCode) {
        if (lines == null) {
            return new ArrayList<>();
        }

        return lines.stream()
                .map(line -> toDomainLine(line, defaultCurrencyCode))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private PurchaseOrderLine toDomainLine(PurchaseOrderLineJpaEntity jpa, String defaultCurrencyCode) {
        PurchaseOrderLine domain = new PurchaseOrderLine();
        domain.setId(jpa.getId());
        domain.setLineNumber(jpa.getLineNumber());
        domain.setRequisitionLineId(jpa.getRequisitionLineId());
        domain.setMaterialCode(jpa.getMaterialCode());
        domain.setMaterialId(jpa.getMaterialId());
        domain.setMaterialName(jpa.getMaterialName());
        domain.setMaterialDescription(jpa.getMaterialDescription());
        domain.setUnitOfMeasure(jpa.getUnitOfMeasure());
        domain.setQuantity(jpa.getQuantity());
        String resolvedCurrencyCode = resolveCurrencyCode(jpa.getCurrencyCode(), defaultCurrencyCode);
        domain.setUnitPrice(toMoney(jpa.getUnitPrice(), resolvedCurrencyCode));
        domain.setLineTotal(toMoney(jpa.getLineTotal(), resolvedCurrencyCode));
        domain.setCurrencyCode(resolvedCurrencyCode);
        domain.setSupplierId(jpa.getSupplierId());
        domain.setSupplierName(jpa.getSupplierName());
        domain.setExpectedDeliveryDate(jpa.getExpectedDeliveryDate());
        domain.setNotes(jpa.getNotes());
        return domain;
    }

    private BigDecimal toAmount(Money money) {
        return money != null ? money.getAmount() : null;
    }

    private Money toMoney(BigDecimal amount, String currencyCode) {
        if (amount == null) {
            return null;
        }

        CurrencyCode currency = CurrencyCode.fromCode(resolveCurrencyCode(currencyCode, CurrencyCode.MAD.getCode()));
        return Money.of(amount, currency);
    }

    private String resolveCurrencyCode(String currencyCode, String fallbackCurrencyCode) {
        if (currencyCode != null && !currencyCode.isBlank()) {
            return currencyCode.trim().toUpperCase();
        }
        return fallbackCurrencyCode != null && !fallbackCurrencyCode.isBlank()
                ? fallbackCurrencyCode.trim().toUpperCase()
                : CurrencyCode.MAD.getCode();
    }
}
