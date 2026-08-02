package com.materia.backend.contexts.purchaseRequisition.domain.valueObjects;

import com.materia.backend.contexts.purchaseRequisition.domain.enums.RequisitionStatus;

import java.time.LocalDate;

/**
 * Domain filter for advanced requisition search.
 */
public class RequisitionSearchFilter {

    private String keyword;
    private String requesterId;
    private String approverId;
    private RequisitionStatus status;
    private String currencyCode;
    private LocalDate requiredDateFrom;
    private LocalDate requiredDateTo;
    private LocalDate submittedDateFrom;
    private LocalDate submittedDateTo;

    public static Builder builder() {
        return new Builder();
    }

    public String getKeyword() {
        return keyword;
    }

    public String getRequesterId() {
        return requesterId;
    }

    public String getApproverId() {
        return approverId;
    }

    public RequisitionStatus getStatus() {
        return status;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public LocalDate getRequiredDateFrom() {
        return requiredDateFrom;
    }

    public LocalDate getRequiredDateTo() {
        return requiredDateTo;
    }

    public LocalDate getSubmittedDateFrom() {
        return submittedDateFrom;
    }

    public LocalDate getSubmittedDateTo() {
        return submittedDateTo;
    }

    public static class Builder {
        private final RequisitionSearchFilter filter = new RequisitionSearchFilter();

        public Builder keyword(String keyword) {
            filter.keyword = keyword;
            return this;
        }

        public Builder requesterId(String requesterId) {
            filter.requesterId = requesterId;
            return this;
        }

        public Builder approverId(String approverId) {
            filter.approverId = approverId;
            return this;
        }

        public Builder status(RequisitionStatus status) {
            filter.status = status;
            return this;
        }

        public Builder currencyCode(String currencyCode) {
            filter.currencyCode = currencyCode;
            return this;
        }

        public Builder requiredDateFrom(LocalDate requiredDateFrom) {
            filter.requiredDateFrom = requiredDateFrom;
            return this;
        }

        public Builder requiredDateTo(LocalDate requiredDateTo) {
            filter.requiredDateTo = requiredDateTo;
            return this;
        }

        public Builder submittedDateFrom(LocalDate submittedDateFrom) {
            filter.submittedDateFrom = submittedDateFrom;
            return this;
        }

        public Builder submittedDateTo(LocalDate submittedDateTo) {
            filter.submittedDateTo = submittedDateTo;
            return this;
        }

        public RequisitionSearchFilter build() {
            return filter;
        }
    }
}
