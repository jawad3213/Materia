package com.materia.backend.contexts.masterData.domain.entities;

import com.materia.backend.common.domain.enums.CurrencyCode;
import com.materia.backend.contexts.masterData.domain.enums.MaterialCategoryType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/** [T078] Category hierarchy and statistics, supplier addresses and activation (feature 001 coverage). */
class CategoryAndSupplierCoverageTest {

    private static Category.Builder category(String code) {
        return Category.builder().code(code).name("Category " + code);
    }

    private static Supplier.Builder supplier() {
        return Supplier.builder().code("SUP-1").name("Acme").country("MA");
    }

    // ---- Category ----

    @Test
    @DisplayName("category: a new category is active, level 0, empty; a child gets a path from its code")
    void category_defaults() {
        Category root = category("ROOT").build();
        assertNotNull(root.getId());
        assertEquals(Category.STATUS_ACTIVE, root.getStatus());
        assertEquals(0, root.getLevel());
        assertTrue(root.isRoot());
        assertFalse(root.hasChildren());
        assertEquals(0, root.getDepth());
        assertEquals("Category ROOT", root.getFullPathName());

        Category child = category("KID").parentId("p-1").parentCode("ROOT").build();
        assertEquals("/KID/", child.getPath());
        assertFalse(child.isRoot());
        assertEquals("ROOT / Category KID", child.getFullPathName());
        child.setParentCode(null);
        assertEquals("Category KID", child.getFullPathName());
    }

    @Test
    @DisplayName("category: code and name are required, at build and on change")
    void category_requiredFields() {
        assertThrows(IllegalArgumentException.class, () -> Category.builder().name("x").build());
        assertThrows(IllegalArgumentException.class, () -> Category.builder().code("X").name(" ").build());
        Category c = category("C").build();
        assertThrows(IllegalArgumentException.class, () -> c.setCode(" "));
        assertThrows(IllegalArgumentException.class, () -> c.setName(null));
    }

    @Test
    @DisplayName("category: children are added once, removed, and keep the sub-category count and total in step")
    void category_children() {
        Category c = category("C").build();
        c.setChildrenIds(null);

        c.addChild("k1");
        c.addChild("k1");
        c.addChild("k2");
        assertEquals(2, c.getSubCategoryCount());
        assertEquals(2, c.getTotalItems());
        assertTrue(c.hasChildren());

        c.removeChild("k1");
        assertEquals(1, c.getSubCategoryCount());
        assertThrows(IllegalArgumentException.class, () -> c.addChild(""));
        assertThrows(IllegalArgumentException.class, () -> c.removeChild(null));
    }

    @Test
    @DisplayName("category: material counts never go below zero; statistics and setters keep the total consistent")
    void category_statistics() {
        Category c = category("C").build();
        c.decrementMaterialCount();
        assertEquals(0, c.getMaterialCount());

        c.setMaterialCount(null);
        c.incrementMaterialCount();
        c.incrementMaterialCount();
        c.decrementMaterialCount();
        assertEquals(1, c.getMaterialCount());

        c.updateStatistics(4, 3);
        assertEquals(7, c.getTotalItems());
        c.setSubCategoryCount(null);
        assertEquals(4, c.getTotalItems());
        c.setTotalItems(null);
        assertEquals(0, c.getTotalItems());
        c.setLevel(null);
        assertEquals(0, c.getLevel());
    }

    @Test
    @DisplayName("category: depth counts the levels in the path")
    void category_depth() {
        Category c = category("C").build();
        c.setPath("/A/B/C/");
        assertEquals(3, c.getDepth());
        c.setPath("");
        assertEquals(0, c.getDepth());
    }

    @Test
    @DisplayName("category: a copy is equal and independent; categories are equal by id only")
    void category_copyAndIdentity() {
        LocalDateTime at = LocalDateTime.now().minusDays(1);
        Category c = category("C").categoryType(MaterialCategoryType.MATERIAL).childrenIds(List.of("k"))
                .createdAt(at).updatedAt(at).createdBy("admin").build();
        Category copy = c.copy();

        assertEquals(c, copy);
        copy.addChild("k2");
        assertEquals(1, c.getChildrenIds().size());
        assertNotEquals(c, category("C").build());
        assertNotEquals(c, null);
        assertNotEquals(c, "C");
        assertEquals(c.hashCode(), copy.hashCode());
        assertTrue(c.toString().contains("C"));
        assertEquals("admin", c.getCreatedBy());
    }

    // ---- Supplier ----

    @Test
    @DisplayName("supplier: a new supplier is active in MAD; code, name and country are required")
    void supplier_defaults() {
        Supplier s = supplier().currencyCode(null).build();
        assertTrue(s.isActive());
        assertEquals(CurrencyCode.MAD, s.getCurrencyCode());

        assertThrows(IllegalArgumentException.class, () -> Supplier.builder().name("A").country("MA").build());
        assertThrows(IllegalArgumentException.class, () -> Supplier.builder().code("S").name(" ").country("MA").build());
        assertThrows(IllegalArgumentException.class, () -> Supplier.builder().code("S").name("A").build());
    }

    @Test
    @DisplayName("supplier: activation toggles status; code, name and currency cannot be blanked")
    void supplier_changes() {
        Supplier s = supplier().createdBy("admin").build();

        s.deactivate();
        assertFalse(s.isActive());
        s.activate();
        assertTrue(s.isActive());
        assertThrows(IllegalArgumentException.class, () -> s.setCode(" "));
        assertThrows(IllegalArgumentException.class, () -> s.setName(""));
        assertThrows(IllegalArgumentException.class, () -> s.setCurrencyCode(null));
        s.setCurrencyCode(CurrencyCode.EUR);
        assertEquals(CurrencyCode.EUR, s.getCurrencyCode());
        assertEquals("admin", s.getCreatedBy());
    }

    @Test
    @DisplayName("supplier: the full address joins the parts that are present, comma-separated, postcode last")
    void supplier_fullAddress() {
        assertEquals("MA", supplier().build().getFullAddress());
        assertEquals("1 Rue X, Casablanca, MA 20000",
                supplier().address("1 Rue X").city("Casablanca").postalCode("20000").build().getFullAddress());
        assertEquals("Rabat, MA", supplier().city("Rabat").build().getFullAddress());

        Supplier noCountry = supplier().build();
        noCountry.setCountry(null);
        assertEquals("", noCountry.getFullAddress());
        noCountry.setPostalCode("20000");
        assertEquals("20000", noCountry.getFullAddress());
    }

    @Test
    @DisplayName("supplier: suppliers are equal by id only")
    void supplier_identity() {
        UUID id = UUID.randomUUID();
        Supplier a = supplier().id(id).build();

        assertEquals(a, supplier().id(id).build());
        assertNotEquals(a, supplier().build());
        assertNotEquals(a, null);
        assertNotEquals(a, "SUP-1");
        assertEquals(a, a);
        assertEquals(a.hashCode(), supplier().id(id).build().hashCode());
        assertNotNull(a.toString());
    }
}
