package com.materia.backend.contexts.masterData.application.dtos.material;

import com.materia.backend.common.domain.valueObjects.Money;

import java.util.UUID;

/**
 * What other contexts may know about a material: identity, description, unit, standard price and whether it can be
 * ordered.
 */
public record MaterialReference(UUID id, String code, String name, String description, String unitOfMeasureCode,
                                Money standardPrice, boolean orderable) {
}
