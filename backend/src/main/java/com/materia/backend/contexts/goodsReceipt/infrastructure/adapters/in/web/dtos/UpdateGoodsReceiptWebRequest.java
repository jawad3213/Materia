package com.materia.backend.contexts.goodsReceipt.infrastructure.adapters.in.web.dtos;

import com.materia.backend.common.application.BaseInput;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Validated web request for partially updating a goods receipt. */
public class UpdateGoodsReceiptWebRequest extends BaseInput {

    @Pattern(regexp = "^(GR-[0-9]{4})?$", message = "Receipt code must use GR-0000 format") private String receiptCode;
    private String purchaseOrderId;
    @Size(max = 100) private String purchaseOrderCode;
    private LocalDate receiptDate;
    private LocalDate expectedDeliveryDate;
    private String receivedBy;
    @Size(max = 255) private String receivedByName;
    @Size(max = 1000) private String notes;
    @Size(max = 100) private String supplierId;
    @Size(max = 255) private String supplierName;
    @Size(max = 1000) private String discrepancyNotes;
    @Valid private List<GoodsReceiptLineWebRequest> lines = new ArrayList<>();

    public String getReceiptCode() { return receiptCode; } public void setReceiptCode(String value) { this.receiptCode = value; }
    public String getPurchaseOrderId() { return purchaseOrderId; } public void setPurchaseOrderId(String value) { this.purchaseOrderId = value; }
    public String getPurchaseOrderCode() { return purchaseOrderCode; } public void setPurchaseOrderCode(String value) { this.purchaseOrderCode = value; }
    public LocalDate getReceiptDate() { return receiptDate; } public void setReceiptDate(LocalDate value) { this.receiptDate = value; }
    public LocalDate getExpectedDeliveryDate() { return expectedDeliveryDate; } public void setExpectedDeliveryDate(LocalDate value) { this.expectedDeliveryDate = value; }
    public String getReceivedBy() { return receivedBy; } public void setReceivedBy(String value) { this.receivedBy = value; }
    public String getReceivedByName() { return receivedByName; } public void setReceivedByName(String value) { this.receivedByName = value; }
    public String getNotes() { return notes; } public void setNotes(String value) { this.notes = value; }
    public String getSupplierId() { return supplierId; } public void setSupplierId(String value) { this.supplierId = value; }
    public String getSupplierName() { return supplierName; } public void setSupplierName(String value) { this.supplierName = value; }
    public String getDiscrepancyNotes() { return discrepancyNotes; } public void setDiscrepancyNotes(String value) { this.discrepancyNotes = value; }
    public List<GoodsReceiptLineWebRequest> getLines() { return lines; }
    public void setLines(List<GoodsReceiptLineWebRequest> value) { this.lines = value != null ? new ArrayList<>(value) : new ArrayList<>(); }
}
