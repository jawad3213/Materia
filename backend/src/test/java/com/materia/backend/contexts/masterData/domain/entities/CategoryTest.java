package com.materia.backend.contexts.masterData.domain.entities;

import com.materia.backend.contexts.masterData.domain.enums.MaterialCategoryType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Domain entity tests for Category (T087, US5).
 */
class CategoryTest {

    private Category.Builder validCategoryBuilder() {
        return Category.builder()
                .code("CAT-001")
                .name("Electronics")
                .description("Electronic components")
                .categoryType(MaterialCategoryType.ELECTRONIC_CAT);
    }

    @Test
    @DisplayName("root category: category with no parent is identified as root")
    void rootCategory_hasNoParent() {
        Category root = validCategoryBuilder()
                .parentId(null)
                .level(0)
                .build();

        assertTrue(root.isRoot());
        assertEquals(0, root.getLevel());
        assertEquals("Electronics", root.getFullPathName());
    }

    @Test
    @DisplayName("sub-category: category with parent is not root and reflects hierarchy")
    void subCategory_reflectsHierarchy() {
        Category sub = validCategoryBuilder()
                .code("CAT-002")
                .name("Microcontrollers")
                .parentId(UUID.randomUUID().toString())
                .parentCode("CAT-001")
                .level(1)
                .path("/CAT-001/CAT-002/")
                .build();

        assertFalse(sub.isRoot());
        assertEquals(1, sub.getLevel());
        assertEquals("CAT-001 / Microcontrollers", sub.getFullPathName());
        assertEquals(2, sub.getDepth());
    }

    @Test
    @DisplayName("children: adding and removing child categories updates counts")
    void children_updatesCounts() {
        Category cat = validCategoryBuilder().build();
        assertEquals(0, cat.getChildrenIds().size());
        assertEquals(0, cat.getSubCategoryCount());

        String childId1 = UUID.randomUUID().toString();
        String childId2 = UUID.randomUUID().toString();

        cat.addChild(childId1);
        cat.addChild(childId2);
        assertTrue(cat.hasChildren());
        assertEquals(2, cat.getChildrenIds().size());
        assertEquals(2, cat.getSubCategoryCount());

        // Duplicate add is idempotent
        cat.addChild(childId1);
        assertEquals(2, cat.getChildrenIds().size());

        cat.removeChild(childId1);
        assertEquals(1, cat.getChildrenIds().size());
        assertEquals(1, cat.getSubCategoryCount());
    }

    @Test
    @DisplayName("children: adding null or empty child ID throws exception")
    void addChild_invalidId_throws() {
        Category cat = validCategoryBuilder().build();
        assertThrows(IllegalArgumentException.class, () -> cat.addChild(null));
        assertThrows(IllegalArgumentException.class, () -> cat.addChild(""));
        assertThrows(IllegalArgumentException.class, () -> cat.removeChild(null));
        assertThrows(IllegalArgumentException.class, () -> cat.removeChild(""));
    }

    @Test
    @DisplayName("status: activation and deactivation toggle status")
    void status_toggle() {
        Category cat = validCategoryBuilder().build();
        assertTrue(cat.isActive());

        cat.deactivate();
        assertFalse(cat.isActive());
        assertEquals(Category.STATUS_INACTIVE, cat.getStatus());

        cat.activate();
        assertTrue(cat.isActive());
        assertEquals(Category.STATUS_ACTIVE, cat.getStatus());
    }

    @Test
    @DisplayName("statistics: increment and decrement material count")
    void statistics_materialCount() {
        Category cat = validCategoryBuilder().build();
        assertEquals(0, cat.getMaterialCount());

        cat.incrementMaterialCount();
        cat.incrementMaterialCount();
        assertEquals(2, cat.getMaterialCount());
        assertEquals(2, cat.getTotalItems());

        cat.decrementMaterialCount();
        assertEquals(1, cat.getMaterialCount());
        assertEquals(1, cat.getTotalItems());

        cat.updateStatistics(10, 3);
        assertEquals(10, cat.getMaterialCount());
        assertEquals(3, cat.getSubCategoryCount());
        assertEquals(13, cat.getTotalItems());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    @DisplayName("validation: code is required")
    void validation_codeRequired(String code) {
        assertThrows(IllegalArgumentException.class, () ->
                Category.builder().code(code).name("Valid").build());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    @DisplayName("validation: name is required")
    void validation_nameRequired(String name) {
        assertThrows(IllegalArgumentException.class, () ->
                Category.builder().code("CAT-001").name(name).build());
    }
}
