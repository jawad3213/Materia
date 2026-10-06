package com.materia.backend.support.fixtures;

import com.materia.backend.common.domain.enums.CurrencyCode;
import com.materia.backend.common.domain.valueObjects.Money;
import com.materia.backend.contexts.masterData.domain.entities.Material;
import com.materia.backend.contexts.masterData.domain.enums.MaterialStatus;
import com.materia.backend.contexts.masterData.domain.enums.MaterialType;
import com.materia.backend.contexts.masterData.domain.enums.UnitOfMeasure;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Builds materials with explicit stock thresholds (FR-015). Shared by US2 (requisition lines),
 * US3 (stock) and US5 (catalogue), so it lives in the foundational support package.
 *
 * <pre>{@code
 * Material m = aMaterial().stock(15).reorderPoint(20).minimum(10).safety(5).build();
 * }</pre>
 *
 * Every threshold is set explicitly rather than left to the domain's defaults, so a stock
 * test states exactly the boundary it is probing.
 */
public final class MaterialFixtures {

    private static final AtomicInteger SEQUENCE = new AtomicInteger(1);

    private MaterialFixtures() {
    }

    public static Builder aMaterial() {
        return new Builder();
    }

    /** A code unique within the run, in the {@code MAT-YYYY-NNNN} format the domain accepts. */
    public static String uniqueCode() {
        return String.format("MAT-2026-%04d", SEQUENCE.getAndIncrement() % 10000);
    }

    public static final class Builder {
        private String code = uniqueCode();
        private String name = "Test material";
        private MaterialStatus status = MaterialStatus.ACTIVE;
        private int currentStock = 100;
        private int minimumStock = 10;
        private int maximumStock = 1000;
        private int reorderPoint = 20;
        private int safetyStock = 5;
        private int stockOnOrder = 0;
        private String standardPrice = "10.00";
        private String categoryId = ReferenceRows.CATEGORY_ID.toString();
        private String supplierId = ReferenceRows.SUPPLIER_ID.toString();

        public Builder code(String code) { this.code = code; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder status(MaterialStatus status) { this.status = status; return this; }
        public Builder stock(int current) { this.currentStock = current; return this; }
        public Builder minimum(int minimum) { this.minimumStock = minimum; return this; }
        public Builder maximum(int maximum) { this.maximumStock = maximum; return this; }
        public Builder reorderPoint(int reorderPoint) { this.reorderPoint = reorderPoint; return this; }
        public Builder safety(int safety) { this.safetyStock = safety; return this; }
        public Builder onOrder(int onOrder) { this.stockOnOrder = onOrder; return this; }
        public Builder price(String price) { this.standardPrice = price; return this; }
        public Builder category(String categoryId) { this.categoryId = categoryId; return this; }
        public Builder supplier(String supplierId) { this.supplierId = supplierId; return this; }

        public Material build() {
            return Material.builder()
                    .code(code)
                    .name(name)
                    .materialType(MaterialType.CONSUMABLE)
                    .unitOfMeasure(UnitOfMeasure.PCE)
                    .status(status)
                    .currentStock(currentStock)
                    .availableStock(currentStock)
                    .minimumStock(minimumStock)
                    .maximumStock(maximumStock)
                    .reorderPoint(reorderPoint)
                    .safetyStock(safetyStock)
                    .stockOnOrder(stockOnOrder)
                    .standardPrice(Money.of(standardPrice, CurrencyCode.MAD))
                    .categoryId(categoryId)
                    .supplierId(supplierId)
                    .build();
        }
    }
}
