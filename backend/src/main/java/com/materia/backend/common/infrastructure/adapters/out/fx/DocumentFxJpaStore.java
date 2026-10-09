package com.materia.backend.common.infrastructure.adapters.out.fx;

import com.materia.backend.common.domain.fx.DocumentFxStore;
import com.materia.backend.common.domain.fx.FxDocument;
import com.materia.backend.common.domain.fx.FxValuation;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * The rate copies on purchase_orders, invoices and payments, read and written with plain SQL. The document entities
 * map these columns read-only, so saving a document never overwrites its copy.
 */
@Component
public class DocumentFxJpaStore implements DocumentFxStore {

    /** Table, amount and valuation-date expressions of each document kind. */
    private record Mapping(String table, String amount, String date) {
    }

    private static Mapping mapping(FxDocument kind) {
        return switch (kind) {
            case PURCHASE_ORDER -> new Mapping("purchase_orders", "grand_total", "coalesce(order_date, cast(created_at as date))");
            case INVOICE -> new Mapping("invoices", "total_amount_with_tax", "coalesce(invoice_date, cast(created_at as date))");
            case PAYMENT -> new Mapping("payments", "total_amount",
                    "coalesce(cast(confirmed_date as date), cast(payment_date as date), cast(created_at as date))");
        };
    }

    @PersistenceContext
    private EntityManager em;

    @Override
    public Optional<DocumentAmount> find(FxDocument kind, UUID id) {
        Mapping m = mapping(kind);
        List<?> rows = em.createNativeQuery("select id, upper(currency_code), " + m.amount() + ", " + m.date()
                        + ", status, fx_reporting_currency from public." + m.table() + " where id = :id")
                .setParameter("id", id)
                .getResultList();
        return rows.stream().findFirst().map(row -> toDocument((Object[]) row));
    }

    @Override
    public List<DocumentAmount> unvalued(FxDocument kind, String reportingCurrency, int limit) {
        Mapping m = mapping(kind);
        List<?> rows = em.createNativeQuery("select id, upper(currency_code), " + m.amount() + ", " + m.date()
                        + ", status, fx_reporting_currency from public." + m.table()
                        + " where status in (:statuses) and currency_code is not null"
                        + " and (fx_reporting_currency is null or fx_reporting_currency <> :reporting)")
                .setParameter("statuses", kind.committedStatuses())
                .setParameter("reporting", reportingCurrency)
                .setMaxResults(limit)
                .getResultList();
        return rows.stream().map(row -> toDocument((Object[]) row)).toList();
    }

    @Override
    public void saveValuation(FxDocument kind, UUID id, FxValuation valuation, BigDecimal amountReporting) {
        em.createNativeQuery("update public." + mapping(kind).table() + " set fx_rate_to_reporting = :rate, "
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
