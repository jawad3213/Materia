package com.materia.backend.contexts.purchaseOrder.infrastructure.adapters.out.persistence.entities;

import com.materia.backend.common.infrastructure.persistence.BaseJpaEntity;
import com.materia.backend.contexts.purchaseOrder.domain.enums.DeliveryStatus;
import com.materia.backend.contexts.purchaseOrder.domain.enums.OrderStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * JPA entity for purchase orders.
 */
@Entity
@Table(name = "purchase_orders", indexes = {
        @Index(name = "idx_po_code", columnList = "order_code", unique = true),
        @Index(name = "idx_po_status", columnList = "status"),
        @Index(name = "idx_po_delivery_status", columnList = "delivery_status"),
        @Index(name = "idx_po_supplier_id", columnList = "supplier_id"),
        @Index(name = "idx_po_requisition_id", columnList = "requisition_id"),
        @Index(name = "idx_po_order_date", columnList = "order_date"),
        @Index(name = "idx_po_expected_delivery_date", columnList = "expected_delivery_date")
})
public class PurchaseOrderJpaEntity extends BaseJpaEntity {

    @Column(name = "order_code", nullable = false, unique = true, length = 50)
    private String orderCode;

    @Column(name = "requisition_id", length = 100)
    private UUID requisitionId;

    @Column(name = "requisition_code", length = 100)
    private String requisitionCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private OrderStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_status", nullable = false, length = 30)
    private DeliveryStatus deliveryStatus;

    @Column(name = "supplier_id", nullable = false, length = 100)
    private UUID supplierId;

    @Column(name = "supplier_name", nullable = false, length = 255)
    private String supplierName;

    @Column(name = "supplier_code", length = 50)
    private String supplierCode;

    @Column(name = "order_date")
    private LocalDate orderDate;

    @Column(name = "expected_delivery_date")
    private LocalDate expectedDeliveryDate;

    @Column(name = "confirmed_delivery_date")
    private LocalDate confirmedDeliveryDate;

    @Column(name = "received_date")
    private LocalDate receivedDate;

    @Column(name = "payment_terms", length = 255)
    private String paymentTerms;

    @Column(name = "payment_delay_days")
    private Integer paymentDelayDays;

    @Column(name = "delivery_terms", length = 255)
    private String deliveryTerms;

    @Column(name = "incoterm", length = 20)
    private String incoterm;

    @Column(name = "currency_code", length = 10)
    private String currencyCode;

    @Column(name = "total_amount", precision = 19, scale = 4)
    private BigDecimal totalAmount;

    @Column(name = "tax_amount", precision = 19, scale = 4)
    private BigDecimal taxAmount;

    @Column(name = "shipping_cost", precision = 19, scale = 4)
    private BigDecimal shippingCost;

    @Column(name = "grand_total", precision = 19, scale = 4)
    private BigDecimal grandTotal;

    @Column(name = "ordered_by", length = 100)
    private String orderedBy;

    @Column(name = "ordered_by_name", length = 255)
    private String orderedByName;

    @Column(name = "approved_by", length = 100)
    private String approvedBy;

    @Column(name = "approved_by_name", length = 255)
    private String approvedByName;

    @Column(name = "assigned_to", length = 100)
    private String assignedTo;

    @Column(name = "assigned_to_name", length = 255)
    private String assignedToName;

    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;

    @Column(name = "assigned_by", length = 100)
    private String assignedBy;

    @Column(name = "assigned_by_name", length = 255)
    private String assignedByName;

    @Column(name = "notes", length = 1000)
    private String notes;

    @Column(name = "internal_notes", length = 1000)
    private String internalNotes;

    @Column(name = "obsoleted_at")
    private LocalDateTime obsoletedAt;

    @Column(name = "obsoleted_by", length = 100)
    private String obsoletedBy;

    @Column(name = "obsoleted_reason", length = 1000)
    private String obsoletedReason;

    @OneToMany(mappedBy = "purchaseOrder", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("lineNumber ASC")
    private List<PurchaseOrderLineJpaEntity> lines = new ArrayList<>();

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

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public DeliveryStatus getDeliveryStatus() {
        return deliveryStatus;
    }

    public void setDeliveryStatus(DeliveryStatus deliveryStatus) {
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

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
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

    public BigDecimal getGrandTotal() {
        return grandTotal;
    }

    public void setGrandTotal(BigDecimal grandTotal) {
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

    public LocalDateTime getObsoletedAt() {
        return obsoletedAt;
    }

    public void setObsoletedAt(LocalDateTime obsoletedAt) {
        this.obsoletedAt = obsoletedAt;
    }

    public String getObsoletedBy() {
        return obsoletedBy;
    }

    public void setObsoletedBy(String obsoletedBy) {
        this.obsoletedBy = obsoletedBy;
    }

    public String getObsoletedReason() {
        return obsoletedReason;
    }

    public void setObsoletedReason(String obsoletedReason) {
        this.obsoletedReason = obsoletedReason;
    }

    public List<PurchaseOrderLineJpaEntity> getLines() {
        return lines;
    }

    public void setLines(List<PurchaseOrderLineJpaEntity> lines) {
        this.lines = lines != null ? lines : new ArrayList<>();
    }
}
