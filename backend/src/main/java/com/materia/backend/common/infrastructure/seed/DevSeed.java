package com.materia.backend.common.infrastructure.seed;

import org.slf4j.Logger;

import java.time.LocalDate;

/**
 * Demo data for local development.
 *
 * <p>Each bounded context seeds its own data with a {@code Dev...Seeder}: it exists only in the {@code dev} profile, runs
 * at start-up when {@code app.seed.demo-data=true}, does nothing once its data exists, and goes through the use cases
 * (never SQL), so the data follows the business rules. Each step builds on the previous one, about
 * {@link #DOCUMENTS_PER_MODULE} documents per module, spread over every status. A document is never dated before the
 * one it comes from: orders placed directly go back a few weeks and are delivered one to two weeks later, returns and
 * invoices take their receipt's date; requisitions (dated by the use cases) and the orders converted from them are of
 * today:
 *
 * <ol>
 *   <li>{@link #ACCOUNTS_ORDER} auth: the three dev accounts below</li>
 *   <li>{@link #EMPLOYEES_ORDER} employees (the dev accounts among them)</li>
 *   <li>{@link #MASTER_DATA_ORDER} master data: categories, suppliers, materials</li>
 *   <li>{@link #REQUISITIONS_ORDER} requisitions, raised by the purchaser and decided by the admin</li>
 *   <li>{@link #PURCHASE_ORDERS_ORDER} purchase orders, most converted from approved requisitions</li>
 *   <li>{@link #GOODS_RECEIPTS_ORDER} goods receipts by the receiver, with some rejected goods</li>
 *   <li>{@link #RETURNS_ORDER} returns of the rejected goods to the suppliers</li>
 *   <li>{@link #INVOICES_ORDER} supplier invoices of the received goods, and credit notes</li>
 *   <li>{@link #PAYMENTS_ORDER} payments of the verified invoices</li>
 * </ol>
 *
 * <p>The accounts share the password {@code app.seed.password}; the login page offers them in development.
 */
public final class DevSeed {

    /** Profile the seeders exist in. */
    public static final String PROFILE = "dev";

    /** Property that switches the seeders on. */
    public static final String ENABLED_PROPERTY = "app.seed.demo-data";

    public static final String ADMIN_EMAIL = "admin@materia.com";
    public static final String PURCHASER_EMAIL = "purchaser@materia.com";
    public static final String RECEIVER_EMAIL = "receiver@materia.com";

    /** Recorded as the author of seeded data. */
    public static final String SEEDER = "dev-seed";

    /** How many documents each module gets. */
    public static final int DOCUMENTS_PER_MODULE = 30;

    /** How many approved requisitions stay unconverted, for the purchaser to convert in the application. */
    public static final int APPROVED_REQUISITIONS_LEFT = 3;

    public static final int ACCOUNTS_ORDER = 10;
    public static final int EMPLOYEES_ORDER = 15;
    public static final int MASTER_DATA_ORDER = 20;
    public static final int REQUISITIONS_ORDER = 30;
    public static final int PURCHASE_ORDERS_ORDER = 40;
    public static final int GOODS_RECEIPTS_ORDER = 50;
    public static final int RETURNS_ORDER = 60;
    public static final int INVOICES_ORDER = 70;
    public static final int PAYMENTS_ORDER = 80;

    private DevSeed() {
    }

    /** A date {@code days} ago, to spread the documents over the last weeks (dashboards, ageing). */
    public static LocalDate daysAgo(int days) {
        return LocalDate.now().minusDays(days);
    }

    /**
     * Seeds one document. A rule the document breaks is logged and the document skipped, so that it neither stops the
     * start-up nor leaves the documents after it unseeded (a seeder never runs again once its data exists).
     *
     * @return whether the document was seeded
     */
    public static boolean attempt(Logger log, String document, Runnable seeding) {
        try {
            seeding.run();
            return true;
        } catch (RuntimeException e) {
            log.warn("[DEV-SEED] Skipped {}: {}", document, e.getMessage());
            return false;
        }
    }
}
