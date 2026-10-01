package com.materia.backend.contexts.masterData.infrastructure.adapters.out.persistence;

import com.materia.backend.contexts.masterData.domain.entities.Material;
import com.materia.backend.contexts.masterData.domain.enums.MaterialStatus;
import com.materia.backend.contexts.masterData.domain.ports.out.MaterialRepository;
import com.materia.backend.support.AbstractIntegrationTest;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;

import static com.materia.backend.support.fixtures.MaterialFixtures.aMaterial;
import static com.materia.backend.support.fixtures.MaterialFixtures.uniqueCode;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Material persistence against real PostgreSQL. Covers stock history (T076, US3) and the
 * catalogue record itself (T104, US5), which share one adapter and one table.
 *
 * <p>Stock history is asserted by counting rows in {@code material_stock_movements} directly,
 * because the domain object does not load its movements back (FINDING-026). Checking the
 * domain object alone could not distinguish "never written" from "written, then destroyed".
 */
class MaterialPersistenceIT extends AbstractIntegrationTest {

    private static final String FINDING_026 =
            "FINDING-026: each save destroys earlier stock movements, so only the latest survives";
    private static final String FINDING_027 =
            "FINDING-027: stockOnOrder has no column and is never persisted";

    @Autowired private MaterialRepository materials;
    @Autowired private JdbcTemplate jdbc;
    @PersistenceContext private EntityManager em;

    private void flushAndClear() {
        em.flush();
        em.clear();
    }

    private int movementRows(Material m) {
        return jdbc.queryForObject("select count(*) from public.material_stock_movements where material_id = ?",
                Integer.class, m.getId());
    }

    // ---- Stock history (T076) ----

    @Test
    @DisplayName("stock: a stock change is written to the movement history")
    void stockMovement_isWritten() {
        Material m = materials.save(aMaterial().stock(40).build());
        flushAndClear();

        Material loaded = materials.findById(m.getId()).orElseThrow();
        loaded.increaseStock(10, "delivery");
        materials.save(loaded);
        flushAndClear();

        assertEquals(1, movementRows(m));
        assertEquals(50, materials.findById(m.getId()).orElseThrow().getCurrentStock());
    }

    @Test
    @Disabled(FINDING_026)
    @DisplayName("stock: the movement history accumulates across separate changes, and nothing earlier is lost")
    void stockMovements_accumulateAcrossSaves() {
        Material m = materials.save(aMaterial().stock(40).build());
        flushAndClear();

        Material first = materials.findById(m.getId()).orElseThrow();
        first.increaseStock(10, "delivery");
        materials.save(first);
        flushAndClear();

        Material second = materials.findById(m.getId()).orElseThrow();
        second.decreaseStock(5);
        materials.save(second);
        flushAndClear();

        assertEquals(2, movementRows(m), "the earlier movement must survive the later save");
    }

    @Test
    @Disabled(FINDING_027)
    @DisplayName("stock: stock on order persists, so virtual stock is still correct after a reload")
    void stockOnOrder_persists() {
        Material m = materials.save(aMaterial().stock(12).onOrder(30).build());
        flushAndClear();

        Material read = materials.findById(m.getId()).orElseThrow();
        assertEquals(30, read.getStockOnOrder());
        assertEquals(42, read.getVirtualStock());
    }

    // ---- Catalogue record (T104) ----

    @Test
    @DisplayName("catalogue: a saved material reads back with its code, status, thresholds and price intact")
    void material_roundTrip() {
        Material m = materials.save(aMaterial().stock(7).safety(3).minimum(6).reorderPoint(9).price("4.25").build());
        flushAndClear();

        Material read = materials.findById(m.getId()).orElseThrow();
        assertEquals(m.getCode().getValue(), read.getCode().getValue());
        assertEquals(MaterialStatus.ACTIVE, read.getStatus());
        assertEquals(7, read.getCurrentStock());
        assertEquals(9, read.getReorderPoint());
        assertEquals(3, read.getSafetyStock());
        assertEquals(0, new BigDecimal("4.25").compareTo(read.getStandardPrice().getAmount()));
    }

    @Test
    @DisplayName("catalogue: a second material with the same code is rejected by the database")
    void material_duplicateCode_isRejected() {
        String code = uniqueCode();
        materials.save(aMaterial().code(code).build());
        flushAndClear();

        // The adapter may flush on save, so the save and the flush are asserted together.
        assertThrows(RuntimeException.class, () -> {
            materials.save(aMaterial().code(code).build());
            flushAndClear();
        });
    }

    @Test
    @DisplayName("catalogue: lookup by code finds the material, and an unknown code finds nothing")
    void material_findByCode() {
        Material m = materials.save(aMaterial().build());
        flushAndClear();

        assertTrue(materials.findByCode(m.getCode().getValue()).isPresent());
        assertTrue(materials.findByCode("MAT-2026-9998").isEmpty());
    }

    @Test
    @DisplayName("catalogue: finding by status returns only materials in that status")
    void material_findByStatus() {
        materials.save(aMaterial().status(MaterialStatus.BLOCKED).build());
        flushAndClear();

        var blocked = materials.findByStatus(MaterialStatus.BLOCKED);
        assertFalse(blocked.isEmpty());
        assertTrue(blocked.stream().allMatch(x -> x.getStatus() == MaterialStatus.BLOCKED));
    }
}
