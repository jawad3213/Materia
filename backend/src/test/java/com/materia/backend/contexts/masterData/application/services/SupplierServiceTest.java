package com.materia.backend.contexts.masterData.application.services;

import com.materia.backend.common.application.PageResponse;
import com.materia.backend.common.application.exceptions.BusinessException;
import com.materia.backend.common.domain.enums.CurrencyCode;
import com.materia.backend.contexts.masterData.application.dtos.supplier.CreateSupplierInput;
import com.materia.backend.contexts.masterData.application.dtos.supplier.SupplierFilterCriteria;
import com.materia.backend.contexts.masterData.application.dtos.supplier.SupplierListOutput;
import com.materia.backend.contexts.masterData.application.dtos.supplier.SupplierOutput;
import com.materia.backend.contexts.masterData.application.dtos.supplier.UpdateSupplierInput;
import com.materia.backend.contexts.masterData.application.mappers.SupplierMapper;
import com.materia.backend.contexts.masterData.domain.entities.Supplier;
import com.materia.backend.contexts.masterData.domain.exceptions.SupplierNotFoundException;
import com.materia.backend.contexts.masterData.domain.ports.out.MaterialRepository;
import com.materia.backend.contexts.masterData.domain.ports.out.SupplierRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Service tests for SupplierService (T093, US5).
 */
@ExtendWith(MockitoExtension.class)
class SupplierServiceTest {

    @Mock private SupplierRepository supplierRepository;
    @Mock private MaterialRepository materialRepository;
    @Mock private SupplierCodeGeneratorService codeGenerator;

    private SupplierMapper mapper;
    private SupplierService supplierService;

    private final UUID supplierId = UUID.randomUUID();
    private Supplier supplier;

    @BeforeEach
    void setUp() {
        mapper = new SupplierMapper();
        supplierService = new SupplierService(supplierRepository, materialRepository, mapper, codeGenerator);

        supplier = Supplier.builder()
                .id(supplierId)
                .code("SUP-001")
                .name("Global Fasteners")
                .country("Morocco")
                .currencyCode(CurrencyCode.MAD)
                .paymentDelay(30)
                .contactEmail("sales@fasteners.test")
                .build();
    }

    @Test
    @DisplayName("create: generates code and persists supplier")
    void create_success() {
        CreateSupplierInput input = new CreateSupplierInput();
        input.setName("New Supplier");
        input.setCountry("Morocco");
        input.setCurrencyCode("MAD");

        when(codeGenerator.generateCode()).thenReturn("SUP-002");
        when(supplierRepository.save(any(Supplier.class))).thenAnswer(inv -> inv.getArgument(0));

        SupplierOutput output = supplierService.create(input);

        assertNotNull(output);
        assertEquals("New Supplier", output.getName());
        assertEquals("SUP-002", output.getCode());
        verify(supplierRepository).save(any(Supplier.class));
    }

    @Test
    @DisplayName("update: updates fields and persists")
    void update_success() {
        when(supplierRepository.findById(supplierId)).thenReturn(Optional.of(supplier));
        when(supplierRepository.save(any(Supplier.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateSupplierInput input = new UpdateSupplierInput();
        input.setName("Renamed Fasteners");
        input.setCountry("Morocco");
        input.setCurrencyCode("MAD");

        when(materialRepository.findBySupplierId(supplierId.toString())).thenReturn(List.of());

        SupplierOutput output = supplierService.update(supplierId, input);

        assertEquals("Renamed Fasteners", output.getName());
        verify(materialRepository).findBySupplierId(supplierId.toString());
    }

    @Test
    @DisplayName("delete: supplier with linked materials is refused (SUPPLIER_HAS_MATERIALS)")
    void delete_withLinkedMaterials_refused() {
        when(supplierRepository.findById(supplierId)).thenReturn(Optional.of(supplier));
        when(materialRepository.existsBySupplierId(supplierId.toString())).thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class, () -> supplierService.delete(supplierId));
        assertEquals("SUPPLIER_HAS_MATERIALS", ex.getErrorCode());
        verify(supplierRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("delete: unreferenced supplier is deleted")
    void delete_unreferenced_succeeds() {
        when(supplierRepository.findById(supplierId)).thenReturn(Optional.of(supplier));
        when(materialRepository.existsBySupplierId(supplierId.toString())).thenReturn(false);

        supplierService.delete(supplierId);
        verify(supplierRepository).deleteById(supplierId);
    }

    @Test
    @DisplayName("lookup: getById and getByCode return supplier or throw SupplierNotFoundException")
    void lookup_operations() {
        when(supplierRepository.findById(supplierId)).thenReturn(Optional.of(supplier));
        when(supplierRepository.findByCode("SUP-001")).thenReturn(Optional.of(supplier));

        assertNotNull(supplierService.getById(supplierId));
        assertNotNull(supplierService.getByCode("SUP-001"));

        UUID unknownId = UUID.randomUUID();
        when(supplierRepository.findById(unknownId)).thenReturn(Optional.empty());
        assertThrows(SupplierNotFoundException.class, () -> supplierService.getById(unknownId));
    }

    @Test
    @DisplayName("search: searchSuppliers returns matching suppliers or empty list")
    void search_operations() {
        when(supplierRepository.search("Fasteners")).thenReturn(List.of(supplier));
        List<SupplierOutput> results = supplierService.searchSuppliers("Fasteners");
        assertEquals(1, results.size());

        when(supplierRepository.search("Unknown")).thenReturn(List.of());
        List<SupplierOutput> empty = supplierService.searchSuppliers("Unknown");
        assertTrue(empty.isEmpty());
    }

    @Test
    @DisplayName("filter: filterList delegates to repository with page parameters")
    void filter_operations() {
        SupplierFilterCriteria criteria = new SupplierFilterCriteria();
        criteria.setCountry("Morocco");

        PageResponse<Supplier> page = new PageResponse<>(List.of(supplier), 0, 10, 1L, 1, true);
        when(supplierRepository.searchAdvanced(any(), eq(0), eq(10))).thenReturn(page);

        PageResponse<SupplierListOutput> response = supplierService.filterList(criteria, 0, 10);
        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        assertEquals(1L, response.getTotalElements());
    }
}
