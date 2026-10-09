package com.materia.backend.common.application.metrics;

import java.math.BigDecimal;

/** A labelled group (a supplier, a material...) with a number of items and an amount in the reporting currency. */
public record CountedAmount(String key, String label, long count, BigDecimal amount) {
}
