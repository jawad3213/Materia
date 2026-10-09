package com.materia.backend.contexts.invoice.infrastructure.adapters.out.fx;

import com.materia.backend.common.domain.fx.FxDocument;
import com.materia.backend.common.infrastructure.adapters.out.fx.SqlDocumentFxStore;
import org.springframework.stereotype.Component;

/** The reporting-currency rate copy of this context's own documents, on its own table. */
@Component
public class InvoiceFxStore extends SqlDocumentFxStore {

    @Override
    public FxDocument kind() {
        return FxDocument.INVOICE;
    }

    @Override
    protected String table() {
        return "invoices";
    }

    @Override
    protected String amountColumn() {
        return "total_amount_with_tax";
    }

    @Override
    protected String dateExpression() {
        return "coalesce(invoice_date, cast(created_at as date))";
    }
}
