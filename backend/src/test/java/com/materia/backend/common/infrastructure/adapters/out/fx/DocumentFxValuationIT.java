package com.materia.backend.common.infrastructure.adapters.out.fx;

import com.materia.backend.common.domain.enums.CurrencyCode;
import com.materia.backend.common.domain.fx.ExchangeRateHistory;
import com.materia.backend.common.domain.fx.ExchangeRateHistory.StoredRate;
import com.materia.backend.common.domain.fx.FxValuation;
import com.materia.backend.common.domain.fx.ReportingFx;
import com.materia.backend.support.AbstractIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Rate copies against PostgreSQL: the stored history, the copy written on committed documents only, and a document
 * saved again keeping its copy. Runs in a rolled-back transaction.
 */
class DocumentFxValuationIT extends AbstractIntegrationTest {

    private static final LocalDate ORDER_DATE = LocalDate.of(2026, 9, 10);

    @Autowired private JdbcTemplate jdbc;
    @Autowired private ExchangeRateHistory history;
    @Autowired private ReportingFx fx;

    @Test
    @DisplayName("history: one rate per day and pair, replaced when stored again; lookups take the closest earlier day")
    void history_storesAndFinds() {
        history.save(new StoredRate(ORDER_DATE.minusDays(5), CurrencyCode.EUR, CurrencyCode.MAD, new BigDecimal("10.90"), FxValuation.LIVE));
        history.save(new StoredRate(ORDER_DATE.minusDays(5), CurrencyCode.EUR, CurrencyCode.MAD, new BigDecimal("10.95"), FxValuation.LIVE));
        history.save(new StoredRate(ORDER_DATE.plusDays(5), CurrencyCode.EUR, CurrencyCode.MAD, new BigDecimal("11.20"), FxValuation.FALLBACK));

        StoredRate before = history.latestOnOrBefore(CurrencyCode.EUR, CurrencyCode.MAD, ORDER_DATE).orElseThrow();
        assertThat(before.rate()).isEqualByComparingTo("10.95");
        assertThat(before.rateDate()).isEqualTo(ORDER_DATE.minusDays(5));
        assertThat(history.earliestAfter(CurrencyCode.EUR, CurrencyCode.MAD, ORDER_DATE).orElseThrow().source())
                .isEqualTo(FxValuation.FALLBACK);
        assertThat(jdbc.queryForObject("select count(*) from exchange_rates where from_currency = 'EUR' and rate_date = ?",
                Integer.class, Date.valueOf(ORDER_DATE.minusDays(5)))).isEqualTo(1);
    }

    @Test
    @DisplayName("documents: a confirmed EUR order is valued at the rate of its order date; a draft is not")
    void backfill_valuesCommittedOrders() {
        history.save(new StoredRate(ORDER_DATE.minusDays(1), CurrencyCode.EUR, CurrencyCode.MAD, new BigDecimal("11.00"), FxValuation.LIVE));
        UUID supplier = UUID.randomUUID();
        jdbc.update("insert into suppliers (id, code, name, status, currency_code, created_at, version) "
                + "values (?, ?, 'Euro Parts', 'ACTIVE', 'EUR', now(), 0)", supplier, "SUP-FX-" + supplier.toString().substring(0, 8));
        UUID confirmed = order(supplier, "CONFIRMED", "80.00");
        UUID draft = order(supplier, "DRAFT", "50.00");

        fx.backfill();

        Map<String, Object> row = jdbc.queryForMap("select fx_rate_to_reporting, amount_reporting, fx_rate_date, fx_rate_source, "
                + "fx_reporting_currency from purchase_orders where id = ?", confirmed);
        assertThat((BigDecimal) row.get("fx_rate_to_reporting")).isEqualByComparingTo("11.00");
        assertThat((BigDecimal) row.get("amount_reporting")).isEqualByComparingTo("880.00");
        assertThat(((Date) row.get("fx_rate_date")).toLocalDate()).isEqualTo(ORDER_DATE.minusDays(1));
        assertThat(row.get("fx_rate_source")).isEqualTo(FxValuation.LIVE);
        assertThat(row.get("fx_reporting_currency")).isEqualTo("MAD");
        assertThat(jdbc.queryForObject("select fx_reporting_currency from purchase_orders where id = ?", String.class, draft)).isNull();
    }

    private UUID order(UUID supplier, String status, String total) {
        UUID id = UUID.randomUUID();
        jdbc.update("insert into purchase_orders (id, order_code, status, delivery_status, supplier_id, supplier_name, "
                        + "order_date, currency_code, total_amount, grand_total, created_at, version) "
                        + "values (?, ?, ?, 'NOT_SHIPPED', ?, 'Euro Parts', ?, 'EUR', ?, ?, ?, 0)",
                id, "PO-FX-" + id.toString().substring(0, 8), status, supplier, Date.valueOf(ORDER_DATE),
                new BigDecimal(total), new BigDecimal(total), Timestamp.valueOf(ORDER_DATE.atStartOfDay()));
        return id;
    }
}
