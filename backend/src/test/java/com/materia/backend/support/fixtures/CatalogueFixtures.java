package com.materia.backend.support.fixtures;

import com.materia.backend.common.domain.enums.CurrencyCode;
import com.materia.backend.contexts.masterData.domain.entities.Category;
import com.materia.backend.contexts.masterData.domain.entities.Supplier;
import com.materia.backend.contexts.masterData.domain.enums.MaterialCategoryType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Fixture builders for Catalogue records: Category and Supplier (T027, US5).
 */
public final class CatalogueFixtures {

    private static final AtomicInteger CAT_SEQ = new AtomicInteger(1);
    private static final AtomicInteger SUP_SEQ = new AtomicInteger(1);

    private CatalogueFixtures() {
    }

    public static CategoryBuilder aCategory() {
        return new CategoryBuilder();
    }

    public static SupplierBuilder aSupplier() {
        return new SupplierBuilder();
    }

    public static String uniqueCategoryCode() {
        return String.format("CAT-2026-%04d", CAT_SEQ.getAndIncrement() % 10000);
    }

    public static String uniqueSupplierCode() {
        return String.format("SUP-2026-%04d", SUP_SEQ.getAndIncrement() % 10000);
    }

    public static final class CategoryBuilder {
        private UUID id = UUID.randomUUID();
        private String code = uniqueCategoryCode();
        private String name = "Test Category";
        private String description = "Category for tests";
        private String parentId = null;
        private String parentCode = null;
        private Integer level = 0;
        private String path = null;
        private List<String> childrenIds = new ArrayList<>();
        private MaterialCategoryType categoryType = MaterialCategoryType.RAW_MATERIAL_CAT;
        private String status = Category.STATUS_ACTIVE;

        public CategoryBuilder id(UUID id) { this.id = id; return this; }
        public CategoryBuilder code(String code) { this.code = code; return this; }
        public CategoryBuilder name(String name) { this.name = name; return this; }
        public CategoryBuilder description(String description) { this.description = description; return this; }
        public CategoryBuilder parentId(String parentId) { this.parentId = parentId; return this; }
        public CategoryBuilder parentCode(String parentCode) { this.parentCode = parentCode; return this; }
        public CategoryBuilder level(Integer level) { this.level = level; return this; }
        public CategoryBuilder path(String path) { this.path = path; return this; }
        public CategoryBuilder childrenIds(List<String> childrenIds) { this.childrenIds = childrenIds; return this; }
        public CategoryBuilder categoryType(MaterialCategoryType categoryType) { this.categoryType = categoryType; return this; }
        public CategoryBuilder status(String status) { this.status = status; return this; }

        public Category build() {
            Category.Builder b = Category.builder()
                    .id(id)
                    .code(code)
                    .name(name)
                    .description(description)
                    .parentId(parentId)
                    .parentCode(parentCode)
                    .level(level)
                    .path(path != null ? path : "/" + code)
                    .childrenIds(childrenIds)
                    .categoryType(categoryType)
                    .status(status);
            return b.build();
        }
    }

    public static final class SupplierBuilder {
        private UUID id = UUID.randomUUID();
        private String code = uniqueSupplierCode();
        private String name = "Acme Supplies";
        private String description = "Reliable test supplier";
        private String contactPerson = "Jane Supplier";
        private String contactEmail = "contact@acme.test";
        private String contactPhone = "+212611223344";
        private String address = "123 Industrial Park";
        private String city = "Casablanca";
        private String country = "Morocco";
        private String postalCode = "20000";
        private List<String> paymentTerms = new ArrayList<>(List.of("NET30"));
        private Integer paymentDelay = 30;
        private CurrencyCode currencyCode = CurrencyCode.MAD;
        private String status = Supplier.STATUS_ACTIVE;

        public SupplierBuilder id(UUID id) { this.id = id; return this; }
        public SupplierBuilder code(String code) { this.code = code; return this; }
        public SupplierBuilder name(String name) { this.name = name; return this; }
        public SupplierBuilder description(String description) { this.description = description; return this; }
        public SupplierBuilder contactPerson(String contactPerson) { this.contactPerson = contactPerson; return this; }
        public SupplierBuilder contactEmail(String contactEmail) { this.contactEmail = contactEmail; return this; }
        public SupplierBuilder contactPhone(String contactPhone) { this.contactPhone = contactPhone; return this; }
        public SupplierBuilder address(String address) { this.address = address; return this; }
        public SupplierBuilder city(String city) { this.city = city; return this; }
        public SupplierBuilder country(String country) { this.country = country; return this; }
        public SupplierBuilder postalCode(String postalCode) { this.postalCode = postalCode; return this; }
        public SupplierBuilder paymentTerms(List<String> paymentTerms) { this.paymentTerms = paymentTerms; return this; }
        public SupplierBuilder paymentDelay(Integer paymentDelay) { this.paymentDelay = paymentDelay; return this; }
        public SupplierBuilder currencyCode(CurrencyCode currencyCode) { this.currencyCode = currencyCode; return this; }
        public SupplierBuilder status(String status) { this.status = status; return this; }

        public Supplier build() {
            Supplier.Builder b = Supplier.builder()
                    .id(id)
                    .code(code)
                    .name(name)
                    .description(description)
                    .contactPerson(contactPerson)
                    .contactEmail(contactEmail)
                    .contactPhone(contactPhone)
                    .address(address)
                    .city(city)
                    .country(country)
                    .postalCode(postalCode)
                    .paymentTerms(paymentTerms)
                    .paymentDelay(paymentDelay)
                    .currencyCode(currencyCode)
                    .status(status);
            return b.build();
        }
    }
}
