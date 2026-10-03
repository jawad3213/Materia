package com.materia.backend.contexts.masterData.domain.entities;

import com.materia.backend.common.domain.enums.CurrencyCode;
import com.materia.backend.common.domain.valueObjects.Money;
import com.materia.backend.contexts.masterData.domain.enums.MaterialStatus;
import com.materia.backend.contexts.masterData.domain.enums.MaterialType;
import com.materia.backend.contexts.masterData.domain.enums.UnitOfMeasure;
import com.materia.backend.contexts.masterData.domain.valueObjects.MaterialCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Domain entity tests for Material catalogue rules (T089, US5).
 */
class MaterialTest {

    private Material.Builder validMaterialBuilder() {
        return Material.builder()
                .code(MaterialCode.of("MAT-2026-0001"))
                .name("Steel Bolt M8")
                .description("Stainless steel bolt")
                .materialType(MaterialType.RAW_MATERIAL)
                .unitOfMeasure(UnitOfMeasure.PCE)
                .categoryId(UUID.randomUUID().toString())
                .categoryName("Hardware")
                .supplierId(UUID.randomUUID().toString())
                .supplierName("Acme Fasteners")
                .status(MaterialStatus.ACTIVE)
                .currentStock(50)
                .minimumStock(10)
                .maximumStock(500)
                .reorderPoint(20)
                .safetyStock(5)
                .standardPrice(Money.of("5.50", CurrencyCode.MAD));
    }

    @Test
    @DisplayName("catalogue: material creates with valid attributes and links")
    void material_validAttributes() {
        Material m = validMaterialBuilder().build();

        assertEquals("MAT-2026-0001", m.getCode().getValue());
        assertEquals("Steel Bolt M8", m.getName());
        assertEquals(MaterialType.RAW_MATERIAL, m.getMaterialType());
        assertEquals(UnitOfMeasure.PCE, m.getUnitOfMeasure());
        assertNotNull(m.getCategoryId());
        assertNotNull(m.getSupplierId());
        assertTrue(m.isActive());
        assertFalse(m.isObsolete());
        assertTrue(m.isOrderable());
    }

    @Test
    @DisplayName("obsolescence: obsolete material is not orderable and cannot increase stock")
    void obsolescence_rules() {
        Material m = validMaterialBuilder()
                .status(MaterialStatus.OBSOLETE)
                .obsoletedAt(LocalDateTime.now())
                .obsoletedBy("admin")
                .obsoletedReason("Replaced by M10")
                .build();

        assertFalse(m.isActive());
        assertTrue(m.isObsolete());
        assertFalse(m.isOrderable());
        assertThrows(IllegalStateException.class, () -> m.increaseStock(10));
    }

    @Test
    @DisplayName("stock consistency: minimum stock cannot exceed maximum stock")
    void stockConsistency_minimumExceedsMaximum_throws() {
        assertThrows(IllegalArgumentException.class, () ->
                validMaterialBuilder().minimumStock(200).maximumStock(100).build());
    }

    @Test
    @DisplayName("stock consistency: negative current or available stock throws")
    void stockConsistency_negativeStock_throws() {
        assertThrows(IllegalArgumentException.class, () ->
                validMaterialBuilder().currentStock(-1).build());
        assertThrows(IllegalArgumentException.class, () ->
                validMaterialBuilder().availableStock(-1).build());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    @DisplayName("validation: name is required")
    void validation_nameRequired(String name) {
        assertThrows(IllegalArgumentException.class, () ->
                validMaterialBuilder().name(name).build());
    }

    @Test
    @DisplayName("validation: required fields cannot be null")
    void validation_requiredFields() {
        assertThrows(IllegalArgumentException.class, () ->
                validMaterialBuilder().materialType(null).build());
        assertThrows(IllegalArgumentException.class, () ->
                validMaterialBuilder().unitOfMeasure((UnitOfMeasure) null).build());
        assertThrows(IllegalArgumentException.class, () ->
                validMaterialBuilder().categoryId(null).build());
    }

    @Test
    @DisplayName("equality: materials with same code or id are considered equal")
    void equality_rules() {
        UUID id = UUID.randomUUID();
        Material m1 = validMaterialBuilder().id(id).build();
        Material m2 = validMaterialBuilder().id(id).build();

        assertEquals(m1, m2);
        assertEquals(m1.hashCode(), m2.hashCode());
    }
}
