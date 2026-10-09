package com.materia.backend.contexts.masterData.domain.ports.in;

import com.materia.backend.contexts.masterData.application.dtos.material.StockAlert;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Reporting figures of the master data context, computed on its own data. */
public interface MaterialMetricsUseCase {

    /** Active materials at or below their reorder level (or, with {@code outOfStockOnly}, with no stock left). */
    long countStockAlerts(boolean outOfStockOnly);

    /** The most urgent stock alerts: out of stock first, then below safety stock, then the furthest below reorder. */
    List<StockAlert> stockAlerts(int limit);

    /** The category name of each of these materials that exists (null when it has none). */
    Map<UUID, String> categoryNamesById(Collection<UUID> materialIds);

    /** The category name of each of these material codes that exists (null when it has none). */
    Map<String, String> categoryNamesByCode(Collection<String> materialCodes);
}
