package com.materia.backend.contexts.goodsReceipt.infrastructure.adapters.out.persistence.mappers;

import com.materia.backend.contexts.goodsReceipt.domain.entities.GoodsReceipt;
import com.materia.backend.contexts.goodsReceipt.domain.entities.GoodsReceiptLine;
import com.materia.backend.contexts.goodsReceipt.domain.valueObjects.ReceiptCode;
import com.materia.backend.contexts.goodsReceipt.infrastructure.adapters.out.persistence.entities.GoodsReceiptJpaEntity;
import com.materia.backend.contexts.goodsReceipt.infrastructure.adapters.out.persistence.entities.GoodsReceiptLineJpaEntity;
import com.materia.backend.contexts.masterData.domain.enums.CurrencyCode;
import com.materia.backend.contexts.masterData.domain.valueObjects.Money;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class GoodsReceiptPersistenceMapper {

    public GoodsReceiptJpaEntity toJpaEntity(GoodsReceipt domain) {
        if (domain == null) {
            return null;
        }

        GoodsReceiptJpaEntity jpa = new GoodsReceiptJpaEntity();
        jpa.setId(domain.getId());
        jpa.setReceiptCode(domain.getReceiptCode() != null ? domain.getReceiptCode().getValue() : null);
        jpa.setPurchaseOrderId(domain.getPurchaseOrderId());
        jpa.setPurchaseOrderCode(domain.getPurchaseOrderCode());
        jpa.setStatus(domain.getStatus());
        jpa.setReceiptDate(domain.getReceiptDate());
        jpa.setExpectedDeliveryDate(domain.getExpectedDeliveryDate());
        jpa.setReceivedBy(domain.getReceivedBy());
        jpa.setReceivedByName(domain.getReceivedByName());
        jpa.setNotes(domain.getNotes());
        jpa.setSupplierId(domain.getSupplierId());
        jpa.setSupplierName(domain.getSupplierName());
        jpa.setTotalQuantityOrdered(domain.getTotalQuantityOrdered());
        jpa.setTotalQuantityReceived(domain.getTotalQuantityReceived());
        jpa.setTotalQuantityRejected(domain.getTotalQuantityRejected());
        jpa.setTotalQuantityAccepted(domain.getTotalQuantityAccepted());
        jpa.setHasDiscrepancy(domain.isHasDiscrepancy());
        jpa.setDiscrepancyNotes(domain.getDiscrepancyNotes());
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

    public GoodsReceipt toDomainEntity(GoodsReceiptJpaEntity jpa) {
        if (jpa == null) {
            return null;
        }

        GoodsReceipt domain = new GoodsReceipt();
        domain.setId(jpa.getId());
        if (jpa.getReceiptCode() != null) {
            domain.setReceiptCode(ReceiptCode.of(jpa.getReceiptCode()));
        }
        domain.setPurchaseOrderId(jpa.getPurchaseOrderId());
        domain.setPurchaseOrderCode(jpa.getPurchaseOrderCode());
        domain.setStatus(jpa.getStatus());
        domain.setReceiptDate(jpa.getReceiptDate());
        domain.setExpectedDeliveryDate(jpa.getExpectedDeliveryDate());
        domain.setReceivedBy(jpa.getReceivedBy());
        domain.setReceivedByName(jpa.getReceivedByName());
        domain.setNotes(jpa.getNotes());
        domain.setSupplierId(jpa.getSupplierId());
        domain.setSupplierName(jpa.getSupplierName());
        domain.setTotalQuantityOrdered(jpa.getTotalQuantityOrdered());
        domain.setTotalQuantityReceived(jpa.getTotalQuantityReceived());
        domain.setTotalQuantityRejected(jpa.getTotalQuantityRejected());
        domain.setTotalQuantityAccepted(jpa.getTotalQuantityAccepted());
        domain.setHasDiscrepancy(jpa.isHasDiscrepancy());
        domain.setDiscrepancyNotes(jpa.getDiscrepancyNotes());
        domain.setObsoletedAt(jpa.getObsoletedAt());
        domain.setObsoletedBy(jpa.getObsoletedBy());
        domain.setObsoletedReason(jpa.getObsoletedReason());
        domain.setLines(toDomainLines(jpa.getLines()));
        domain.setCreatedAt(jpa.getCreatedAt());
        domain.setUpdatedAt(jpa.getUpdatedAt());
        domain.setVersion(jpa.getVersion());
        domain.setCreatedBy(jpa.getCreatedBy());
        domain.setUpdatedBy(jpa.getUpdatedBy());
        return domain;
    }

    private List<GoodsReceiptLineJpaEntity> toLineJpaEntities(List<GoodsReceiptLine> lines, GoodsReceiptJpaEntity parent) {
        if (lines == null) {
            return new ArrayList<>();
        }

        return lines.stream()
                .map(line -> toLineJpaEntity(line, parent))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private GoodsReceiptLineJpaEntity toLineJpaEntity(GoodsReceiptLine domain, GoodsReceiptJpaEntity parent) {
        GoodsReceiptLineJpaEntity jpa = new GoodsReceiptLineJpaEntity();
        jpa.setId(domain.getId());
        jpa.setGoodsReceipt(parent);
        jpa.setLineNumber(domain.getLineNumber());
        jpa.setPurchaseOrderLineId(domain.getPurchaseOrderLineId());
        jpa.setMaterialCode(domain.getMaterialCode());
        jpa.setMaterialId(domain.getMaterialId());
        jpa.setMaterialName(domain.getMaterialName());
        jpa.setUnitOfMeasure(domain.getUnitOfMeasure());
        jpa.setQuantityOrdered(domain.getQuantityOrdered());
        jpa.setQuantityReceived(domain.getQuantityReceived());
        jpa.setQuantityRejected(domain.getQuantityRejected());
        jpa.setQuantityAccepted(domain.getQuantityAccepted());
        jpa.setQuantityPending(domain.getQuantityPending());
        jpa.setQualityStatus(domain.getQualityStatus());
        jpa.setQualityNotes(domain.getQualityNotes());
        jpa.setRejectionReason(domain.getRejectionReason());
        jpa.setStockBefore(domain.getStockBefore());
        jpa.setStockAfter(domain.getStockAfter());
        jpa.setUnitPrice(toAmount(domain.getUnitPrice()));
        jpa.setLineTotal(toAmount(resolveLineTotal(domain)));
        jpa.setCurrencyCode(resolveCurrencyCode(domain.getUnitPrice(), domain.getLineTotal()));
        jpa.setSupplierId(domain.getSupplierId());
        jpa.setSupplierName(domain.getSupplierName());
        jpa.setBatchNumber(domain.getBatchNumber());
        jpa.setExpiryDate(domain.getExpiryDate());
        jpa.setStorageLocation(domain.getStorageLocation());
        jpa.setNotes(domain.getNotes());
        return jpa;
    }

    private List<GoodsReceiptLine> toDomainLines(List<GoodsReceiptLineJpaEntity> lines) {
        if (lines == null) {
            return new ArrayList<>();
        }

        return lines.stream()
                .map(this::toDomainLine)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private GoodsReceiptLine toDomainLine(GoodsReceiptLineJpaEntity jpa) {
        GoodsReceiptLine domain = new GoodsReceiptLine();
        domain.setId(jpa.getId());
        domain.setLineNumber(jpa.getLineNumber());
        domain.setPurchaseOrderLineId(jpa.getPurchaseOrderLineId());
        domain.setMaterialCode(jpa.getMaterialCode());
        domain.setMaterialId(jpa.getMaterialId());
        domain.setMaterialName(jpa.getMaterialName());
        domain.setUnitOfMeasure(jpa.getUnitOfMeasure());
        domain.setQuantityOrdered(jpa.getQuantityOrdered());
        domain.setQuantityReceived(jpa.getQuantityReceived());
        domain.setQuantityRejected(jpa.getQuantityRejected());
        domain.setQuantityAccepted(jpa.getQuantityAccepted());
        domain.setQuantityPending(jpa.getQuantityPending());
        domain.setQualityStatus(jpa.getQualityStatus());
        domain.setQualityNotes(jpa.getQualityNotes());
        domain.setRejectionReason(jpa.getRejectionReason());
        domain.setStockBefore(jpa.getStockBefore());
        domain.setStockAfter(jpa.getStockAfter());
        domain.setUnitPrice(toMoney(jpa.getUnitPrice(), jpa.getCurrencyCode()));
        domain.setLineTotal(resolveDomainLineTotal(jpa));
        domain.setSupplierId(jpa.getSupplierId());
        domain.setSupplierName(jpa.getSupplierName());
        domain.setBatchNumber(jpa.getBatchNumber());
        domain.setExpiryDate(jpa.getExpiryDate());
        domain.setStorageLocation(jpa.getStorageLocation());
        domain.setNotes(jpa.getNotes());
        return domain;
    }

    private Money resolveDomainLineTotal(GoodsReceiptLineJpaEntity jpa) {
        Money explicit = toMoney(jpa.getLineTotal(), jpa.getCurrencyCode());
        if (explicit != null) {
            return explicit;
        }

        Money unitPrice = toMoney(jpa.getUnitPrice(), jpa.getCurrencyCode());
        if (unitPrice == null || jpa.getQuantityReceived() == null) {
            return null;
        }

        return unitPrice.multiply(BigDecimal.valueOf(jpa.getQuantityReceived()));
    }

    private Money resolveLineTotal(GoodsReceiptLine line) {
        if (line.getLineTotal() != null) {
            return line.getLineTotal();
        }
        if (line.getUnitPrice() == null || line.getQuantityReceived() == null) {
            return null;
        }
        return line.getUnitPrice().multiply(BigDecimal.valueOf(line.getQuantityReceived()));
    }

    private BigDecimal toAmount(Money money) {
        return money != null ? money.getAmount() : null;
    }

    private Money toMoney(BigDecimal amount, String currencyCode) {
        if (amount == null) {
            return null;
        }

        CurrencyCode currency = CurrencyCode.fromCode(resolveCurrencyCode(currencyCode));
        return Money.of(amount, currency);
    }

    private String resolveCurrencyCode(Money primaryMoney, Money secondaryMoney) {
        if (primaryMoney != null && primaryMoney.getCurrency() != null) {
            return primaryMoney.getCurrency().getCode();
        }
        if (secondaryMoney != null && secondaryMoney.getCurrency() != null) {
            return secondaryMoney.getCurrency().getCode();
        }
        return CurrencyCode.MAD.getCode();
    }

    private String resolveCurrencyCode(String currencyCode) {
        if (currencyCode != null && !currencyCode.isBlank()) {
            return currencyCode.trim().toUpperCase();
        }
        return CurrencyCode.MAD.getCode();
    }
}
