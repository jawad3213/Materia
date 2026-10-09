package com.materia.backend.contexts.purchaseOrder.infrastructure.adapters.out.fx;

import com.materia.backend.common.domain.fx.FxDocument;
import com.materia.backend.common.infrastructure.adapters.out.fx.SqlDocumentFxStore;
import org.springframework.stereotype.Component;

/** The reporting-currency rate copy of this context's own documents, on its own table. */
@Component
public class PurchaseOrderFxStore extends SqlDocumentFxStore {

    @Override
    public FxDocument kind() {
        return FxDocument.PURCHASE_ORDER;
    }

    @Override
    protected String table() {
        return "purchase_orders";
    }

    @Override
    protected String amountColumn() {
        return "grand_total";
    }

    @Override
    protected String dateExpression() {
        return "coalesce(order_date, cast(created_at as date))";
    }
}
