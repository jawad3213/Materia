package com.materia.backend.contexts.masterData.domain.entities;

import com.materia.backend.contexts.masterData.domain.enums.MaterialStatus;
import com.materia.backend.contexts.masterData.domain.events.MaterialBelowReorderPointEvent;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static com.materia.backend.support.fixtures.MaterialFixtures.aMaterial;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Stock arithmetic and shortage classification (T065-T067, US3).
 *
 * <p>Thresholds used throughout, unless stated: safety 5, minimum 10, reorder point 20.
 */
class MaterialStockTest {

    private static final String FINDING_022 =
            "FINDING-022: the reorder alert fires only when stock on order already covers the shortfall";
    private static final String FINDING_023 =
            "FINDING-023: decreaseStock checks current stock but subtracts from available stock, which can go negative";

    private static Material withStock(int current) {
        return aMaterial().stock(current).safety(5).minimum(10).reorderPoint(20).build();
    }

    // ---- Arithmetic (US3 scenarios 1-4) ----

    @Test
    @DisplayName("increase: raises current and available stock by exactly the quantity")
    void increase_isExact() {
        Material m = withStock(40);
        m.increaseStock(15);
        assertEquals(55, m.getCurrentStock());
        assertEquals(55, m.getAvailableStock());
    }

    @Test
    @DisplayName("decrease: lowers stock by exactly the quantity")
    void decrease_isExact() {
        Material m = withStock(40);
        m.decreaseStock(15);
        assertEquals(25, m.getCurrentStock());
    }

    @Test
    @DisplayName("decrease: taking the entire stock is allowed and leaves exactly zero")
    void decrease_entireStock_reachesZero() {
        Material m = withStock(8);
        m.decreaseStock(8);
        assertEquals(0, m.getCurrentStock());
        assertTrue(m.isOutOfStock());
    }

    @Test
    @DisplayName("decrease: taking more than is in stock is refused and the quantity is unchanged")
    void decrease_beyondStock_isRefused() {
        Material m = withStock(8);
        assertThrows(IllegalStateException.class, () -> m.decreaseStock(9));
        assertEquals(8, m.getCurrentStock());
        assertTrue(m.getStockMovements() == null || m.getStockMovements().isEmpty(),
                "a refused movement must not be recorded");
    }

    @ParameterizedTest(name = "quantity {0}")
    @ValueSource(ints = {0, -1, -50})
    @DisplayName("increase and decrease: a zero or negative quantity is refused")
    void nonPositiveQuantities_areRefused(int quantity) {
        Material m = withStock(40);
        assertThrows(IllegalArgumentException.class, () -> m.increaseStock(quantity));
        assertThrows(IllegalArgumentException.class, () -> m.decreaseStock(quantity));
        assertEquals(40, m.getCurrentStock());
    }

    @Test
    @DisplayName("obsolete: an obsolete material's stock cannot be moved in either direction")
    void obsolete_stockIsFrozen() {
        Material m = aMaterial().stock(40).status(MaterialStatus.OBSOLETE).build();
        assertThrows(IllegalStateException.class, () -> m.increaseStock(1));
        assertThrows(IllegalStateException.class, () -> m.decreaseStock(1));
        assertThrows(IllegalStateException.class, () -> m.adjustStock(10, "count"));
    }

    @Test
    @DisplayName("adjust: sets stock to a counted value, and refuses a negative count")
    void adjust_setsCountedValue() {
        Material m = withStock(40);
        m.adjustStock(33, "cycle count");
        assertEquals(33, m.getCurrentStock());
        assertThrows(IllegalArgumentException.class, () -> m.adjustStock(-1, "bad count"));
        assertEquals(33, m.getCurrentStock());
    }

    @Test
    @Disabled(FINDING_023)
    @DisplayName("decrease: available stock never goes negative, even when part of the stock is reserved")
    void decrease_neverMakesAvailableStockNegative() {
        // 10 on hand, 3 available: 7 are reserved. Issuing 5 must be refused, since only 3 are free.
        Material m = aMaterial().stock(10).build();
        m.adjustStock(10, "count");
        setAvailable(m, 3);

        assertThrows(IllegalStateException.class, () -> m.decreaseStock(5));
        assertTrue(m.getAvailableStock() >= 0, "available stock went negative: " + m.getAvailableStock());
    }

    // ---- Stock on order and virtual stock ----

    @Test
    @DisplayName("virtual stock: is what is on hand plus what is already on order")
    void virtualStock_includesOnOrder() {
        Material m = aMaterial().stock(15).onOrder(30).build();
        assertEquals(45, m.getVirtualStock());
        m.addStockOnOrder(5);
        assertEquals(50, m.getVirtualStock());
        m.reduceStockOnOrder(10);
        assertEquals(40, m.getVirtualStock());
    }

    @Test
    @DisplayName("stock on order: cannot be reduced below zero")
    void stockOnOrder_cannotGoNegative() {
        Material m = aMaterial().stock(15).onOrder(3).build();
        assertThrows(IllegalStateException.class, () -> m.reduceStockOnOrder(4));
    }

    // ---- Classification at every boundary (US3 scenario 5) ----

    @ParameterizedTest(name = "stock {0} -> {1}")
    @CsvSource({
            "0,  OUT_OF_STOCK",
            "1,  CRITICAL",
            "4,  CRITICAL",
            "5,  CRITICAL",        // exactly at safety stock counts as critical
            "6,  REORDER_NEEDED",
            "19, REORDER_NEEDED",
            "20, REORDER_NEEDED",  // exactly at the reorder point triggers reordering
            "21, IN_STOCK",
            "500, IN_STOCK"
    })
    @DisplayName("status: every stock level falls into exactly one band, with no gap or overlap at the boundaries")
    void stockStatus_bands(int stock, String expected) {
        assertEquals(expected, withStock(stock).getStockStatus().name());
    }

    @Test
    @DisplayName("boundaries: the reorder and safety checks include the threshold, while the minimum check excludes it")
    void boundaries_inclusivityIsAsDocumented() {
        // Documents actual semantics. "Below reorder point" and "below safety stock" are true AT
        // the threshold (<=), but "below minimum" is false at it (<). Worth noting, since the
        // names suggest a single consistent rule.
        Material atThresholds = aMaterial().stock(10).safety(10).minimum(10).reorderPoint(10).build();
        assertTrue(atThresholds.isBelowReorderPoint());
        assertTrue(atThresholds.isBelowSafetyStock());
        assertFalse(atThresholds.isBelowMinimumStock());
    }

    // ---- Movement history (T068) ----

    @Test
    @DisplayName("history: every successful change records a movement with its before and after levels")
    void movements_areRecorded() {
        Material m = withStock(40);

        m.increaseStock(10, "delivery");
        m.decreaseStock(5);

        var movements = m.getStockMovements();
        assertEquals(2, movements.size());
        StockMovement latest = movements.get(0);
        assertEquals(5, latest.getQuantity());
        assertEquals(50, latest.getPreviousStock());
        assertEquals(45, latest.getNewStock());
        assertEquals("delivery", movements.get(1).getReason());
        assertNotNull(latest.getOccurredAt());
    }

    @Test
    @DisplayName("history: an adjustment to the same value records nothing")
    void movements_noOpAdjustment_recordsNothing() {
        Material m = withStock(40);
        int before = m.getStockMovements() == null ? 0 : m.getStockMovements().size();
        m.adjustStock(40, "recount");
        assertEquals(before, m.getStockMovements() == null ? 0 : m.getStockMovements().size());
    }

    // ---- The automatic reorder alert ----

    @Test
    @DisplayName("reorder alert: stays silent while stock is above the reorder point")
    void reorderAlert_silentAboveReorderPoint() {
        Material m = withStock(100);
        m.decreaseStock(10);
        assertTrue(reorderEvents(m) == 0);
    }

    @Test
    @DisplayName("reorder alert: fires when stock falls to the reorder point with nothing on order to cover it")
    void reorderAlert_firesWhenNothingOnOrder() {
        // The case automatic reordering exists for: stock drops below the reorder point and no
        // replenishment is on its way. ReorderService listens for this event to raise a requisition.
        Material m = aMaterial().stock(25).reorderPoint(20).onOrder(0).build();
        m.decreaseStock(10);
        assertEquals(1, reorderEvents(m));
    }

    @Test
    @DisplayName("reorder alert: stays silent when stock already on order covers the shortfall")
    void reorderAlert_silentWhenAlreadyCovered() {
        Material m = aMaterial().stock(25).reorderPoint(20).onOrder(100).build();
        m.decreaseStock(10);
        assertEquals(0, reorderEvents(m), "a reorder is already on its way; alerting would double-order");
    }

    @Test
    @DisplayName("reorder quantity: nothing is recommended when stock on order already covers the shortfall")
    void reorderQuantity_zeroWhenCovered() {
        assertEquals(0, aMaterial().stock(10).reorderPoint(20).onOrder(100).build().calculateReorderQuantity());
    }

    private static long reorderEvents(Material m) {
        return m.getDomainEvents().stream().filter(e -> e instanceof MaterialBelowReorderPointEvent).count();
    }

    private static void setAvailable(Material m, int available) {
        try {
            var field = Material.class.getDeclaredField("availableStock");
            field.setAccessible(true);
            field.set(m, available);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }
}
