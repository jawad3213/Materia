package com.materia.backend.common.domain.fx;

import java.util.List;

/**
 * 🔹 DOCUMENTS VALUED IN THE REPORTING CURRENCY
 *
 * Each kind is valued once it is committed, at the rate of its own date; the statuses listed are the committed ones.
 */
public enum FxDocument {

    /** Valued when confirmed by the supplier, at the order date. */
    PURCHASE_ORDER(List.of("CONFIRMED", "READY_FOR_RECEIPT", "PARTIALLY_RECEIVED", "COMPLETED")),

    /** Valued when verified, at the invoice date. */
    INVOICE(List.of("VERIFIED", "PAID")),

    /** Valued when executed, at the execution date. */
    PAYMENT(List.of("COMPLETED"));

    private final List<String> committedStatuses;

    FxDocument(List<String> committedStatuses) {
        this.committedStatuses = committedStatuses;
    }

    public List<String> committedStatuses() {
        return committedStatuses;
    }

    public boolean isCommitted(String status) {
        return status != null && committedStatuses.contains(status.toUpperCase());
    }
}
