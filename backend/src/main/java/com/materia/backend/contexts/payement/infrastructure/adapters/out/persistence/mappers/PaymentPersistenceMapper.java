package com.materia.backend.contexts.payement.infrastructure.adapters.out.persistence.mappers;

import com.materia.backend.contexts.masterData.domain.enums.CurrencyCode;
import com.materia.backend.contexts.masterData.domain.valueObjects.Money;
import com.materia.backend.contexts.payement.domain.entities.Payment;
import com.materia.backend.contexts.payement.domain.entities.PaymentLine;
import com.materia.backend.contexts.payement.domain.valueObjects.PaymentCode;
import com.materia.backend.contexts.payement.infrastructure.adapters.out.persistence.entities.PaymentJpaEntity;
import com.materia.backend.contexts.payement.infrastructure.adapters.out.persistence.entities.PaymentLineJpaEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class PaymentPersistenceMapper {

    public PaymentJpaEntity toJpaEntity(Payment domainEntity) {
        if (domainEntity == null) return null;

        PaymentJpaEntity entity = new PaymentJpaEntity();
        
        entity.setId(domainEntity.getId());
        entity.setCreatedAt(domainEntity.getCreatedAt());
        entity.setCreatedBy(domainEntity.getCreatedBy());
        entity.setUpdatedAt(domainEntity.getUpdatedAt());
        entity.setUpdatedBy(domainEntity.getUpdatedBy());
        entity.setVersion(domainEntity.getVersion());

        entity.setPaymentCode(domainEntity.getPaymentCode() != null ? domainEntity.getPaymentCode().getValue() : null);
        entity.setSupplierId(domainEntity.getSupplierId());
        entity.setSupplierName(domainEntity.getSupplierName());
        entity.setSupplierCode(domainEntity.getSupplierCode());
        entity.setStatus(domainEntity.getStatus());
        
        entity.setTotalAmount(domainEntity.getTotalAmount() != null ? domainEntity.getTotalAmount().getAmount() : null);
        entity.setPaidAmount(domainEntity.getPaidAmount() != null ? domainEntity.getPaidAmount().getAmount() : null);
        entity.setCurrencyCode(domainEntity.getCurrencyCode());
        
        entity.setPaymentDate(domainEntity.getPaymentDate());
        entity.setConfirmedDate(domainEntity.getConfirmedDate());
        
        entity.setBankReference(domainEntity.getBankReference());
        entity.setTransactionId(domainEntity.getTransactionId());
        entity.setPaymentMethod(domainEntity.getPaymentMethod());
        entity.setPaymentReceipt(domainEntity.getPaymentReceipt());
        
        entity.setNotes(domainEntity.getNotes());
        entity.setInternalNotes(domainEntity.getInternalNotes());

        if (domainEntity.getLines() != null) {
            entity.setLines(domainEntity.getLines().stream()
                    .map(this::toLineJpaEntity)
                    .collect(Collectors.toList()));
        }

        return entity;
    }

    private PaymentLineJpaEntity toLineJpaEntity(PaymentLine domainEntity) {
        if (domainEntity == null) return null;

        PaymentLineJpaEntity entity = new PaymentLineJpaEntity();
        
        entity.setId(domainEntity.getId());
        entity.setCreatedAt(domainEntity.getCreatedAt());
        entity.setCreatedBy(domainEntity.getCreatedBy());
        entity.setUpdatedAt(domainEntity.getUpdatedAt());
        entity.setUpdatedBy(domainEntity.getUpdatedBy());
        entity.setVersion(domainEntity.getVersion());

        entity.setLineNumber(domainEntity.getLineNumber());
        entity.setInvoiceId(domainEntity.getInvoiceId());
        entity.setInvoiceCode(domainEntity.getInvoiceCode());
        entity.setSupplierId(domainEntity.getSupplierId());
        entity.setSupplierName(domainEntity.getSupplierName());
        
        entity.setAmount(domainEntity.getAmount() != null ? domainEntity.getAmount().getAmount() : null);
        entity.setPaidAmount(domainEntity.getPaidAmount() != null ? domainEntity.getPaidAmount().getAmount() : null);
        entity.setCurrencyCode(domainEntity.getCurrencyCode());
        
        entity.setPaid(domainEntity.isPaid());
        entity.setNotes(domainEntity.getNotes());

        return entity;
    }

    public Payment toDomain(PaymentJpaEntity jpaEntity) {
        if (jpaEntity == null) return null;

        CurrencyCode currencyCodeEnum = jpaEntity.getCurrencyCode() != null 
                ? CurrencyCode.valueOf(jpaEntity.getCurrencyCode()) 
                : CurrencyCode.MAD;
                
        Money totalAmount = jpaEntity.getTotalAmount() != null ? Money.of(jpaEntity.getTotalAmount(), currencyCodeEnum) : null;
        Money paidAmount = jpaEntity.getPaidAmount() != null ? Money.of(jpaEntity.getPaidAmount(), currencyCodeEnum) : null;

        Payment payment = Payment.builder()
                .id(jpaEntity.getId())
                .paymentCode(jpaEntity.getPaymentCode() != null ? PaymentCode.of(jpaEntity.getPaymentCode()) : null)
                .supplierId(jpaEntity.getSupplierId())
                .supplierName(jpaEntity.getSupplierName())
                .supplierCode(jpaEntity.getSupplierCode())
                .status(jpaEntity.getStatus())
                .totalAmount(totalAmount)
                .paidAmount(paidAmount)
                .currencyCode(jpaEntity.getCurrencyCode())
                .paymentDate(jpaEntity.getPaymentDate())
                .confirmedDate(jpaEntity.getConfirmedDate())
                .bankReference(jpaEntity.getBankReference())
                .transactionId(jpaEntity.getTransactionId())
                .paymentMethod(jpaEntity.getPaymentMethod())
                .paymentReceipt(jpaEntity.getPaymentReceipt())
                .notes(jpaEntity.getNotes())
                .internalNotes(jpaEntity.getInternalNotes())
                .lines(toLineDomainList(jpaEntity.getLines()))
                .build();
                
        payment.setCreatedAt(jpaEntity.getCreatedAt());
        payment.setCreatedBy(jpaEntity.getCreatedBy());
        payment.setUpdatedAt(jpaEntity.getUpdatedAt());
        payment.setUpdatedBy(jpaEntity.getUpdatedBy());
        payment.setVersion(jpaEntity.getVersion());

        return payment;
    }

    private List<PaymentLine> toLineDomainList(List<PaymentLineJpaEntity> jpaEntities) {
        if (jpaEntities == null) return new ArrayList<>();
        return jpaEntities.stream().map(this::toLineDomain).collect(Collectors.toList());
    }

    private PaymentLine toLineDomain(PaymentLineJpaEntity jpaEntity) {
        if (jpaEntity == null) return null;
        
        CurrencyCode currencyCodeEnum = jpaEntity.getCurrencyCode() != null 
                ? CurrencyCode.valueOf(jpaEntity.getCurrencyCode()) 
                : CurrencyCode.MAD;
                
        Money amount = jpaEntity.getAmount() != null ? Money.of(jpaEntity.getAmount(), currencyCodeEnum) : null;
        Money paidAmount = jpaEntity.getPaidAmount() != null ? Money.of(jpaEntity.getPaidAmount(), currencyCodeEnum) : null;

        PaymentLine line = PaymentLine.builder()
                .id(jpaEntity.getId())
                .lineNumber(jpaEntity.getLineNumber())
                .invoiceId(jpaEntity.getInvoiceId())
                .invoiceCode(jpaEntity.getInvoiceCode())
                .supplierId(jpaEntity.getSupplierId())
                .supplierName(jpaEntity.getSupplierName())
                .amount(amount)
                .paidAmount(paidAmount)
                .currencyCode(jpaEntity.getCurrencyCode())
                .isPaid(jpaEntity.isPaid())
                .notes(jpaEntity.getNotes())
                .build();
                
        line.setCreatedAt(jpaEntity.getCreatedAt());
        line.setCreatedBy(jpaEntity.getCreatedBy());
        line.setUpdatedAt(jpaEntity.getUpdatedAt());
        line.setUpdatedBy(jpaEntity.getUpdatedBy());
        line.setVersion(jpaEntity.getVersion());
        
        return line;
    }
}
