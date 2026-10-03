package com.materia.backend.contexts.purchaseRequisition.domain.entities;

import com.materia.backend.common.domain.enums.CurrencyCode;
import com.materia.backend.common.domain.valueObjects.Money;
import com.materia.backend.contexts.masterData.domain.entities.Material;
import com.materia.backend.contexts.purchaseRequisition.domain.enums.RequisitionStatus;
import com.materia.backend.contexts.purchaseRequisition.domain.exceptions.RequisitionCurrencyMismatchException;
import com.materia.backend.contexts.purchaseRequisition.domain.exceptions.RequisitionInvalidStatusTransitionException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.materia.backend.support.fixtures.MaterialFixtures.aMaterial;
import static com.materia.backend.support.fixtures.RequisitionFixtures.aLine;
import static com.materia.backend.support.fixtures.RequisitionFixtures.aRequisition;
import static org.junit.jupiter.api.Assertions.*;

/** [T082] Requisition builder, line maintenance, checks and identity, and RequisitionLine rules (feature 001 coverage). */
class RequisitionCoverageTest {

    private static Requisition.Builder minimal() {
        return Requisition.builder().title("Bolts").requesterId("u-1").requesterName("Uma").addLine(aLine(2, "5.00"));
    }

    /** setLines() never stores null, but a mapper-built entity can carry a null list; the getters guard against it. */
    private static void nullLines(Requisition r) {
        try {
            java.lang.reflect.Field f = Requisition.class.getDeclaredField("lines");
            f.setAccessible(true);
            f.set(r, null);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    // ---- Builder ----

    @Test
    @DisplayName("builder: defaults are DRAFT, MAD, a generated id and code, the line total, and a needed date 30 days out")
    void builder_defaults() {
        Requisition r = minimal().build();

        assertNotNull(r.getId());
        assertNotNull(r.getRequisitionCode());
        assertEquals(RequisitionStatus.DRAFT, r.getStatus());
        assertEquals(0, new BigDecimal("10.00").compareTo(r.getTotalAmount().getAmount()));
        assertEquals(LocalDate.now().plusDays(30), r.getRequiredDate());
        assertEquals(1, r.getLines().get(0).getLineNumber());
    }

    @Test
    @DisplayName("builder: the needed date is the earliest line date when none is given")
    void builder_requiredDateFromLines() {
        RequisitionLine later = aLine(1, "1.00");
        later.setRequiredDate(LocalDate.now().plusDays(9));
        RequisitionLine sooner = aLine(1, "1.00");
        sooner.setRequiredDate(LocalDate.now().plusDays(3));

        Requisition r = Requisition.builder().title("t").requesterId("u").requesterName("U")
                .lines(List.of(later, sooner)).build();

        assertEquals(LocalDate.now().plusDays(3), r.getRequiredDate());
    }

    @Test
    @DisplayName("builder: every explicit header, approval, conversion and audit value is kept")
    void builder_keepsExplicitValues() {
        UUID id = UUID.randomUUID();
        LocalDate today = LocalDate.now();
        LocalDateTime at = LocalDateTime.now().minusDays(1);
        Requisition r = minimal().id(id).requisitionCode("REQ-2026-0042").description("d").justification("j")
                .status(RequisitionStatus.APPROVED).requiredDate(today.plusDays(5)).submittedDate(today)
                .approvedDate(today).convertedDate(today).cancelledDate(today).cancellationReason("c")
                .currencyCode("EUR").approverId("m").approverName("Max").rejectionReason("r").approvalNotes("n")
                .purchaseOrderId("po-1").purchaseOrderCode("PO-1").createdBy("admin").createdAt(at).updatedAt(at)
                .build();

        assertEquals(id, r.getId());
        assertEquals("REQ-2026-0042", r.getRequisitionCode().getValue());
        assertEquals("d", r.getDescription());
        assertEquals("j", r.getJustification());
        assertEquals(RequisitionStatus.APPROVED, r.getStatus());
        assertEquals(today.plusDays(5), r.getRequiredDate());
        assertEquals(today, r.getSubmittedDate());
        assertEquals(today, r.getApprovedDate());
        assertEquals(today, r.getConvertedDate());
        assertEquals(today, r.getCancelledDate());
        assertEquals("c", r.getCancellationReason());
        assertEquals("m", r.getApproverId());
        assertEquals("Max", r.getApproverName());
        assertEquals("r", r.getRejectionReason());
        assertEquals("n", r.getApprovalNotes());
        assertEquals("po-1", r.getPurchaseOrderId());
        assertEquals("PO-1", r.getPurchaseOrderCode());
        assertEquals("admin", r.getCreatedBy());
        assertEquals(at, r.getCreatedAt());
        assertEquals(at, r.getUpdatedAt());
    }

    @Test
    @DisplayName("builder: title, requester and requester name are required; over-long texts and past dates are refused")
    void builder_validation() {
        assertThrows(IllegalArgumentException.class, () -> minimal().title(" ").build());
        assertThrows(IllegalArgumentException.class, () -> minimal().requesterId(null).build());
        assertThrows(IllegalArgumentException.class, () -> minimal().requesterName(" ").build());
        assertThrows(IllegalArgumentException.class, () -> minimal().title("x".repeat(Requisition.MAX_TITLE_LENGTH + 1)));
        assertThrows(IllegalArgumentException.class,
                () -> minimal().description("x".repeat(Requisition.MAX_DESCRIPTION_LENGTH + 1)));
        assertThrows(IllegalArgumentException.class,
                () -> minimal().justification("x".repeat(Requisition.MAX_JUSTIFICATION_LENGTH + 1)));
        assertThrows(IllegalArgumentException.class, () -> minimal().requiredDate(LocalDate.now().minusDays(1)));
        assertThrows(IllegalArgumentException.class, () -> minimal().addLine(null));
        assertThrows(IllegalArgumentException.class, () -> minimal().lines(null));
    }

    @Test
    @DisplayName("builder: at least one line, each with a material, a positive quantity and a material name")
    void builder_lineValidation() {
        Requisition.Builder noLines = Requisition.builder().title("t").requesterId("u").requesterName("U");
        assertThrows(IllegalArgumentException.class, noLines::build);

        RequisitionLine noMaterial = aLine(1, "1.00");
        noMaterial.setMaterialCode(" ");
        assertThrows(IllegalArgumentException.class, () -> noLines.lines(List.of(noMaterial)).build());

        RequisitionLine zero = aLine(1, "1.00");
        zero.setQuantity(0);
        assertThrows(IllegalArgumentException.class, () -> noLines.lines(List.of(zero)).build());

        RequisitionLine noName = aLine(1, "1.00");
        noName.setMaterialName(null);
        assertThrows(IllegalArgumentException.class, () -> noLines.lines(List.of(noName)).build());
    }

    @Test
    @DisplayName("builder: lines in different currencies are refused")
    void builder_currencyMismatch() {
        assertThrows(RequisitionCurrencyMismatchException.class, () -> minimal()
                .addLine(aLine(1, "1.00", CurrencyCode.EUR)).build());
    }

    // ---- Line maintenance ----

    @Test
    @DisplayName("lines: adding, updating and removing by index renumber the lines and recompute the total")
    void lines_maintenance() {
        Requisition r = aRequisition().withLine(1, "10.00").withLine(2, "5.00").build();

        r.addLine(aLine(3, "1.00"));
        assertEquals(3, r.getLines().get(2).getLineNumber());
        assertEquals(0, new BigDecimal("23.00").compareTo(r.getTotalAmount().getAmount()));

        r.updateLine(0, aLine(1, "2.00"));
        assertEquals(1, r.getLines().get(0).getLineNumber());
        assertEquals(0, new BigDecimal("15.00").compareTo(r.getTotalAmount().getAmount()));

        r.removeLine(0);
        assertEquals(1, r.getLines().get(0).getLineNumber());
        assertEquals(0, new BigDecimal("13.00").compareTo(r.getTotalAmount().getAmount()));
        assertEquals(2, r.getTotalProductCount());
        assertEquals(5, r.getTotalQuantity());
        assertEquals(2, r.getMaterialCodes().size());
    }

    @Test
    @DisplayName("lines: bad indexes, null lines and a null line id are refused")
    void lines_refusals() {
        Requisition r = aRequisition().withLine(1, "10.00").build();

        assertThrows(IllegalArgumentException.class, () -> r.addLine(null));
        assertThrows(IllegalArgumentException.class, () -> r.removeLine(-1));
        assertThrows(IllegalArgumentException.class, () -> r.removeLine(5));
        assertThrows(IllegalArgumentException.class, () -> r.updateLine(9, aLine(1, "1.00")));
        assertThrows(IllegalArgumentException.class, () -> r.updateLine(0, null));
        assertThrows(IllegalArgumentException.class, () -> r.removeLine((UUID) null));
    }

    @Test
    @DisplayName("lines: removing by id renumbers when found and changes nothing when unknown")
    void lines_removeById() {
        RequisitionLine first = aLine(1, "1.00");
        first.setId(UUID.randomUUID());
        RequisitionLine second = aLine(1, "2.00");
        second.setId(UUID.randomUUID());
        Requisition r = aRequisition().withLine(first).withLine(second).build();

        r.removeLine(UUID.randomUUID());
        assertEquals(2, r.getLines().size());

        r.removeLine(first.getId());
        assertEquals(1, r.getLines().size());
        assertEquals(1, second.getLineNumber());
        assertEquals(0, new BigDecimal("2.00").compareTo(r.getTotalAmount().getAmount()));
    }

    @Test
    @DisplayName("totals: no lines give zero in the requisition's currency (MAD when invalid); lines without totals are skipped")
    void totals_edges() {
        Requisition r = aRequisition().withLine(1, "1.00").build();
        r.setLines(null);
        r.setCurrencyCode("EUR");
        r.recalculateTotal();
        assertEquals(CurrencyCode.EUR, r.getTotalAmount().getCurrency());

        r.setCurrencyCode("???");
        r.recalculateTotal();
        assertEquals(CurrencyCode.MAD, r.getTotalAmount().getCurrency());

        RequisitionLine unpriced = new RequisitionLine("MAT-X", 1);
        List<RequisitionLine> lines = new ArrayList<>();
        lines.add(null);
        lines.add(unpriced);
        r.setLines(lines);
        r.recalculateTotal();
        assertEquals(0, r.getTotalAmount().getAmount().signum());
        assertEquals(CurrencyCode.MAD, r.getTotalAmount().getCurrency());
    }

    @Test
    @DisplayName("aggregates: missing lines count as zero; missing quantities and codes are ignored")
    void aggregates_withMissingValues() {
        Requisition r = aRequisition().withLine(1, "1.00").build();
        RequisitionLine blank = new RequisitionLine(null, null);
        r.setLines(List.of(blank));
        assertEquals(0, r.getTotalQuantity());
        assertTrue(r.getMaterialCodes().isEmpty());
        assertFalse(r.isFullyReceived());

        r.setLines(null);
        assertFalse(r.isFullyReceived());
        nullLines(r);
        assertEquals(0, r.getTotalProductCount());
        assertEquals(0, r.getTotalQuantity());
        assertTrue(r.getMaterialCodes().isEmpty());
    }

    // ---- Checks ----

    @Test
    @DisplayName("checks: a requisition without status answers no to every capability check")
    void checks_withoutStatus() {
        Requisition r = aRequisition().build();
        r.setStatus(null);

        assertFalse(r.isModifiable());
        assertFalse(r.isConvertible());
        assertFalse(r.isDeletable());
        assertFalse(r.isCancellable());
        assertFalse(r.isSubmittable());
        assertFalse(r.isApprovable());
        assertFalse(r.isRejectable());
        assertThrows(RequisitionInvalidStatusTransitionException.class, () -> r.cancel("u", "x"));
    }

    @Test
    @DisplayName("overdue: only a past needed date on a requisition that is neither converted nor rejected")
    void overdue() {
        Requisition r = aRequisition().build();
        r.setRequiredDate(LocalDate.now().minusDays(1));
        assertTrue(r.isOverdue());
        r.setStatus(RequisitionStatus.CONVERTED);
        assertFalse(r.isOverdue());
        r.setStatus(RequisitionStatus.REJECTED);
        assertFalse(r.isOverdue());
        r.setRequiredDate(null);
        assertFalse(r.isOverdue());
        r.setStatus(RequisitionStatus.DRAFT);
        r.setRequiredDate(LocalDate.now().plusDays(1));
        assertFalse(r.isOverdue());
    }

    @Test
    @DisplayName("fully received: only when every line has received its whole quantity")
    void fullyReceived() {
        RequisitionLine line = aLine(2, "1.00");
        Requisition r = aRequisition().withLine(line).build();
        assertFalse(r.isFullyReceived());
        line.receiveQuantity(2, 0);
        assertTrue(r.isFullyReceived());
    }

    @Test
    @DisplayName("convert: a rejected requisition gets the rejection message; revert needs the same purchase order")
    void convertAndRevertRefusals() {
        Requisition rejected = aRequisition().inStatus(RequisitionStatus.REJECTED).build();
        assertThrows(RequisitionInvalidStatusTransitionException.class, () -> rejected.convert("po", "PO", "u"));

        Requisition converted = aRequisition().inStatus(RequisitionStatus.CONVERTED).build();
        converted.setPurchaseOrderId(null);
        assertThrows(RequisitionInvalidStatusTransitionException.class,
                () -> converted.revertConversion("po", "u"));
    }

    // ---- Identity ----

    @Test
    @DisplayName("identity: equal by id or by code, never to null or another type; toString tolerates missing lines")
    void identity() {
        Requisition a = aRequisition().build();
        Requisition sameCode = aRequisition().build();
        sameCode.setRequisitionCode(a.getRequisitionCode());
        Requisition other = aRequisition().build();

        assertEquals(a, a);
        assertEquals(a, sameCode);
        assertNotEquals(a, other);
        assertNotEquals(a, null);
        assertNotEquals(a, "requisition");
        assertEquals(a.hashCode(), a.hashCode());
        nullLines(a);
        assertTrue(a.toString().contains("linesCount=0"));
    }

    // ---- RequisitionLine ----

    @Test
    @DisplayName("line from material: copies code, name, unit and price, and computes the total")
    void line_fromMaterial() {
        Material m = aMaterial().price("4.00").build();
        m.setId(UUID.randomUUID());

        RequisitionLine line = new RequisitionLine(m, 3, LocalDate.now().plusDays(2));

        assertEquals(m.getCode().getValue(), line.getMaterialCode());
        assertEquals(m.getId(), line.getMaterialId());
        assertEquals(m.getName(), line.getMaterialName());
        assertEquals(m.getStandardPrice(), line.getStandardPrice());
        assertEquals(m.getStandardPrice().getCurrencyCode(), line.getCurrencyCode());
        assertEquals(line.getCurrencyCode(), line.getCurrencyCodeLine());
        assertEquals(0, new BigDecimal("12.00").compareTo(line.getLineTotal().getAmount()));
        assertTrue(line.isValid());
        assertFalse(line.isDraft());
    }

    @Test
    @DisplayName("line from material: a material without price or unit leaves them empty")
    void line_fromBareMaterial() {
        Material m = aMaterial().build();
        m.setStandardPrice(null);
        m.setUnitOfMeasure((String) null);

        RequisitionLine line = new RequisitionLine(m, 1, null);

        assertNull(line.getUnitOfMeasure());
        assertNull(line.getCurrencyCode());
        assertNull(line.getLineTotal());
        assertFalse(line.isValid());
    }

    @Test
    @DisplayName("line: material and a positive quantity are required; prices must be present and positive")
    void line_refusals() {
        Material m = aMaterial().build();
        assertThrows(IllegalArgumentException.class, () -> new RequisitionLine(null, 1, null));
        assertThrows(IllegalArgumentException.class, () -> new RequisitionLine(m, 0, null));
        assertThrows(IllegalArgumentException.class, () -> new RequisitionLine(m, null, null));
        RequisitionLine line = aLine(2, "1.00");
        assertThrows(IllegalArgumentException.class, () -> line.populateFromMaterial(null));
        assertThrows(IllegalArgumentException.class, () -> line.updateQuantity(0));
        assertThrows(IllegalArgumentException.class, () -> line.updateQuantity(null));
        assertThrows(IllegalArgumentException.class, () -> line.updateUnitPrice(null));
        assertThrows(IllegalArgumentException.class, () -> line.updateUnitPrice(Money.of("0", CurrencyCode.MAD)));
    }

    @Test
    @DisplayName("line receiving: quantities are non-negative and together cannot exceed the ordered quantity")
    void line_receiving() {
        RequisitionLine line = aLine(10, "1.00");

        assertThrows(IllegalArgumentException.class, () -> line.receiveQuantity(-1, 0));
        assertThrows(IllegalArgumentException.class, () -> line.receiveQuantity(null, 0));
        assertThrows(IllegalArgumentException.class, () -> line.receiveQuantity(1, -1));
        assertThrows(IllegalArgumentException.class, () -> line.receiveQuantity(1, null));
        assertThrows(IllegalArgumentException.class, () -> line.receiveQuantity(8, 3));

        line.receiveQuantity(4, 1);
        assertTrue(line.isPartiallyReceived());
        assertTrue(line.hasRejection());
        assertFalse(line.isFullyReceived());
        assertEquals(40.0, line.getReceivedPercentage());
        assertEquals(10.0, line.getRejectedPercentage());
        assertEquals(6, line.getRemainingQuantity());

        line.updateQuantity(20);
        assertEquals(0, new BigDecimal("20.00").compareTo(line.getLineTotal().getAmount()));
    }

    @Test
    @DisplayName("line progress: a missing quantity gives zero percentages and remaining; missing counts are zero")
    void line_progressWithMissingValues() {
        RequisitionLine line = new RequisitionLine("MAT-1", null);
        assertEquals(0.0, line.getReceivedPercentage());
        assertEquals(0.0, line.getRejectedPercentage());
        assertEquals(0, line.getRemainingQuantity());
        assertFalse(line.isFullyReceived());
        assertFalse(line.isPartiallyReceived());

        line.setQuantity(0);
        assertEquals(0.0, line.getReceivedPercentage());
        assertEquals(0.0, line.getRejectedPercentage());

        line.setQuantity(4);
        line.setQuantityReceived(null);
        line.setQuantityRejected(null);
        assertEquals(0.0, line.getReceivedPercentage());
        assertEquals(0.0, line.getRejectedPercentage());
        assertEquals(4, line.getRemainingQuantity());
        assertFalse(line.hasRejection());
        assertFalse(line.isFullyReceived());
        assertFalse(line.isPartiallyReceived());
        line.calculateLineTotal();
        assertNull(line.getLineTotal());
    }

    @Test
    @DisplayName("line validity: a draft has no material code; validity needs code, positive quantity and price")
    void line_validity() {
        assertTrue(new RequisitionLine(null, 1).isDraft());
        assertTrue(new RequisitionLine("", 1).isDraft());
        assertFalse(new RequisitionLine("", 1).isValid());
        assertFalse(new RequisitionLine("M", null).isValid());
        assertFalse(new RequisitionLine("M", 0).isValid());
        assertFalse(new RequisitionLine("M", 1).isValid());
    }

    @Test
    @DisplayName("line copy: every field is carried over; lines are equal by id only")
    void line_copyAndIdentity() {
        RequisitionLine line = aLine(3, "2.00");
        line.setId(UUID.randomUUID());
        line.setLineNumber(4);
        line.setMaterialDescription("desc");
        line.setSupplierId(UUID.randomUUID());
        line.setSupplierName("Acme");
        line.setSupplierCode("SUP-1");
        line.setNotes("n");
        line.setDeliveryTerms("FOB");
        line.setStorageLocation("A1");
        line.setBatchNumber("B1");
        line.setExpiryDate(LocalDate.now().plusYears(1));
        line.setStandardPrice(Money.of("2.00", CurrencyCode.MAD));
        line.setCurrencyCode("MAD");
        line.setCurrencyCodeLine("MAD");
        line.setRequiredDate(LocalDate.now());
        line.setLineTotal(Money.of("6.00", CurrencyCode.MAD));
        line.setUnitPrice(Money.of("2.00", CurrencyCode.MAD));

        RequisitionLine copy = line.copy();

        assertEquals(line, copy);
        assertEquals(line.hashCode(), copy.hashCode());
        assertTrue(copy.hasSpecificSupplier());
        assertEquals("desc", copy.getMaterialDescription());
        assertEquals("Acme", copy.getSupplierName());
        assertEquals("SUP-1", copy.getSupplierCode());
        assertEquals("n", copy.getNotes());
        assertEquals("FOB", copy.getDeliveryTerms());
        assertEquals("A1", copy.getStorageLocation());
        assertEquals("B1", copy.getBatchNumber());
        assertEquals(line.getExpiryDate(), copy.getExpiryDate());
        assertEquals(4, copy.getLineNumber());
        assertEquals("PCE", copy.getUnitOfMeasure());

        RequisitionLine unsaved = new RequisitionLine("M", 1);
        assertNotEquals(unsaved, new RequisitionLine("M", 1));
        assertEquals(0, unsaved.hashCode());
        assertEquals(unsaved, unsaved);
        assertNotEquals(line, null);
        assertNotEquals(line, "line");
        assertFalse(unsaved.hasSpecificSupplier());
        assertTrue(line.toString().contains("lineNumber=4"));
    }
}
