package com.materia.backend.common.application.metrics;

import java.math.BigDecimal;
import java.time.YearMonth;

/** An amount for one month, in the reporting currency. */
public record MonthAmount(YearMonth month, BigDecimal amount) {
}
