package com.materia.backend.contexts.masterData.application.dtos.material;

/** The stock of a material before and after a movement. */
public record StockMovement(int stockBefore, int stockAfter) {
}
