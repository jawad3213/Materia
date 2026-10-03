package com.materia.backend.contexts.masterData.application.services;

import com.materia.backend.common.application.PageResponse;
import com.materia.backend.common.application.exceptions.BusinessException;
import com.materia.backend.common.application.exceptions.ValidationException;
import com.materia.backend.contexts.masterData.application.dtos.material.MaterialFilterCriteria;
import com.materia.backend.contexts.masterData.application.dtos.material.MaterialSearchCriteria;
import com.materia.backend.contexts.masterData.application.dtos.material.UpdateMaterialInput;
import com.materia.backend.contexts.masterData.application.mappers.MaterialMapper;
import com.materia.backend.contexts.masterData.domain.entities.Category;
import com.materia.backend.contexts.masterData.domain.entities.Material;
import com.materia.backend.contexts.masterData.domain.entities.Supplier;
import com.materia.backend.contexts.masterData.domain.enums.MaterialStatus;
import com.materia.backend.contexts.masterData.domain.enums.MaterialType;
import com.materia.backend.contexts.masterData.domain.exceptions.InsufficientStockException;
import com.materia.backend.contexts.masterData.domain.ports.out.CategoryRepository;
import com.materia.backend.contexts.masterData.domain.ports.out.MaterialRepository;
import com.materia.backend.contexts.masterData.domain.ports.out.SupplierRepository;
import com.materia.backend.contexts.masterData.domain.valueObjects.MaterialSearchFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.materia.backend.support.fixtures.CatalogueFixtures.aCategory;
import static com.materia.backend.support.fixtures.CatalogueFixtures.aSupplier;
import static com.materia.backend.support.fixtures.MaterialFixtures.aMaterial;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/** [T079] MaterialService paths not covered by MaterialServiceTest/MaterialServiceStockTest (feature 001 coverage). */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MaterialServiceCoverageTest {

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
        when(materials.save(any(Material.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private Material stored(Material m) {
        m.setId(UUID.randomUUID());
        when(materials.findById(m.getId())).thenReturn(Optional.of(m));
        return m;
    }

    @Test
    @DisplayName("update: a new category and supplier are resolved and their names copied onto the material")
    void update_relinksCategoryAndSupplier() {
        Material m = stored(aMaterial().build());
        Category category = aCategory().build();
        Supplier supplier = aSupplier().build();
        when(categories.findById(category.getId())).thenReturn(Optional.of(category));
        when(suppliers.findById(supplier.getId())).thenReturn(Optional.of(supplier));
        UpdateMaterialInput input = new UpdateMaterialInput();
        input.setCategoryId(category.getId().toString());
        input.setSupplierId(supplier.getId().toString());

        service.update(m.getId(), input);

        assertEquals(category.getId().toString(), m.getCategoryId());
        assertEquals(category.getName(), m.getCategoryName());
        assertEquals(supplier.getId().toString(), m.getSupplierId());
        assertEquals(supplier.getName(), m.getSupplierName());
    }

    @Test
    @DisplayName("update: leaving category and supplier out keeps the current links")
    void update_withoutLinks_keepsThem() {
        Material m = stored(aMaterial().category("cat-9").supplier("sup-9").build());

        service.update(m.getId(), new UpdateMaterialInput());

        assertEquals("cat-9", m.getCategoryId());
        assertEquals("sup-9", m.getSupplierId());
        verifyNoInteractions(categories, suppliers);
    }

    @Test
    @DisplayName("decrease: an obsolete material, too little stock, or a non-positive quantity is refused")
    void decrease_refusals() {
        Material obsolete = stored(aMaterial().stock(10).status(MaterialStatus.OBSOLETE).build());
        Material low = stored(aMaterial().stock(2).build());
        Material noStock = stored(aMaterial().stock(0).build());
        noStock.setCurrentStock(null);

        assertThrows(BusinessException.class, () -> service.decreaseStock(obsolete.getId(), 1));
        assertThrows(InsufficientStockException.class, () -> service.decreaseStock(low.getId(), 3));
        assertThrows(InsufficientStockException.class, () -> service.decreaseStock(noStock.getId(), 1));
        assertThrows(ValidationException.class, () -> service.decreaseStock(low.getId(), 0));
        verify(materials, never()).save(any());
    }

    @Test
    @DisplayName("decrease: dropping below the reorder point publishes the material's below-reorder event")
    void decrease_publishesDomainEvents() {
        Material m = stored(aMaterial().stock(25).reorderPoint(20).build());

        service.decreaseStock(m.getId(), 10);

        verify(events, atLeastOnce()).publishEvent(any(Object.class));
    }

    @Test
    @DisplayName("recommendation: above the reorder point the standard EOQ is suggested, or 100 when none is set")
    void recommendation_aboveReorderPoint_suggestsEoq() {
        Material withEoq = stored(aMaterial().stock(50).reorderPoint(20).price("2.00").build());
        withEoq.setEconomicOrderQuantity(30);
        var out = service.getReorderRecommendation(withEoq.getId());
        assertEquals(30, out.getRecommendedQuantity());
        assertEquals("Standard EOQ quantity", out.getReason());

        Material bare = stored(aMaterial().stock(50).reorderPoint(20).build());
        bare.setEconomicOrderQuantity(null);
        bare.setStandardPrice(null);
        var fallback = service.getReorderRecommendation(bare.getId());
        assertEquals(100, fallback.getRecommendedQuantity());
        assertNull(fallback.getEstimatedCost());
        assertEquals("MAD", fallback.getCurrencyCode());
    }

    @Test
    @DisplayName("recommendation: below the reorder point the domain recommendation is used")
    void recommendation_belowReorderPoint_usesDomain() {
        Material m = stored(aMaterial().stock(2).reorderPoint(20).safety(5).price("1.00").build());

        var out = service.getReorderRecommendation(m.getId());

        assertTrue(out.getIsUrgent());
        assertNotNull(out.getEstimatedCost());
    }

    @Test
    @DisplayName("manual reorder: the reported quantity is the requested one, else the EOQ, else 100")
    void manualReorder_reportedQuantity() {
        Material m = stored(aMaterial().build());
        when(reorderService.triggerManualReorder(any(), any(), any())).thenReturn("req-7");

        assertEquals(12, service.triggerReorder(m.getId(), 12, "x").getQuantity());
        m.setEconomicOrderQuantity(40);
        assertEquals(40, service.triggerReorder(m.getId(), 0, null).getQuantity());
        m.setEconomicOrderQuantity(null);
        assertEquals(100, service.triggerReorder(m.getId(), null, null).getQuantity());
        assertEquals("req-7", service.triggerReorder(m.getId(), null, null).getRequisitionId());
    }

    @Test
    @DisplayName("search and filter: status and type are parsed when given and omitted when blank")
    void searchAndFilter_parseOptionalEnums() {
        when(materials.searchAdvanced(any(), anyInt(), anyInt()))
                .thenReturn(new PageResponse<>(List.of(aMaterial().build()), 0, 10, 1L, 1, true));

        MaterialSearchCriteria search = new MaterialSearchCriteria();
        search.setStatus("active");
        search.setMaterialType("component");
        search.setName("bolt");
        assertEquals(1, service.searchAdvancedList(search, 0, 10).getContent().size());

        MaterialFilterCriteria filter = new MaterialFilterCriteria();
        filter.setStatus(" ");
        filter.setMaterialType(" ");
        assertEquals(1, service.filterList(filter, 0, 10).getContent().size());

        assertEquals(1, service.getAllList(0, 10).getContent().size());

        ArgumentCaptor<MaterialSearchFilter> used = ArgumentCaptor.forClass(MaterialSearchFilter.class);
        verify(materials, times(3)).searchAdvanced(used.capture(), eq(0), eq(10));
        assertEquals(MaterialStatus.ACTIVE, used.getAllValues().get(0).getStatus());
        assertEquals(MaterialType.COMPONENT, used.getAllValues().get(0).getMaterialType());
        assertNull(used.getAllValues().get(1).getStatus());
        assertNull(used.getAllValues().get(1).getMaterialType());
    }

    @Test
    @DisplayName("filter: a status and type given to the filter endpoint are parsed")
    void filter_parsesEnums() {
        when(materials.searchAdvanced(any(), anyInt(), anyInt()))
                .thenReturn(new PageResponse<>(List.of(), 0, 10, 0L, 0, true));
        MaterialFilterCriteria filter = new MaterialFilterCriteria();
        filter.setStatus("BLOCKED");
        filter.setMaterialType("TOL");

        service.filterList(filter, 0, 10);

        ArgumentCaptor<MaterialSearchFilter> used = ArgumentCaptor.forClass(MaterialSearchFilter.class);
        verify(materials).searchAdvanced(used.capture(), eq(0), eq(10));
        assertEquals(MaterialStatus.BLOCKED, used.getValue().getStatus());
        assertEquals(MaterialType.TOOL, used.getValue().getMaterialType());
    }
}
