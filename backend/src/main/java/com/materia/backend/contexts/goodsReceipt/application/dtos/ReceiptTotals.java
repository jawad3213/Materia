package com.materia.backend.contexts.goodsReceipt.application.dtos;

/** Quantities and punctuality of a set of goods receipts. */
public record ReceiptTotals(long receipts, long unitsReceived, long unitsAccepted, long unitsRejected,
                            long datedReceipts, long onTimeReceipts) {

    public static final ReceiptTotals NONE = new ReceiptTotals(0, 0, 0, 0, 0, 0);

    public ReceiptTotals plus(ReceiptTotals other) {
        return new ReceiptTotals(receipts + other.receipts, unitsReceived + other.unitsReceived,
                unitsAccepted + other.unitsAccepted, unitsRejected + other.unitsRejected,
                datedReceipts + other.datedReceipts, onTimeReceipts + other.onTimeReceipts);
    }
}
