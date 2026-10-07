package com.materia.backend.contexts.masterData.application.services;

import com.materia.backend.contexts.masterData.application.dtos.material.StockMovement;
import com.materia.backend.contexts.masterData.domain.entities.Material;
import com.materia.backend.contexts.masterData.domain.ports.out.MaterialRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Optional;
import java.util.UUID;

import static com.materia.backend.support.fixtures.MaterialFixtures.aMaterial;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** Material lookups and stock movements offered to the other contexts. */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MaterialInventoryServiceTest {

    @Mock private MaterialRepository materials;

    private MaterialInventoryService service;

    @BeforeEach
    void setUp() {
        service = new MaterialInventoryService(materials);
        when(materials.save(any(Material.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private Material stored(Material material) {
        material.setId(UUID.randomUUID());
        when(materials.findById(material.getId())).thenReturn(Optional.of(material));
        when(materials.findByCode(material.getCode().getValue())).thenReturn(Optional.of(material));
        return material;
    }

    @Test
    @DisplayName("lookup: a material is described by id or code; unknown and missing keys find nothing")
    void lookup() {
        Material m = stored(aMaterial().price("2.00").build());

        assertEquals(m.getCode().getValue(), service.findMaterial(m.getId()).orElseThrow().code());
        assertEquals(m.getId(), service.findMaterialByCode(m.getCode().getValue()).orElseThrow().id());
        assertTrue(service.findMaterial(UUID.randomUUID()).isEmpty());
        assertTrue(service.findMaterial(null).isEmpty());
        assertTrue(service.findMaterialByCode(null).isEmpty());
    }

    @Test
    @DisplayName("on order: added by id, falling back to the code; nothing for an unknown material or a zero quantity")
    void addStockOnOrder() {
        Material m = stored(aMaterial().onOrder(0).build());

        assertTrue(service.addStockOnOrder(UUID.randomUUID(), m.getCode().getValue(), 5, "user-1"));
        assertEquals(5, m.getStockOnOrder());
        assertEquals("user-1", m.getUpdatedBy());

        assertFalse(service.addStockOnOrder(null, "UNKNOWN", 5, null));
        assertFalse(service.addStockOnOrder(m.getId(), null, 0, null));
        verify(materials, times(1)).save(m);
    }

    @Test
    @DisplayName("on order: a release never takes the stock on order below zero")
    void releaseStockOnOrder() {
        Material m = stored(aMaterial().onOrder(3).build());

        assertEquals(3, service.releaseStockOnOrder(m.getId(), null, 10, null));
        assertEquals(0, m.getStockOnOrder());
        assertEquals(0, service.releaseStockOnOrder(m.getId(), null, 10, null));
        verify(materials, times(1)).save(m);
    }

    @Test
    @DisplayName("receipt: accepted goods enter stock and everything received leaves the stock on order")
    void receiveGoods() {
        Material m = stored(aMaterial().stock(10).onOrder(8).build());

        StockMovement movement = service.receiveGoods(m.getId(), null, 6, 8, "Goods receipt GR-1", "user-1")
                .orElseThrow();

        assertEquals(10, movement.stockBefore());
        assertEquals(16, movement.stockAfter());
        assertEquals(0, m.getStockOnOrder());
    }

    @Test
    @DisplayName("receipt: the material is found by id when one is given, without falling back to the code")
    void receiveGoods_strictLookup() {
        Material m = stored(aMaterial().build());

        assertTrue(service.receiveGoods(UUID.randomUUID(), m.getCode().getValue(), 1, 1, "r", null).isEmpty());
        assertTrue(service.receiveGoods(null, m.getCode().getValue(), 1, 1, "r", null).isPresent());
    }
}
