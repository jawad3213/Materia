package com.materia.backend.common.infrastructure.adapters.out.fx;

import com.materia.backend.common.domain.fx.DocumentFxStore;
import com.materia.backend.common.domain.fx.FxDocument;
import com.materia.backend.common.domain.fx.FxValuation;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * The copy of a document's reporting-currency rate, read and written with plain SQL on the owning context's own
 * table. Each context that owns valued documents extends this for its table: shared code never names a table, so
 * every context keeps doing this on its own database once it is a separate service.
 */
public abstract class SqlDocumentFxStore implements DocumentFxStore {

    @PersistenceContext
    private EntityManager em;

    /** The kind of document this store owns. */
    public abstract FxDocument kind();

    /** The owner's table, e.g. {@code purchase_orders}. */
    protected abstract String table();

    /** SQL expression of the amount to value. */
    protected abstract String amountColumn();

    /** SQL expression of the date whose rate values the document. */
    protected abstract String dateExpression();

    @Override
    public Optional<DocumentAmount> find(FxDocument kind, UUID id) {
        List<?> rows = em.createNativeQuery("select id, upper(currency_code), " + amountColumn() + ", " + dateExpression()
                        + ", status, fx_reporting_currency from public." + table() + " where id = :id")
                .setParameter("id", id)
                .getResultList();
        return rows.stream().findFirst().map(row -> toDocument((Object[]) row));
    }

    @Override
    public List<DocumentAmount> unvalued(FxDocument kind, String reportingCurrency, int limit) {
        List<?> rows = em.createNativeQuery("select id, upper(currency_code), " + amountColumn() + ", " + dateExpression()
                        + ", status, fx_reporting_currency from public." + table()
                        + " where status in (:statuses) and currency_code is not null"
                        + " and (fx_reporting_currency is null or fx_reporting_currency <> :reporting)")
                .setParameter("statuses", kind().committedStatuses())
                .setParameter("reporting", reportingCurrency)
                .setMaxResults(limit)
                .getResultList();
        return rows.stream().map(row -> toDocument((Object[]) row)).toList();
    }

    @Override
    public void saveValuation(FxDocument kind, UUID id, FxValuation valuation, BigDecimal amountReporting) {
        em.createNativeQuery("update public." + table() + " set fx_rate_to_reporting = :rate, "
                        + "amount_reporting = :amount, fx_rate_date = :rateDate, fx_rate_source = :source, "
                        + "fx_reporting_currency = :reporting where id = :id")
                .setParameter("rate", valuation.rate())
                .setParameter("amount", amountReporting)
                .setParameter("rateDate", valuation.rateDate())
                .setParameter("source", valuation.source())
                .setParameter("reporting", valuation.reporting().getCode())
                .setParameter("id", id)
                .executeUpdate();
    }

    private static DocumentAmount toDocument(Object[] row) {
        return new DocumentAmount(
                row[0] instanceof UUID uuid ? uuid : UUID.fromString(row[0].toString()),
                (String) row[1],
                row[2] == null ? null : new BigDecimal(row[2].toString()),
                toDate(row[3]),
                row[4] != null ? row[4].toString() : null,
                (String) row[5]);
    }

    private static LocalDate toDate(Object value) {
        if (value == null) return null;
        if (value instanceof LocalDate date) return date;
        if (value instanceof Date date) return date.toLocalDate();
        if (value instanceof LocalDateTime dateTime) return dateTime.toLocalDate();
        if (value instanceof Timestamp timestamp) return timestamp.toLocalDateTime().toLocalDate();
        return LocalDate.parse(value.toString());
    }
}
