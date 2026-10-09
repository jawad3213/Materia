package com.materia.backend.contexts.invoice.domain.ports.out;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * 🔹 PRICE TOLERANCE SETTING (OUTPUT PORT)
 *
 * The price tolerance an administrator set on the Settings page, if any; otherwise the configured value applies.
 */
public interface PriceToleranceSetting {

    Optional<BigDecimal> current();
}
