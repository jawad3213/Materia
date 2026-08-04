package com.materia.backend.contexts.purchaseOrder.infrastructure.adapters.in.web.dtos.purchaseOrder;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Web request DTO for updating a purchase order.
 */
public class UpdatePurchaseOrderWebRequest {

    private UUID requisitionId;

    @Size(max = 50, message = "Requisition code must not exceed 50 characters")
    private String requisitionCode;

    private UUID supplierId;

    @Size(max = 255, message = "Supplier name must not exceed 255 characters")
    private String supplierName;

    @Size(max = 50, message = "Supplier code must not exceed 50 characters")
    private String supplierCode;

    private LocalDate orderDate;

    private LocalDate expectedDeliveryDate;

    @Size(max = 255, message = "Payment terms must not exceed 255 characters")
    private String paymentTerms;

    @PositiveOrZero(message = "Payment delay days cannot be negative")
    private Integer paymentDelayDays;

    @Size(max = 255, message = "Delivery terms must not exceed 255 characters")
    private String deliveryTerms;

    @Size(max = 50, message = "Incoterm must not exceed 50 characters")
    private String incoterm;

    @Size(min = 3, max = 3, message = "Currency code must be exactly 3 characters if provided")
    private String currencyCode;

    @DecimalMin(value = "0.00", inclusive = true, message = "Tax amount cannot be negative")
    private BigDecimal taxAmount;

    @DecimalMin(value = "0.00", inclusive = true, message = "Shipping cost cannot be negative")
    private BigDecimal shippingCost;

    private String orderedBy;

    @Size(max = 255, message = "Ordered by name must not exceed 255 characters")
    private String orderedByName;

    private String approvedBy;

    @Size(max = 255, message = "Approved by name must not exceed 255 characters")
    private String approvedByName;

    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    private String notes;

    @Size(max = 1000, message = "Internal notes must not exceed 1000 characters")
    private String internalNotes;

    @Valid
    private List<PurchaseOrderLineWebRequest> lines = new ArrayList<>();

    private String updatedBy;

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

    public BigDecimal getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(BigDecimal taxAmount) {
        this.taxAmount = taxAmount;
    }

    public BigDecimal getShippingCost() {
        return shippingCost;
    }

    public void setShippingCost(BigDecimal shippingCost) {
        this.shippingCost = shippingCost;
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

    public List<PurchaseOrderLineWebRequest> getLines() {
        return lines;
    }

    public void setLines(List<PurchaseOrderLineWebRequest> lines) {
        this.lines = lines != null ? new ArrayList<>(lines) : new ArrayList<>();
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }
}
