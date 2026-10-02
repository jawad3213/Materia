package com.materia.backend.contexts.masterData.domain.valueObjects;

import com.materia.backend.common.domain.enums.CurrencyCode;
import com.materia.backend.contexts.masterData.domain.enums.MaterialStatus;
import com.materia.backend.contexts.masterData.domain.enums.MaterialType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Predicate construction and value tests for MaterialSearchFilter and SupplierSearchFilter (T091, US5).
 */
class SearchFiltersTest {

    @Test
    @DisplayName("material filter: builds with all criteria accurately preserved")
    void materialFilter_buildsAccurately() {
        MaterialSearchFilter filter = MaterialSearchFilter.builder()
                .code("MAT-001")
                .name("Steel")
                .description("Heavy duty")
                .shortDescription("Steel")
                .searchKeywords("metal,bolt")
                .alternativeName("Alloy")
                .categoryId("cat-1")
                .materialType(MaterialType.RAW_MATERIAL)
                .status(MaterialStatus.ACTIVE)
                .build();

        assertEquals("MAT-001", filter.getCode());
        assertEquals("Steel", filter.getName());
        assertEquals("Heavy duty", filter.getDescription());
        assertEquals("Steel", filter.getShortDescription());
        assertEquals("metal,bolt", filter.getSearchKeywords());
        assertEquals("Alloy", filter.getAlternativeName());
        assertEquals("cat-1", filter.getCategoryId());
        assertEquals(MaterialType.RAW_MATERIAL, filter.getMaterialType());
        assertEquals(MaterialStatus.ACTIVE, filter.getStatus());
    }

    @Test
    @DisplayName("supplier filter: builds with all criteria accurately preserved")
    void supplierFilter_buildsAccurately() {
        SupplierSearchFilter filter = SupplierSearchFilter.builder()
                .code("SUP-001")
                .name("Global Logistics")
                .description("Shipping partner")
                .contactPerson("Karim")
                .contactEmail("karim@gl.test")
                .fullAddress("123 Harbour Road")
                .status("ACTIVE")
                .currencyCode(CurrencyCode.MAD)
                .country("Morocco")
                .build();

        assertEquals("SUP-001", filter.getCode());
        assertEquals("Global Logistics", filter.getName());
        assertEquals("Shipping partner", filter.getDescription());
        assertEquals("Karim", filter.getContactPerson());
        assertEquals("karim@gl.test", filter.getContactEmail());
        assertEquals("123 Harbour Road", filter.getFullAddress());
        assertEquals("ACTIVE", filter.getStatus());
        assertEquals(CurrencyCode.MAD, filter.getCurrencyCode());
        assertEquals("Morocco", filter.getCountry());
    }
}
