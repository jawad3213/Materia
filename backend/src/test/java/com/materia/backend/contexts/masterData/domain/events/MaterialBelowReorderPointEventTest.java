package com.materia.backend.contexts.masterData.domain.events;

import com.materia.backend.contexts.masterData.domain.enums.StockStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/** [T076] The below-reorder-point signal: its content, criticality and suggested reorder quantity (feature 001 coverage). */
class MaterialBelowReorderPointEventTest {

    private static MaterialBelowReorderPointEvent event(int stock, int reorderPoint, int safety, StockStatus status) {
        return new MaterialBelowReorderPointEvent(UUID.randomUUID(), "MAT-0001", "Bolts", stock, reorderPoint, safety,
                "sup-1", status);
    }

    @Test
    @DisplayName("event: carries the material, stock levels, supplier and status it was raised for")
    void content() {
        MaterialBelowReorderPointEvent e = event(15, 20, 5, StockStatus.REORDER_NEEDED);

        assertNotNull(e.getEntityId());
        assertNotNull(e.getOccurredAt());
        assertEquals("MATERIAL_BELOW_REORDER_POINT", e.getEventType());
        assertEquals(1, e.getVersion());
        assertEquals("MAT-0001", e.getMaterialCode());
        assertEquals("Bolts", e.getMaterialName());
        assertEquals(15, e.getCurrentStock());
        assertEquals(20, e.getReorderPoint());
        assertEquals(5, e.getSafetyStock());
        assertEquals("sup-1", e.getSupplierId());
        assertEquals(StockStatus.REORDER_NEEDED, e.getStockStatus());
    }

    @Test
    @DisplayName("event: only critical and out-of-stock levels are critical")
    void criticality() {
        assertTrue(event(2, 20, 5, StockStatus.CRITICAL).isCritical());
        assertTrue(event(0, 20, 5, StockStatus.OUT_OF_STOCK).isCritical());
        assertFalse(event(15, 20, 5, StockStatus.REORDER_NEEDED).isCritical());
        assertFalse(event(25, 20, 5, StockStatus.IN_STOCK).isCritical());
    }

    @Test
    @DisplayName("event: reorder up to the reorder point, plus the safety stock when below it")
    void reorderQuantity() {
        assertEquals(5, event(15, 20, 5, StockStatus.REORDER_NEEDED).getReorderQuantity());
        assertEquals(22, event(3, 20, 5, StockStatus.CRITICAL).getReorderQuantity());
    }
}
