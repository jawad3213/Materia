package com.materia.backend.contexts.masterData.application.services;

import com.materia.backend.common.application.exceptions.ValidationException;
import com.materia.backend.common.domain.enums.CurrencyCode;
import com.materia.backend.common.domain.valueObjects.Money;
import com.materia.backend.contexts.masterData.application.dtos.material.CreateMaterialInput;
import com.materia.backend.contexts.masterData.application.dtos.material.MaterialOutput;
import com.materia.backend.contexts.masterData.application.dtos.material.UpdateMaterialInput;
import com.materia.backend.contexts.masterData.application.mappers.MaterialMapper;
import com.materia.backend.contexts.masterData.domain.entities.Category;
import com.materia.backend.contexts.masterData.domain.entities.Material;
import com.materia.backend.contexts.masterData.domain.entities.Supplier;
import com.materia.backend.contexts.masterData.domain.enums.MaterialStatus;
import com.materia.backend.contexts.masterData.domain.enums.MaterialType;
import com.materia.backend.contexts.masterData.domain.enums.UnitOfMeasure;
import com.materia.backend.contexts.masterData.domain.exceptions.CategoryNotFoundException;
import com.materia.backend.contexts.masterData.domain.exceptions.MaterialNotFoundException;
import com.materia.backend.contexts.masterData.domain.exceptions.SupplierNotFoundException;
import com.materia.backend.contexts.masterData.domain.ports.out.CategoryRepository;
import com.materia.backend.contexts.masterData.domain.ports.out.MaterialRepository;
import com.materia.backend.contexts.masterData.domain.ports.out.SupplierRepository;
import com.materia.backend.contexts.masterData.domain.valueObjects.MaterialCode;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Service tests for MaterialService catalogue operations (T094, T095, US5).
 */
@ExtendWith(MockitoExtension.class)
class MaterialServiceTest {

    @Mock private MaterialRepository materialRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private SupplierRepository supplierRepository;
    @Mock private MaterialCodeGeneratorService codeGenerator;
    @Mock private MaterialStockDomainService stockDomainService;
    @Mock private ReorderService reorderService;
    @Mock private ApplicationEventPublisher eventPublisher;

    private MaterialMapper mapper;
    private MaterialService materialService;

    private final UUID materialId = UUID.randomUUID();
    private final UUID categoryId = UUID.randomUUID();
    private final UUID supplierId = UUID.randomUUID();

    private Material material;
    private Category category;
    private Supplier supplier;

    @BeforeEach
    void setUp() {
        mapper = new MaterialMapper();
        materialService = new MaterialService(
                materialRepository,
                categoryRepository,
                supplierRepository,
                mapper,
                codeGenerator,
                stockDomainService,
                reorderService,
                eventPublisher
        );

        category = Category.builder()
                .id(categoryId)
                .code("CAT-001")
                .name("Fasteners")
                .build();

        supplier = Supplier.builder()
                .id(supplierId)
                .code("SUP-001")
                .name("Acme Fasteners")
                .country("Morocco")
                .build();

        material = Material.builder()
                .id(materialId)
                .code("MAT-2026-0001")
                .name("Steel Bolt")
                .materialType(MaterialType.RAW_MATERIAL)
                .unitOfMeasure(UnitOfMeasure.PCE)
                .categoryId(categoryId.toString())
                .categoryName("Fasteners")
                .supplierId(supplierId.toString())
                .supplierName("Acme Fasteners")
                .status(MaterialStatus.ACTIVE)
                .currentStock(100)
                .minimumStock(10)
                .maximumStock(1000)
                .reorderPoint(20)
                .safetyStock(5)
                .standardPrice(Money.of("10.00", CurrencyCode.MAD))
                .build();
    }

    @Test
    @DisplayName("create: generates code, validates category and supplier, and persists material")
    void create_success() {
        CreateMaterialInput input = new CreateMaterialInput();
        input.setName("Steel Bolt");
        input.setMaterialType("RAW_MATERIAL");
        input.setUnitOfMeasure("PCE");
        input.setCategoryId(categoryId.toString());
        input.setSupplierId(supplierId.toString());
        input.setMinimumStock(10);
        input.setMaximumStock(1000);

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(supplierRepository.findById(supplierId)).thenReturn(Optional.of(supplier));
        when(codeGenerator.generateCode(MaterialType.RAW_MATERIAL)).thenReturn(MaterialCode.of("MAT-2026-0002"));
        when(materialRepository.save(any(Material.class))).thenAnswer(inv -> inv.getArgument(0));

        MaterialOutput output = materialService.create(input);

        assertNotNull(output);
        assertEquals("Steel Bolt", output.getName());
        assertEquals("MAT-2026-0002", output.getCode());
        verify(materialRepository).save(any(Material.class));
    }

    @Test
    @DisplayName("create: invalid categoryId throws CategoryNotFoundException")
    void create_unknownCategory_throws() {
        CreateMaterialInput input = new CreateMaterialInput();
        input.setName("Steel Bolt");
        input.setMaterialType("RAW_MATERIAL");
        input.setCategoryId(UUID.randomUUID().toString());

        when(categoryRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(CategoryNotFoundException.class, () -> materialService.create(input));
    }

    @Test
    @DisplayName("create: invalid supplierId throws SupplierNotFoundException")
    void create_unknownSupplier_throws() {
        CreateMaterialInput input = new CreateMaterialInput();
        input.setName("Steel Bolt");
        input.setMaterialType("RAW_MATERIAL");
        input.setCategoryId(categoryId.toString());
        input.setSupplierId(UUID.randomUUID().toString());

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(supplierRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(SupplierNotFoundException.class, () -> materialService.create(input));
    }

    @Test
    @DisplayName("update: updates properties and persists")
    void update_success() {
        when(materialRepository.findById(materialId)).thenReturn(Optional.of(material));
        when(materialRepository.save(any(Material.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateMaterialInput input = new UpdateMaterialInput();
        input.setName("Updated Steel Bolt");
        input.setCategoryId(categoryId.toString());
        input.setSupplierId(supplierId.toString());

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(supplierRepository.findById(supplierId)).thenReturn(Optional.of(supplier));

        MaterialOutput output = materialService.update(materialId, input);

        assertNotNull(output);
        assertEquals("Updated Steel Bolt", output.getName());
    }

    @Test
    @DisplayName("delete: deletes existing material, throws MaterialNotFoundException if unknown")
    void delete_operations() {
        when(materialRepository.findById(materialId)).thenReturn(Optional.of(material));
        materialService.delete(materialId);
        verify(materialRepository).deleteById(materialId);

        UUID unknownId = UUID.randomUUID();
        when(materialRepository.findById(unknownId)).thenReturn(Optional.empty());
        assertThrows(MaterialNotFoundException.class, () -> materialService.delete(unknownId));
    }

    @Test
    @DisplayName("lookup: getById and getByCode return material or throw MaterialNotFoundException")
    void lookup_operations() {
        when(materialRepository.findById(materialId)).thenReturn(Optional.of(material));
        when(materialRepository.findByCode("MAT-2026-0001")).thenReturn(Optional.of(material));

        assertNotNull(materialService.getById(materialId));
        assertNotNull(materialService.getByCode("MAT-2026-0001"));

        UUID unknownId = UUID.randomUUID();
        when(materialRepository.findById(unknownId)).thenReturn(Optional.empty());
        assertThrows(MaterialNotFoundException.class, () -> materialService.getById(unknownId));
    }

    @Test
    @DisplayName("getAll: returns list of all materials")
    void getAll_returnsList() {
        when(materialRepository.findAll()).thenReturn(List.of(material));
        List<MaterialOutput> results = materialService.getAll();
        assertEquals(1, results.size());
    }
}
