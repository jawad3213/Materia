package com.materia.backend.support.fixtures;

import com.materia.backend.common.domain.enums.CurrencyCode;
import com.materia.backend.common.domain.valueObjects.Money;
import com.materia.backend.contexts.purchaseRequisition.domain.entities.Requisition;
import com.materia.backend.contexts.purchaseRequisition.domain.entities.RequisitionLine;
import com.materia.backend.contexts.purchaseRequisition.domain.enums.RequisitionStatus;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Builds requisitions in any lifecycle state in one call (FR-015).
 *
 * <pre>{@code
 * Requisition r = aRequisition().inStatus(APPROVED).withLine(5, "12.50").build();
 * }</pre>
 *
 * The status is set directly rather than reached by driving real transitions, so a test of
 * one transition does not depend on every earlier transition also working.
 */
public final class RequisitionFixtures {

    private RequisitionFixtures() {
    }

    /**
     * Codes are unique across the run because the database enforces uniqueness. Numbering starts
     * at 5000 so it never collides with codes the real generator issues from 0001 upwards.
     */
    private static final java.util.concurrent.atomic.AtomicInteger SEQUENCE =
            new java.util.concurrent.atomic.AtomicInteger(5000);

    public static Builder aRequisition() {
        return new Builder();
    }

    public static String uniqueCode() {
        return String.format("REQ-%d-%04d", LocalDate.now().getYear(), 5000 + (SEQUENCE.getAndIncrement() - 5000) % 5000);
    }

    /** A priced line: {@code quantity × unitPrice} in MAD, with its line total already computed. */
    public static RequisitionLine aLine(int quantity, String unitPrice) {
        return aLine(quantity, unitPrice, CurrencyCode.MAD);
    }

    public static RequisitionLine aLine(int quantity, String unitPrice, CurrencyCode currency) {
        RequisitionLine line = new RequisitionLine("MAT-" + UUID.randomUUID().toString().substring(0, 6), quantity);
        line.setMaterialId(UUID.randomUUID());
        line.setMaterialName("Test material");
        line.setUnitOfMeasure("PCE");
        line.updateUnitPrice(Money.of(unitPrice, currency));
        return line;
    }

    public static final class Builder {
        private RequisitionStatus status = RequisitionStatus.DRAFT;
        private final List<RequisitionLine> lines = new ArrayList<>();
        private String requesterId = "requester-" + UUID.randomUUID();
        private String title = "Office supplies";
        private boolean withDefaultLine = true;

        public Builder inStatus(RequisitionStatus status) {
            this.status = status;
            return this;
        }

        public Builder withLine(int quantity, String unitPrice) {
            this.lines.add(aLine(quantity, unitPrice));
            return this;
        }

        public Builder withLine(RequisitionLine line) {
            this.lines.add(line);
            return this;
        }

        /** No lines at all. Submission requires at least one, so this exercises that rule. */
        public Builder withoutLines() {
            this.withDefaultLine = false;
            this.lines.clear();
            return this;
        }

        public Builder requestedBy(String requesterId) {
            this.requesterId = requesterId;
            return this;
        }

        public Builder titled(String title) {
            this.title = title;
            return this;
        }

        public Requisition build() {
            List<RequisitionLine> resolved = new ArrayList<>(lines);
            // The domain builder refuses a requisition with no lines, so one is always built
            // with a line. A lineless requisition is reached the way it happens for real: by
            // removing its last line.
            if (resolved.isEmpty()) {
                resolved.add(aLine(1, "10.00"));
            }
            Requisition.Builder builder = Requisition.builder()
                    .id(UUID.randomUUID())
                    .requisitionCode(uniqueCode())
                    .title(title)
                    .requesterId(requesterId)
                    .requesterName("Test Requester")
                    .requiredDate(LocalDate.now().plusDays(14))
                    .status(status);
            resolved.forEach(builder::addLine);
            Requisition requisition = builder.build();
            for (int i = 0; i < requisition.getLines().size(); i++) {
                requisition.getLines().get(i).setLineNumber(i + 1);
            }
            if (!withDefaultLine) {
                requisition.removeLine(0);
            }
            return requisition;
        }
    }
}
