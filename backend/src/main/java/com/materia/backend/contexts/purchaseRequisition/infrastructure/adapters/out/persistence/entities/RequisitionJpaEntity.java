package com.materia.backend.contexts.purchaseRequisition.infrastructure.adapters.out.persistence.entities;

import com.materia.backend.common.infrastructure.persistence.BaseJpaEntity;
import com.materia.backend.contexts.purchaseRequisition.domain.enums.RequisitionStatus;
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
import java.util.ArrayList;
import java.util.List;

/**
 * JPA entity for purchase requisitions.
 */
@Entity
@Table(name = "purchase_requisitions", indexes = {
        @Index(name = "idx_pr_code", columnList = "requisition_code", unique = true),
        @Index(name = "idx_pr_status", columnList = "status"),
        @Index(name = "idx_pr_requester_id", columnList = "requester_id"),
        @Index(name = "idx_pr_approver_id", columnList = "approver_id"),
        @Index(name = "idx_pr_required_date", columnList = "required_date")
})
public class RequisitionJpaEntity extends BaseJpaEntity {

    @Column(name = "requisition_code", nullable = false, unique = true, length = 50)
    private String requisitionCode;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "justification", length = 500)
    private String justification;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private RequisitionStatus status;

    @Column(name = "requester_id", nullable = false, length = 100)
    private String requesterId;

    @Column(name = "requester_name", nullable = false, length = 255)
    private String requesterName;

    @Column(name = "required_date")
    private LocalDate requiredDate;

    @Column(name = "submitted_date")
    private LocalDate submittedDate;

    @Column(name = "approved_date")
    private LocalDate approvedDate;

    @Column(name = "converted_date")
    private LocalDate convertedDate;

    @Column(name = "total_amount", precision = 19, scale = 4)
    private BigDecimal totalAmount;

    @Column(name = "currency_code", length = 10)
    private String currencyCode;

    @Column(name = "approver_id", length = 100)
    private String approverId;

    @Column(name = "approver_name", length = 255)
    private String approverName;

    @Column(name = "rejection_reason", length = 1000)
    private String rejectionReason;

    @Column(name = "approval_notes", length = 1000)
    private String approvalNotes;

    @Column(name = "purchase_order_id", length = 100)
    private String purchaseOrderId;

    @Column(name = "purchase_order_code", length = 100)
    private String purchaseOrderCode;

    @OneToMany(mappedBy = "requisition", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("lineNumber ASC")
    private List<RequisitionLineJpaEntity> lines = new ArrayList<>();

    public String getRequisitionCode() {
        return requisitionCode;
    }

    public void setRequisitionCode(String requisitionCode) {
        this.requisitionCode = requisitionCode;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getJustification() {
        return justification;
    }

    public void setJustification(String justification) {
        this.justification = justification;
    }

    public RequisitionStatus getStatus() {
        return status;
    }

    public void setStatus(RequisitionStatus status) {
        this.status = status;
    }

    public String getRequesterId() {
        return requesterId;
    }

    public void setRequesterId(String requesterId) {
        this.requesterId = requesterId;
    }

    public String getRequesterName() {
        return requesterName;
    }

    public void setRequesterName(String requesterName) {
        this.requesterName = requesterName;
    }

    public LocalDate getRequiredDate() {
        return requiredDate;
    }

    public void setRequiredDate(LocalDate requiredDate) {
        this.requiredDate = requiredDate;
    }

    public LocalDate getSubmittedDate() {
        return submittedDate;
    }

    public void setSubmittedDate(LocalDate submittedDate) {
        this.submittedDate = submittedDate;
    }

    public LocalDate getApprovedDate() {
        return approvedDate;
    }

    public void setApprovedDate(LocalDate approvedDate) {
        this.approvedDate = approvedDate;
    }

    public LocalDate getConvertedDate() {
        return convertedDate;
    }

    public void setConvertedDate(LocalDate convertedDate) {
        this.convertedDate = convertedDate;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public String getApproverId() {
        return approverId;
    }

    public void setApproverId(String approverId) {
        this.approverId = approverId;
    }

    public String getApproverName() {
        return approverName;
    }

    public void setApproverName(String approverName) {
        this.approverName = approverName;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public String getApprovalNotes() {
        return approvalNotes;
    }

    public void setApprovalNotes(String approvalNotes) {
        this.approvalNotes = approvalNotes;
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

    public List<RequisitionLineJpaEntity> getLines() {
        return lines;
    }

    public void setLines(List<RequisitionLineJpaEntity> lines) {
        this.lines = lines != null ? lines : new ArrayList<>();
    }
}
