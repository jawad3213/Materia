package com.materia.backend.common.infrastructure.seed;

/**
 * Demo data for local development.
 *
 * <p>Each bounded context seeds its own data with a {@code Dev...Seeder}: it exists only in the {@code dev} profile, runs
 * at start-up when {@code app.seed.demo-data=true}, does nothing once its data exists, and goes through the use cases
 * (never SQL), so the data follows the business rules. They run in this order:
 *
 * <ol>
 *   <li>{@link #ACCOUNTS_ORDER} auth: the three dev accounts below</li>
 *   <li>{@link #MASTER_DATA_ORDER} master data: categories, suppliers, materials</li>
 *   <li>{@link #REQUISITIONS_ORDER} requisitions in every state, raised by the purchaser</li>
 *   <li>{@link #PURCHASE_ORDERS_ORDER} purchase orders, one of them converted from an approved requisition</li>
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

    /** The approved requisition the purchase order seeder converts into an order. */
    public static final String REQUISITION_TO_ORDER = "Copper & alloys for the assembly line";

    public static final int ACCOUNTS_ORDER = 10;
    public static final int MASTER_DATA_ORDER = 20;
    public static final int REQUISITIONS_ORDER = 30;
    public static final int PURCHASE_ORDERS_ORDER = 40;

    private DevSeed() {
    }
}
