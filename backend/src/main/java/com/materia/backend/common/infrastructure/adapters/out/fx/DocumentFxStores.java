package com.materia.backend.common.infrastructure.adapters.out.fx;

import com.materia.backend.common.domain.fx.DocumentFxStore;
import com.materia.backend.common.domain.fx.FxDocument;
import com.materia.backend.common.domain.fx.FxValuation;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Sends each request to the store of the context that owns that kind of document. A kind no context in this
 * application owns has nothing to value here (it is valued by its own service).
 */
@Component
@Primary
public class DocumentFxStores implements DocumentFxStore {

    private final Map<FxDocument, SqlDocumentFxStore> byKind = new EnumMap<>(FxDocument.class);

    public DocumentFxStores(List<SqlDocumentFxStore> stores) {
        stores.forEach(store -> byKind.put(store.kind(), store));
    }

    @Override
    public Optional<DocumentAmount> find(FxDocument kind, UUID id) {
        SqlDocumentFxStore store = byKind.get(kind);
        return store != null ? store.find(kind, id) : Optional.empty();
    }

    @Override
    public List<DocumentAmount> unvalued(FxDocument kind, String reportingCurrency, int limit) {
        SqlDocumentFxStore store = byKind.get(kind);
        return store != null ? store.unvalued(kind, reportingCurrency, limit) : List.of();
    }

    @Override
    public void saveValuation(FxDocument kind, UUID id, FxValuation valuation, BigDecimal amountReporting) {
        SqlDocumentFxStore store = byKind.get(kind);
        if (store != null) store.saveValuation(kind, id, valuation, amountReporting);
    }
}
