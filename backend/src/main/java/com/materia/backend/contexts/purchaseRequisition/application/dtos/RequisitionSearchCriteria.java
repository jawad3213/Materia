package com.materia.backend.contexts.purchaseRequisition.application.dtos;

import java.time.LocalDate;

/**
 * Criteria for advanced purchase requisition search.
 */
public class RequisitionSearchCriteria {

    private String keyword;
    private String requesterId;
    private String approverId;
    private String status;
    private String currencyCode;
    private LocalDate requiredDateFrom;
    private LocalDate requiredDateTo;
    private LocalDate submittedDateFrom;
    private LocalDate submittedDateTo;

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getRequesterId() {
        return requesterId;
    }

    public void setRequesterId(String requesterId) {
        this.requesterId = requesterId;
    }

    public String getApproverId() {
        return approverId;
    }

    public void setApproverId(String approverId) {
        this.approverId = approverId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public LocalDate getRequiredDateFrom() {
        return requiredDateFrom;
    }

    public void setRequiredDateFrom(LocalDate requiredDateFrom) {
        this.requiredDateFrom = requiredDateFrom;
    }

    public LocalDate getRequiredDateTo() {
        return requiredDateTo;
    }

    public void setRequiredDateTo(LocalDate requiredDateTo) {
        this.requiredDateTo = requiredDateTo;
    }

    public LocalDate getSubmittedDateFrom() {
        return submittedDateFrom;
    }

    public void setSubmittedDateFrom(LocalDate submittedDateFrom) {
        this.submittedDateFrom = submittedDateFrom;
    }

    public LocalDate getSubmittedDateTo() {
        return submittedDateTo;
    }

    public void setSubmittedDateTo(LocalDate submittedDateTo) {
        this.submittedDateTo = submittedDateTo;
    }
}
