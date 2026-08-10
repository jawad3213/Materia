package com.materia.backend.contexts.returnToVendor.infrastructure.adapters.out.persistence.entities;

import com.materia.backend.common.infrastructure.persistence.BaseJpaEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "return_to_vendor")
public class ReturnToVendorJpaEntity extends BaseJpaEntity {

    @Column(name = "return_code", unique = true, nullable = false, length = 50)
    private String returnCode;

    @Column(name = "goods_receipt_id", length = 100)
    private String goodsReceiptId;

    @Column(name = "goods_receipt_code", length = 100)
    private String goodsReceiptCode;

    @Column(name = "purchase_order_id", length = 100)
    private String purchaseOrderId;

    @Column(name = "purchase_order_code", length = 100)
    private String purchaseOrderCode;

    @Column(name = "supplier_id", length = 100)
    private String supplierId;

    @Column(name = "supplier_name", length = 200)
    private String supplierName;

    @Column(name = "supplier_code", length = 100)
    private String supplierCode;

    @Column(name = "status", length = 50)
    private String status;

    @Column(name = "resolution_type", length = 50)
    private String resolutionType;

    @Column(name = "return_date")
    private LocalDate returnDate;

    @Column(name = "resolution_date")
    private LocalDate resolutionDate;

    @Column(name = "return_reason", length = 500)
    private String returnReason;

    @Column(name = "supplier_response", length = 500)
    private String supplierResponse;

    @Column(name = "rejection_summary", length = 500)
    private String rejectionSummary;

    @Column(name = "credit_note_reference", length = 100)
    private String creditNoteReference;

    @Column(name = "credit_note_amount", length = 50)
    private String creditNoteAmount;

    @Column(name = "replacement_purchase_order_reference", length = 100)
    private String replacementPurchaseOrderReference;

    @Column(name = "replacement_purchase_order_code", length = 100)
    private String replacementPurchaseOrderCode;

    @Column(name = "notes", length = 1000)
    private String notes;

    @Column(name = "internal_notes", length = 1000)
    private String internalNotes;

    @OneToMany(mappedBy = "returnToVendor", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ReturnToVendorLineJpaEntity> lines = new ArrayList<>();

    public String getReturnCode() { return returnCode; }
    public void setReturnCode(String returnCode) { this.returnCode = returnCode; }

    public String getGoodsReceiptId() { return goodsReceiptId; }
    public void setGoodsReceiptId(String goodsReceiptId) { this.goodsReceiptId = goodsReceiptId; }

    public String getGoodsReceiptCode() { return goodsReceiptCode; }
    public void setGoodsReceiptCode(String goodsReceiptCode) { this.goodsReceiptCode = goodsReceiptCode; }

    public String getPurchaseOrderId() { return purchaseOrderId; }
    public void setPurchaseOrderId(String purchaseOrderId) { this.purchaseOrderId = purchaseOrderId; }

    public String getPurchaseOrderCode() { return purchaseOrderCode; }
    public void setPurchaseOrderCode(String purchaseOrderCode) { this.purchaseOrderCode = purchaseOrderCode; }

    public String getSupplierId() { return supplierId; }
    public void setSupplierId(String supplierId) { this.supplierId = supplierId; }

    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }

    public String getSupplierCode() { return supplierCode; }
    public void setSupplierCode(String supplierCode) { this.supplierCode = supplierCode; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getResolutionType() { return resolutionType; }
    public void setResolutionType(String resolutionType) { this.resolutionType = resolutionType; }

    public LocalDate getReturnDate() { return returnDate; }
    public void setReturnDate(LocalDate returnDate) { this.returnDate = returnDate; }

    public LocalDate getResolutionDate() { return resolutionDate; }
    public void setResolutionDate(LocalDate resolutionDate) { this.resolutionDate = resolutionDate; }

    public String getReturnReason() { return returnReason; }
    public void setReturnReason(String returnReason) { this.returnReason = returnReason; }

    public String getSupplierResponse() { return supplierResponse; }
    public void setSupplierResponse(String supplierResponse) { this.supplierResponse = supplierResponse; }

    public String getRejectionSummary() { return rejectionSummary; }
    public void setRejectionSummary(String rejectionSummary) { this.rejectionSummary = rejectionSummary; }

    public String getCreditNoteReference() { return creditNoteReference; }
    public void setCreditNoteReference(String creditNoteReference) { this.creditNoteReference = creditNoteReference; }

    public String getCreditNoteAmount() { return creditNoteAmount; }
    public void setCreditNoteAmount(String creditNoteAmount) { this.creditNoteAmount = creditNoteAmount; }

    public String getReplacementPurchaseOrderReference() { return replacementPurchaseOrderReference; }
    public void setReplacementPurchaseOrderReference(String replacementPurchaseOrderReference) { this.replacementPurchaseOrderReference = replacementPurchaseOrderReference; }

    public String getReplacementPurchaseOrderCode() { return replacementPurchaseOrderCode; }
    public void setReplacementPurchaseOrderCode(String replacementPurchaseOrderCode) { this.replacementPurchaseOrderCode = replacementPurchaseOrderCode; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getInternalNotes() { return internalNotes; }
    public void setInternalNotes(String internalNotes) { this.internalNotes = internalNotes; }

    public List<ReturnToVendorLineJpaEntity> getLines() { return lines; }
    public void setLines(List<ReturnToVendorLineJpaEntity> lines) { this.lines = lines; }
}
