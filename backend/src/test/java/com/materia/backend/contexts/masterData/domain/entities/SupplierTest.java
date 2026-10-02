package com.materia.backend.contexts.masterData.domain.entities;

import com.materia.backend.common.domain.enums.CurrencyCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Domain entity tests for Supplier (T088, US5).
 */
class SupplierTest {

    private Supplier.Builder validSupplierBuilder() {
        return Supplier.builder()
                .code("SUP-001")
                .name("Global Logistics")
                .country("Morocco")
                .city("Casablanca")
                .contactEmail("info@globallogistics.com")
                .contactPerson("Karim Alami")
                .paymentDelay(30)
                .currencyCode(CurrencyCode.MAD);
    }

    @Test
    @DisplayName("supplier: creates with valid attributes and default active status")
    void supplier_validAttributes() {
        Supplier s = validSupplierBuilder().build();

        assertEquals("SUP-001", s.getCode());
        assertEquals("Global Logistics", s.getName());
        assertEquals("Morocco", s.getCountry());
        assertEquals(CurrencyCode.MAD, s.getCurrencyCode());
        assertEquals(30, s.getPaymentDelay());
        assertTrue(s.isActive());
        assertEquals(Supplier.STATUS_ACTIVE, s.getStatus());
    }

    @Test
    @DisplayName("status: activation and deactivation toggle status")
    void status_toggle() {
        Supplier s = validSupplierBuilder().build();
        assertTrue(s.isActive());

        s.deactivate();
        assertFalse(s.isActive());
        assertEquals(Supplier.STATUS_INACTIVE, s.getStatus());

        s.activate();
        assertTrue(s.isActive());
        assertEquals(Supplier.STATUS_ACTIVE, s.getStatus());
    }

    @Test
    @DisplayName("payment terms: can configure and modify payment terms")
    void paymentTerms_configuration() {
        Supplier s = validSupplierBuilder()
                .paymentTerms(new ArrayList<>(List.of("NET30", "NET60")))
                .build();

        assertEquals(2, s.getPaymentTerms().size());
        assertTrue(s.getPaymentTerms().contains("NET30"));

        s.getPaymentTerms().add("COD");
        assertEquals(3, s.getPaymentTerms().size());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    @DisplayName("validation: code is required")
    void validation_codeRequired(String code) {
        assertThrows(IllegalArgumentException.class, () ->
                Supplier.builder().code(code).name("Valid").country("Morocco").build());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    @DisplayName("validation: name is required")
    void validation_nameRequired(String name) {
        assertThrows(IllegalArgumentException.class, () ->
                Supplier.builder().code("SUP-001").name(name).country("Morocco").build());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    @DisplayName("validation: country is required")
    void validation_countryRequired(String country) {
        assertThrows(IllegalArgumentException.class, () ->
                Supplier.builder().code("SUP-001").name("Valid").country(country).build());
    }
}
