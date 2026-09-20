package com.materia.backend.contexts.purchaseOrder.application.dtos;

import com.materia.backend.common.application.BaseOutput;
import com.materia.backend.common.domain.valueObjects.Money;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO for purchase order operations.
 */
public class PurchaseOrderOutput extends BaseOutput {

    private UUID id;
    private String orderCode;
    private UUID requisitionId;
    private String requisitionCode;
    private String status;
    private String deliveryStatus;
    private UUID supplierId;
    private String supplierName;
    private String supplierCode;
    private LocalDate orderDate;
    private LocalDate expectedDeliveryDate;
    private LocalDate confirmedDeliveryDate;
    private LocalDate receivedDate;
    private String paymentTerms;
    private Integer paymentDelayDays;
    private String deliveryTerms;
    private String incoterm;
    private String currencyCode;
    private Money totalAmount;
    private Money taxAmount;
    private Money shippingCost;
    private Money grandTotal;
    private String orderedBy;
    private String orderedByName;
    private String approvedBy;
    private String approvedByName;
    private String assignedTo;
    private String assignedToName;
    private LocalDateTime assignedAt;
    private String assignedBy;
    private String assignedByName;
    private String notes;
    private String internalNotes;
    private String createdBy;
    private LocalDateTime createdAt;
    private String updatedBy;
    private LocalDateTime updatedAt;
    private List<PurchaseOrderLineOutput> lines = new ArrayList<>();

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getOrderCode() {
        return orderCode;
    }

    public void setOrderCode(String orderCode) {
        this.orderCode = orderCode;
    }

    public UUID getRequisitionId() {
        return requisitionId;
    }

    public void setRequisitionId(UUID requisitionId) {
        this.requisitionId = requisitionId;
    }

    public String getRequisitionCode() {
        return requisitionCode;
    }

    public void setRequisitionCode(String requisitionCode) {
        this.requisitionCode = requisitionCode;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDeliveryStatus() {
        return deliveryStatus;
    }

    public void setDeliveryStatus(String deliveryStatus) {
        this.deliveryStatus = deliveryStatus;
    }

    public UUID getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(UUID supplierId) {
        this.supplierId = supplierId;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    public String getSupplierCode() {
        return supplierCode;
    }

    public void setSupplierCode(String supplierCode) {
        this.supplierCode = supplierCode;
    }

    public LocalDate getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDate orderDate) {
        this.orderDate = orderDate;
    }

    public LocalDate getExpectedDeliveryDate() {
        return expectedDeliveryDate;
    }

    public void setExpectedDeliveryDate(LocalDate expectedDeliveryDate) {
        this.expectedDeliveryDate = expectedDeliveryDate;
    }

    public LocalDate getConfirmedDeliveryDate() {
        return confirmedDeliveryDate;
    }

    public void setConfirmedDeliveryDate(LocalDate confirmedDeliveryDate) {
        this.confirmedDeliveryDate = confirmedDeliveryDate;
    }

    public LocalDate getReceivedDate() {
        return receivedDate;
    }

    public void setReceivedDate(LocalDate receivedDate) {
        this.receivedDate = receivedDate;
    }

    public String getPaymentTerms() {
        return paymentTerms;
    }

    public void setPaymentTerms(String paymentTerms) {
        this.paymentTerms = paymentTerms;
    }

    public Integer getPaymentDelayDays() {
        return paymentDelayDays;
    }

    public void setPaymentDelayDays(Integer paymentDelayDays) {
        this.paymentDelayDays = paymentDelayDays;
    }

    public String getDeliveryTerms() {
        return deliveryTerms;
    }

    public void setDeliveryTerms(String deliveryTerms) {
        this.deliveryTerms = deliveryTerms;
    }

    public String getIncoterm() {
        return incoterm;
    }

    public void setIncoterm(String incoterm) {
        this.incoterm = incoterm;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public Money getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Money totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Money getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(Money taxAmount) {
        this.taxAmount = taxAmount;
    }

    public Money getShippingCost() {
        return shippingCost;
    }

    public void setShippingCost(Money shippingCost) {
        this.shippingCost = shippingCost;
    }

    public Money getGrandTotal() {
        return grandTotal;
    }

    public void setGrandTotal(Money grandTotal) {
        this.grandTotal = grandTotal;
    }

    public String getOrderedBy() {
        return orderedBy;
    }

    public void setOrderedBy(String orderedBy) {
        this.orderedBy = orderedBy;
    }

    public String getOrderedByName() {
        return orderedByName;
    }

    public void setOrderedByName(String orderedByName) {
        this.orderedByName = orderedByName;
    }

    public String getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(String approvedBy) {
        this.approvedBy = approvedBy;
    }

    public String getApprovedByName() {
        return approvedByName;
    }

    public void setApprovedByName(String approvedByName) {
        this.approvedByName = approvedByName;
    }

    public String getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(String assignedTo) {
        this.assignedTo = assignedTo;
    }

    public String getAssignedToName() {
        return assignedToName;
    }

    public void setAssignedToName(String assignedToName) {
        this.assignedToName = assignedToName;
    }

    public LocalDateTime getAssignedAt() {
        return assignedAt;
    }

    public void setAssignedAt(LocalDateTime assignedAt) {
        this.assignedAt = assignedAt;
    }

    public String getAssignedBy() {
        return assignedBy;
    }

    public void setAssignedBy(String assignedBy) {
        this.assignedBy = assignedBy;
    }

    public String getAssignedByName() {
        return assignedByName;
    }

    public void setAssignedByName(String assignedByName) {
        this.assignedByName = assignedByName;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getInternalNotes() {
        return internalNotes;
    }

    public void setInternalNotes(String internalNotes) {
        this.internalNotes = internalNotes;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<PurchaseOrderLineOutput> getLines() {
        return lines;
    }

    public void setLines(List<PurchaseOrderLineOutput> lines) {
        this.lines = lines != null ? new ArrayList<>(lines) : new ArrayList<>();
    }
}
