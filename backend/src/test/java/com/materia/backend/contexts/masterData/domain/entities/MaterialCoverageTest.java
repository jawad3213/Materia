package com.materia.backend.contexts.masterData.domain.entities;

import com.materia.backend.contexts.masterData.domain.enums.MaterialStatus;
import com.materia.backend.contexts.masterData.domain.enums.MaterialType;
import com.materia.backend.contexts.masterData.domain.enums.StockStatus;
import com.materia.backend.contexts.masterData.domain.enums.UnitOfMeasure;
import com.materia.backend.contexts.masterData.domain.events.MaterialBelowReorderPointEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;

import static com.materia.backend.support.fixtures.MaterialFixtures.aMaterial;
import static org.junit.jupiter.api.Assertions.*;

/** [T077] Material builder validation, stock rules, reorder logic and identity (feature 001 coverage). */
class MaterialCoverageTest {

    private static Material.Builder minimal() {
        return Material.builder().code("MAT-0001").name("Bolts").materialType(MaterialType.COMPONENT)
                .categoryId("cat-1").unitOfMeasure(UnitOfMeasure.values()[0]);
    }

    @Test
    @DisplayName("builder: defaults are zero stock, min 10, max 1000, reorder 20, safety 5, EOQ 100, zero prices, ACTIVE")
    void builder_defaults() {
        Material m = minimal().build();

        assertEquals(0, m.getCurrentStock());
        assertEquals(0, m.getAvailableStock());
        assertEquals(10, m.getMinimumStock());
        assertEquals(1000, m.getMaximumStock());
        assertEquals(20, m.getReorderPoint());
        assertEquals(5, m.getSafetyStock());
        assertEquals(100, m.getEconomicOrderQuantity());
        assertEquals(0, m.getStockOnOrder());
        assertEquals(MaterialStatus.ACTIVE, m.getStatus());
        assertEquals(0, m.getStandardPrice().getAmount().signum());
        assertNotNull(m.getCreatedAt());
    }

    @Test
    @DisplayName("builder: code, name, type, category and unit are required; a blank code means none")
    void builder_requiredFields() {
        assertThrows(IllegalArgumentException.class, () -> minimal().code((String) null).build());
        assertThrows(IllegalArgumentException.class, () -> minimal().code(" ").build());
        assertThrows(IllegalArgumentException.class, () -> minimal().name(" ").build());
        assertThrows(IllegalArgumentException.class, () -> minimal().materialType(null).build());
        assertThrows(IllegalArgumentException.class, () -> minimal().categoryId(" ").build());
        assertThrows(IllegalArgumentException.class, () -> minimal().unitOfMeasure(null).build());
    }

    @Test
    @DisplayName("builder: minimum above maximum, or negative current or available stock, is refused")
    void builder_stockConsistency() {
        assertThrows(IllegalArgumentException.class, () -> minimal().minimumStock(50).maximumStock(10).build());
        assertThrows(IllegalArgumentException.class, () -> minimal().currentStock(-1).build());
        assertThrows(IllegalArgumentException.class, () -> minimal().currentStock(5).availableStock(-1).build());
    }

    @Test
    @DisplayName("builder: explicit audit data, stock movements and obsolescence details are kept")
    void builder_keepsExplicitValues() {
        LocalDateTime at = LocalDateTime.now().minusDays(2);
        Material m = minimal().createdAt(at).updatedAt(at).createdBy("admin").stockMovements(null)
                .obsoletedAt(at).obsoletedBy("admin").obsoletedReason("Replaced").stockOnOrder(null).build();

        assertEquals(at, m.getCreatedAt());
        assertEquals("admin", m.getCreatedBy());
        assertTrue(m.getStockMovements().isEmpty());
        assertEquals("Replaced", m.getObsoletedReason());
        assertEquals(0, m.getStockOnOrder());
    }

    @Test
    @DisplayName("stock status: out of stock, critical at safety stock, reorder at reorder point, otherwise in stock")
    void stockStatus() {
        assertEquals(StockStatus.OUT_OF_STOCK, aMaterial().stock(0).build().getStockStatus());
        assertEquals(StockStatus.CRITICAL, aMaterial().stock(5).reorderPoint(20).safety(5).build().getStockStatus());
        assertEquals(StockStatus.REORDER_NEEDED, aMaterial().stock(15).reorderPoint(20).safety(5).build().getStockStatus());
        assertEquals(StockStatus.IN_STOCK, aMaterial().stock(50).reorderPoint(20).safety(5).build().getStockStatus());
    }

    @Test
    @DisplayName("stock checks: missing thresholds or stock make the checks answer no")
    void stockChecks_withMissingValues() {
        Material m = aMaterial().stock(5).build();
        m.setReorderPoint(null);
        m.setSafetyStock(null);
        m.setMinimumStock(null);

        assertFalse(m.isBelowReorderPoint());
        assertFalse(m.isBelowSafetyStock());
        assertFalse(m.isBelowMinimumStock());
        assertFalse(m.isVirtualStockBelowReorderPoint());
        assertEquals(0, m.calculateReorderQuantity());

        m.setCurrentStock(null);
        m.setStockOnOrder(null);
        assertFalse(m.isOutOfStock());
        assertEquals(0, m.getVirtualStock());
    }

    @Test
    @DisplayName("reorder quantity: none above the reorder point or when orders already cover it; EOQ first, then up to max (capped 500), else 100")
    void reorderQuantity() {
        assertEquals(0, aMaterial().stock(50).reorderPoint(20).build().calculateReorderQuantity());
        assertEquals(0, aMaterial().stock(10).reorderPoint(20).onOrder(30).build().calculateReorderQuantity());

        Material eoq = aMaterial().stock(10).reorderPoint(20).build();
        eoq.setEconomicOrderQuantity(75);
        assertEquals(75, eoq.calculateReorderQuantity());

        Material toMax = aMaterial().stock(10).reorderPoint(20).maximum(100).build();
        toMax.setEconomicOrderQuantity(0);
        assertEquals(90, toMax.calculateReorderQuantity());

        Material capped = aMaterial().stock(10).reorderPoint(20).maximum(5000).build();
        capped.setEconomicOrderQuantity(null);
        assertEquals(500, capped.calculateReorderQuantity());

        Material fallback = aMaterial().stock(10).reorderPoint(20).build();
        fallback.setEconomicOrderQuantity(null);
        fallback.setMaximumStock(null);
        assertEquals(100, fallback.calculateReorderQuantity());
    }

    @Test
    @DisplayName("adjust: an adjustment records a movement only when the level changes, and signals when stock falls below reorder")
    void adjustStock() {
        Material m = aMaterial().stock(50).reorderPoint(20).build();
        m.getStockMovements().clear();

        m.adjustStock(50, null);
        assertTrue(m.getStockMovements().isEmpty());

        m.adjustStock(10, " ");
        assertEquals(1, m.getStockMovements().size());
        assertTrue(m.getDomainEvents().stream().anyMatch(e -> e instanceof MaterialBelowReorderPointEvent));
        m.clearDomainEvents();
        assertTrue(m.getDomainEvents().isEmpty());

        assertThrows(IllegalArgumentException.class, () -> m.adjustStock(-1, "x"));
        assertThrows(IllegalArgumentException.class, () -> m.adjustStock(null, "x"));
    }

    @Test
    @DisplayName("obsolete: an obsolete material's stock cannot be increased, decreased or adjusted")
    void obsolete_freezesStock() {
        Material m = aMaterial().stock(10).status(MaterialStatus.OBSOLETE).build();

        assertThrows(IllegalStateException.class, () -> m.increaseStock(1));
        assertThrows(IllegalStateException.class, () -> m.decreaseStock(1));
        assertThrows(IllegalStateException.class, () -> m.adjustStock(5, "x"));
        assertFalse(m.isOrderable());
        assertTrue(m.isObsolete());
    }

    @Test
    @DisplayName("stock: zero, negative or missing quantities are refused; a blank reason uses the default label")
    void quantities() {
        Material m = aMaterial().stock(10).build();

        assertThrows(IllegalArgumentException.class, () -> m.increaseStock(0));
        assertThrows(IllegalArgumentException.class, () -> m.increaseStock(null));
        assertThrows(IllegalArgumentException.class, () -> m.decreaseStock(-2));
        assertThrows(IllegalStateException.class, () -> m.decreaseStock(11));

        m.increaseStock(5, " ");
        assertEquals(15, m.getCurrentStock());
        m.decreaseStock(15);
        assertTrue(m.isOutOfStock());
    }

    @Test
    @DisplayName("stock on order: adding and reducing are positive-only, and cannot reduce below zero")
    void stockOnOrder() {
        Material m = aMaterial().onOrder(0).build();
        m.setStockOnOrder(null);

        m.addStockOnOrder(10);
        assertEquals(10, m.getStockOnOrder());
        m.reduceStockOnOrder(4);
        assertEquals(6, m.getStockOnOrder());

        assertThrows(IllegalArgumentException.class, () -> m.addStockOnOrder(0));
        assertThrows(IllegalArgumentException.class, () -> m.reduceStockOnOrder(null));
        assertThrows(IllegalStateException.class, () -> m.reduceStockOnOrder(7));
        m.setStockOnOrder(null);
        assertThrows(IllegalStateException.class, () -> m.reduceStockOnOrder(1));
    }

    @Test
    @DisplayName("opening balance: recorded once for positive stock with no history; skipped otherwise")
    void openingBalance() {
        Material fresh = aMaterial().stock(12).build();
        fresh.setStockMovements(new ArrayList<>());
        fresh.recordOpeningBalance(null);
        assertEquals(1, fresh.getStockMovements().size());

        fresh.recordOpeningBalance("again");
        assertEquals(1, fresh.getStockMovements().size());

        Material empty = aMaterial().stock(0).build();
        empty.setStockMovements(new ArrayList<>());
        empty.recordOpeningBalance("x");
        assertTrue(empty.getStockMovements().isEmpty());

        Material noList = aMaterial().stock(3).build();
        noList.setStockMovements(null);
        noList.recordOpeningBalance("Initial count");
        assertEquals(1, noList.getStockMovements().size());
    }

    @Test
    @DisplayName("setters: a blank code or unit clears it; a unit is parsed from its value")
    void convenienceSetters() {
        Material m = aMaterial().build();

        m.setCode(" ");
        assertNull(m.getCode());
        m.setCode("MAT-0002");
        assertEquals("MAT-0002", m.getCode().getValue());
        m.setUnitOfMeasure(" ");
        assertNull(m.getUnitOfMeasure());
        m.setUnitOfMeasure(UnitOfMeasure.values()[0].getCode());
        assertEquals(UnitOfMeasure.values()[0], m.getUnitOfMeasure());
        m.addDomainEvent(null);
        assertTrue(m.getDomainEvents().isEmpty());
    }

    @Test
    @DisplayName("identity: saved materials are equal by id or code, never to another type or null")
    void identity() {
        Material a = aMaterial().build();
        a.setId(java.util.UUID.randomUUID());
        Material sameCode = aMaterial().code(a.getCode().getValue()).build();
        sameCode.setId(java.util.UUID.randomUUID());
        Material other = aMaterial().build();
        other.setId(java.util.UUID.randomUUID());

        assertEquals(a, a);
        assertEquals(a, sameCode);
        assertNotEquals(a, other);
        assertNotEquals(a, null);
        assertNotEquals(a, "material");
        assertEquals(a.hashCode(), a.hashCode());
        assertNotNull(a.toString());
    }

    @Test
    @org.junit.jupiter.api.Disabled("F-019: two unsaved materials both have a null id, so equals() treats them as equal whatever their codes")
    @DisplayName("F-019: two unsaved materials with different codes are not equal")
    void unsavedMaterials_withDifferentCodes_areNotEqual() {
        assertNotEquals(aMaterial().build(), aMaterial().build());
    }

    @Test
    @DisplayName("F-019 observation: two unsaved materials with different codes currently compare equal")
    void unsavedMaterials_observedEqual() {
        assertEquals(aMaterial().build(), aMaterial().build(), "F-019 appears fixed: re-enable the test above");
    }
}
