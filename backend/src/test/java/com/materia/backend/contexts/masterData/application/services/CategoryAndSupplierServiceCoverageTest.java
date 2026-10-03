package com.materia.backend.contexts.masterData.application.services;

import com.materia.backend.common.application.PageResponse;
import com.materia.backend.common.application.exceptions.ValidationException;
import com.materia.backend.contexts.masterData.application.dtos.category.UpdateCategoryInput;
import com.materia.backend.contexts.masterData.application.dtos.supplier.SupplierFilterCriteria;
import com.materia.backend.contexts.masterData.application.dtos.supplier.SupplierSearchCriteria;
import com.materia.backend.contexts.masterData.application.dtos.supplier.UpdateSupplierInput;
import com.materia.backend.contexts.masterData.application.mappers.CategoryMapper;
import com.materia.backend.contexts.masterData.application.mappers.SupplierMapper;
import com.materia.backend.contexts.masterData.domain.entities.Category;
import com.materia.backend.contexts.masterData.domain.entities.Material;
import com.materia.backend.contexts.masterData.domain.entities.Supplier;
import com.materia.backend.contexts.masterData.domain.exceptions.CategoryNotFoundException;
import com.materia.backend.contexts.masterData.domain.ports.out.CategoryRepository;
import com.materia.backend.contexts.masterData.domain.ports.out.MaterialRepository;
import com.materia.backend.contexts.masterData.domain.ports.out.SupplierRepository;
import com.materia.backend.contexts.masterData.domain.valueObjects.SupplierSearchFilter;
import com.materia.backend.common.domain.enums.CurrencyCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.materia.backend.support.fixtures.CatalogueFixtures.aCategory;
import static com.materia.backend.support.fixtures.CatalogueFixtures.aSupplier;
import static com.materia.backend.support.fixtures.MaterialFixtures.aMaterial;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/** [T079] Category hierarchy maintenance and supplier search/sync paths (feature 001 coverage). */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CategoryAndSupplierServiceCoverageTest {

    @Mock private CategoryRepository categories;
    @Mock private SupplierRepository suppliers;
    @Mock private MaterialRepository materials;
    @Mock private CategoryCodeGeneratorService categoryCodes;
    @Mock private SupplierCodeGeneratorService supplierCodes;

    private CategoryService categoryService;
    private SupplierService supplierService;

    @BeforeEach
    void setUp() {
        categoryService = new CategoryService(categories, materials, new CategoryMapper(), categoryCodes);
        supplierService = new SupplierService(suppliers, materials, new SupplierMapper(), supplierCodes);
        when(categories.save(any(Category.class))).thenAnswer(inv -> inv.getArgument(0));
        when(suppliers.save(any(Supplier.class))).thenAnswer(inv -> inv.getArgument(0));
        when(categories.findByParentId(anyString())).thenReturn(List.of());
        when(materials.findByCategoryId(anyString())).thenReturn(List.of());
        when(materials.findBySupplierId(anyString())).thenReturn(List.of());
    }

    private Category stored(Category c) {
        when(categories.findById(c.getId())).thenReturn(Optional.of(c));
        return c;
    }

    // ---- Categories ----

    @Test
    @DisplayName("category move: under a new parent, level and path follow the parent and children are refreshed")
    void move_underParent_refreshesHierarchy() {
        Category parent = stored(aCategory().code("PAR").build());
        parent.setLevel(1);
        parent.setPath("/ROOT/PAR/");
        Category moved = stored(aCategory().code("MOV").build());
        Category child = aCategory().code("KID").build();
        when(categories.findByParentId(moved.getId().toString())).thenReturn(List.of(child));
        UpdateCategoryInput input = new UpdateCategoryInput();
        input.setParentId(" " + parent.getId() + " ");

        categoryService.update(moved.getId(), input);

        assertEquals(parent.getId().toString(), moved.getParentId());
        assertEquals(2, moved.getLevel());
        assertEquals("/ROOT/PAR/MOV/", moved.getPath());
        assertEquals("/ROOT/PAR/MOV/KID/", child.getPath());
        verify(categories).save(child);
    }

    @Test
    @DisplayName("category move: a parent without a path builds the child path from the parent's code")
    void move_underParentWithoutPath() {
        Category parent = stored(aCategory().code("PAR").build());
        parent.setPath(null);
        parent.setLevel(null);
        Category moved = stored(aCategory().code("MOV").build());
        UpdateCategoryInput input = new UpdateCategoryInput();
        input.setParentId(parent.getId().toString());

        categoryService.update(moved.getId(), input);

        assertEquals("/PAR/MOV/", moved.getPath());
        assertEquals(1, moved.getLevel());
    }

    @Test
    @DisplayName("category move: a blank parent makes the category a root")
    void move_toRoot() {
        Category moved = stored(aCategory().code("MOV").parentId(UUID.randomUUID().toString()).build());
        UpdateCategoryInput input = new UpdateCategoryInput();
        input.setParentId(" ");

        categoryService.update(moved.getId(), input);

        assertNull(moved.getParentId());
        assertEquals(0, moved.getLevel());
        assertEquals("/MOV/", moved.getPath());
    }

    @Test
    @DisplayName("category move: a category cannot be its own parent or move under its own descendant")
    void move_cycles_areRefused() {
        Category c = stored(aCategory().code("C").build());
        Category grandChild = aCategory().code("GC").build();
        Category child = aCategory().code("CH").build();
        when(categories.findByParentId(c.getId().toString())).thenReturn(List.of(child));
        when(categories.findByParentId(child.getId().toString())).thenReturn(List.of(grandChild));

        UpdateCategoryInput self = new UpdateCategoryInput();
        self.setParentId(c.getId().toString());
        assertThrows(ValidationException.class, () -> categoryService.update(c.getId(), self));

        UpdateCategoryInput underDescendant = new UpdateCategoryInput();
        underDescendant.setParentId(grandChild.getId().toString());
        assertThrows(ValidationException.class, () -> categoryService.update(c.getId(), underDescendant));
    }

    @Test
    @DisplayName("category move: a malformed or unknown parent id is refused")
    void move_badParent_isRefused() {
        Category c = stored(aCategory().code("C").build());
        UpdateCategoryInput malformed = new UpdateCategoryInput();
        malformed.setParentId("not-a-uuid");
        assertThrows(ValidationException.class, () -> categoryService.update(c.getId(), malformed));

        UpdateCategoryInput unknown = new UpdateCategoryInput();
        unknown.setParentId(UUID.randomUUID().toString());
        when(categories.findById(UUID.fromString(unknown.getParentId()))).thenReturn(Optional.empty());
        assertThrows(CategoryNotFoundException.class, () -> categoryService.update(c.getId(), unknown));
    }

    @Test
    @DisplayName("category rename: materials in the category get the new category name; none means nothing saved")
    void rename_syncsMaterials() {
        Category c = stored(aCategory().code("C").name("Old").build());
        Material m = aMaterial().category(c.getId().toString()).build();
        when(materials.findByCategoryId(c.getId().toString())).thenReturn(List.of(m));
        UpdateCategoryInput rename = new UpdateCategoryInput();
        rename.setName("New");

        categoryService.update(c.getId(), rename);

        assertEquals("New", m.getCategoryName());
        verify(materials).saveAll(List.of(m));

        Category empty = stored(aCategory().code("E").name("Old").build());
        UpdateCategoryInput rename2 = new UpdateCategoryInput();
        rename2.setName("Newer");
        categoryService.update(empty.getId(), rename2);
        verify(materials, times(1)).saveAll(any());
    }

    @Test
    @DisplayName("category reads: sub-categories are listed by parent")
    void subCategories() {
        UUID parent = UUID.randomUUID();
        when(categories.findByParentId(parent.toString())).thenReturn(List.of(aCategory().build()));

        assertEquals(1, categoryService.getSubCategories(parent).size());
    }

    // ---- Suppliers ----

    @Test
    @DisplayName("supplier rename: materials from the supplier get the new name; an unchanged name syncs nothing")
    void supplierRename_syncsMaterials() {
        Supplier s = aSupplier().name("Old").build();
        when(suppliers.findById(s.getId())).thenReturn(Optional.of(s));
        Material m = aMaterial().supplier(s.getId().toString()).build();
        when(materials.findBySupplierId(s.getId().toString())).thenReturn(List.of(m));

        UpdateSupplierInput same = new UpdateSupplierInput();
        same.setName("Old");
        supplierService.update(s.getId(), same);
        verify(materials, never()).saveAll(any());

        UpdateSupplierInput rename = new UpdateSupplierInput();
        rename.setName("New");
        supplierService.update(s.getId(), rename);
        assertEquals("New", m.getSupplierName());
        verify(materials).saveAll(List.of(m));
    }

    @Test
    @DisplayName("supplier rename: with no materials for the supplier, nothing is saved")
    void supplierRename_noMaterials() {
        Supplier s = aSupplier().name("Old").build();
        when(suppliers.findById(s.getId())).thenReturn(Optional.of(s));
        UpdateSupplierInput rename = new UpdateSupplierInput();
        rename.setName("New");

        supplierService.update(s.getId(), rename);

        verify(materials, never()).saveAll(any());
    }

    @Test
    @DisplayName("supplier search and filter: blank criteria are dropped, a valid currency is parsed, an invalid one ignored")
    void supplierSearchAndFilter() {
        when(suppliers.searchAdvanced(any(), anyInt(), anyInt()))
                .thenReturn(new PageResponse<>(List.of(aSupplier().build()), 0, 10, 1L, 1, true));

        SupplierFilterCriteria filter = new SupplierFilterCriteria();
        filter.setStatus(" ACTIVE ");
        filter.setCurrencyCode(" eur ");
        filter.setCountry(" ");
        assertEquals(1, supplierService.filterList(filter, 0, 10).getContent().size());

        SupplierSearchCriteria search = new SupplierSearchCriteria();
        search.setCurrencyCode("XXX");
        search.setStatus(" ");
        search.setCountry("MA");
        search.setName("acme");
        assertEquals(1, supplierService.searchAdvancedList(search, 0, 10).getContent().size());

        SupplierFilterCriteria blank = new SupplierFilterCriteria();
        supplierService.filterList(blank, 0, 10);

        ArgumentCaptor<SupplierSearchFilter> used = ArgumentCaptor.forClass(SupplierSearchFilter.class);
        verify(suppliers, times(3)).searchAdvanced(used.capture(), eq(0), eq(10));
        assertEquals("ACTIVE", used.getAllValues().get(0).getStatus());
        assertEquals(CurrencyCode.EUR, used.getAllValues().get(0).getCurrencyCode());
        assertNull(used.getAllValues().get(0).getCountry());
        assertNull(used.getAllValues().get(1).getCurrencyCode());
        assertNull(used.getAllValues().get(1).getStatus());
        assertEquals("MA", used.getAllValues().get(1).getCountry());
        assertNull(used.getAllValues().get(2).getStatus());
    }
}
