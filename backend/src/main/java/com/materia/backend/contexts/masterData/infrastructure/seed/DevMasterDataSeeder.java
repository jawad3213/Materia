package com.materia.backend.contexts.masterData.infrastructure.seed;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.materia.backend.common.infrastructure.seed.DevSeed;
import com.materia.backend.contexts.masterData.application.dtos.category.CreateCategoryInput;
import com.materia.backend.contexts.masterData.application.dtos.material.CreateMaterialInput;
import com.materia.backend.contexts.masterData.application.dtos.supplier.CreateSupplierInput;
import com.materia.backend.contexts.masterData.domain.ports.in.CategoryUseCase;
import com.materia.backend.contexts.masterData.domain.ports.in.MaterialUseCase;
import com.materia.backend.contexts.masterData.domain.ports.in.SupplierUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Dev only: the categories, suppliers and materials of {@code seed/dev/master-data.json}, created through the master
 * data use cases (codes, stock levels and validation as in the application). Skipped once any material exists.
 */
@Component
@Profile(DevSeed.PROFILE)
@ConditionalOnProperty(name = DevSeed.ENABLED_PROPERTY, havingValue = "true")
@Order(DevSeed.MASTER_DATA_ORDER)
public class DevMasterDataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DevMasterDataSeeder.class);
    private static final String DATA = "seed/dev/master-data.json";

    private final CategoryUseCase categories;
    private final SupplierUseCase suppliers;
    private final MaterialUseCase materials;
    private final ObjectMapper json = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public DevMasterDataSeeder(CategoryUseCase categories, SupplierUseCase suppliers, MaterialUseCase materials) {
        this.categories = categories;
        this.suppliers = suppliers;
        this.materials = materials;
    }

    @Override
    public void run(ApplicationArguments args) throws IOException {
        if (!materials.getAll().isEmpty()) {
            log.info("[DEV-SEED] Master data already present, nothing seeded");
            return;
        }
        JsonNode data;
        try (InputStream in = new ClassPathResource(DATA).getInputStream()) {
            data = json.readTree(in);
        }

        Map<String, String> categoryIds = new HashMap<>();
        for (JsonNode node : data.get("rootCategories")) {
            categoryIds.put(node.get("name").asText(), createCategory(node, null));
        }
        for (JsonNode node : data.get("childCategories")) {
            categoryIds.put(node.get("name").asText(), createCategory(node, categoryIds.get(node.get("parentName").asText())));
        }

        Map<String, String> supplierIds = new HashMap<>();
        for (JsonNode node : data.get("suppliers")) {
            CreateSupplierInput input = json.convertValue(node, CreateSupplierInput.class);
            input.setCreatedBy(DevSeed.SEEDER);
            supplierIds.put(input.getName(), suppliers.create(input).getId().toString());
        }

        int created = 0;
        for (JsonNode node : data.get("materials")) {
            CreateMaterialInput input = json.convertValue(node, CreateMaterialInput.class);
            input.setCategoryId(categoryIds.get(node.get("categoryName").asText()));
            input.setSupplierId(supplierIds.get(node.get("supplierName").asText()));
            input.setCreatedBy(DevSeed.SEEDER);
            materials.create(input);
            created++;
        }
        log.info("[DEV-SEED] Created {} categories, {} suppliers and {} materials",
                categoryIds.size(), supplierIds.size(), created);
    }

    private String createCategory(JsonNode node, String parentId) {
        CreateCategoryInput input = json.convertValue(node, CreateCategoryInput.class);
        input.setParentId(parentId);
        input.setCreatedBy(DevSeed.SEEDER);
        return categories.create(input).getId().toString();
    }
}
