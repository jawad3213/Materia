package com.materia.backend.contexts.masterData.application.dtos.material;

import java.util.UUID;

/** An active material at or below its reorder level. */
public record StockAlert(UUID id, String code, String name, int currentStock, Integer reorderPoint, Integer safetyStock,
                         int stockOnOrder, String unit) {
}
