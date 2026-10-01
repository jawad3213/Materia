package com.materia.backend.contexts.masterData.application.services;

import com.materia.backend.common.application.exceptions.BusinessException;
import com.materia.backend.contexts.masterData.application.mappers.MaterialMapper;
import com.materia.backend.contexts.masterData.domain.entities.Material;
import com.materia.backend.contexts.masterData.domain.enums.MaterialStatus;
import com.materia.backend.contexts.masterData.domain.exceptions.InsufficientStockException;
import com.materia.backend.contexts.masterData.domain.exceptions.MaterialNotFoundException;
import com.materia.backend.contexts.masterData.domain.ports.out.CategoryRepository;
import com.materia.backend.contexts.masterData.domain.ports.out.MaterialRepository;
import com.materia.backend.contexts.masterData.domain.ports.out.SupplierRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.materia.backend.support.fixtures.MaterialFixtures.aMaterial;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** The stock paths of MaterialService (T072). Catalogue paths are covered under US5. */
@ExtendWith(MockitoExtension.class)
class MaterialServiceStockTest {

    @Mock private MaterialRepository materials;
    @Mock private CategoryRepository categories;
    @Mock private SupplierRepository suppliers;
    @Mock private MaterialCodeGeneratorService codeGenerator;
    @Mock private ReorderService reorderService;
    @Mock private ApplicationEventPublisher events;

    private MaterialService service;

    @BeforeEach
    void setUp() {
        service = new MaterialService(materials, categories, suppliers, new MaterialMapper(), codeGenerator,
                new MaterialStockDomainService(), reorderService, events);
        lenient().when(materials.save(any())).thenAnswer(i -> i.getArgument(0));
    }

    private Material stored(Material m) {
        when(materials.findById(m.getId())).thenReturn(Optional.of(m));
        return m;
    }

    @Test
    @DisplayName("increase: persists the new level")
    void increase_persists() {
        Material m = stored(aMaterial().stock(10).build());

        service.increaseStock(m.getId(), 5);

        assertEquals(15, m.getCurrentStock());
        verify(materials).save(m);
    }

    @Test
    @DisplayName("decrease: taking more than is in stock is refused with a specific error, and nothing is saved")
    void decrease_insufficient_isRefusedAndNotSaved() {
        Material m = stored(aMaterial().stock(3).build());

        assertThrows(InsufficientStockException.class, () -> service.decreaseStock(m.getId(), 4));
        assertEquals(3, m.getCurrentStock());
        verify(materials, never()).save(any());
    }

    @Test
    @DisplayName("decrease and increase: an obsolete material is refused with MATERIAL_OBSOLETE")
    void obsolete_isRefusedWithSpecificCode() {
        Material m = stored(aMaterial().stock(10).status(MaterialStatus.OBSOLETE).build());

        BusinessException up = assertThrows(BusinessException.class, () -> service.increaseStock(m.getId(), 1));
        BusinessException down = assertThrows(BusinessException.class, () -> service.decreaseStock(m.getId(), 1));
        assertEquals("MATERIAL_OBSOLETE", up.getErrorCode());
        assertEquals("MATERIAL_OBSOLETE", down.getErrorCode());
        verify(materials, never()).save(any());
    }

    @Test
    @DisplayName("stock operations: a zero or negative quantity is refused before anything changes")
    void nonPositive_isRefused() {
        Material m = stored(aMaterial().stock(10).build());

        assertThrows(RuntimeException.class, () -> service.increaseStock(m.getId(), 0));
        assertThrows(RuntimeException.class, () -> service.decreaseStock(m.getId(), -2));
        assertEquals(10, m.getCurrentStock());
        verify(materials, never()).save(any());
    }

    @Test
    @DisplayName("stock operations: an unknown material is a not-found")
    void unknownMaterial_isNotFound() {
        UUID unknown = UUID.randomUUID();
        when(materials.findById(unknown)).thenReturn(Optional.empty());

        assertThrows(MaterialNotFoundException.class, () -> service.increaseStock(unknown, 1));
        assertThrows(MaterialNotFoundException.class, () -> service.decreaseStock(unknown, 1));
    }

    @Test
    @DisplayName("decrease: raised domain events are published and then cleared, so none is published twice")
    void decrease_publishesAndClearsEvents() {
        // A stock level whose decrease raises an event under the current rule (see FINDING-022).
        Material m = stored(aMaterial().stock(25).reorderPoint(20).onOrder(100).build());

        service.decreaseStock(m.getId(), 10);

        verify(events, atLeastOnce()).publishEvent(any(Object.class));
        assertTrue(m.getDomainEvents().isEmpty(), "events must be cleared after publishing");
    }

    @Test
    @DisplayName("shortage lists: only active materials are considered")
    void shortageLists_considerActiveOnly() {
        when(materials.findByStatus(MaterialStatus.ACTIVE)).thenReturn(List.of(aMaterial().stock(0).build()));

        assertEquals(1, service.getOutOfStockMaterials().size());
        assertEquals(0, service.getCriticalMaterials().size());
        verify(materials, atLeastOnce()).findByStatus(MaterialStatus.ACTIVE);
        verify(materials, never()).findByStatus(MaterialStatus.INACTIVE);
    }
}
