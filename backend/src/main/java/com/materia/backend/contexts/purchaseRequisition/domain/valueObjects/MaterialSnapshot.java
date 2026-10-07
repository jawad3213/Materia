package com.materia.backend.contexts.purchaseRequisition.domain.valueObjects;

import com.materia.backend.common.domain.valueObjects.Money;

import java.util.UUID;

/** The material a requisition line asks for, as known when the line is written. */
public record MaterialSnapshot(UUID id, String code, String name, String description, String unitOfMeasureCode,
                               Money standardPrice, boolean orderable) {
}
