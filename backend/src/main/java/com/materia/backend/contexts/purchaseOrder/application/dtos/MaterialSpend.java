package com.materia.backend.contexts.purchaseOrder.application.dtos;

import java.math.BigDecimal;
import java.util.UUID;

/** Order-line value per material, in the reporting currency; {@code lines} is the number of order lines. */
public record MaterialSpend(UUID materialId, String materialCode, long lines, BigDecimal amount) {
}
