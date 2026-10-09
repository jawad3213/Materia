package com.materia.backend.contexts.invoice.application.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;

/** What remains to pay on invoices sharing a due date (null when they have none), in {@code currency}. */
public record OutstandingByDueDate(LocalDate dueDate, String currency, long invoices, BigDecimal outstanding) {
}
