package com.materia.backend.contexts.masterData.domain.valueObjects;

import com.materia.backend.common.domain.enums.CurrencyCode;
import com.materia.backend.common.domain.valueObjects.Money;
import com.materia.backend.contexts.masterData.domain.enums.MaterialType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Year;

import static org.junit.jupiter.api.Assertions.*;

/** [T075] Reorder quantities and material codes (feature 001 coverage). */
class MasterDataValueObjectsTest {

    @Test
    @DisplayName("reorder: a positive quantity is kept with its cost, reason and urgency; equal by all four")
    void reorderQuantity() {
        Money cost = Money.of("100.00", CurrencyCode.MAD);
        ReorderQuantity q = new ReorderQuantity(20, cost, "Below reorder point", true);

        assertEquals(20, q.getQuantity());
        assertEquals(cost, q.getEstimatedCost());
        assertEquals("Below reorder point", q.getReason());
        assertTrue(q.isUrgent());
        assertEquals(new ReorderQuantity(20, cost, "Below reorder point", true), q);
        assertEquals(q.hashCode(), new ReorderQuantity(20, cost, "Below reorder point", true).hashCode());
        assertNotEquals(q, new ReorderQuantity(21, cost, "Below reorder point", true));
        assertNotEquals(q, new ReorderQuantity(20, cost, "Below reorder point", false));
        assertNotEquals(q, new ReorderQuantity(20, cost, "Other", true));
        assertNotEquals(q, new ReorderQuantity(20, Money.of("1.00", CurrencyCode.MAD), "Below reorder point", true));
        assertNotEquals(q, null);
        assertNotEquals(q, "20");
        assertEquals(q, q);
        assertTrue(q.toString().contains("quantity=20"));
    }

    @Test
    @DisplayName("reorder: a zero or negative quantity is refused")
    void reorderQuantity_mustBePositive() {
        assertThrows(IllegalArgumentException.class, () -> new ReorderQuantity(0, null, "x", false));
        assertThrows(IllegalArgumentException.class, () -> new ReorderQuantity(-5, null, "x", false));
    }

    @Test
    @DisplayName("code: year-based and legacy codes parse into prefix, number and year")
    void materialCode_parsing() {
        MaterialCode yearBased = MaterialCode.of("RMT-2026-0042");
        assertEquals("RMT", yearBased.getPrefix());
        assertEquals("0042", yearBased.getNumber());
        assertEquals(42, yearBased.getNumberAsInt());
        assertEquals(2026, yearBased.getYear());
        assertEquals(-1, MaterialCode.of("MAT-0001").getYear());
    }

    @Test
    @DisplayName("code: generation pads numbers, keeps the year, and uses the type's prefix (default MAT)")
    void materialCode_generation() {
        int year = Year.now().getValue();
        assertEquals("CMP-2026-0005", MaterialCode.fromPrefixYearAndNumber("cmp", 2026, 5).getValue());
        assertEquals("MAT-" + year + "-0001", MaterialCode.createDefault().getValue());
        assertEquals("MAT-" + year + "-0001", MaterialCode.createForType(null).getValue());
        assertEquals("ELC-" + year + "-0001", MaterialCode.createForType(MaterialType.ELECTRONIC).getValue());
        assertEquals("RMT-2026-0043", MaterialCode.generateNext("RMT-2026-0042").getValue());
        assertEquals("RMT-2026-0043", MaterialCode.of("RMT-2026-0042").increment().getValue());
        assertEquals("MAT-" + year + "-0002", MaterialCode.generateNext("MAT-0001").getValue());
        assertEquals("FGD-2026-0042", MaterialCode.of("RMT-2026-0042").withNewPrefix("FGD").getValue());
        assertEquals("FGD-" + year + "-0001", MaterialCode.of("RMT-0001").withNewPrefix("FGD").getValue());
        assertThrows(IllegalArgumentException.class, () -> MaterialCode.fromPrefixYearAndNumber(" ", 2026, 1));
        assertThrows(IllegalArgumentException.class, () -> MaterialCode.fromPrefixYearAndNumber(null, 2026, 1));
        assertThrows(IllegalArgumentException.class, () -> MaterialCode.fromPrefixYearAndNumber("MAT", 2026, -1));
        assertThrows(IllegalArgumentException.class, () -> MaterialCode.fromPrefixYearAndNumber("MAT", 2026, 10000));
    }

    @Test
    @DisplayName("code: each type predicate recognises its own prefix only")
    void materialCode_typePredicates() {
        assertTrue(MaterialCode.of("RMT-0001").isRawMaterial());
        assertTrue(MaterialCode.of("FGD-0001").isFinishedGood());
        assertTrue(MaterialCode.of("CMP-0001").isComponent());
        assertTrue(MaterialCode.of("PKG-0001").isPackaging());
        assertTrue(MaterialCode.of("SPR-0001").isSparePart());
        assertTrue(MaterialCode.of("CNS-0001").isConsumable());
        assertTrue(MaterialCode.of("SRV-0001").isService());
        assertTrue(MaterialCode.of("TOL-0001").isTool());
        assertTrue(MaterialCode.of("CHM-0001").isChemical());
        assertTrue(MaterialCode.of("ELC-0001").isElectronic());
        assertFalse(MaterialCode.of("MAT-0001").isRawMaterial());
        assertTrue(MaterialCode.of("rmt-0001").startsWith("RMT"));
    }

    @Test
    @DisplayName("code: codes compare by value")
    void materialCode_equality() {
        MaterialCode code = MaterialCode.of("MAT-0001");
        assertEquals(MaterialCode.of("MAT-0001"), code);
        assertEquals(code.hashCode(), MaterialCode.of("MAT-0001").hashCode());
        assertNotEquals(code, MaterialCode.of("MAT-0002"));
        assertNotEquals(code, null);
        assertNotEquals(code, "MAT-0001");
        assertEquals(code, code);
        assertEquals("MAT-0001", code.toString());
    }

    @ParameterizedTest(name = "\"{0}\"")
    @NullAndEmptySource
    @ValueSource(strings = {"  ", "M-0001", "MAT0001", "MAT-001", "TOOLONGPREFIX-0001"})
    @DisplayName("code: blank and malformed material codes are refused")
    void materialCode_malformed(String raw) {
        assertThrows(IllegalArgumentException.class, () -> MaterialCode.of(raw));
    }
}
