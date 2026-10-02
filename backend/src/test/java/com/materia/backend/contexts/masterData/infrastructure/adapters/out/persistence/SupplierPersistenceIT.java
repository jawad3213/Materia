package com.materia.backend.contexts.masterData.infrastructure.adapters.out.persistence;

import com.materia.backend.common.domain.enums.CurrencyCode;
import com.materia.backend.contexts.masterData.domain.entities.Supplier;
import com.materia.backend.contexts.masterData.domain.ports.out.SupplierRepository;
import com.materia.backend.support.AbstractIntegrationTest;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Optional;

import static com.materia.backend.support.fixtures.CatalogueFixtures.aSupplier;
import static com.materia.backend.support.fixtures.CatalogueFixtures.uniqueSupplierCode;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Supplier persistence against real PostgreSQL schema (T103, US5).
 */
class SupplierPersistenceIT extends AbstractIntegrationTest {

    @Autowired private SupplierRepository suppliers;
    @PersistenceContext private EntityManager em;

    private void flushAndClear() {
        em.flush();
        em.clear();
    }

    @Test
    @DisplayName("save and findById: supplier survives round-trip with all attributes intact")
    void supplier_roundTrip() {
        Supplier saved = suppliers.save(aSupplier().name("Global Fasteners").country("Morocco").build());
        flushAndClear();

        Optional<Supplier> reloaded = suppliers.findById(saved.getId());
        assertTrue(reloaded.isPresent());
        assertEquals(saved.getCode(), reloaded.get().getCode());
        assertEquals("Global Fasteners", reloaded.get().getName());
        assertEquals("Morocco", reloaded.get().getCountry());
        assertEquals(CurrencyCode.MAD, reloaded.get().getCurrencyCode());
        assertEquals(Supplier.STATUS_ACTIVE, reloaded.get().getStatus());
    }

    @Test
    @DisplayName("uniqueness: duplicate supplier code is rejected by the database")
    void supplier_duplicateCode_rejected() {
        String code = uniqueSupplierCode();
        suppliers.save(aSupplier().code(code).build());
        flushAndClear();

        assertThrows(DataIntegrityViolationException.class, () -> {
            suppliers.save(aSupplier().code(code).build());
            flushAndClear();
        });
    }

    @Test
    @DisplayName("search: keyword search finds matching supplier")
    void supplier_search() {
        String code = uniqueSupplierCode();
        suppliers.save(aSupplier().code(code).name("UniqueSpecialtyParts Ltd").build());
        flushAndClear();

        List<Supplier> results = suppliers.search("UniqueSpecialtyParts");
        assertFalse(results.isEmpty());
        assertTrue(results.stream().anyMatch(s -> s.getCode().equals(code)));
    }
}
