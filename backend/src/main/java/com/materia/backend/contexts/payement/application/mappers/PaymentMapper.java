package com.materia.backend.contexts.payement.application.mappers;

import com.materia.backend.common.application.BaseMapper;
import com.materia.backend.contexts.masterData.domain.valueObjects.Money;
import com.materia.backend.contexts.payement.application.dtos.CreatePaymentInput;
import com.materia.backend.contexts.payement.application.dtos.CreatePaymentLineInput;
import com.materia.backend.contexts.payement.application.dtos.PaymentLineOutput;
import com.materia.backend.contexts.payement.application.dtos.PaymentOutput;
import com.materia.backend.contexts.payement.application.dtos.UpdatePaymentInput;
import com.materia.backend.contexts.payement.domain.entities.Payment;
import com.materia.backend.contexts.payement.domain.entities.PaymentLine;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class PaymentMapper implements BaseMapper<Payment, CreatePaymentInput, UpdatePaymentInput, PaymentOutput> {

    @Override
    public Payment toEntity(CreatePaymentInput request) {
        if (request == null) return null;

        String currencyCode = request.getCurrencyCode() != null ? request.getCurrencyCode() : "MAD";
        com.materia.backend.contexts.masterData.domain.enums.CurrencyCode currencyEnum = com.materia.backend.contexts.masterData.domain.enums.CurrencyCode.valueOf(currencyCode);
        Money totalAmount = request.getTotalAmount() != null ? Money.of(request.getTotalAmount(), currencyEnum) : null;

        return Payment.builder()
                .supplierId(request.getSupplierId())
                .supplierName(request.getSupplierName())
                .supplierCode(request.getSupplierCode())
                .totalAmount(totalAmount)
                .currencyCode(currencyCode)
                .notes(request.getNotes())
                .internalNotes(request.getInternalNotes())
                .lines(toLineEntities(request.getLines(), currencyCode))
                .createdBy(request.getUserId())
                .build();
    }

    @Override
    public void updateEntity(Payment entity, UpdatePaymentInput request) {
        if (entity == null || request == null) return;

        if (request.getNotes() != null) entity.setNotes(request.getNotes());
        if (request.getInternalNotes() != null) entity.setInternalNotes(request.getInternalNotes());

        if (request.getUserId() != null) {
            entity.setUpdatedBy(request.getUserId());
        }
    }

    @Override
    public PaymentOutput toResponse(Payment entity) {
        if (entity == null) return null;

        PaymentOutput response = new PaymentOutput();
        
        response.setId(entity.getId());
        response.setPaymentCode(entity.getPaymentCode() != null ? entity.getPaymentCode().getValue() : null);
        response.setSupplierId(entity.getSupplierId());
        response.setSupplierName(entity.getSupplierName());
        response.setSupplierCode(entity.getSupplierCode());
        response.setStatus(entity.getStatus());
        
        response.setTotalAmount(entity.getTotalAmount() != null ? entity.getTotalAmount().getAmount() : null);
        response.setPaidAmount(entity.getPaidAmount() != null ? entity.getPaidAmount().getAmount() : null);
        response.setCurrencyCode(entity.getCurrencyCode());
        
        response.setPaymentDate(entity.getPaymentDate());
        response.setConfirmedDate(entity.getConfirmedDate());
        
        response.setBankReference(entity.getBankReference());
        response.setTransactionId(entity.getTransactionId());
        response.setPaymentMethod(entity.getPaymentMethod());
        response.setPaymentReceipt(entity.getPaymentReceipt());
        
        response.setNotes(entity.getNotes());
        response.setInternalNotes(entity.getInternalNotes());
        
        response.setCreatedAt(entity.getCreatedAt());
        response.setCreatedBy(entity.getCreatedBy());
        response.setUpdatedAt(entity.getUpdatedAt());
        response.setUpdatedBy(entity.getUpdatedBy());
        
        response.setLines(toLineOutputs(entity.getLines()));
        
        return response;
    }

    private List<PaymentLine> toLineEntities(List<CreatePaymentLineInput> lines, String currencyCode) {
        if (lines == null) return new ArrayList<>();
        return lines.stream().map(line -> toLineEntity(line, currencyCode)).collect(Collectors.toList());
    }

    private PaymentLine toLineEntity(CreatePaymentLineInput line, String currencyCode) {
        if (line == null) return null;
        
        com.materia.backend.contexts.masterData.domain.enums.CurrencyCode currencyEnum = com.materia.backend.contexts.masterData.domain.enums.CurrencyCode.valueOf(currencyCode);
        Money lineAmount = line.getAmount() != null ? Money.of(line.getAmount(), currencyEnum) : null;
        
        return PaymentLine.builder()
                .invoiceId(line.getInvoiceId())
                .invoiceCode(line.getInvoiceCode())
                .supplierId(line.getSupplierId())
                .supplierName(line.getSupplierName())
                .amount(lineAmount)
                .currencyCode(currencyCode)
                .notes(line.getNotes())
                .build();
    }

    private List<PaymentLineOutput> toLineOutputs(List<PaymentLine> lines) {
        if (lines == null) return new ArrayList<>();
        return lines.stream().map(this::toLineOutput).collect(Collectors.toList());
    }

    private PaymentLineOutput toLineOutput(PaymentLine line) {
        if (line == null) return null;
        
        PaymentLineOutput output = new PaymentLineOutput();
        output.setId(line.getId());
        output.setLineNumber(line.getLineNumber());
        output.setInvoiceId(line.getInvoiceId());
        output.setInvoiceCode(line.getInvoiceCode());
        output.setSupplierId(line.getSupplierId());
        output.setSupplierName(line.getSupplierName());
        
        output.setAmount(line.getAmount() != null ? line.getAmount().getAmount() : null);
        output.setPaidAmount(line.getPaidAmount() != null ? line.getPaidAmount().getAmount() : null);
        output.setCurrencyCode(line.getCurrencyCode());
        
        output.setPaid(line.isPaid());
        output.setNotes(line.getNotes());
        
        return output;
    }
}
