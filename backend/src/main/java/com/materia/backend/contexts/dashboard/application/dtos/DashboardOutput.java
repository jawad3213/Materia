package com.materia.backend.contexts.dashboard.application.dtos;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * The procurement dashboard. Every money figure is in {@code currency}, the currency most of the documents use;
 * documents in other currencies are left out of money totals (and counted in {@code otherCurrencyDocuments}) so
 * that amounts are never added across currencies. A section the user may not read is {@code null} or empty.
 */
public record DashboardOutput(
        LocalDateTime generatedAt,
        String currency,
        int otherCurrencyDocuments,
        List<Kpi> kpis,
        MonthlyTrend monthlyTrend,
        List<Slice> orderStatus,
        List<Slice> spendByCategory,
        List<SupplierStats> topSuppliers,
        ReceiptQuality receiptQuality,
        List<AgingBucket> invoiceAging,
        List<StockAlert> stockAlerts,
        List<Activity> recentActivity) {

    /** A headline figure: a count or an amount, with an optional secondary line and the page it opens. */
    public record Kpi(String key, String label, BigDecimal value, String unit, String hint, String link, String tone) {
    }

    /** One value per month over the last twelve months, oldest first; {@code months} are "YYYY-MM". */
    public record MonthlyTrend(List<String> months, List<BigDecimal> ordered, List<BigDecimal> invoiced, List<BigDecimal> paid) {
    }

    /** A labelled share of a whole (a status, a category), with a count and/or an amount. */
    public record Slice(String key, String label, long count, BigDecimal amount) {
    }

    /** Spend with a supplier, and how its deliveries went (rates are 0-100, null when it has no receipt yet). */
    public record SupplierStats(String supplierId, String supplierName, BigDecimal spend, long orders,
                                Double acceptanceRate, Double onTimeRate) {
    }

    /** Quality and punctuality of every completed goods receipt. Rates are percentages, null without receipts. */
    public record ReceiptQuality(long receipts, long unitsReceived, long unitsAccepted, long unitsRejected,
                                 Double acceptanceRate, long onTimeReceipts, Double onTimeRate) {
    }

    /** Outstanding invoice amounts by how far past their due date they are. */
    public record AgingBucket(String key, String label, long invoices, BigDecimal amount) {
    }

    /** A material at or below its reorder level. */
    public record StockAlert(String materialId, String code, String name, String status, int currentStock,
                             Integer reorderPoint, Integer safetyStock, int stockOnOrder, String unit) {
    }

    /** A recently created document of any module. */
    public record Activity(String type, String code, String title, String status, LocalDateTime date, String link) {
    }
}
