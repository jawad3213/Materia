package com.materia.backend.contexts.invoice.application.mappers;

import com.materia.backend.common.application.BaseMapper;
import com.materia.backend.contexts.invoice.application.dtos.CreateInvoiceInput;
import com.materia.backend.contexts.invoice.application.dtos.InvoiceLineInput;
import com.materia.backend.contexts.invoice.application.dtos.InvoiceLineOutput;
import com.materia.backend.contexts.invoice.application.dtos.InvoiceOutput;
import com.materia.backend.contexts.invoice.application.dtos.UpdateInvoiceInput;
import com.materia.backend.contexts.invoice.domain.entities.Invoice;
import com.materia.backend.contexts.invoice.domain.entities.InvoiceLine;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class InvoiceMapper implements BaseMapper<Invoice, CreateInvoiceInput, UpdateInvoiceInput, InvoiceOutput> {

    @Override
    public Invoice toEntity(CreateInvoiceInput request) {
        if (request == null) return null;
        
        return Invoice.builder()
                .purchaseOrderId(request.getPurchaseOrderId())
                .purchaseOrderCode(request.getPurchaseOrderCode())
                .goodsReceiptId(request.getGoodsReceiptId())
                .goodsReceiptCode(request.getGoodsReceiptCode())
                .supplierId(request.getSupplierId())
                .supplierName(request.getSupplierName())
                .supplierCode(request.getSupplierCode())
                .invoiceType(request.getInvoiceType())
                .externalReference(request.getExternalReference())
                .invoiceDate(request.getInvoiceDate())
                .dueDate(request.getDueDate())
                .currencyCode(request.getCurrencyCode())
                .notes(request.getNotes())
                .internalNotes(request.getInternalNotes())
                .lines(toLineEntities(request.getLines()))
                .createdBy(request.getUserId())
                .build();
    }
    
    @Override
    public void updateEntity(Invoice entity, UpdateInvoiceInput request) {
        if (entity == null || request == null) return;
        
        if (request.getPurchaseOrderId() != null) entity.setPurchaseOrderId(request.getPurchaseOrderId());
        if (request.getPurchaseOrderCode() != null) entity.setPurchaseOrderCode(request.getPurchaseOrderCode());
        if (request.getGoodsReceiptId() != null) entity.setGoodsReceiptId(request.getGoodsReceiptId());
        if (request.getGoodsReceiptCode() != null) entity.setGoodsReceiptCode(request.getGoodsReceiptCode());
        if (request.getSupplierId() != null) entity.setSupplierId(request.getSupplierId());
        if (request.getSupplierName() != null) entity.setSupplierName(request.getSupplierName());
        if (request.getSupplierCode() != null) entity.setSupplierCode(request.getSupplierCode());
        if (request.getInvoiceType() != null) entity.setInvoiceType(request.getInvoiceType());
        if (request.getExternalReference() != null) entity.setExternalReference(request.getExternalReference());
        if (request.getInvoiceDate() != null) entity.setInvoiceDate(request.getInvoiceDate());
        if (request.getDueDate() != null) entity.setDueDate(request.getDueDate());
        if (request.getCurrencyCode() != null) entity.setCurrencyCode(request.getCurrencyCode());
        if (request.getNotes() != null) entity.setNotes(request.getNotes());
        if (request.getInternalNotes() != null) entity.setInternalNotes(request.getInternalNotes());
        
        if (request.getLines() != null && !request.getLines().isEmpty()) {
            entity.setLines(toLineEntities(request.getLines()));
        }
        
        if (request.getUserId() != null) {
            entity.setUpdatedBy(request.getUserId());
        }
    }
    
    @Override
    public InvoiceOutput toResponse(Invoice entity) {
        if (entity == null) return null;
        
        InvoiceOutput response = new InvoiceOutput();
        response.setId(entity.getId());
        response.setInvoiceCode(entity.getInvoiceCode() != null ? entity.getInvoiceCode().getValue() : null);
        response.setPurchaseOrderId(entity.getPurchaseOrderId());
        response.setPurchaseOrderCode(entity.getPurchaseOrderCode());
        response.setGoodsReceiptId(entity.getGoodsReceiptId());
        response.setGoodsReceiptCode(entity.getGoodsReceiptCode());
        response.setSupplierId(entity.getSupplierId());
        response.setSupplierName(entity.getSupplierName());
        response.setSupplierCode(entity.getSupplierCode());
        response.setInvoiceType(entity.getInvoiceType());
        response.setStatus(entity.getStatus());
        response.setExternalReference(entity.getExternalReference());
        response.setInvoiceDate(entity.getInvoiceDate());
        response.setDueDate(entity.getDueDate());
        response.setReceivedDate(entity.getReceivedDate());
        response.setPaymentDate(entity.getPaymentDate());
        response.setTotalAmount(entity.getTotalAmount());
        response.setTotalTaxAmount(entity.getTotalTaxAmount());
        response.setTotalAmountWithTax(entity.getTotalAmountWithTax());
        response.setCurrencyCode(entity.getCurrencyCode());
        response.setVerified(entity.isVerified());
        response.setHasDiscrepancy(entity.hasDiscrepancy());
        response.setDiscrepancySummary(entity.getDiscrepancySummary());
        response.setVerificationDate(entity.getVerificationDate());
        response.setVerifiedBy(entity.getVerifiedBy());
        response.setVerifiedByName(entity.getVerifiedByName());
        response.setPaidAmount(entity.getPaidAmount());
        response.setPaidAt(entity.getPaidAt());
        response.setPaidBy(entity.getPaidBy());
        response.setPaidByName(entity.getPaidByName());
        response.setNotes(entity.getNotes());
        response.setInternalNotes(entity.getInternalNotes());
        
        response.setLines(toLineOutputs(entity.getLines()));
        return response;
    }
    
    private List<InvoiceLine> toLineEntities(List<InvoiceLineInput> lines) {
        if (lines == null) return new ArrayList<>();
        return lines.stream().map(this::toLineEntity).collect(Collectors.toList());
    }
    
    private InvoiceLine toLineEntity(InvoiceLineInput line) {
        if (line == null) return null;
        return InvoiceLine.builder()
                .purchaseOrderLineId(line.getPurchaseOrderLineId())
                .goodsReceiptLineId(line.getGoodsReceiptLineId())
                .materialCode(line.getMaterialCode())
                .materialName(line.getMaterialName())
                .unitOfMeasure(line.getUnitOfMeasure())
                .quantityInvoiced(line.getQuantityInvoiced())
                .unitPrice(line.getUnitPrice())
                .taxAmount(line.getTaxAmount())
                .notes(line.getNotes())
                .build();
    }
    
    private List<InvoiceLineOutput> toLineOutputs(List<InvoiceLine> lines) {
        if (lines == null) return new ArrayList<>();
        return lines.stream().map(this::toLineOutput).collect(Collectors.toList());
    }
    
    private InvoiceLineOutput toLineOutput(InvoiceLine line) {
        if (line == null) return null;
        InvoiceLineOutput output = new InvoiceLineOutput();
        output.setId(line.getId());
        output.setLineNumber(line.getLineNumber());
        output.setPurchaseOrderLineId(line.getPurchaseOrderLineId());
        output.setGoodsReceiptLineId(line.getGoodsReceiptLineId());
        output.setMaterialCode(line.getMaterialCode());
        output.setMaterialName(line.getMaterialName());
        output.setUnitOfMeasure(line.getUnitOfMeasure());
        output.setQuantityOrdered(line.getQuantityOrdered());
        output.setQuantityReceived(line.getQuantityReceived());
        output.setQuantityInvoiced(line.getQuantityInvoiced());
        output.setQuantityDiscrepancy(line.getQuantityDiscrepancy());
        output.setUnitPrice(line.getUnitPrice());
        output.setLineTotal(line.getLineTotal());
        output.setTaxAmount(line.getTaxAmount());
        output.setLineTotalWithTax(line.getLineTotalWithTax());
        output.setCurrencyCode(line.getCurrencyCode());
        output.setHasQuantityDiscrepancy(line.isHasQuantityDiscrepancy());
        output.setDiscrepancyNotes(line.getDiscrepancyNotes());
        output.setNotes(line.getNotes());
        return output;
    }
}
