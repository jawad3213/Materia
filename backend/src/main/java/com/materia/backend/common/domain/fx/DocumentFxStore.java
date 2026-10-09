package com.materia.backend.common.domain.fx;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 🔹 DOCUMENT FX STORE (OUTPUT PORT)
 *
 * Reads what a document needs to be valued and writes the copy of its rate, without going through the document's
 * own aggregate: the copy is reporting data and never changes the document itself.
 */
public interface DocumentFxStore {

    /**
     * @param valuedIn reporting currency of the existing copy, or null when the document has none
     */
    record DocumentAmount(UUID id, String currency, BigDecimal amount, LocalDate date, String status, String valuedIn) {
    }

    Optional<DocumentAmount> find(FxDocument kind, UUID id);

    /** Committed documents with no copy for {@code reportingCurrency}, at most {@code limit}. */
    List<DocumentAmount> unvalued(FxDocument kind, String reportingCurrency, int limit);

    void saveValuation(FxDocument kind, UUID id, FxValuation valuation, BigDecimal amountReporting);
}
