package com.materia.backend.contexts.masterData.domain.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/** [T074] Master data enum lookups and classifications (feature 001 coverage, spec 002 F-012). */
class MasterDataEnumsTest {

    // ---- MaterialStatus ----

    @ParameterizedTest(name = "{0}")
    @EnumSource(MaterialStatus.class)
    @DisplayName("material status: each status is found by code, enum name, code or label in any case")
    void materialStatus_lookups(MaterialStatus status) {
        assertEquals(status, MaterialStatus.fromCode(status.getCode()));
        assertEquals(status, MaterialStatus.fromValue(" " + status.name().toLowerCase() + " "));
        assertEquals(status, MaterialStatus.fromValue(status.getLabel().toUpperCase()));
        assertTrue(MaterialStatus.isValid(status.getCode()));
        assertFalse(status.getDescription().isBlank());
        assertTrue(status.toString().contains(status.getLabel()));
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(MaterialStatus.class)
    @DisplayName("material status: only ACTIVE is usable, orderable and receivable; BLOCKED and OBSOLETE block")
    void materialStatus_predicates(MaterialStatus status) {
        boolean active = status == MaterialStatus.ACTIVE;
        assertEquals(active, status.isUsable());
        assertEquals(active, status.isOrderable());
        assertEquals(active, status.isReceivable());
        assertEquals(status == MaterialStatus.DRAFT, status.isInReview());
        assertEquals(status == MaterialStatus.OBSOLETE, status.isEndOfLife());
        assertEquals(status == MaterialStatus.BLOCKED || status == MaterialStatus.OBSOLETE, status.isBlocked());
        assertEquals(EnumSet.of(MaterialStatus.DRAFT, MaterialStatus.ACTIVE, MaterialStatus.INACTIVE).contains(status),
                status.isModifiable());
    }

    @Test
    @DisplayName("material status: status groups match the predicates; a null code is no status; unknown values are refused")
    void materialStatus_groupsAndRefusals() {
        assertArrayEquals(new MaterialStatus[]{MaterialStatus.ACTIVE}, MaterialStatus.getActiveStatuses());
        assertTrue(Arrays.stream(MaterialStatus.getBlockingStatuses()).allMatch(MaterialStatus::isBlocked));
        assertTrue(Arrays.stream(MaterialStatus.getModifiableStatuses()).allMatch(MaterialStatus::isModifiable));
        assertNull(MaterialStatus.fromCode(null));
        assertFalse(MaterialStatus.isValid(null));
        assertFalse(MaterialStatus.isValid("nope"));
        assertThrows(IllegalArgumentException.class, () -> MaterialStatus.fromCode("nope"));
        assertThrows(IllegalArgumentException.class, () -> MaterialStatus.fromValue(" "));
        assertThrows(IllegalArgumentException.class, () -> MaterialStatus.fromValue(null));
        assertThrows(IllegalArgumentException.class, () -> MaterialStatus.fromValue("nope"));
    }

    // ---- MaterialType ----

    @ParameterizedTest(name = "{0}")
    @EnumSource(MaterialType.class)
    @DisplayName("material type: each type is found by prefix, label, enum name or label in any case")
    void materialType_lookups(MaterialType type) {
        assertEquals(type, MaterialType.fromCode(type.getPrefix()));
        assertEquals(type, MaterialType.fromLabel(type.getLabel()));
        assertEquals(type, MaterialType.fromValue(type.name().toLowerCase()));
        assertEquals(type, MaterialType.fromValue(" " + type.getPrefix().toLowerCase()));
        assertEquals(type, MaterialType.fromValue(type.getLabel().toLowerCase()));
        assertTrue(MaterialType.isValidCode(type.getPrefix()));
        assertTrue(MaterialType.findByDepartment(type.getDepartment()).contains(type));
        assertFalse(type.getDescription().isBlank());
        assertNotNull(type.toString());
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(MaterialType.class)
    @DisplayName("material type: production, sales and maintenance classifications match their lists; only SERVICE is not physical")
    void materialType_classifications(MaterialType type) {
        assertEquals(MaterialType.getProductionTypes().contains(type), type.isProduction());
        assertEquals(MaterialType.getSalesTypes().contains(type), type.isSales());
        assertEquals(MaterialType.getMaintenanceTypes().contains(type), type.isMaintenance());
        assertEquals(type != MaterialType.SERVICE, type.isPhysical());
        assertEquals(type == MaterialType.RAW_MATERIAL, type.isRawMaterial());
        assertEquals(type == MaterialType.FINISHED_GOOD, type.isFinishedGood());
    }

    @Test
    @DisplayName("material type: prefix and label lists are complete; blank and unknown inputs are refused")
    void materialType_listsAndRefusals() {
        assertEquals(MaterialType.values().length, MaterialType.getPrefixes().size());
        assertEquals(MaterialType.values().length, MaterialType.getLabels().size());
        assertFalse(MaterialType.isValidCode(null));
        assertFalse(MaterialType.isValidCode(""));
        for (String bad : new String[]{null, ""}) {
            assertThrows(IllegalArgumentException.class, () -> MaterialType.fromCode(bad));
            assertThrows(IllegalArgumentException.class, () -> MaterialType.fromLabel(bad));
            assertThrows(IllegalArgumentException.class, () -> MaterialType.fromValue(bad));
        }
        assertThrows(IllegalArgumentException.class, () -> MaterialType.fromCode("XXX"));
        assertThrows(IllegalArgumentException.class, () -> MaterialType.fromLabel("Nothing"));
        assertThrows(IllegalArgumentException.class, () -> MaterialType.fromValue("Nothing"));
        assertTrue(MaterialType.findByDepartment("Nowhere").isEmpty());
    }

    // ---- MaterialCategoryType ----

    @ParameterizedTest(name = "{0}")
    @EnumSource(MaterialCategoryType.class)
    @DisplayName("category type: each type is found by code, label, enum name or label in any case")
    void categoryType_lookups(MaterialCategoryType type) {
        assertEquals(type, MaterialCategoryType.fromCode(type.getCode()));
        assertEquals(type, MaterialCategoryType.fromLabel(type.getLabel()));
        assertEquals(type, MaterialCategoryType.fromValue(type.name().toLowerCase()));
        assertEquals(type, MaterialCategoryType.fromValue(type.getCode().toLowerCase()));
        assertEquals(type, MaterialCategoryType.fromValue(type.getLabel().toUpperCase()));
        assertTrue(MaterialCategoryType.isValidCode(type.getCode()));
        assertFalse(type.getDomain().isBlank());
        assertTrue(type.toString().contains(type.getCode()));
        assertFalse(type.getDescription().isBlank());
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(MaterialCategoryType.class)
    @DisplayName("category type: material, commercial and organisational predicates match their lists")
    void categoryType_classifications(MaterialCategoryType type) {
        assertEquals(MaterialCategoryType.getMaterialCategories().contains(type), type.isMaterialCategory());
        assertEquals(MaterialCategoryType.getCommercialCategories().contains(type), type.isCommercialCategory());
        assertEquals(MaterialCategoryType.getOrganizationalCategories().contains(type), type.isOrganizationalCategory());
    }

    @Test
    @DisplayName("category type: groups are as documented; code and label lists are complete; bad input is refused")
    void categoryType_groupsAndRefusals() {
        assertEquals(Set.of(MaterialCategoryType.PRODUCT, MaterialCategoryType.FAMILY, MaterialCategoryType.BRAND,
                MaterialCategoryType.SEASONAL), Set.copyOf(MaterialCategoryType.getCommercialCategories()));
        assertEquals(List.of(MaterialCategoryType.DEPARTMENT, MaterialCategoryType.PROJECT),
                MaterialCategoryType.getOrganizationalCategories());
        assertEquals(MaterialCategoryType.values().length, MaterialCategoryType.getCodes().size());
        assertEquals(MaterialCategoryType.values().length, MaterialCategoryType.getLabels().size());
        assertFalse(MaterialCategoryType.isValidCode(null));
        assertFalse(MaterialCategoryType.isValidCode(""));
        for (String bad : new String[]{null, ""}) {
            assertThrows(IllegalArgumentException.class, () -> MaterialCategoryType.fromCode(bad));
            assertThrows(IllegalArgumentException.class, () -> MaterialCategoryType.fromLabel(bad));
            assertThrows(IllegalArgumentException.class, () -> MaterialCategoryType.fromValue(bad));
        }
        assertThrows(IllegalArgumentException.class, () -> MaterialCategoryType.fromCode("ZZZ"));
        assertThrows(IllegalArgumentException.class, () -> MaterialCategoryType.fromLabel("Nothing"));
        assertThrows(IllegalArgumentException.class, () -> MaterialCategoryType.fromValue("Nothing"));
    }

    // ---- UnitOfMeasure ----

    @ParameterizedTest(name = "{0}")
    @EnumSource(UnitOfMeasure.class)
    @DisplayName("unit of measure: each unit is found by code, enum name, code or label in any case, and belongs to its category")
    void unit_lookups(UnitOfMeasure unit) {
        assertEquals(unit, UnitOfMeasure.fromCode(unit.getCode()));
        assertEquals(unit, UnitOfMeasure.fromValue(unit.name().toLowerCase()));
        assertEquals(unit, UnitOfMeasure.fromValue(" " + unit.getLabel().toUpperCase() + " "));
        assertTrue(UnitOfMeasure.isValid(unit.getCode()));
        assertTrue(UnitOfMeasure.getByCategory(unit.getCategory()).contains(unit));
        assertTrue(unit.toString().startsWith(unit.getCode()));
    }

    @Test
    @DisplayName("unit of measure: mass, volume and count groups are non-empty and consistent; bad input is refused")
    void unit_groupsAndRefusals() {
        assertTrue(UnitOfMeasure.getMassUnits().stream().allMatch(u -> u.getCategory().equals("Mass")));
        assertTrue(UnitOfMeasure.getVolumeUnits().stream().allMatch(u -> u.getCategory().equals("Volume")));
        assertFalse(UnitOfMeasure.getCountUnits().isEmpty());
        assertNull(UnitOfMeasure.fromCode(null));
        assertFalse(UnitOfMeasure.isValid(null));
        assertFalse(UnitOfMeasure.isValid("??"));
        assertThrows(IllegalArgumentException.class, () -> UnitOfMeasure.fromCode("??"));
        assertThrows(IllegalArgumentException.class, () -> UnitOfMeasure.fromValue(null));
        assertThrows(IllegalArgumentException.class, () -> UnitOfMeasure.fromValue(" "));
        assertThrows(IllegalArgumentException.class, () -> UnitOfMeasure.fromValue("lightyear"));
    }

    // ---- StockStatus ----

    @ParameterizedTest(name = "{0}")
    @EnumSource(StockStatus.class)
    @DisplayName("stock status: each status is found by its code and carries a label, icon and description")
    void stockStatus_lookups(StockStatus status) {
        assertEquals(status, StockStatus.fromCode(status.getCode()));
        assertFalse(status.getLabel().isBlank());
        assertFalse(status.getIcon().isBlank());
        assertFalse(status.getDescription().isBlank());
    }

    @ParameterizedTest(name = "\"{0}\"")
    @NullAndEmptySource
    @ValueSource(strings = {"in_stock", "LOW"})
    @DisplayName("stock status: unknown or differently cased codes are refused")
    void stockStatus_unknown_isRefused(String code) {
        assertThrows(IllegalArgumentException.class, () -> StockStatus.fromCode(code));
    }
}
