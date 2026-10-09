package com.materia.backend.contexts.payment.infrastructure.adapters.out.fx;

import com.materia.backend.common.domain.fx.FxDocument;
import com.materia.backend.common.infrastructure.adapters.out.fx.SqlDocumentFxStore;
import org.springframework.stereotype.Component;

/** The reporting-currency rate copy of this context's own documents, on its own table. */
@Component
public class PaymentFxStore extends SqlDocumentFxStore {

    @Override
    public FxDocument kind() {
        return FxDocument.PAYMENT;
    }

    @Override
    protected String table() {
        return "payments";
    }

    @Override
    protected String amountColumn() {
        return "total_amount";
    }

    @Override
    protected String dateExpression() {
        return "coalesce(cast(confirmed_date as date), cast(payment_date as date), cast(created_at as date))";
    }
}
