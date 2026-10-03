package com.materia.backend.support.fixtures;

import com.materia.backend.common.domain.enums.CurrencyCode;
import com.materia.backend.common.domain.valueObjects.Money;
import com.materia.backend.contexts.purchaseOrder.domain.entities.PurchaseOrder;
import com.materia.backend.contexts.purchaseOrder.domain.entities.PurchaseOrderLine;
import com.materia.backend.contexts.purchaseOrder.domain.enums.OrderStatus;
import com.materia.backend.contexts.purchaseOrder.domain.valueObjects.OrderCode;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Builds purchase orders in any lifecycle state in one call (FR-022).
 *
 * <pre>{@code
 * PurchaseOrder po = anOrder().withLine(5, "12.50").inStatus(CONFIRMED).build();
 * }</pre>
 *
 * Unlike {@code RequisitionFixtures}, the status is reached by driving the real transitions
 * (submit, confirm, assign, receive), so a fixture can never produce an order the system could
 * not reach — e.g. a "ready for receipt" order with no assigned receiver. The one exception is
 * the legacy {@code RECEIVED} status, which no transition produces any more.
 */
public final class PurchaseOrderFixtures {

    /** Order codes are unique in the database; numbering starts high to stay clear of the real generator. */
    private static final AtomicInteger SEQUENCE = new AtomicInteger(5000);

    public static final String BUYER = "buyer-1";
    public static final String DEFAULT_RECEIVER_ID = "receiver-1";
    public static final String DEFAULT_RECEIVER_NAME = "Rita Receiver";

    private PurchaseOrderFixtures() {
    }

    public static Builder anOrder() {
        return new Builder();
    }

    public static String uniqueCode() {
        return String.format("PO-%d-%04d", LocalDate.now().getYear(), 5000 + (SEQUENCE.getAndIncrement() - 5000) % 5000);
    }

    /** A priced line for one fresh material: {@code quantity × unitPrice} in the given currency. */
    public static PurchaseOrderLine aLine(int quantity, String unitPrice, CurrencyCode currency) {
        return PurchaseOrderLine.builder()
                .materialCode("MAT-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase())
                .materialId(UUID.randomUUID())
                .materialName("Test material")
                .unitOfMeasure("PCE")
                .quantity(quantity)
                .unitPrice(Money.of(unitPrice, currency))
                .currencyCode(currency.getCode())
                .build();
    }

    public static final class Builder {
        private OrderStatus status = OrderStatus.DRAFT;
        private final List<PurchaseOrderLine> lines = new ArrayList<>();
        private CurrencyCode currency = CurrencyCode.MAD;
        private UUID requisitionId;
        private String requisitionCode;
        private String receiverId = DEFAULT_RECEIVER_ID;
        private String receiverName = DEFAULT_RECEIVER_NAME;
        private UUID supplierId = UUID.randomUUID();
        private String notes;

        public Builder inStatus(OrderStatus status) {
            this.status = status;
            return this;
        }

        public Builder withLine(int quantity, String unitPrice) {
            this.lines.add(aLine(quantity, unitPrice, currency));
            return this;
        }

        public Builder withLine(PurchaseOrderLine line) {
            this.lines.add(line);
            return this;
        }

        /** {@code n} lines of quantity 10 at 5.00 each. */
        public Builder withLines(int n) {
            for (int i = 0; i < n; i++) {
                withLine(10, "5.00");
            }
            return this;
        }

        /** Applies to lines added after this call, and to the default line. */
        public Builder currency(CurrencyCode currency) {
            this.currency = currency;
            return this;
        }

        public Builder fromRequisition(UUID requisitionId, String requisitionCode) {
            this.requisitionId = requisitionId;
            this.requisitionCode = requisitionCode;
            return this;
        }

        /** The receiver used when the requested status requires an assignment. */
        public Builder assignedTo(String receiverId, String receiverName) {
            this.receiverId = receiverId;
            this.receiverName = receiverName;
            return this;
        }

        public Builder supplier(UUID supplierId) {
            this.supplierId = supplierId;
            return this;
        }

        public Builder notes(String notes) {
            this.notes = notes;
            return this;
        }

        public PurchaseOrder build() {
            List<PurchaseOrderLine> resolved = new ArrayList<>(lines);
            if (resolved.isEmpty()) {
                resolved.add(aLine(10, "5.00", currency));
            }
            for (int i = 0; i < resolved.size(); i++) {
                resolved.get(i).setLineNumber(i + 1);
            }

            PurchaseOrder order = PurchaseOrder.builder()
                    .id(UUID.randomUUID())
                    .orderCode(OrderCode.of(uniqueCode()))
                    .supplierId(supplierId)
                    .supplierName("Acme Supplies")
                    .supplierCode("SUP-0001")
                    .orderedBy(BUYER)
                    .orderedByName("Bob Buyer")
                    .currencyCode(currency.getCode())
                    .requisitionId(requisitionId)
                    .requisitionCode(requisitionCode)
                    .notes(notes)
                    .createdBy(BUYER)
                    .lines(resolved)
                    .build();

            advance(order);
            return order;
        }

        private void advance(PurchaseOrder order) {
            switch (status) {
                case DRAFT -> { }
                case SUBMITTED -> order.submit(BUYER);
                case CONFIRMED -> confirmed(order);
                case READY_FOR_RECEIPT -> readyForReceipt(order);
                case PARTIALLY_RECEIVED -> {
                    readyForReceipt(order);
                    order.recordReceipt(false, receiverId);
                }
                case COMPLETED -> {
                    readyForReceipt(order);
                    order.recordReceipt(true, receiverId);
                }
                case CANCELLED -> order.cancel(BUYER, "No longer needed");
                case REJECTED -> {
                    order.submit(BUYER);
                    order.reject(BUYER, "Supplier cannot deliver");
                }
                // Legacy: no transition produces RECEIVED any more; kept so existing rows load.
                case RECEIVED -> {
                    readyForReceipt(order);
                    order.setStatus(OrderStatus.RECEIVED);
                }
            }
        }

        private void confirmed(PurchaseOrder order) {
            order.submit(BUYER);
            order.confirm(BUYER);
        }

        private void readyForReceipt(PurchaseOrder order) {
            confirmed(order);
            order.assignReceiver(BUYER, "Bob Buyer", receiverId, receiverName);
        }
    }
}
