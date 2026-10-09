package com.materia.backend.common.application.metrics;

import java.math.BigDecimal;
import java.util.Map;

/**
 * How to bring amounts into the reporting currency: a document's own rate copy when it was made for this reporting
 * currency, else the latest rate of its currency ({@code current}: currency code → rate; the reporting currency at 1).
 */
public record ReportingRates(String reportingCurrency, Map<String, BigDecimal> current) {
}
