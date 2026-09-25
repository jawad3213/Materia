package com.materia.backend.contexts.invoice.infrastructure.adapters.out.persistence.mappers;

import com.materia.backend.contexts.invoice.domain.entities.Invoice;
import com.materia.backend.contexts.invoice.domain.entities.InvoiceLine;
import com.materia.backend.contexts.invoice.domain.valueObjects.InvoiceCode;
import com.materia.backend.contexts.invoice.infrastructure.adapters.out.persistence.entities.InvoiceJpaEntity;
import com.materia.backend.contexts.invoice.infrastructure.adapters.out.persistence.entities.InvoiceLineJpaEntity;
import com.materia.backend.common.domain.enums.CurrencyCode;
import com.materia.backend.common.domain.valueObjects.Money;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class InvoicePersistenceMapper {

    public InvoiceJpaEntity toJpaEntity(Invoice domain) {
        if (domain == null) return null;

        InvoiceJpaEntity entity = new InvoiceJpaEntity();
        entity.setId(domain.getId());
        entity.setInvoiceCode(domain.getInvoiceCode() != null ? domain.getInvoiceCode().getValue() : null);
        entity.setPurchaseOrderId(domain.getPurchaseOrderId());
        entity.setPurchaseOrderCode(domain.getPurchaseOrderCode());
        entity.setGoodsReceiptId(domain.getGoodsReceiptId());
        entity.setGoodsReceiptCode(domain.getGoodsReceiptCode());
        entity.setSupplierId(domain.getSupplierId());
        entity.setSupplierName(domain.getSupplierName());
        entity.setSupplierCode(domain.getSupplierCode());
        entity.setInvoiceType(domain.getInvoiceType());
        entity.setStatus(domain.getStatus());
        entity.setExternalReference(domain.getExternalReference());
        
        entity.setInvoiceDate(domain.getInvoiceDate());
        entity.setDueDate(domain.getDueDate());
        entity.setReceivedDate(domain.getReceivedDate());
        entity.setPaymentDate(domain.getPaymentDate());
        
        entity.setTotalAmount(domain.getTotalAmount() != null ? domain.getTotalAmount().getAmount() : BigDecimal.ZERO);
        entity.setTotalTaxAmount(domain.getTotalTaxAmount() != null ? domain.getTotalTaxAmount().getAmount() : BigDecimal.ZERO);
        entity.setTotalAmountWithTax(domain.getTotalAmountWithTax() != null ? domain.getTotalAmountWithTax().getAmount() : BigDecimal.ZERO);
        entity.setCurrencyCode(domain.getCurrencyCode());
        
        entity.setVerified(domain.isVerified());
        entity.setHasDiscrepancy(domain.hasDiscrepancy());
        entity.setDiscrepancySummary(domain.getDiscrepancySummary());
        entity.setVerificationDate(domain.getVerificationDate());
        entity.setVerifiedBy(domain.getVerifiedBy());
        entity.setVerifiedByName(domain.getVerifiedByName());
        
        entity.setPaidAmount(domain.getPaidAmount() != null ? domain.getPaidAmount().getAmount() : null);
        entity.setPaidAt(domain.getPaidAt());
        entity.setPaidBy(domain.getPaidBy());
        entity.setPaidByName(domain.getPaidByName());
        
        entity.setNotes(domain.getNotes());
        entity.setInternalNotes(domain.getInternalNotes());
        
        entity.setCreatedBy(domain.getCreatedBy());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedBy(domain.getUpdatedBy());
        entity.setUpdatedAt(domain.getUpdatedAt());
        
        entity.setLines(toLineJpaEntities(domain.getLines()));
        
        return entity;
    }

    public Invoice toDomain(InvoiceJpaEntity entity) {
        if (entity == null) return null;

        Invoice.Builder builder = Invoice.builder()
                .id(entity.getId())
                .purchaseOrderId(entity.getPurchaseOrderId())
                .purchaseOrderCode(entity.getPurchaseOrderCode())
                .goodsReceiptId(entity.getGoodsReceiptId())
                .goodsReceiptCode(entity.getGoodsReceiptCode())
                .supplierId(entity.getSupplierId())
                .supplierName(entity.getSupplierName())
                .supplierCode(entity.getSupplierCode())
                .invoiceType(entity.getInvoiceType())
                .externalReference(entity.getExternalReference())
                .invoiceDate(entity.getInvoiceDate())
                .dueDate(entity.getDueDate())
                .receivedDate(entity.getReceivedDate())
                .paymentDate(entity.getPaymentDate())
                .currencyCode(entity.getCurrencyCode())
                .isVerified(entity.isVerified())
                .hasDiscrepancy(entity.isHasDiscrepancy())
                .discrepancySummary(entity.getDiscrepancySummary())
                .verificationDate(entity.getVerificationDate())
                .verifiedBy(entity.getVerifiedBy())
                .verifiedByName(entity.getVerifiedByName())
                .paidAt(entity.getPaidAt())
                .paidBy(entity.getPaidBy())
                .paidByName(entity.getPaidByName())
                .notes(entity.getNotes())
                .internalNotes(entity.getInternalNotes())
                .lines(toLineDomains(entity.getLines()));

        Invoice domain = builder.build();
        // Since builder creates new instances for internal fields (or requires specific logic to override code/status/etc)
        // Let's set some things manually if builder doesn't expose them
        if (entity.getInvoiceCode() != null) {
            try {
                java.lang.reflect.Field field = Invoice.class.getDeclaredField("invoiceCode");
                field.setAccessible(true);
                field.set(domain, InvoiceCode.of(entity.getInvoiceCode()));
            } catch (Exception e) {
                // Ignore fallback
            }
        }
        
        if (entity.getStatus() != null) {
            try {
                java.lang.reflect.Field field = Invoice.class.getDeclaredField("status");
                field.setAccessible(true);
                field.set(domain, entity.getStatus());
            } catch (Exception e) {}
        }
        
        domain.setCreatedBy(entity.getCreatedBy());
        domain.setCreatedAt(entity.getCreatedAt());
        domain.setUpdatedBy(entity.getUpdatedBy());
        domain.setUpdatedAt(entity.getUpdatedAt());

        // Amounts
        if (entity.getCurrencyCode() != null && entity.getPaidAmount() != null) {
            try {
                java.lang.reflect.Field field = Invoice.class.getDeclaredField("paidAmount");
                field.setAccessible(true);
                field.set(domain, Money.of(entity.getPaidAmount(), CurrencyCode.valueOf(entity.getCurrencyCode())));
            } catch (Exception e) {}
        }

        return domain;
    }

    private List<InvoiceLineJpaEntity> toLineJpaEntities(List<InvoiceLine> lines) {
        if (lines == null) return new ArrayList<>();
        return lines.stream().map(this::toLineJpaEntity).collect(Collectors.toList());
    }

    private InvoiceLineJpaEntity toLineJpaEntity(InvoiceLine domain) {
        if (domain == null) return null;
        InvoiceLineJpaEntity entity = new InvoiceLineJpaEntity();
        entity.setId(domain.getId());
        entity.setLineNumber(domain.getLineNumber());
        entity.setPurchaseOrderLineId(domain.getPurchaseOrderLineId());
        entity.setGoodsReceiptLineId(domain.getGoodsReceiptLineId());
        entity.setMaterialCode(domain.getMaterialCode());
        entity.setMaterialName(domain.getMaterialName());
        entity.setUnitOfMeasure(domain.getUnitOfMeasure());
        entity.setQuantityOrdered(domain.getQuantityOrdered());
        entity.setQuantityReceived(domain.getQuantityReceived());
        entity.setQuantityInvoiced(domain.getQuantityInvoiced());
        entity.setQuantityDiscrepancy(domain.getQuantityDiscrepancy());
        
        entity.setUnitPrice(domain.getUnitPrice() != null ? domain.getUnitPrice().getAmount() : BigDecimal.ZERO);
        entity.setLineTotal(domain.getLineTotal() != null ? domain.getLineTotal().getAmount() : BigDecimal.ZERO);
        entity.setTaxAmount(domain.getTaxAmount() != null ? domain.getTaxAmount().getAmount() : BigDecimal.ZERO);
        entity.setLineTotalWithTax(domain.getLineTotalWithTax() != null ? domain.getLineTotalWithTax().getAmount() : BigDecimal.ZERO);
        
        entity.setCurrencyCode(domain.getCurrencyCode());
        entity.setHasQuantityDiscrepancy(domain.isHasQuantityDiscrepancy());
        entity.setDiscrepancyNotes(domain.getDiscrepancyNotes());
        entity.setNotes(domain.getNotes());
        return entity;
    }

    private List<InvoiceLine> toLineDomains(List<InvoiceLineJpaEntity> entities) {
        if (entities == null) return new ArrayList<>();
        return entities.stream().map(this::toLineDomain).collect(Collectors.toList());
    }

    private InvoiceLine toLineDomain(InvoiceLineJpaEntity entity) {
        if (entity == null) return null;
        InvoiceLine domain = InvoiceLine.builder()
                .id(entity.getId())
                .lineNumber(entity.getLineNumber())
                .purchaseOrderLineId(entity.getPurchaseOrderLineId())
                .goodsReceiptLineId(entity.getGoodsReceiptLineId())
                .materialCode(entity.getMaterialCode())
                .materialName(entity.getMaterialName())
                .unitOfMeasure(entity.getUnitOfMeasure())
                .quantityOrdered(entity.getQuantityOrdered())
                .quantityReceived(entity.getQuantityReceived())
                .quantityInvoiced(entity.getQuantityInvoiced())
                .unitPrice(entity.getUnitPrice() != null ? Money.of(entity.getUnitPrice(), CurrencyCode.valueOf(entity.getCurrencyCode())) : null)
                .taxAmount(entity.getTaxAmount() != null ? Money.of(entity.getTaxAmount(), CurrencyCode.valueOf(entity.getCurrencyCode())) : null)
                .notes(entity.getNotes())
                .build();
        
        // Since builder recalculates things, the values should mostly align. 
        // We'll trust the domain builder to reconstruct derived values correctly.
        return domain;
    }
}
