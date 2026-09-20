package com.materia.backend.contexts.purchaseOrder.application.mappers;

import com.materia.backend.common.application.BaseMapper;
import com.materia.backend.common.domain.enums.CurrencyCode;
import com.materia.backend.contexts.purchaseOrder.application.dtos.CreatePurchaseOrderInput;
import com.materia.backend.contexts.purchaseOrder.application.dtos.PurchaseOrderLineInput;
import com.materia.backend.contexts.purchaseOrder.application.dtos.PurchaseOrderLineOutput;
import com.materia.backend.contexts.purchaseOrder.application.dtos.PurchaseOrderOutput;
import com.materia.backend.contexts.purchaseOrder.application.dtos.UpdatePurchaseOrderInput;
import com.materia.backend.contexts.purchaseOrder.domain.entities.PurchaseOrder;
import com.materia.backend.contexts.purchaseOrder.domain.entities.PurchaseOrderLine;
import com.materia.backend.contexts.purchaseOrder.domain.enums.DeliveryStatus;
import com.materia.backend.contexts.purchaseOrder.domain.enums.OrderStatus;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper for purchase orders.
 */
@Component
public class PurchaseOrderMapper implements BaseMapper<PurchaseOrder, CreatePurchaseOrderInput, UpdatePurchaseOrderInput, PurchaseOrderOutput> {

    @Override
    public PurchaseOrder toEntity(CreatePurchaseOrderInput request) {
        if (request == null) {
            return null;
        }

        return PurchaseOrder.builder()
                .requisitionId(request.getRequisitionId())
                .requisitionCode(request.getRequisitionCode())
                .supplierId(request.getSupplierId())
                .supplierName(request.getSupplierName())
                .supplierCode(request.getSupplierCode())
                .orderDate(request.getOrderDate())
                .expectedDeliveryDate(request.getExpectedDeliveryDate())
                .paymentTerms(request.getPaymentTerms())
                .paymentDelayDays(request.getPaymentDelayDays())
                .deliveryTerms(request.getDeliveryTerms())
                .incoterm(request.getIncoterm())
                .currencyCode(request.getCurrencyCode())
                .taxAmount(request.getTaxAmount())
                .shippingCost(request.getShippingCost())
                .orderedBy(request.getOrderedBy())
                .orderedByName(request.getOrderedByName())
                .approvedBy(request.getApprovedBy())
                .approvedByName(request.getApprovedByName())
                .notes(request.getNotes())
                .internalNotes(request.getInternalNotes())
                .lines(toLineEntities(request.getLines()))
                .createdBy(request.getUserId())
                .build();
    }

    @Override
    public void updateEntity(PurchaseOrder entity, UpdatePurchaseOrderInput request) {
        if (entity == null || request == null) {
            return;
        }

        if (request.getRequisitionId() != null) {
            entity.setRequisitionId(request.getRequisitionId());
        }
        if (request.getRequisitionCode() != null) {
            entity.setRequisitionCode(request.getRequisitionCode());
        }
        if (request.getSupplierId() != null) {
            entity.setSupplierId(request.getSupplierId());
        }
        if (request.getSupplierName() != null) {
            entity.setSupplierName(request.getSupplierName());
        }
        if (request.getSupplierCode() != null) {
            entity.setSupplierCode(request.getSupplierCode());
        }
        if (request.getOrderDate() != null) {
            entity.setOrderDate(request.getOrderDate());
        }
        if (request.getExpectedDeliveryDate() != null) {
            entity.setExpectedDeliveryDate(request.getExpectedDeliveryDate());
        }
        if (request.getPaymentTerms() != null) {
            entity.setPaymentTerms(request.getPaymentTerms());
        }
        if (request.getPaymentDelayDays() != null) {
            entity.setPaymentDelayDays(request.getPaymentDelayDays());
        }
        if (request.getDeliveryTerms() != null) {
            entity.setDeliveryTerms(request.getDeliveryTerms());
        }
        if (request.getIncoterm() != null) {
            entity.setIncoterm(request.getIncoterm());
        }
        if (request.getCurrencyCode() != null) {
            entity.setCurrencyCode(request.getCurrencyCode());
        }
        if (request.getTaxAmount() != null) {
            entity.setTaxAmount(request.getTaxAmount());
        }
        if (request.getShippingCost() != null) {
            entity.setShippingCost(request.getShippingCost());
        }
        if (request.getOrderedBy() != null) {
            entity.setOrderedBy(request.getOrderedBy());
        }
        if (request.getOrderedByName() != null) {
            entity.setOrderedByName(request.getOrderedByName());
        }
        if (request.getApprovedBy() != null) {
            entity.setApprovedBy(request.getApprovedBy());
        }
        if (request.getApprovedByName() != null) {
            entity.setApprovedByName(request.getApprovedByName());
        }
        if (request.getNotes() != null) {
            entity.setNotes(request.getNotes());
        }
        if (request.getInternalNotes() != null) {
            entity.setInternalNotes(request.getInternalNotes());
        }
        if (request.getLines() != null && !request.getLines().isEmpty()) {
            entity.setLines(toLineEntities(request.getLines()));
            entity.recalculateTotals();
        }
        if (request.getUserId() != null) {
            entity.setUpdatedBy(request.getUserId());
        }
    }

    @Override
    public PurchaseOrderOutput toResponse(PurchaseOrder entity) {
        if (entity == null) {
            return null;
        }

        PurchaseOrderOutput response = new PurchaseOrderOutput();
        response.setId(entity.getId());
        response.setOrderCode(entity.getOrderCode() != null ? entity.getOrderCode().getValue() : null);
        response.setRequisitionId(entity.getRequisitionId());
        response.setRequisitionCode(entity.getRequisitionCode());
        response.setStatus(entity.getStatus() != null ? entity.getStatus().name() : null);
        response.setDeliveryStatus(entity.getDeliveryStatus() != null ? entity.getDeliveryStatus().name() : null);
        response.setSupplierId(entity.getSupplierId());
        response.setSupplierName(entity.getSupplierName());
        response.setSupplierCode(entity.getSupplierCode());
        response.setOrderDate(entity.getOrderDate());
        response.setExpectedDeliveryDate(entity.getExpectedDeliveryDate());
        response.setConfirmedDeliveryDate(entity.getConfirmedDeliveryDate());
        response.setReceivedDate(entity.getReceivedDate());
        response.setPaymentTerms(entity.getPaymentTerms());
        response.setPaymentDelayDays(entity.getPaymentDelayDays());
        response.setDeliveryTerms(entity.getDeliveryTerms());
        response.setIncoterm(entity.getIncoterm());
        response.setCurrencyCode(entity.getCurrencyCode());
        response.setTotalAmount(entity.getTotalAmount());
        response.setTaxAmount(entity.getTaxAmount());
        response.setShippingCost(entity.getShippingCost());
        response.setGrandTotal(entity.getGrandTotal());
        response.setOrderedBy(entity.getOrderedBy());
        response.setOrderedByName(entity.getOrderedByName());
        response.setApprovedBy(entity.getApprovedBy());
        response.setApprovedByName(entity.getApprovedByName());
        response.setAssignedTo(entity.getAssignedTo());
        response.setAssignedToName(entity.getAssignedToName());
        response.setAssignedAt(entity.getAssignedAt());
        response.setAssignedBy(entity.getAssignedBy());
        response.setAssignedByName(entity.getAssignedByName());
        response.setNotes(entity.getNotes());
        response.setInternalNotes(entity.getInternalNotes());
        response.setCreatedBy(entity.getCreatedBy());
        response.setCreatedAt(entity.getCreatedAt());
        response.setUpdatedBy(entity.getUpdatedBy());
        response.setUpdatedAt(entity.getUpdatedAt());
        response.setLines(toLineOutputs(entity.getLines()));
        return response;
    }

    public OrderStatus toOrderStatus(String status) {
        return status != null ? OrderStatus.fromCode(status) : null;
    }

    public DeliveryStatus toDeliveryStatus(String status) {
        return status != null ? DeliveryStatus.fromCode(status) : null;
    }

    private List<PurchaseOrderLine> toLineEntities(List<PurchaseOrderLineInput> lines) {
        if (lines == null) {
            return new ArrayList<>();
        }

        return lines.stream()
                .map(this::toLineEntity)
                .filter(line -> line != null)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private PurchaseOrderLine toLineEntity(PurchaseOrderLineInput line) {
        if (line == null) {
            return null;
        }

        return PurchaseOrderLine.builder()
                .id(line.getId())
                .lineNumber(line.getLineNumber())
                .requisitionLineId(line.getRequisitionLineId())
                .materialCode(line.getMaterialCode())
                .materialId(line.getMaterialId())
                .materialName(line.getMaterialName())
                .materialDescription(line.getMaterialDescription())
                .unitOfMeasure(line.getUnitOfMeasure())
                .quantity(line.getQuantity())
                .unitPrice(line.getUnitPrice())
                .currencyCode(resolveLineCurrencyCode(line))
                .supplierId(line.getSupplierId())
                .supplierName(line.getSupplierName())
                .expectedDeliveryDate(line.getExpectedDeliveryDate())
                .notes(line.getNotes())
                .build();
    }

    private String resolveLineCurrencyCode(PurchaseOrderLineInput line) {
        if (line.getCurrencyCode() != null && !line.getCurrencyCode().isBlank()) {
            return line.getCurrencyCode().trim().toUpperCase();
        }
        if (line.getUnitPrice() != null) {
            return line.getUnitPrice().getCurrencyCode();
        }
        return CurrencyCode.MAD.getCode();
    }

    private List<PurchaseOrderLineOutput> toLineOutputs(List<PurchaseOrderLine> lines) {
        if (lines == null) {
            return new ArrayList<>();
        }

        return lines.stream()
                .map(this::toLineOutput)
                .filter(line -> line != null)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private PurchaseOrderLineOutput toLineOutput(PurchaseOrderLine line) {
        if (line == null) {
            return null;
        }

        PurchaseOrderLineOutput output = new PurchaseOrderLineOutput();
        output.setId(line.getId());
        output.setLineNumber(line.getLineNumber());
        output.setRequisitionLineId(line.getRequisitionLineId());
        output.setMaterialCode(line.getMaterialCode());
        output.setMaterialId(line.getMaterialId());
        output.setMaterialName(line.getMaterialName());
        output.setMaterialDescription(line.getMaterialDescription());
        output.setUnitOfMeasure(line.getUnitOfMeasure());
        output.setQuantity(line.getQuantity());
        output.setUnitPrice(line.getUnitPrice());
        output.setLineTotal(line.getLineTotal());
        output.setCurrencyCode(line.getCurrencyCode());
        output.setSupplierId(line.getSupplierId());
        output.setSupplierName(line.getSupplierName());
        output.setExpectedDeliveryDate(line.getExpectedDeliveryDate());
        output.setNotes(line.getNotes());
        return output;
    }
}
