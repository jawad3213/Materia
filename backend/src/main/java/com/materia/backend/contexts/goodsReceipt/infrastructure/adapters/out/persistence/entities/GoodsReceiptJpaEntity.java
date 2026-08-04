package com.materia.backend.contexts.goodsReceipt.infrastructure.adapters.out.persistence.entities;

import com.materia.backend.common.infrastructure.persistence.BaseJpaEntity;
import com.materia.backend.contexts.goodsReceipt.domain.enums.ReceiptStatus;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "goods_receipts", indexes = {
        @Index(name = "idx_gr_receipt_code", columnList = "receipt_code", unique = true),
        @Index(name = "idx_gr_purchase_order_id", columnList = "purchase_order_id"),
        @Index(name = "idx_gr_status", columnList = "status"),
        @Index(name = "idx_gr_received_by", columnList = "received_by"),
        @Index(name = "idx_gr_supplier_id", columnList = "supplier_id"),
        @Index(name = "idx_gr_receipt_date", columnList = "receipt_date")
})
public class GoodsReceiptJpaEntity extends BaseJpaEntity {

    @Column(name = "receipt_code", nullable = false, unique = true, length = 50)
    private String receiptCode;

    @Column(name = "purchase_order_id", nullable = false, length = 100)
    private String purchaseOrderId;

    @Column(name = "purchase_order_code", length = 100)
    private String purchaseOrderCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private ReceiptStatus status;

    @Column(name = "receipt_date")
    private LocalDate receiptDate;

    @Column(name = "expected_delivery_date")
    private LocalDate expectedDeliveryDate;

    @Column(name = "received_by", nullable = false, length = 100)
    private String receivedBy;

    @Column(name = "received_by_name", nullable = false, length = 255)
    private String receivedByName;

    @Column(name = "notes", length = 1000)
    private String notes;

    @Column(name = "supplier_id", length = 100)
    private String supplierId;

    @Column(name = "supplier_name", length = 255)
    private String supplierName;

    @Column(name = "total_quantity_ordered")
    private Integer totalQuantityOrdered;

    @Column(name = "total_quantity_received")
    private Integer totalQuantityReceived;

    @Column(name = "total_quantity_rejected")
    private Integer totalQuantityRejected;

    @Column(name = "total_quantity_accepted")
    private Integer totalQuantityAccepted;

    @Column(name = "has_discrepancy", nullable = false)
    private boolean hasDiscrepancy;

    @Column(name = "discrepancy_notes", length = 1000)
    private String discrepancyNotes;

    @Column(name = "obsoleted_at")
    private LocalDateTime obsoletedAt;

    @Column(name = "obsoleted_by", length = 100)
    private String obsoletedBy;

    @Column(name = "obsoleted_reason", length = 1000)
    private String obsoletedReason;

    @OneToMany(mappedBy = "goodsReceipt", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("lineNumber ASC")
    private List<GoodsReceiptLineJpaEntity> lines = new ArrayList<>();

    public String getReceiptCode() {
        return receiptCode;
    }

    public void setReceiptCode(String receiptCode) {
        this.receiptCode = receiptCode;
    }

    public String getPurchaseOrderId() {
        return purchaseOrderId;
    }

    public void setPurchaseOrderId(String purchaseOrderId) {
        this.purchaseOrderId = purchaseOrderId;
    }

    public String getPurchaseOrderCode() {
        return purchaseOrderCode;
    }

    public void setPurchaseOrderCode(String purchaseOrderCode) {
        this.purchaseOrderCode = purchaseOrderCode;
    }

    public ReceiptStatus getStatus() {
        return status;
    }

    public void setStatus(ReceiptStatus status) {
        this.status = status;
    }

    public LocalDate getReceiptDate() {
        return receiptDate;
    }

    public void setReceiptDate(LocalDate receiptDate) {
        this.receiptDate = receiptDate;
    }

    public LocalDate getExpectedDeliveryDate() {
        return expectedDeliveryDate;
    }

    public void setExpectedDeliveryDate(LocalDate expectedDeliveryDate) {
        this.expectedDeliveryDate = expectedDeliveryDate;
    }

    public String getReceivedBy() {
        return receivedBy;
    }

    public void setReceivedBy(String receivedBy) {
        this.receivedBy = receivedBy;
    }

    public String getReceivedByName() {
        return receivedByName;
    }

    public void setReceivedByName(String receivedByName) {
        this.receivedByName = receivedByName;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(String supplierId) {
        this.supplierId = supplierId;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    public Integer getTotalQuantityOrdered() {
        return totalQuantityOrdered;
    }

    public void setTotalQuantityOrdered(Integer totalQuantityOrdered) {
        this.totalQuantityOrdered = totalQuantityOrdered;
    }

    public Integer getTotalQuantityReceived() {
        return totalQuantityReceived;
    }

    public void setTotalQuantityReceived(Integer totalQuantityReceived) {
        this.totalQuantityReceived = totalQuantityReceived;
    }

    public Integer getTotalQuantityRejected() {
        return totalQuantityRejected;
    }

    public void setTotalQuantityRejected(Integer totalQuantityRejected) {
        this.totalQuantityRejected = totalQuantityRejected;
    }

    public Integer getTotalQuantityAccepted() {
        return totalQuantityAccepted;
    }

    public void setTotalQuantityAccepted(Integer totalQuantityAccepted) {
        this.totalQuantityAccepted = totalQuantityAccepted;
    }

    public boolean isHasDiscrepancy() {
        return hasDiscrepancy;
    }

    public void setHasDiscrepancy(boolean hasDiscrepancy) {
        this.hasDiscrepancy = hasDiscrepancy;
    }

    public String getDiscrepancyNotes() {
        return discrepancyNotes;
    }

    public void setDiscrepancyNotes(String discrepancyNotes) {
        this.discrepancyNotes = discrepancyNotes;
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

    public List<GoodsReceiptLineJpaEntity> getLines() {
        return lines;
    }

    public void setLines(List<GoodsReceiptLineJpaEntity> lines) {
        this.lines = lines != null ? lines : new ArrayList<>();
    }
}
