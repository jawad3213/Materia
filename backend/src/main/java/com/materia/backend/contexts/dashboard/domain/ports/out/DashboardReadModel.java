package com.materia.backend.contexts.dashboard.domain.ports.out;

import com.materia.backend.contexts.goodsReceipt.domain.enums.ReceiptStatus;
import com.materia.backend.contexts.invoice.domain.enums.InvoiceStatus;
import com.materia.backend.contexts.invoice.domain.enums.InvoiceType;
import com.materia.backend.contexts.payment.domain.enums.PaymentStatus;
import com.materia.backend.contexts.purchaseOrder.domain.enums.OrderStatus;
import com.materia.backend.contexts.purchaseRequisition.domain.enums.RequisitionStatus;
import com.materia.backend.contexts.returnToVendor.domain.enums.ReturnStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Aggregates the dashboard reads from every module. Each method answers one question with a count, a sum or a
 * small grouped result computed by the database, so the dashboard never loads whole document lists. Currency codes
 * are compared and returned in upper case.
 */
public interface DashboardReadModel {

    /** An amount for one month. */
    record MonthAmount(YearMonth month, BigDecimal amount) {
    }

    /** A labelled group (a category, a supplier) with a number of items and an amount. */
    record Group(String key, String label, long count, BigDecimal amount) {
    }

    /** Quantities and punctuality of a set of goods receipts. */
    record ReceiptTotals(long receipts, long unitsReceived, long unitsAccepted, long unitsRejected,
                         long datedReceipts, long onTimeReceipts) {
        public static final ReceiptTotals NONE = new ReceiptTotals(0, 0, 0, 0, 0, 0);
    }

    /** Outstanding invoices sharing a due date (null when they have none) and a currency. */
    record DueGroup(LocalDate dueDate, String currency, long invoices, BigDecimal outstanding) {
    }

    /** An active material at or below its reorder level. */
    record StockRow(UUID id, String code, String name, int currentStock, Integer reorderPoint, Integer safetyStock,
                    int stockOnOrder, String unit) {
    }

    /** One of the latest documents of a module. */
    record ActivityRow(UUID id, String code, String title, String status, LocalDateTime createdAt) {
    }

    /** Modules whose latest documents the activity feed shows. */
    enum ActivitySource { REQUISITION, PURCHASE_ORDER, GOODS_RECEIPT, INVOICE, PAYMENT, RETURN }

    // ---- Currency ----

    /** How many orders, invoices and payments (those asked for) use each currency. */
    Map<String, Long> documentsByCurrency(boolean orders, boolean invoices, boolean payments);

    // ---- Requisitions ----

    long countRequisitions(RequisitionStatus status);

    // ---- Purchase orders ----

    long countOrders(Set<OrderStatus> statuses);

    long countOrdersExpectedBefore(Set<OrderStatus> statuses, LocalDate date);

    BigDecimal sumOrders(Set<OrderStatus> statuses, String currency);

    /** Grand totals per month of order date, between {@code from} and {@code to} inclusive. */
    List<MonthAmount> ordersByMonth(Set<OrderStatus> statuses, String currency, LocalDate from, LocalDate to);

    Map<OrderStatus, Long> ordersByStatus();

    /** Order-line value per material category ("Uncategorized" when the material has none); count = lines. */
    List<Group> spendByCategory(Set<OrderStatus> statuses, String currency);

    /** Grand totals per supplier; key = supplier id, count = orders. */
    List<Group> spendBySupplier(Set<OrderStatus> statuses, String currency);

    // ---- Goods receipts ----

    long countReceipts(Set<ReceiptStatus> statuses, LocalDate from, LocalDate to, boolean withRejectionOnly);

    ReceiptTotals receiptTotals(Set<ReceiptStatus> statuses);

    Map<UUID, ReceiptTotals> receiptTotalsBySupplier(Set<ReceiptStatus> statuses);

    // ---- Invoices ----

    /** Invoices of the type and statuses that still have something to pay, grouped by due date and currency. */
    List<DueGroup> outstandingByDueDate(InvoiceType type, Set<InvoiceStatus> statuses);

    /** Totals with tax per month of invoice date, credit notes subtracted, statuses in {@code excluded} left out. */
    List<MonthAmount> invoicedByMonth(Set<InvoiceStatus> excluded, String currency, LocalDate from, LocalDate to);

    // ---- Payments ----

    long countPayments(PaymentStatus status);

    /** Totals per month of confirmation, between {@code from} and {@code to} inclusive. */
    List<MonthAmount> paymentsByMonth(PaymentStatus status, String currency, LocalDate from, LocalDate to);

    // ---- Stock ----

    /** Active materials at or below their reorder point (or, with {@code outOfStockOnly}, with no stock left). */
    long countStockAlerts(boolean outOfStockOnly);

    /** The most urgent stock alerts: out of stock first, then below safety stock, then the furthest below reorder. */
    List<StockRow> stockAlerts(int limit);

    // ---- Returns ----

    long countReturns(ReturnStatus status);

    // ---- Activity ----

    /** The latest documents of a module, newest first. */
    List<ActivityRow> latest(ActivitySource source, int limit);
}
