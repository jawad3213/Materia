package com.materia.backend.contexts.masterData.domain.valueObjects;

import com.materia.backend.contexts.masterData.domain.enums.MaterialType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Value object tests for MaterialCode (T090, US5).
 */
class MaterialCodeTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "MAT-2026-0001",
            "RMT-2026-0042",
            "FGD-2025-9999",
            "CMP-1234",
            "PKG-0001",
            "TOOL-2026-0010"
    })
    @DisplayName("valid formats: correctly formed material codes are accepted")
    void validCodes_accepted(String code) {
        MaterialCode mc = MaterialCode.of(code);
        assertEquals(code, mc.getValue());
        assertEquals(code, mc.toString());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {
            "   ",
            "INVALID",
            "M-1",
            "MAT-2026-01",
            "MAT-ABC-0001",
            "A_VERY_LONG_PREFIX_THAT_EXCEEDS_LIMITS-2026-0001"
    })
    @DisplayName("invalid formats: malformed codes throw IllegalArgumentException")
    void invalidCodes_rejected(String code) {
        assertThrows(IllegalArgumentException.class, () -> MaterialCode.of(code));
    }

    @Test
    @DisplayName("generation: prefix and number generates expected code")
    void fromPrefixAndNumber() {
        MaterialCode mc = MaterialCode.fromPrefixYearAndNumber("MAT", 2026, 42);
        assertEquals("MAT-2026-0042", mc.getValue());
    }

    @Test
    @DisplayName("generation: from material type uses designated prefix")
    void fromMaterialType() {
        MaterialCode mc = MaterialCode.createForType(MaterialType.RAW_MATERIAL);
        assertTrue(mc.getValue().startsWith("RMT-"));
    }

    @Test
    @DisplayName("equality: codes with the same string value are equal")
    void equality() {
        MaterialCode c1 = MaterialCode.of("MAT-2026-0001");
        MaterialCode c2 = MaterialCode.of("MAT-2026-0001");
        MaterialCode c3 = MaterialCode.of("MAT-2026-0002");

        assertEquals(c1, c2);
        assertEquals(c1.hashCode(), c2.hashCode());
        assertNotEquals(c1, c3);
    }
}
