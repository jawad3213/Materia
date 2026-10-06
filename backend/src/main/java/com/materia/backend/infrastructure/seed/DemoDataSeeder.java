package com.materia.backend.infrastructure.seed;

import com.materia.backend.common.domain.enums.CurrencyCode;
import com.materia.backend.common.infrastructure.persistence.BaseJpaEntity;
import com.materia.backend.contexts.auth.domain.enums.Role;
import com.materia.backend.contexts.auth.domain.enums.UserStatus;
import com.materia.backend.contexts.auth.infrastructure.adapters.out.persistence.entities.UserJpaEntity;
import com.materia.backend.contexts.employee.domain.enums.EmploymentStatus;
import com.materia.backend.contexts.employee.infrastructure.adapters.out.persistence.entities.EmployeeJpaEntity;
import com.materia.backend.contexts.goodsReceipt.domain.enums.QualityStatus;
import com.materia.backend.contexts.goodsReceipt.domain.enums.ReceiptStatus;
import com.materia.backend.contexts.goodsReceipt.infrastructure.adapters.out.persistence.entities.GoodsReceiptJpaEntity;
import com.materia.backend.contexts.goodsReceipt.infrastructure.adapters.out.persistence.entities.GoodsReceiptLineJpaEntity;
import com.materia.backend.contexts.invoice.domain.enums.InvoiceStatus;
import com.materia.backend.contexts.invoice.domain.enums.InvoiceType;
import com.materia.backend.contexts.invoice.infrastructure.adapters.out.persistence.entities.InvoiceJpaEntity;
import com.materia.backend.contexts.invoice.infrastructure.adapters.out.persistence.entities.InvoiceLineJpaEntity;
import com.materia.backend.contexts.masterData.domain.enums.MaterialStatus;
import com.materia.backend.contexts.masterData.domain.enums.StockMovementType;
import com.materia.backend.contexts.masterData.domain.ports.out.CodeSequenceRepository;
import com.materia.backend.contexts.masterData.infrastructure.adapters.out.persistence.entities.CategoryJpaEntity;
import com.materia.backend.contexts.masterData.infrastructure.adapters.out.persistence.entities.MaterialJpaEntity;
import com.materia.backend.contexts.masterData.infrastructure.adapters.out.persistence.entities.MaterialStockMovementJpaEntity;
import com.materia.backend.contexts.masterData.infrastructure.adapters.out.persistence.entities.SupplierJpaEntity;
import com.materia.backend.contexts.payment.domain.enums.PaymentStatus;
import com.materia.backend.contexts.payment.infrastructure.adapters.out.persistence.entities.PaymentJpaEntity;
import com.materia.backend.contexts.payment.infrastructure.adapters.out.persistence.entities.PaymentLineJpaEntity;
import com.materia.backend.contexts.purchaseOrder.domain.enums.DeliveryStatus;
import com.materia.backend.contexts.purchaseOrder.domain.enums.OrderStatus;
import com.materia.backend.contexts.purchaseOrder.infrastructure.adapters.out.persistence.entities.PurchaseOrderJpaEntity;
import com.materia.backend.contexts.purchaseOrder.infrastructure.adapters.out.persistence.entities.PurchaseOrderLineJpaEntity;
import com.materia.backend.contexts.purchaseRequisition.domain.enums.RequisitionStatus;
import com.materia.backend.contexts.purchaseRequisition.infrastructure.adapters.out.persistence.entities.RequisitionJpaEntity;
import com.materia.backend.contexts.purchaseRequisition.infrastructure.adapters.out.persistence.entities.RequisitionLineJpaEntity;
import com.materia.backend.contexts.returnToVendor.domain.enums.ResolutionType;
import com.materia.backend.contexts.returnToVendor.domain.enums.ReturnStatus;
import com.materia.backend.contexts.returnToVendor.infrastructure.adapters.out.persistence.entities.ReturnToVendorJpaEntity;
import com.materia.backend.contexts.returnToVendor.infrastructure.adapters.out.persistence.entities.ReturnToVendorLineJpaEntity;
import com.materia.backend.infrastructure.seed.DemoCatalog.CategorySpec;
import com.materia.backend.infrastructure.seed.DemoCatalog.MaterialSpec;
import com.materia.backend.infrastructure.seed.DemoCatalog.PersonSpec;
import com.materia.backend.infrastructure.seed.DemoCatalog.SupplierSpec;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Fills an empty platform with a year of realistic procurement activity, so that every page and the dashboard
 * show meaningful figures: staff accounts, master data, then requisitions, purchase orders, goods receipts (with
 * quality rejections), returns to vendor, invoices, credit notes and payments, in every status of their lifecycle,
 * dated over the twelve months before today.
 *
 * <p>Runs at startup only when {@code app.seed.enabled=true}, and only once: it does nothing when the demo
 * suppliers already exist. Documents are written directly as JPA entities, because the application services
 * stamp every transition with the current date; document numbers still come from the shared code sequences,
 * so documents created later in the application continue the numbering.
 */
@Component
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true")
public class DemoDataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoDataSeeder.class);

    /** Password of every demo account. */
    static final String DEMO_PASSWORD = "Materia@2026";

    private static final BigDecimal VAT_RATE = new BigDecimal("0.20");
    private static final Set<OrderStatus> OPEN_ORDERS = EnumSet.of(OrderStatus.SUBMITTED, OrderStatus.CONFIRMED,
            OrderStatus.READY_FOR_RECEIPT, OrderStatus.PARTIALLY_RECEIVED);
    private static final Set<OrderStatus> COMMITTED_ORDERS = EnumSet.of(OrderStatus.SUBMITTED, OrderStatus.CONFIRMED,
            OrderStatus.READY_FOR_RECEIPT, OrderStatus.PARTIALLY_RECEIVED, OrderStatus.COMPLETED);
    private static final String DELIVERY_ADDRESS = "Main warehouse, Zone Industrielle Ouled Saleh, Bouskoura";

    @PersistenceContext
    private EntityManager em;

    private final TransactionTemplate transaction;
    private final PasswordEncoder passwordEncoder;
    private final CodeSequenceRepository sequences;

    // ---- state of one run ----
    private final Random random = new Random(20261005L);
    private LocalDate today;
    private LocalDate start;
    private final List<BaseJpaEntity> roots = new ArrayList<>();
    private final List<Stamp> stamps = new ArrayList<>();
    private final List<PendingCode> pendingCodes = new ArrayList<>();
    private final List<Runnable> links = new ArrayList<>();

    private final List<Actor> admins = new ArrayList<>();
    private final List<Actor> purchasers = new ArrayList<>();
    private final List<Actor> receivers = new ArrayList<>();
    private final Map<String, CategoryJpaEntity> categories = new HashMap<>();
    private final Map<String, Supplier> suppliers = new LinkedHashMap<>();
    private final List<Mat> materials = new ArrayList<>();
    private final List<Order> orders = new ArrayList<>();
    private final List<Receipt> receipts = new ArrayList<>();
    private final List<InvoiceJpaEntity> invoices = new ArrayList<>();
    private final List<PaymentJpaEntity> payments = new ArrayList<>();
    private final List<ReturnToVendorJpaEntity> returns = new ArrayList<>();
    private final List<RequisitionJpaEntity> requisitions = new ArrayList<>();

    public DemoDataSeeder(PlatformTransactionManager transactionManager, PasswordEncoder passwordEncoder,
                          CodeSequenceRepository sequences) {
        this.transaction = new TransactionTemplate(transactionManager);
        this.transaction.setTimeout(900);
        this.passwordEncoder = passwordEncoder;
        this.sequences = sequences;
    }

    @Override
    public void run(ApplicationArguments args) {
        Boolean seeded = transaction.execute(status -> alreadySeeded());
        if (Boolean.TRUE.equals(seeded)) {
            log.info("Demo data already present, seeding skipped");
            return;
        }
        long began = System.currentTimeMillis();
        log.info("Seeding demo data...");
        transaction.executeWithoutResult(status -> seed());
        log.info("Demo data seeded in {} ms: {} requisitions, {} purchase orders, {} goods receipts, {} invoices, "
                        + "{} payments, {} returns. Demo accounts use the password '{}'.",
                System.currentTimeMillis() - began, requisitions.size(), orders.size(), receipts.size(),
                invoices.size(), payments.size(), returns.size(), DEMO_PASSWORD);
    }

    private boolean alreadySeeded() {
        return em.createQuery("select count(s) from SupplierJpaEntity s where s.name = :name", Long.class)
                .setParameter("name", DemoCatalog.SUPPLIERS.get(0).name())
                .getSingleResult() > 0;
    }

    private void seed() {
        // Documents reference each other both ways (a requisition its order, the order its requisition) and are all
        // inserted in one flush: check the cross-module foreign keys at commit rather than at each insert.
        em.createNativeQuery("SET CONSTRAINTS ALL DEFERRED").executeUpdate();
        today = LocalDate.now();
        start = today.minusMonths(12).withDayOfMonth(1);

        seedPeople();
        seedCategories();
        seedSuppliers();
        seedMaterials();
        seedOrders();
        seedOpenRequisitions();
        seedReturns();
        seedInvoices();
        seedPayments();

        assignCodes();
        links.forEach(Runnable::run);
        simulateStock();

        roots.forEach(em::persist);
        em.flush();
        // Bulk updates flush first; with nothing managed, that flush has nothing to check.
        em.clear();
        applyStamps();
    }

    // ============================================================
    // PEOPLE & MASTER DATA
    // ============================================================

    private void seedPeople() {
        int index = 0;
        for (PersonSpec person : DemoCatalog.PEOPLE) {
            LocalDate hired = start.minusMonths(18).plusDays(index * 41L);
            UserJpaEntity user = em.createQuery("select u from UserJpaEntity u where u.email = :email", UserJpaEntity.class)
                    .setParameter("email", person.email())
                    .getResultStream().findFirst().orElse(null);
            if (user == null) {
                user = new UserJpaEntity();
                user.setEmail(person.email());
                user.setPasswordHash(passwordEncoder.encode(DEMO_PASSWORD));
                user.setRole(person.role());
                user.setEnabled(true);
                user.setFirstName(person.firstName());
                user.setLastName(person.lastName());
                user.setFullName(person.fullName());
                user.setPhone(person.phone());
                user.setStatus(UserStatus.ACTIVE);
                user.setDepartment(person.department());
                user.setMustChangePassword(false);
                user.setCreatedBy("seed");
                persistLater(user, at(hired, 9), at(hired, 9));
            }
            Actor actor = new Actor(user.getId().toString(), person.fullName());
            switch (person.role()) {
                case ADMIN -> admins.add(actor);
                case PURCHASER -> purchasers.add(actor);
                default -> receivers.add(actor);
            }

            boolean employeeExists = em.createQuery("select count(e) from EmployeeJpaEntity e where e.email = :email", Long.class)
                    .setParameter("email", person.email())
                    .getSingleResult() > 0;
            if (!employeeExists) {
                EmployeeJpaEntity employee = new EmployeeJpaEntity();
                employee.setCode(employeeCode());
                employee.setFirstName(person.firstName());
                employee.setLastName(person.lastName());
                employee.setFullName(person.fullName());
                employee.setEmail(person.email());
                employee.setPhone(person.phone());
                employee.setStatus(person.role() == Role.RECEIVER && index == DemoCatalog.PEOPLE.size() - 1
                        ? EmploymentStatus.PROBATION : EmploymentStatus.ACTIVE);
                employee.setUserId(user.getId());
                employee.setHireDate(hired);
                employee.setCreatedBy("seed");
                persistLater(employee, at(hired, 9), at(hired, 9));
            }
            index++;
        }
    }

    private void seedCategories() {
        int year = start.minusMonths(1).getYear();
        LocalDateTime created = at(start.minusDays(25), 10);
        for (CategorySpec spec : DemoCatalog.CATEGORIES) {
            CategoryJpaEntity category = new CategoryJpaEntity();
            category.setCode(masterCode("CAT", year));
            category.setName(spec.name());
            category.setDescription(spec.description());
            category.setShortDescription(spec.description());
            category.setCategoryType(spec.type());
            category.setStatus("ACTIVE");
            CategoryJpaEntity parent = spec.parentKey() != null ? categories.get(spec.parentKey()) : null;
            if (parent != null) {
                category.setParentId(parent.getId());
                category.setParentCode(parent.getCode());
                category.setLevel(1);
                category.setPath("/" + parent.getCode() + "/" + category.getCode() + "/");
            } else {
                category.setLevel(0);
                category.setPath("/" + category.getCode() + "/");
            }
            category.setCreatedBy(admins.get(0).id());
            categories.put(spec.key(), category);
            persistLater(category, created, created);
        }
    }

    private void seedSuppliers() {
        int year = start.minusMonths(1).getYear();
        LocalDateTime created = at(start.minusDays(22), 11);
        for (SupplierSpec spec : DemoCatalog.SUPPLIERS) {
            SupplierJpaEntity entity = new SupplierJpaEntity();
            entity.setCode(masterCode("SUP", year));
            entity.setName(spec.name());
            entity.setDescription(spec.description());
            entity.setContactPerson(spec.contactPerson());
            entity.setContactEmail(spec.email());
            entity.setContactPhone(spec.phone());
            entity.setAddress(spec.address());
            entity.setCity(spec.city());
            entity.setCountry(spec.country());
            entity.setPostalCode(spec.postalCode());
            entity.setPaymentTerms(new ArrayList<>(spec.paymentTerms()));
            entity.setPaymentDelay(spec.paymentDelay());
            entity.setCurrencyCode(spec.currency());
            entity.setStatus(spec.active() ? "ACTIVE" : "INACTIVE");
            entity.setCreatedBy(purchasers.get(0).id());
            suppliers.put(spec.key(), new Supplier(spec, entity));
            persistLater(entity, created, spec.active() ? created : at(today.minusMonths(4), 16));
        }
    }

    private void seedMaterials() {
        int year = start.minusMonths(1).getYear();
        LocalDateTime created = at(start.minusDays(18), 14);
        for (MaterialSpec spec : DemoCatalog.MATERIALS) {
            Supplier supplier = suppliers.get(spec.supplierKey());
            CategoryJpaEntity category = categories.get(spec.categoryKey());
            CurrencyCode currency = supplier.spec.currency();
            BigDecimal price = new BigDecimal(spec.price());

            MaterialJpaEntity entity = new MaterialJpaEntity();
            entity.setCode(masterCode(spec.type().getPrefix(), year));
            entity.setName(spec.name());
            entity.setDescription(spec.description());
            entity.setShortDescription(spec.name());
            entity.setSearchKeywords(spec.name().toLowerCase() + " " + category.getName().toLowerCase());
            entity.setCategoryId(category.getId());
            entity.setCategoryName(category.getName());
            entity.setSupplierId(supplier.entity.getId());
            entity.setSupplierName(supplier.entity.getName());
            entity.setMaterialType(spec.type());
            entity.setStatus(spec.status());
            entity.setUnitOfMeasure(spec.unit());
            entity.setCurrentStock(0);
            entity.setAvailableStock(0);
            entity.setMinimumStock(spec.safetyStock());
            entity.setMaximumStock(spec.maximumStock());
            entity.setReorderPoint(spec.reorderPoint());
            entity.setSafetyStock(spec.safetyStock());
            entity.setEconomicOrderQuantity(spec.orderQuantity());
            entity.setStockOnOrder(0);
            entity.setStandardPrice(price);
            entity.setStandardPriceCurrency(currency);
            entity.setCostPrice(price);
            entity.setCostPriceCurrency(currency);
            entity.setLastPurchasePrice(price);
            entity.setLastPurchasePriceCurrency(currency);
            entity.setAveragePurchasePrice(price);
            entity.setAveragePurchasePriceCurrency(currency);
            entity.setCreatedBy(purchasers.get(0).id());
            LocalDateTime updated = created;
            if (spec.status() == MaterialStatus.OBSOLETE || spec.status() == MaterialStatus.BLOCKED) {
                updated = at(today.minusMonths(5), 15);
                entity.setObsoletedAt(updated);
                entity.setObsoletedBy(admins.get(0).id());
                entity.setObsoletedReason(spec.description());
            }

            Mat mat = new Mat(spec, entity, supplier, category);
            materials.add(mat);
            if (spec.status() == MaterialStatus.ACTIVE) {
                supplier.materials.add(mat);
            }
            persistLater(entity, created, updated);
        }
    }

    // ============================================================
    // REQUISITIONS & PURCHASE ORDERS
    // ============================================================

    /** Orders are placed on working days, a little more often as the business grows over the year. */
    private void seedOrders() {
        long span = Math.max(1, ChronoUnit.DAYS.between(start, today));
        for (LocalDate day = start; !day.isAfter(today); day = day.plusDays(1)) {
            if (day.getDayOfWeek() == DayOfWeek.SATURDAY || day.getDayOfWeek() == DayOfWeek.SUNDAY) continue;
            double rate = 0.38 + 0.22 * ChronoUnit.DAYS.between(start, day) / span;
            int count = (chance(rate) ? 1 : 0) + (chance(rate * 0.25) ? 1 : 0);
            for (int i = 0; i < count; i++) {
                createOrder(day);
            }
        }
    }

    private void createOrder(LocalDate orderDate) {
        Supplier supplier = weightedSupplier();
        long age = ChronoUnit.DAYS.between(orderDate, today);
        OrderStatus status = plannedStatus(age);
        Actor buyer = pick(purchasers);
        Actor approver = chance(0.85) ? admins.get(0) : admins.get(1);
        Actor receiver = pick(receivers);
        double inflation = 1 + 0.05 * ChronoUnit.DAYS.between(start, orderDate) / 365.0;
        String currency = supplier.spec.currency().name();

        PurchaseOrderJpaEntity po = new PurchaseOrderJpaEntity();
        Order order = new Order(po, supplier, receiver);
        pendingCode("PO", orderDate, po::setOrderCode);
        po.setSupplierId(supplier.entity.getId());
        po.setSupplierName(supplier.entity.getName());
        po.setSupplierCode(supplier.entity.getCode());
        po.setOrderDate(orderDate);
        LocalDate expected = orderDate.plusDays(between(supplier.spec.minLeadDays(), supplier.spec.maxLeadDays()));
        po.setExpectedDeliveryDate(expected);
        po.setPaymentTerms(String.join(", ", supplier.spec.paymentTerms()));
        po.setPaymentDelayDays(supplier.spec.paymentDelay());
        po.setDeliveryTerms("Delivery to " + DELIVERY_ADDRESS);
        po.setIncoterm(supplier.spec.currency() == CurrencyCode.MAD ? "DAP" : "CIP");
        po.setCurrencyCode(currency);
        po.setOrderedBy(buyer.id());
        po.setOrderedByName(buyer.name());
        po.setCreatedBy(buyer.id());
        if (chance(0.25)) {
            po.setNotes(pick(List.of("Deliver with certificates of conformity", "Partial deliveries accepted",
                    "Urgent - risk of line stoppage", "Please call the warehouse 24h before delivery",
                    "Prices as per annual framework agreement")));
        }

        int lineCount = Math.min(supplier.materials.size(), 1 + random.nextInt(4));
        List<Mat> chosen = new ArrayList<>(supplier.materials);
        java.util.Collections.shuffle(chosen, random);
        BigDecimal total = BigDecimal.ZERO;
        for (int i = 0; i < lineCount; i++) {
            Mat mat = chosen.get(i);
            int quantity = niceQuantity(mat.spec.orderQuantity() * (0.6 + 0.8 * random.nextDouble()));
            BigDecimal unitPrice = scale(new BigDecimal(mat.spec.price())
                    .multiply(BigDecimal.valueOf(inflation * (0.97 + 0.09 * random.nextDouble()))));
            BigDecimal lineTotal = scale(unitPrice.multiply(BigDecimal.valueOf(quantity)));

            PurchaseOrderLineJpaEntity line = new PurchaseOrderLineJpaEntity();
            line.setPurchaseOrder(po);
            line.setLineNumber(i + 1);
            line.setMaterialCode(mat.entity.getCode());
            line.setMaterialId(mat.entity.getId());
            line.setMaterialName(mat.entity.getName());
            line.setMaterialDescription(mat.entity.getDescription());
            line.setUnitOfMeasure(mat.spec.unit().name());
            line.setQuantity(quantity);
            line.setUnitPrice(unitPrice);
            line.setLineTotal(lineTotal);
            line.setCurrencyCode(currency);
            line.setSupplierId(supplier.entity.getId());
            line.setSupplierName(supplier.entity.getName());
            line.setExpectedDeliveryDate(expected);
            line.setCreatedBy(buyer.id());
            po.getLines().add(line);
            order.materials.put(line, mat);
            total = total.add(lineTotal);
        }
        BigDecimal tax = supplier.spec.currency() == CurrencyCode.MAD ? scale(total.multiply(VAT_RATE)) : BigDecimal.ZERO.setScale(2);
        BigDecimal shipping = supplier.spec.currency() == CurrencyCode.MAD
                ? (chance(0.45) ? BigDecimal.ZERO.setScale(2) : scale(BigDecimal.valueOf(between(15, 90) * 10L)))
                : scale(BigDecimal.valueOf(between(12, 45) * 10L));
        po.setTotalAmount(total);
        po.setTaxAmount(tax);
        po.setShippingCost(shipping);
        po.setGrandTotal(total.add(tax).add(shipping));

        if (chance(0.75)) {
            createConvertedRequisition(order, orderDate, approver);
        }

        LocalDateTime created = at(orderDate, between(8, 11));
        LocalDateTime updated = created;
        if (status == OrderStatus.DRAFT) {
            po.setStatus(OrderStatus.DRAFT);
            po.setDeliveryStatus(DeliveryStatus.NOT_SHIPPED);
        } else if (status == OrderStatus.CANCELLED || status == OrderStatus.REJECTED) {
            LocalDate closed = min(orderDate.plusDays(between(1, 6)), today);
            po.setStatus(status);
            po.setDeliveryStatus(DeliveryStatus.NOT_SHIPPED);
            po.setApprovedBy(approver.id());
            po.setApprovedByName(approver.name());
            String reason = status == OrderStatus.CANCELLED
                    ? pick(List.of("Need cancelled after production plan change", "Supplier price increase not accepted",
                    "Duplicate order", "Replaced by a framework agreement call-off"))
                    : pick(List.of("Item discontinued by the manufacturer", "Production capacity fully booked until next month",
                    "Minimum order quantity not reached", "Credit limit exceeded, prepayment required"));
            po.setNotes((po.getNotes() != null ? po.getNotes() + " " : "")
                    + (status == OrderStatus.CANCELLED ? "Cancelled: " : "Rejected: ") + reason);
            po.setUpdatedBy(status == OrderStatus.CANCELLED ? buyer.id() : approver.id());
            updated = at(closed, 15);
        } else {
            po.setApprovedBy(approver.id());
            po.setApprovedByName(approver.name());
            updated = progressOrder(order, status, orderDate, expected, buyer);
        }
        orders.add(order);
        persistLater(po, created, updated);
        stampChildren("PurchaseOrderLineJpaEntity", "purchaseOrder", po.getId(), created, updated);
    }

    /** Takes a sent order through confirmation, assignment and receipts, as far as its planned status and today allow. */
    private LocalDateTime progressOrder(Order order, OrderStatus planned, LocalDate orderDate, LocalDate expected, Actor buyer) {
        PurchaseOrderJpaEntity po = order.po;
        LocalDate confirmed = orderDate.plusDays(between(1, 3));
        if (planned == OrderStatus.SUBMITTED || confirmed.isAfter(today)) {
            po.setStatus(OrderStatus.SUBMITTED);
            po.setDeliveryStatus(DeliveryStatus.NOT_SHIPPED);
            return at(orderDate, 16);
        }
        po.setConfirmedDeliveryDate(expected.plusDays(chance(0.7) ? 0 : between(1, 4)));
        LocalDate assigned = confirmed.plusDays(between(0, 2));
        if (planned == OrderStatus.CONFIRMED || assigned.isAfter(today)) {
            po.setStatus(OrderStatus.CONFIRMED);
            po.setDeliveryStatus(chance(0.5) ? DeliveryStatus.SHIPPED : DeliveryStatus.NOT_SHIPPED);
            return at(confirmed, 14);
        }

        // Supplier punctuality decides when the goods arrive.
        LocalDate firstDate = chance(Math.min(0.97, order.supplier.spec.onTimeRate() + 0.08))
                ? expected.minusDays(between(0, 3))
                : expected.plusDays(between(1, 10));
        if (firstDate.isBefore(confirmed.plusDays(1))) firstDate = confirmed.plusDays(1);
        if (assigned.isAfter(firstDate)) assigned = firstDate;
        po.setAssignedTo(order.receiver.id());
        po.setAssignedToName(order.receiver.name());
        po.setAssignedAt(at(assigned, 10));
        po.setAssignedBy(buyer.id());
        po.setAssignedByName(buyer.name());

        boolean wantsReceipt = planned == OrderStatus.COMPLETED || planned == OrderStatus.PARTIALLY_RECEIVED;
        if (!wantsReceipt || firstDate.isAfter(today)) {
            po.setStatus(OrderStatus.READY_FOR_RECEIPT);
            po.setDeliveryStatus(expected.isBefore(today)
                    ? (chance(0.7) ? DeliveryStatus.DELAYED : DeliveryStatus.IN_TRANSIT)
                    : (chance(0.5) ? DeliveryStatus.IN_TRANSIT : DeliveryStatus.SHIPPED));
            if (!expected.isAfter(today.plusDays(1)) && chance(0.35)) {
                draftReceipt(order, min(today, expected.plusDays(1)));
            }
            return at(assigned, 11);
        }

        if (chance(0.03)) {
            cancelledReceipt(order, firstDate);
        }

        boolean split = planned == OrderStatus.PARTIALLY_RECEIVED || chance(0.22);
        if (!split) {
            Map<PurchaseOrderLineJpaEntity, Integer> all = new LinkedHashMap<>();
            po.getLines().forEach(line -> all.put(line, line.getQuantity()));
            receive(order, firstDate, all, false);
            closeOrder(order, firstDate);
            return at(firstDate, 16);
        }

        Map<PurchaseOrderLineJpaEntity, Integer> first = new LinkedHashMap<>();
        for (PurchaseOrderLineJpaEntity line : po.getLines()) {
            int quantity = line.getQuantity();
            int part = quantity == 1 ? (chance(0.5) ? 1 : 0)
                    : Math.max(1, Math.min(quantity - 1, niceQuantity(quantity * (0.35 + 0.4 * random.nextDouble()))));
            first.put(line, part);
        }
        receive(order, firstDate, first, false);
        LocalDate secondDate = firstDate.plusDays(between(4, 14));
        if (planned == OrderStatus.COMPLETED && secondDate.isBefore(today)) {
            Map<PurchaseOrderLineJpaEntity, Integer> rest = new LinkedHashMap<>();
            po.getLines().forEach(line -> rest.put(line, line.getQuantity() - order.received(line)));
            receive(order, secondDate, rest, false);
            closeOrder(order, secondDate);
            return at(secondDate, 16);
        }
        po.setStatus(OrderStatus.PARTIALLY_RECEIVED);
        po.setDeliveryStatus(DeliveryStatus.PARTIAL);
        return at(firstDate, 16);
    }

    private void closeOrder(Order order, LocalDate receivedDate) {
        order.po.setStatus(OrderStatus.COMPLETED);
        order.po.setDeliveryStatus(DeliveryStatus.DELIVERED);
        order.po.setReceivedDate(receivedDate);
        order.po.setUpdatedBy(order.receiver.id());
    }

    private OrderStatus plannedStatus(long age) {
        if (age <= 2) return weighted(OrderStatus.DRAFT, 35, OrderStatus.SUBMITTED, 50, OrderStatus.CONFIRMED, 15);
        if (age <= 7) return weighted(OrderStatus.DRAFT, 8, OrderStatus.SUBMITTED, 25, OrderStatus.CONFIRMED, 40,
                OrderStatus.READY_FOR_RECEIPT, 22, OrderStatus.CANCELLED, 5);
        if (age <= 20) return weighted(OrderStatus.SUBMITTED, 4, OrderStatus.CONFIRMED, 14, OrderStatus.READY_FOR_RECEIPT, 38,
                OrderStatus.PARTIALLY_RECEIVED, 14, OrderStatus.COMPLETED, 28, OrderStatus.CANCELLED, 2);
        if (age <= 45) return weighted(OrderStatus.READY_FOR_RECEIPT, 9, OrderStatus.PARTIALLY_RECEIVED, 13,
                OrderStatus.COMPLETED, 72, OrderStatus.CANCELLED, 3, OrderStatus.REJECTED, 3);
        return weighted(OrderStatus.COMPLETED, 88, OrderStatus.PARTIALLY_RECEIVED, 3, OrderStatus.CANCELLED, 5,
                OrderStatus.REJECTED, 4);
    }

    /** The requisition the order was converted from: created by a requester, approved, then converted. */
    private void createConvertedRequisition(Order order, LocalDate orderDate, Actor approver) {
        PurchaseOrderJpaEntity po = order.po;
        Actor requester = pickRequester();
        LocalDate created = orderDate.minusDays(between(2, 8));
        LocalDate submitted = created.plusDays(between(0, 1));
        LocalDate approved = min(submitted.plusDays(between(1, 3)), orderDate);

        RequisitionJpaEntity requisition = requisitionHeader(order.supplier, order.materials.values().iterator().next(), requester,
                created, orderDate.plusDays(between(10, 25)));
        requisition.setStatus(RequisitionStatus.CONVERTED);
        requisition.setSubmittedDate(submitted);
        requisition.setApprovedDate(approved);
        requisition.setConvertedDate(orderDate);
        requisition.setApproverId(approver.id());
        requisition.setApproverName(approver.name());
        requisition.setApprovalNotes(chance(0.4) ? "Approved, within budget" : null);
        requisition.setPurchaseOrderId(po.getId());
        links.add(() -> requisition.setPurchaseOrderCode(po.getOrderCode()));
        requisition.setUpdatedBy(po.getOrderedBy());

        BigDecimal total = BigDecimal.ZERO;
        for (PurchaseOrderLineJpaEntity poLine : po.getLines()) {
            Mat mat = order.materials.get(poLine);
            RequisitionLineJpaEntity line = requisitionLine(requisition, mat, poLine.getLineNumber(), poLine.getQuantity(),
                    poLine.getUnitPrice(), requisition.getRequiredDate());
            poLine.setRequisitionLineId(line.getId());
            order.requisitionLines.put(poLine, line);
            total = total.add(line.getLineTotal());
        }
        requisition.setTotalAmount(total);
        po.setRequisitionId(requisition.getId());
        links.add(() -> po.setRequisitionCode(requisition.getRequisitionCode()));

        requisitions.add(requisition);
        pendingCode("REQ", created, requisition::setRequisitionCode);
        LocalDateTime createdAt = at(created, between(8, 12));
        persistLater(requisition, createdAt, at(orderDate, 10));
        stampChildren("RequisitionLineJpaEntity", "requisition", requisition.getId(), createdAt, at(orderDate, 10));
    }

    /** Requisitions that never became orders: waiting for approval, approved, drafts, rejected and cancelled. */
    private void seedOpenRequisitions() {
        record Plan(RequisitionStatus status, int count, int minAge, int maxAge) {
        }
        List<Plan> plans = List.of(
                new Plan(RequisitionStatus.SUBMITTED, 7, 0, 9),
                new Plan(RequisitionStatus.APPROVED, 4, 1, 12),
                new Plan(RequisitionStatus.DRAFT, 4, 0, 6),
                new Plan(RequisitionStatus.REJECTED, 6, 10, 320),
                new Plan(RequisitionStatus.CANCELLED, 4, 15, 300));
        for (Plan plan : plans) {
            for (int i = 0; i < plan.count(); i++) {
                Supplier supplier = weightedSupplier();
                LocalDate created = today.minusDays(between(plan.minAge(), plan.maxAge()));
                Actor requester = pickRequester();
                Actor approver = chance(0.85) ? admins.get(0) : admins.get(1);
                List<Mat> chosen = new ArrayList<>(supplier.materials);
                java.util.Collections.shuffle(chosen, random);
                int lineCount = Math.min(chosen.size(), between(1, 3));

                RequisitionJpaEntity requisition = requisitionHeader(supplier, chosen.get(0), requester, created,
                        created.plusDays(between(12, 30)));
                requisition.setStatus(plan.status());
                BigDecimal total = BigDecimal.ZERO;
                for (int l = 0; l < lineCount; l++) {
                    Mat mat = chosen.get(l);
                    int quantity = niceQuantity(mat.spec.orderQuantity() * (0.5 + 0.8 * random.nextDouble()));
                    RequisitionLineJpaEntity line = requisitionLine(requisition, mat, l + 1, quantity,
                            new BigDecimal(mat.spec.price()), requisition.getRequiredDate());
                    total = total.add(line.getLineTotal());
                }
                requisition.setTotalAmount(total);

                LocalDate submitted = min(created.plusDays(between(0, 1)), today);
                LocalDate decided = min(submitted.plusDays(between(1, 3)), today);
                LocalDateTime updated = at(created, 12);
                switch (plan.status()) {
                    case SUBMITTED -> {
                        requisition.setSubmittedDate(submitted);
                        updated = at(submitted, 15);
                    }
                    case APPROVED -> {
                        requisition.setSubmittedDate(submitted);
                        requisition.setApprovedDate(decided);
                        requisition.setApproverId(approver.id());
                        requisition.setApproverName(approver.name());
                        requisition.setApprovalNotes(pick(List.of("Approved - order with the usual supplier",
                                "Approved, please group with next order", "OK, urgent for production")));
                        updated = at(decided, 11);
                    }
                    case REJECTED -> {
                        requisition.setSubmittedDate(submitted);
                        requisition.setApproverId(approver.id());
                        requisition.setApproverName(approver.name());
                        requisition.setRejectionReason(pick(List.of("Budget exceeded for this quarter",
                                "Duplicate of an existing requisition", "Quantity not justified by consumption history",
                                "Stock available in the secondary warehouse", "Please use the framework agreement supplier")));
                        updated = at(decided, 11);
                    }
                    case CANCELLED -> {
                        LocalDate cancelled = min(created.plusDays(between(1, 5)), today);
                        requisition.setCancelledDate(cancelled);
                        requisition.setCancellationReason(pick(List.of("Production order cancelled by the customer",
                                "Need covered by an internal transfer", "Created by mistake", "Project postponed to next year")));
                        updated = at(cancelled, 16);
                    }
                    default -> {
                    }
                }
                requisitions.add(requisition);
                pendingCode("REQ", created, requisition::setRequisitionCode);
                LocalDateTime createdAt = at(created, between(8, 12));
                if (updated.isBefore(createdAt)) updated = createdAt;
                persistLater(requisition, createdAt, updated);
                stampChildren("RequisitionLineJpaEntity", "requisition", requisition.getId(), createdAt, updated);
            }
        }
    }

    private RequisitionJpaEntity requisitionHeader(Supplier supplier, Mat mainMaterial, Actor requester, LocalDate created,
                                                   LocalDate requiredDate) {
        String purpose = pick(DemoCatalog.REQUISITION_PURPOSES.get(mainMaterial.categoryKey()));
        RequisitionJpaEntity requisition = new RequisitionJpaEntity();
        requisition.setTitle(purpose + " - " + mainMaterial.entity.getName());
        requisition.setDescription(purpose + ". Materials requested from " + supplier.entity.getName()
                + " for delivery to the " + DELIVERY_ADDRESS.toLowerCase() + ".");
        requisition.setJustification(pick(List.of("Stock below reorder point", "Planned consumption for the coming weeks",
                "Customer order backlog", "Preventive maintenance plan", "Safety stock rebuild")));
        requisition.setRequesterId(requester.id());
        requisition.setRequesterName(requester.name());
        requisition.setRequiredDate(requiredDate);
        requisition.setCurrencyCode(supplier.spec.currency().name());
        requisition.setCreatedBy(requester.id());
        return requisition;
    }

    private RequisitionLineJpaEntity requisitionLine(RequisitionJpaEntity requisition, Mat mat, int number, int quantity,
                                                     BigDecimal unitPrice, LocalDate requiredDate) {
        RequisitionLineJpaEntity line = new RequisitionLineJpaEntity();
        line.setRequisition(requisition);
        line.setLineNumber(number);
        line.setMaterialCode(mat.entity.getCode());
        line.setMaterialId(mat.entity.getId());
        line.setMaterialName(mat.entity.getName());
        line.setMaterialDescription(mat.entity.getDescription());
        line.setUnitOfMeasure(mat.spec.unit().name());
        line.setStandardPrice(new BigDecimal(mat.spec.price()));
        line.setUnitPrice(unitPrice);
        line.setCurrencyCode(mat.supplier.spec.currency().name());
        line.setCurrencyCodeLine(mat.supplier.spec.currency().name());
        line.setQuantity(quantity);
        line.setQuantityReceived(0);
        line.setQuantityRejected(0);
        line.setRequiredDate(requiredDate);
        line.setLineTotal(scale(unitPrice.multiply(BigDecimal.valueOf(quantity))));
        line.setSupplierId(mat.supplier.entity.getId());
        line.setSupplierName(mat.supplier.entity.getName());
        line.setSupplierCode(mat.supplier.entity.getCode());
        line.setDeliveryTerms("Delivery to " + DELIVERY_ADDRESS);
        line.setStorageLocation(storageLocation(mat));
        line.setCreatedBy(requisition.getCreatedBy());
        requisition.getLines().add(line);
        return line;
    }

    // ============================================================
    // GOODS RECEIPTS
    // ============================================================

    /** A completed receipt of the given quantities; quality control may reject part of them. */
    private Receipt receive(Order order, LocalDate date, Map<PurchaseOrderLineJpaEntity, Integer> quantities, boolean replacement) {
        PurchaseOrderJpaEntity po = order.po;
        GoodsReceiptJpaEntity gr = receiptHeader(order, date);
        Receipt receipt = new Receipt(gr, order, date, replacement);

        int number = 1;
        for (Map.Entry<PurchaseOrderLineJpaEntity, Integer> entry : quantities.entrySet()) {
            int received = entry.getValue();
            if (received <= 0) continue;
            PurchaseOrderLineJpaEntity poLine = entry.getKey();
            Mat mat = order.materials.get(poLine);

            int rejected = 0;
            QualityStatus quality = QualityStatus.ACCEPTED;
            String reason = null;
            if (!replacement && chance(order.supplier.spec.defectRate())) {
                rejected = received <= 30 && chance(0.35)
                        ? received
                        : Math.max(1, (int) Math.round(received * (0.05 + 0.2 * random.nextDouble())));
                quality = rejected == received ? QualityStatus.REJECTED : QualityStatus.PARTIAL;
                reason = pick(DemoCatalog.REJECTION_REASONS.get(mat.categoryKey()));
            }
            int accepted = received - rejected;
            order.receivedByLine.merge(poLine, received, Integer::sum);

            GoodsReceiptLineJpaEntity line = new GoodsReceiptLineJpaEntity();
            line.setGoodsReceipt(gr);
            line.setLineNumber(number++);
            line.setPurchaseOrderLineId(poLine.getId());
            line.setMaterialCode(mat.entity.getCode());
            line.setMaterialId(mat.entity.getId());
            line.setMaterialName(mat.entity.getName());
            line.setUnitOfMeasure(mat.spec.unit().name());
            line.setQuantityOrdered(poLine.getQuantity());
            line.setQuantityReceived(received);
            line.setQuantityRejected(rejected);
            line.setQuantityAccepted(accepted);
            line.setQuantityPending(Math.max(0, poLine.getQuantity() - order.received(poLine)));
            line.setQualityStatus(quality);
            line.setQualityNotes(rejected > 0 ? rejected + " " + mat.spec.unit().name() + " rejected at incoming inspection"
                    : (replacement ? "Replacement goods inspected, compliant" : "Visual and dimensional check OK"));
            line.setRejectionReason(reason);
            line.setUnitPrice(poLine.getUnitPrice());
            line.setLineTotal(scale(poLine.getUnitPrice().multiply(BigDecimal.valueOf(received))));
            line.setCurrencyCode(po.getCurrencyCode());
            line.setSupplierId(order.supplier.entity.getId());
            line.setSupplierName(order.supplier.entity.getName());
            line.setBatchNumber(batchNumber(mat, date));
            line.setExpiryDate(mat.spec.type() == com.materia.backend.contexts.masterData.domain.enums.MaterialType.CHEMICAL
                    ? date.plusMonths(between(12, 24)) : null);
            line.setStorageLocation(storageLocation(mat));
            line.setCreatedBy(order.receiver.id());
            gr.getLines().add(line);
            receipt.materials.put(line, mat);
            receipt.poLines.put(line, poLine);

            RequisitionLineJpaEntity requisitionLine = order.requisitionLines.get(poLine);
            if (requisitionLine != null) {
                requisitionLine.setQuantityReceived(requisitionLine.getQuantityReceived() + accepted);
                requisitionLine.setQuantityRejected(requisitionLine.getQuantityRejected() + rejected);
            }
            if (accepted > 0) {
                mat.events.add(new StockEvent(at(date, between(10, 16)), StockMovementType.RECEIPT, accepted, line, gr));
            }
        }

        boolean complete = po.getLines().stream().allMatch(l -> order.received(l) >= l.getQuantity());
        gr.setStatus(complete || replacement ? ReceiptStatus.COMPLETED : ReceiptStatus.PARTIAL);
        totals(gr);
        if (replacement) {
            gr.setNotes("Replacement delivery for goods returned to the supplier");
        } else if (gr.getStatus() == ReceiptStatus.PARTIAL) {
            gr.setNotes("Partial delivery, balance announced by the supplier");
        }
        order.receipts.add(receipt);
        receipts.add(receipt);
        LocalDateTime createdAt = at(date, 9);
        persistLater(gr, createdAt, at(date, 17));
        stampChildren("GoodsReceiptLineJpaEntity", "goodsReceipt", gr.getId(), createdAt, at(date, 17));
        return receipt;
    }

    /** A receipt the receiver has started but not completed yet: no stock effect. */
    private void draftReceipt(Order order, LocalDate date) {
        GoodsReceiptJpaEntity gr = receiptHeader(order, date);
        gr.setStatus(ReceiptStatus.DRAFT);
        int number = 1;
        for (PurchaseOrderLineJpaEntity poLine : order.po.getLines()) {
            Mat mat = order.materials.get(poLine);
            GoodsReceiptLineJpaEntity line = new GoodsReceiptLineJpaEntity();
            line.setGoodsReceipt(gr);
            line.setLineNumber(number++);
            line.setPurchaseOrderLineId(poLine.getId());
            line.setMaterialCode(mat.entity.getCode());
            line.setMaterialId(mat.entity.getId());
            line.setMaterialName(mat.entity.getName());
            line.setUnitOfMeasure(mat.spec.unit().name());
            line.setQuantityOrdered(poLine.getQuantity());
            line.setQuantityReceived(poLine.getQuantity());
            line.setQuantityRejected(0);
            line.setQuantityAccepted(poLine.getQuantity());
            line.setQuantityPending(0);
            line.setQualityStatus(QualityStatus.UNDER_REVIEW);
            line.setUnitPrice(poLine.getUnitPrice());
            line.setLineTotal(poLine.getLineTotal());
            line.setCurrencyCode(order.po.getCurrencyCode());
            line.setSupplierId(order.supplier.entity.getId());
            line.setSupplierName(order.supplier.entity.getName());
            line.setStorageLocation(storageLocation(mat));
            line.setCreatedBy(order.receiver.id());
            gr.getLines().add(line);
        }
        totals(gr);
        gr.setNotes("Unloading in progress, quality check pending");
        LocalDateTime createdAt = at(date, between(8, 11));
        persistLater(gr, createdAt, createdAt);
        stampChildren("GoodsReceiptLineJpaEntity", "goodsReceipt", gr.getId(), createdAt, createdAt);
    }

    /** A receipt entered against the wrong order and cancelled right away. */
    private void cancelledReceipt(Order order, LocalDate date) {
        GoodsReceiptJpaEntity gr = receiptHeader(order, date);
        gr.setStatus(ReceiptStatus.CANCELLED);
        gr.setTotalQuantityOrdered(order.po.getLines().stream().mapToInt(PurchaseOrderLineJpaEntity::getQuantity).sum());
        gr.setTotalQuantityReceived(0);
        gr.setTotalQuantityAccepted(0);
        gr.setTotalQuantityRejected(0);
        gr.setNotes("Cancelled: Delivery note entered against the wrong purchase order");
        LocalDateTime createdAt = at(date, 9);
        persistLater(gr, createdAt, at(date, 10));
    }

    private GoodsReceiptJpaEntity receiptHeader(Order order, LocalDate date) {
        PurchaseOrderJpaEntity po = order.po;
        GoodsReceiptJpaEntity gr = new GoodsReceiptJpaEntity();
        pendingCode("GR", date, gr::setReceiptCode);
        gr.setPurchaseOrderId(po.getId());
        links.add(() -> gr.setPurchaseOrderCode(po.getOrderCode()));
        gr.setReceiptDate(date);
        gr.setExpectedDeliveryDate(po.getExpectedDeliveryDate());
        gr.setReceivedBy(order.receiver.id());
        gr.setReceivedByName(order.receiver.name());
        gr.setSupplierId(order.supplier.entity.getId());
        gr.setSupplierName(order.supplier.entity.getName());
        gr.setHasDiscrepancy(false);
        gr.setCreatedBy(order.receiver.id());
        return gr;
    }

    private void totals(GoodsReceiptJpaEntity gr) {
        int ordered = 0, received = 0, rejected = 0, accepted = 0;
        StringBuilder discrepancies = new StringBuilder();
        for (GoodsReceiptLineJpaEntity line : gr.getLines()) {
            ordered += line.getQuantityOrdered();
            received += line.getQuantityReceived();
            rejected += line.getQuantityRejected();
            accepted += line.getQuantityAccepted();
            if (line.getQuantityRejected() > 0) {
                if (discrepancies.length() > 0) discrepancies.append("; ");
                discrepancies.append(line.getMaterialName()).append(": ").append(line.getQuantityRejected())
                        .append(" rejected (").append(line.getRejectionReason()).append(")");
            }
        }
        gr.setTotalQuantityOrdered(ordered);
        gr.setTotalQuantityReceived(received);
        gr.setTotalQuantityRejected(rejected);
        gr.setTotalQuantityAccepted(accepted);
        gr.setHasDiscrepancy(rejected > 0);
        gr.setDiscrepancyNotes(rejected > 0 ? truncate(discrepancies.toString(), 1000) : null);
    }

    // ============================================================
    // RETURNS TO VENDOR
    // ============================================================

    /** Every receipt with rejected goods sends them back; the supplier then replaces them or issues a credit note. */
    private void seedReturns() {
        for (Receipt receipt : new ArrayList<>(receipts)) {
            if (receipt.replacement || receipt.gr.getTotalQuantityRejected() == 0) continue;
            Order order = receipt.order;
            GoodsReceiptJpaEntity gr = receipt.gr;
            Actor receiver = order.receiver;
            Actor buyer = actorById(order.po.getOrderedBy());
            LocalDate returnDate = min(receipt.date.plusDays(between(1, 3)), today);
            long age = ChronoUnit.DAYS.between(returnDate, today);

            ReturnStatus status;
            if (age <= 4) status = chance(0.6) ? ReturnStatus.DRAFT : ReturnStatus.PENDING;
            else if (age <= 25) status = chance(0.7) ? ReturnStatus.PENDING : ReturnStatus.RESOLVED;
            else status = chance(0.9) ? ReturnStatus.RESOLVED : ReturnStatus.CANCELLED;
            LocalDate resolutionDate = returnDate.plusDays(between(6, 20));
            if (status == ReturnStatus.RESOLVED && resolutionDate.isAfter(today)) status = ReturnStatus.PENDING;

            ReturnToVendorJpaEntity rtv = new ReturnToVendorJpaEntity();
            pendingCode("RTN", returnDate, rtv::setReturnCode);
            rtv.setGoodsReceiptId(gr.getId());
            rtv.setPurchaseOrderId(order.po.getId());
            links.add(() -> {
                rtv.setGoodsReceiptCode(gr.getReceiptCode());
                rtv.setPurchaseOrderCode(order.po.getOrderCode());
            });
            rtv.setSupplierId(order.supplier.entity.getId());
            rtv.setSupplierName(order.supplier.entity.getName());
            rtv.setSupplierCode(order.supplier.entity.getCode());
            rtv.setCurrencyCode(order.po.getCurrencyCode());
            rtv.setReturnDate(returnDate);
            rtv.setRejectionSummary(truncate(gr.getDiscrepancyNotes(), 500));
            rtv.setReturnReason("Goods rejected at incoming quality inspection");
            rtv.setCreatedBy(receiver.id());

            BigDecimal value = BigDecimal.ZERO;
            Map<PurchaseOrderLineJpaEntity, Integer> returned = new LinkedHashMap<>();
            int number = 1;
            for (GoodsReceiptLineJpaEntity grLine : gr.getLines()) {
                if (grLine.getQuantityRejected() == 0) continue;
                ReturnToVendorLineJpaEntity line = new ReturnToVendorLineJpaEntity();
                line.setReturnToVendor(rtv);
                line.setLineNumber(number++);
                line.setGoodsReceiptLineId(grLine.getId());
                line.setPurchaseOrderLineId(grLine.getPurchaseOrderLineId());
                line.setMaterialId(grLine.getMaterialId());
                line.setMaterialCode(grLine.getMaterialCode());
                line.setMaterialName(grLine.getMaterialName());
                line.setUnitOfMeasure(grLine.getUnitOfMeasure());
                line.setUnitPrice(grLine.getUnitPrice());
                line.setRejectedQuantity(grLine.getQuantityRejected());
                line.setQuantityToReturn(grLine.getQuantityRejected());
                line.setQuantityAlreadyReturned(0);
                line.setRejectionReason(grLine.getRejectionReason());
                line.setQualityNotes(grLine.getQualityNotes());
                line.setDefectDescription(grLine.getRejectionReason() + ", photos attached to the quality report");
                line.setCreatedBy(receiver.id());
                rtv.getLines().add(line);
                value = value.add(grLine.getUnitPrice().multiply(BigDecimal.valueOf(grLine.getQuantityRejected())));
                returned.put(receipt.poLines.get(grLine), grLine.getQuantityRejected());
            }

            rtv.setStatus(status.name());
            LocalDateTime updated = at(returnDate, 11);
            switch (status) {
                case DRAFT -> rtv.setNotes("Goods set aside in the quarantine area, waiting for pickup");
                case PENDING -> rtv.setNotes("Goods handed over to the supplier's carrier, awaiting settlement");
                case CANCELLED -> {
                    rtv.setNotes("Cancelled: Supplier granted a discount, goods reworked and kept");
                    updated = at(returnDate.plusDays(between(2, 6)), 15);
                }
                case RESOLVED -> {
                    updated = at(resolutionDate, 15);
                    rtv.setResolutionDate(resolutionDate);
                    if (chance(0.55)) {
                        String reference = "AV-" + resolutionDate.getYear() + "-" + between(1000, 9999);
                        rtv.setResolutionType(ResolutionType.CREDIT_NOTE.name());
                        rtv.setCreditNoteReference(reference);
                        rtv.setCreditNoteAmount(scale(value).toPlainString());
                        rtv.setSupplierResponse("Defect acknowledged, credit note " + reference + " issued");
                        rtv.getLines().forEach(l -> l.setCreditNote(true));
                        creditNote(receipt, rtv, reference, resolutionDate, buyer);
                    } else {
                        String reference = "BL-" + between(100000, 999999);
                        rtv.setResolutionType(ResolutionType.REPLACEMENT.name());
                        rtv.setReplacementPurchaseOrderReference(reference);
                        rtv.setSupplierResponse("Replacement shipped with delivery note " + reference);
                        rtv.getLines().forEach(l -> l.setReplaced(true));
                        LocalDate replacementDate = resolutionDate.plusDays(between(3, 10));
                        if (replacementDate.isBefore(today)) {
                            receive(order, replacementDate, returned, true);
                        } else if (order.po.getStatus() == OrderStatus.COMPLETED) {
                            // The replacement is expected again: the order goes back to partially received.
                            order.po.setStatus(OrderStatus.PARTIALLY_RECEIVED);
                            order.po.setDeliveryStatus(DeliveryStatus.PARTIAL);
                            order.po.setReceivedDate(null);
                        }
                    }
                }
            }
            if (status != ReturnStatus.DRAFT) {
                rtv.setInternalNotes("Return authorization requested by " + buyer.name());
            }
            returns.add(rtv);
            LocalDateTime createdAt = at(returnDate, 9);
            persistLater(rtv, createdAt, updated);
            stampChildren("ReturnToVendorLineJpaEntity", "returnToVendor", rtv.getId(), createdAt, updated);
        }
    }

    // ============================================================
    // INVOICES
    // ============================================================

    /** One supplier invoice per completed receipt, taken as far as its age allows: drafts to paid. */
    private void seedInvoices() {
        Actor finance = admins.get(1);
        for (Receipt receipt : receipts) {
            if (receipt.replacement) continue;
            LocalDate invoiceDate = receipt.date.plusDays(between(0, 5));
            if (invoiceDate.isAfter(today)) continue;
            boolean overbilled = chance(0.05);
            long age = ChronoUnit.DAYS.between(invoiceDate, today);

            if (overbilled && age > 20) {
                // The supplier billed more than delivered: the invoice is cancelled and a corrected one follows.
                InvoiceJpaEntity wrong = standardInvoice(receipt, invoiceDate, true);
                wrong.setStatus(InvoiceStatus.CANCELLED);
                wrong.setInternalNotes("Cancelled: quantities billed above quantities received, corrected invoice requested");
                LocalDate cancelled = invoiceDate.plusDays(between(2, 5));
                wrong.setUpdatedBy(finance.id());
                persistInvoice(wrong, at(invoiceDate.plusDays(1), 10), at(cancelled, 15));
                LocalDate corrected = min(cancelled.plusDays(between(3, 8)), today);
                InvoiceJpaEntity invoice = standardInvoice(receipt, corrected, false);
                invoice.setNotes("Corrected invoice, replaces " + wrong.getExternalReference());
                progressInvoice(invoice, finance);
            } else {
                InvoiceJpaEntity invoice = standardInvoice(receipt, invoiceDate, overbilled);
                if (overbilled) {
                    invoice.setStatus(InvoiceStatus.SUBMITTED);
                    invoice.setInternalNotes("Blocked: quantity discrepancy with the goods receipt, supplier contacted");
                    persistInvoice(invoice, at(invoice.getReceivedDate(), 10), at(invoice.getReceivedDate(), 16));
                } else {
                    progressInvoice(invoice, finance);
                }
            }
        }
    }

    /** Takes a valid invoice through submission and verification; payment is planned by {@link #seedPayments()}. */
    private void progressInvoice(InvoiceJpaEntity invoice, Actor finance) {
        LocalDate received = invoice.getReceivedDate();
        long age = ChronoUnit.DAYS.between(received, today);
        LocalDateTime created = at(received, between(9, 12));
        if (age <= 3 && chance(0.5)) {
            invoice.setStatus(InvoiceStatus.DRAFT);
            persistInvoice(invoice, created, created);
            return;
        }
        if (age <= 8 && chance(0.7)) {
            invoice.setStatus(InvoiceStatus.SUBMITTED);
            persistInvoice(invoice, created, at(received, 16));
            return;
        }
        LocalDate verified = min(received.plusDays(between(1, 6)), today);
        invoice.setStatus(InvoiceStatus.VERIFIED);
        invoice.setVerified(true);
        invoice.setVerificationDate(at(verified, 11));
        invoice.setVerifiedBy(finance.id());
        invoice.setVerifiedByName(finance.name());
        invoice.setUpdatedBy(finance.id());
        persistInvoice(invoice, created, at(verified, 11));
    }

    private InvoiceJpaEntity standardInvoice(Receipt receipt, LocalDate invoiceDate, boolean overbilled) {
        InvoiceJpaEntity invoice = invoiceHeader(receipt, InvoiceType.STANDARD, invoiceDate);
        invoice.setReceivedDate(min(invoiceDate.plusDays(between(0, 3)), today));
        invoice.setDueDate(invoiceDate.plusDays(receipt.order.supplier.spec.paymentDelay()));
        invoice.setExternalReference(supplierInvoiceNumber(invoiceDate));
        boolean taxed = CurrencyCode.MAD.name().equals(invoice.getCurrencyCode());
        int number = 1;
        StringBuilder discrepancies = new StringBuilder();
        for (GoodsReceiptLineJpaEntity grLine : receipt.gr.getLines()) {
            int invoiced = grLine.getQuantityReceived();
            if (overbilled && number == 1) {
                invoiced += Math.max(1, invoiced / 10);
                discrepancies.append(grLine.getMaterialName()).append(": invoiced ").append(invoiced)
                        .append(", received ").append(grLine.getQuantityReceived());
            }
            invoice.getLines().add(invoiceLine(invoice, receipt, grLine, number++, invoiced, taxed));
        }
        if (overbilled) {
            invoice.setHasDiscrepancy(true);
            invoice.setDiscrepancySummary(discrepancies.toString());
        }
        invoiceTotals(invoice);
        return invoice;
    }

    /** The credit note a supplier issues for returned goods; it is deducted from what the company owes. */
    private void creditNote(Receipt receipt, ReturnToVendorJpaEntity rtv, String reference, LocalDate date, Actor buyer) {
        Actor finance = admins.get(1);
        InvoiceJpaEntity invoice = invoiceHeader(receipt, InvoiceType.CREDIT_NOTE, date);
        invoice.setReceivedDate(date);
        invoice.setExternalReference(reference);
        boolean taxed = CurrencyCode.MAD.name().equals(invoice.getCurrencyCode());
        int number = 1;
        for (GoodsReceiptLineJpaEntity grLine : receipt.gr.getLines()) {
            if (grLine.getQuantityRejected() == 0) continue;
            invoice.getLines().add(invoiceLine(invoice, receipt, grLine, number++, grLine.getQuantityRejected(), taxed));
        }
        invoiceTotals(invoice);
        LocalDate verified = min(date.plusDays(between(1, 3)), today);
        invoice.setStatus(InvoiceStatus.VERIFIED);
        invoice.setVerified(true);
        invoice.setVerificationDate(at(verified, 10));
        invoice.setVerifiedBy(finance.id());
        invoice.setVerifiedByName(finance.name());
        links.add(() -> invoice.setNotes("Credit note for return " + rtv.getReturnCode()));
        invoice.setCreatedBy(buyer.id());
        persistInvoice(invoice, at(date, 14), at(verified, 10));
    }

    private InvoiceJpaEntity invoiceHeader(Receipt receipt, InvoiceType type, LocalDate invoiceDate) {
        Order order = receipt.order;
        InvoiceJpaEntity invoice = new InvoiceJpaEntity();
        pendingCode("INV", invoiceDate, invoice::setInvoiceCode);
        invoice.setPurchaseOrderId(order.po.getId());
        invoice.setGoodsReceiptId(receipt.gr.getId());
        links.add(() -> {
            invoice.setPurchaseOrderCode(order.po.getOrderCode());
            invoice.setGoodsReceiptCode(receipt.gr.getReceiptCode());
        });
        invoice.setSupplierId(order.supplier.entity.getId());
        invoice.setSupplierName(order.supplier.entity.getName());
        invoice.setSupplierCode(order.supplier.entity.getCode());
        invoice.setInvoiceType(type);
        invoice.setInvoiceDate(invoiceDate);
        invoice.setCurrencyCode(order.po.getCurrencyCode());
        invoice.setVerified(false);
        invoice.setHasDiscrepancy(false);
        invoice.setCreatedBy(admins.get(1).id());
        return invoice;
    }

    private InvoiceLineJpaEntity invoiceLine(InvoiceJpaEntity invoice, Receipt receipt, GoodsReceiptLineJpaEntity grLine,
                                             int number, int invoiced, boolean taxed) {
        BigDecimal lineTotal = scale(grLine.getUnitPrice().multiply(BigDecimal.valueOf(invoiced)));
        BigDecimal tax = taxed ? scale(lineTotal.multiply(VAT_RATE)) : BigDecimal.ZERO.setScale(2);
        InvoiceLineJpaEntity line = new InvoiceLineJpaEntity();
        line.setInvoice(invoice);
        line.setLineNumber(number);
        line.setPurchaseOrderLineId(grLine.getPurchaseOrderLineId());
        line.setGoodsReceiptLineId(grLine.getId());
        line.setMaterialCode(grLine.getMaterialCode());
        line.setMaterialName(grLine.getMaterialName());
        line.setUnitOfMeasure(grLine.getUnitOfMeasure());
        line.setQuantityOrdered(grLine.getQuantityOrdered());
        line.setQuantityReceived(grLine.getQuantityReceived());
        line.setQuantityInvoiced(invoiced);
        int gap = invoice.getInvoiceType() == InvoiceType.STANDARD ? invoiced - grLine.getQuantityReceived() : 0;
        line.setQuantityDiscrepancy(gap);
        line.setHasQuantityDiscrepancy(gap != 0);
        line.setDiscrepancyNotes(gap != 0 ? "Invoiced quantity exceeds received quantity by " + gap : null);
        line.setUnitPrice(grLine.getUnitPrice());
        line.setLineTotal(lineTotal);
        line.setTaxAmount(tax);
        line.setLineTotalWithTax(lineTotal.add(tax));
        line.setCurrencyCode(invoice.getCurrencyCode());
        line.setCreatedBy(invoice.getCreatedBy());
        return line;
    }

    private void invoiceTotals(InvoiceJpaEntity invoice) {
        BigDecimal total = BigDecimal.ZERO;
        BigDecimal tax = BigDecimal.ZERO;
        for (InvoiceLineJpaEntity line : invoice.getLines()) {
            total = total.add(line.getLineTotal());
            tax = tax.add(line.getTaxAmount());
        }
        invoice.setTotalAmount(total);
        invoice.setTotalTaxAmount(tax);
        invoice.setTotalAmountWithTax(total.add(tax));
    }

    private void persistInvoice(InvoiceJpaEntity invoice, LocalDateTime created, LocalDateTime updated) {
        invoices.add(invoice);
        persistLater(invoice, created, updated);
        stampChildren("InvoiceLineJpaEntity", "invoice", invoice.getId(), created, updated);
    }

    // ============================================================
    // PAYMENTS
    // ============================================================

    /**
     * Verified invoices are paid around their due date, grouped per supplier and week. Some stay unpaid past their
     * due date, a few are paid in part, recent ones wait in prepared (pending) or draft payments, and a few
     * payments were cancelled before being executed.
     */
    private void seedPayments() {
        Actor finance = admins.get(1);
        Map<String, List<PayItem>> executed = new LinkedHashMap<>();
        Map<UUID, List<InvoiceJpaEntity>> toPrepare = new LinkedHashMap<>();
        List<InvoiceJpaEntity> drafts = new ArrayList<>();

        for (InvoiceJpaEntity invoice : invoices) {
            if (invoice.getInvoiceType() != InvoiceType.STANDARD || invoice.getStatus() != InvoiceStatus.VERIFIED) continue;
            LocalDate verified = invoice.getVerificationDate().toLocalDate();
            LocalDate payDate = invoice.getDueDate().plusDays(between(-12, 12));
            if (!payDate.isAfter(verified)) payDate = verified.plusDays(between(1, 4));
            BigDecimal total = invoice.getTotalAmountWithTax();

            if (payDate.isBefore(today) && !chance(0.07)) {
                boolean partial = chance(0.04);
                BigDecimal amount = partial ? scale(total.multiply(BigDecimal.valueOf(0.4 + 0.2 * random.nextDouble()))) : total;
                String key = invoice.getSupplierId() + "|" + payDate.with(DayOfWeek.MONDAY);
                executed.computeIfAbsent(key, k -> new ArrayList<>()).add(new PayItem(invoice, amount, payDate, partial));
            } else if (!payDate.isBefore(today)
                    && ChronoUnit.DAYS.between(today, invoice.getDueDate()) <= 20 && chance(0.5)) {
                toPrepare.computeIfAbsent(invoice.getSupplierId(), k -> new ArrayList<>()).add(invoice);
            } else if (!payDate.isBefore(today) && chance(0.12)) {
                drafts.add(invoice);
            }
        }

        List<List<PayItem>> groups = new ArrayList<>(executed.values());
        for (List<PayItem> group : groups) {
            LocalDate confirmed = group.stream().map(PayItem::payDate).max(Comparator.naturalOrder()).orElseThrow();
            LocalDate prepared = confirmed.minusDays(between(0, 2));
            LocalDate latestVerified = group.stream().map(i -> i.invoice().getVerificationDate().toLocalDate())
                    .max(Comparator.naturalOrder()).orElseThrow();
            if (prepared.isBefore(latestVerified)) prepared = latestVerified;

            PaymentJpaEntity payment = paymentHeader(group.get(0).invoice(), PaymentStatus.COMPLETED, prepared);
            String method = paymentMethod(group.get(0).invoice());
            payment.setPaymentMethod(method);
            payment.setBankReference(bankReference(method, confirmed));
            payment.setTransactionId(method.equals("CASH") ? null : "TRX-" + Long.toHexString(random.nextLong() & 0xFFFFFFFFFFL).toUpperCase());
            payment.setPaymentDate(at(confirmed, 10));
            payment.setConfirmedDate(at(confirmed, between(11, 17)));
            payment.setPaymentReceipt(method.equals("BANK_TRANSFER") ? "avis-virement-" + payment.getBankReference() + ".pdf" : null);
            payment.setUpdatedBy(finance.id());
            int number = 1;
            for (PayItem item : group) {
                PaymentLineJpaEntity line = paymentLine(payment, item.invoice(), item.amount(), number++);
                line.setPaid(true);
                line.setPaidAmount(item.amount());
                line.setNotes(item.partial() ? "Partial payment, balance after settlement of the open return" : null);
                InvoiceJpaEntity invoice = item.invoice();
                BigDecimal paid = (invoice.getPaidAmount() != null ? invoice.getPaidAmount() : BigDecimal.ZERO).add(item.amount());
                invoice.setPaidAmount(paid);
                invoice.setPaidAt(payment.getConfirmedDate());
                invoice.setPaidBy(finance.id());
                invoice.setPaidByName(finance.name());
                invoice.setUpdatedBy(finance.id());
                if (paid.compareTo(invoice.getTotalAmountWithTax()) >= 0) {
                    invoice.setStatus(InvoiceStatus.PAID);
                    invoice.setPaymentDate(confirmed);
                }
                restamp(invoice, payment.getConfirmedDate());
            }
            paymentTotals(payment);
            payment.setPaidAmount(payment.getTotalAmount());
            persistPayment(payment, at(prepared, 9), payment.getConfirmedDate());

            if (chance(0.04) && confirmed.isAfter(start.plusMonths(1))) {
                cancelledPayment(group, prepared.minusDays(between(4, 9)));
            }
        }

        for (List<InvoiceJpaEntity> batch : toPrepare.values()) {
            LocalDate latestVerified = batch.stream().map(i -> i.getVerificationDate().toLocalDate())
                    .max(Comparator.naturalOrder()).orElseThrow();
            LocalDate prepared = max(today.minusDays(between(0, 3)), latestVerified);
            PaymentJpaEntity payment = paymentHeader(batch.get(0), PaymentStatus.PENDING, prepared);
            payment.setPaymentDate(at(prepared, 10));
            payment.setNotes("Prepared for the next payment run");
            int number = 1;
            for (InvoiceJpaEntity invoice : batch) {
                paymentLine(payment, invoice, invoice.getTotalAmountWithTax(), number++);
            }
            paymentTotals(payment);
            persistPayment(payment, at(prepared, 10), at(prepared, 10));
        }

        for (InvoiceJpaEntity invoice : drafts) {
            LocalDate created = max(today.minusDays(between(0, 5)), invoice.getVerificationDate().toLocalDate());
            PaymentJpaEntity payment = paymentHeader(invoice, PaymentStatus.DRAFT, created);
            payment.setNotes("Draft - waiting for the treasury forecast");
            paymentLine(payment, invoice, invoice.getTotalAmountWithTax(), 1);
            paymentTotals(payment);
            persistPayment(payment, at(created, 15), at(created, 15));
        }
    }

    private void cancelledPayment(List<PayItem> group, LocalDate prepared) {
        PaymentJpaEntity payment = paymentHeader(group.get(0).invoice(), PaymentStatus.CANCELLED, prepared);
        payment.setPaymentDate(at(prepared, 10));
        payment.setNotes("Cancelled: Supplier bank details changed, payment re-issued to the new account");
        payment.setInternalNotes("New RIB received by email and confirmed by phone");
        int number = 1;
        for (PayItem item : group) {
            paymentLine(payment, item.invoice(), item.amount(), number++);
        }
        paymentTotals(payment);
        persistPayment(payment, at(prepared, 10), at(prepared.plusDays(1), 9));
    }

    private PaymentJpaEntity paymentHeader(InvoiceJpaEntity invoice, PaymentStatus status, LocalDate date) {
        PaymentJpaEntity payment = new PaymentJpaEntity();
        pendingCode("PAY", date, payment::setPaymentCode);
        payment.setSupplierId(invoice.getSupplierId());
        payment.setSupplierName(invoice.getSupplierName());
        payment.setSupplierCode(invoice.getSupplierCode());
        payment.setStatus(status);
        payment.setCurrencyCode(invoice.getCurrencyCode());
        payment.setCreatedBy(admins.get(1).id());
        return payment;
    }

    private PaymentLineJpaEntity paymentLine(PaymentJpaEntity payment, InvoiceJpaEntity invoice, BigDecimal amount, int number) {
        PaymentLineJpaEntity line = new PaymentLineJpaEntity();
        line.setPayment(payment);
        line.setLineNumber(number);
        line.setInvoiceId(invoice.getId());
        links.add(() -> line.setInvoiceCode(invoice.getInvoiceCode()));
        line.setSupplierId(invoice.getSupplierId());
        line.setSupplierName(invoice.getSupplierName());
        line.setAmount(amount);
        line.setCurrencyCode(invoice.getCurrencyCode());
        line.setPaid(false);
        line.setCreatedBy(payment.getCreatedBy());
        payment.getLines().add(line);
        return line;
    }

    private void paymentTotals(PaymentJpaEntity payment) {
        payment.setTotalAmount(payment.getLines().stream().map(PaymentLineJpaEntity::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
    }

    private void persistPayment(PaymentJpaEntity payment, LocalDateTime created, LocalDateTime updated) {
        payments.add(payment);
        persistLater(payment, created, updated);
        stampChildren("PaymentLineJpaEntity", "payment", payment.getId(), created, updated);
    }

    private String paymentMethod(InvoiceJpaEntity invoice) {
        Supplier supplier = suppliers.values().stream()
                .filter(s -> s.entity.getId().equals(invoice.getSupplierId()))
                .findFirst().orElseThrow();
        boolean byCheck = supplier.spec.paymentTerms().contains("Check");
        if (supplier.spec.paymentTerms().contains("Cash on delivery") && invoice.getTotalAmountWithTax().compareTo(BigDecimal.valueOf(5000)) < 0) {
            return "CASH";
        }
        if (byCheck && chance(0.7)) return "CHECK";
        return chance(0.93) ? "BANK_TRANSFER" : "CARD";
    }

    private String bankReference(String method, LocalDate date) {
        return switch (method) {
            case "BANK_TRANSFER" -> String.format("VIR%02d%02d%02d%04d", date.getYear() % 100, date.getMonthValue(),
                    date.getDayOfMonth(), between(0, 9999));
            case "CHECK" -> "CHQ-" + between(1000000, 9999999);
            case "CARD" -> "CB-" + between(100000, 999999);
            default -> null;
        };
    }

    // ============================================================
    // STOCK
    // ============================================================

    /**
     * Replays a year of stock for each material: an opening balance, the accepted quantities of every receipt and
     * twice-monthly issues to production, ending at a level that leaves a few materials below their reorder point,
     * their safety stock or out of stock. Also sets the stock on order and the purchase prices.
     */
    private void simulateStock() {
        Map<Mat, Integer> onOrder = new HashMap<>();
        Map<Mat, List<BigDecimal>> prices = new HashMap<>();
        for (Order order : orders) {
            OrderStatus status = order.po.getStatus();
            for (PurchaseOrderLineJpaEntity line : order.po.getLines()) {
                Mat mat = order.materials.get(line);
                if (OPEN_ORDERS.contains(status)) {
                    onOrder.merge(mat, Math.max(0, line.getQuantity() - order.received(line)), Integer::sum);
                }
                if (COMMITTED_ORDERS.contains(status)) {
                    prices.computeIfAbsent(mat, m -> new ArrayList<>()).add(line.getUnitPrice());
                }
            }
        }

        for (Mat mat : materials) {
            MaterialJpaEntity entity = mat.entity;
            if (mat.spec.status() != MaterialStatus.ACTIVE) continue;

            int receivedTotal = mat.events.stream().mapToInt(StockEvent::quantity).sum();
            int target = targetStock(mat.spec);
            int usage = (int) Math.round(mat.spec.monthlyUsage() * 12 * (0.85 + 0.25 * random.nextDouble()));
            int opening = target + usage - receivedTotal;
            if (opening < mat.spec.safetyStock()) {
                opening = between(mat.spec.reorderPoint(), Math.max(mat.spec.reorderPoint() + 1, mat.spec.maximumStock() / 2));
            }
            int issuesTotal = opening + receivedTotal - target;

            List<LocalDateTime> issueDates = new ArrayList<>();
            for (LocalDate month = start; !month.isAfter(today); month = month.plusMonths(1)) {
                for (int day : new int[]{10, 25}) {
                    LocalDate date = month.withDayOfMonth(day);
                    if (date.isBefore(today)) issueDates.add(at(date, 7));
                }
            }
            int perIssue = issueDates.isEmpty() ? 0 : issuesTotal / issueDates.size();

            List<Object[]> timeline = new ArrayList<>();
            mat.events.forEach(e -> timeline.add(new Object[]{e.at(), e}));
            issueDates.forEach(d -> timeline.add(new Object[]{d, null}));
            timeline.sort(Comparator.comparing(o -> (LocalDateTime) o[0]));

            int stock = opening;
            LocalDateTime openingAt = at(start.minusDays(3), 8);
            movement(mat, StockMovementType.OPENING_BALANCE, opening, 0, opening, "Opening balance after annual inventory", openingAt);
            LocalDateTime last = openingAt;
            for (Object[] event : timeline) {
                LocalDateTime when = (LocalDateTime) event[0];
                if (event[1] instanceof StockEvent receipt) {
                    int before = stock;
                    stock += receipt.quantity();
                    receipt.line().setStockBefore(before);
                    receipt.line().setStockAfter(stock);
                    movement(mat, StockMovementType.RECEIPT, receipt.quantity(), before, stock,
                            "Goods receipt " + receipt.gr().getReceiptCode(), when);
                } else {
                    int quantity = Math.min(stock, (int) Math.round(perIssue * (0.7 + 0.6 * random.nextDouble())));
                    if (quantity <= 0) continue;
                    movement(mat, StockMovementType.ISSUE, quantity, stock, stock - quantity, issueReason(mat), when);
                    stock -= quantity;
                }
                last = when;
            }
            LocalDateTime closing = max(last.plusHours(2), at(today.minusDays(1), 15));
            if (closing.isAfter(LocalDateTime.now())) closing = LocalDateTime.now().minusMinutes(30);
            if (stock > target) {
                movement(mat, StockMovementType.ISSUE, stock - target, stock, target, issueReason(mat), closing);
                stock = target;
            } else if (stock < target) {
                movement(mat, StockMovementType.ADJUSTMENT, target - stock, stock, target, "Inventory count adjustment", closing);
                stock = target;
            }

            entity.setCurrentStock(stock);
            entity.setAvailableStock(stock);
            entity.setStockOnOrder(onOrder.getOrDefault(mat, 0));
            List<BigDecimal> paid = prices.get(mat);
            if (paid != null && !paid.isEmpty()) {
                BigDecimal average = paid.stream().reduce(BigDecimal.ZERO, BigDecimal::add)
                        .divide(BigDecimal.valueOf(paid.size()), 2, RoundingMode.HALF_UP);
                entity.setLastPurchasePrice(paid.get(paid.size() - 1));
                entity.setAveragePurchasePrice(average);
                entity.setCostPrice(average);
            }
            restamp(entity, closing);
        }
    }

    private int targetStock(MaterialSpec spec) {
        return switch (spec.target()) {
            case OUT -> 0;
            case CRITICAL -> between(Math.min(1, spec.safetyStock()), Math.max(1, spec.safetyStock() - 1));
            case REORDER -> between(spec.safetyStock() + 1, Math.max(spec.safetyStock() + 1, spec.reorderPoint() - 1));
            case NORMAL -> between((int) (spec.reorderPoint() * 1.3) + 1,
                    Math.max((int) (spec.reorderPoint() * 1.3) + 2, (int) (spec.maximumStock() * 0.7)));
        };
    }

    private void movement(Mat mat, StockMovementType type, int quantity, int before, int after, String reason, LocalDateTime when) {
        MaterialStockMovementJpaEntity movement = new MaterialStockMovementJpaEntity();
        movement.setMaterial(mat.entity);
        movement.setMovementType(type);
        movement.setQuantity(quantity);
        movement.setPreviousStock(before);
        movement.setNewStock(after);
        movement.setReason(reason);
        movement.setOccurredAt(when);
        mat.entity.getStockMovements().add(movement);
        stamps.add(new Stamp("MaterialStockMovementJpaEntity", movement.getId(), when, when, null));
    }

    private String issueReason(Mat mat) {
        return switch (mat.categoryKey()) {
            case "SPARE", "TOOL" -> "Issued to maintenance";
            case "PACK" -> "Issued to the shipping department";
            case "PPE" -> "Issued to workshop staff";
            default -> "Issued to production";
        };
    }

    // ============================================================
    // CODES, PERSISTENCE AND DATES
    // ============================================================

    /** Numbers documents in date order, per type and year, from the shared sequences. */
    private void assignCodes() {
        pendingCodes.sort(Comparator.comparing(PendingCode::date));
        for (PendingCode pending : pendingCodes) {
            pending.setter().accept(nextCode(pending.prefix(), pending.date().getYear()));
        }
    }

    private String masterCode(String prefix, int year) {
        return nextCode(prefix, year);
    }

    private String employeeCode() {
        return nextFreeCode("EMP", () -> String.format("EMP-%04d", sequences.getNextValueAndIncrement("EMP")));
    }

    private String nextCode(String prefix, int year) {
        return nextFreeCode(prefix, () -> String.format("%s-%d-%04d", prefix, year,
                sequences.getNextValueAndIncrement(prefix + "-" + year)));
    }

    /**
     * Draws numbers from the sequence until one is unused: rows created before the sequences existed (or by
     * hand) may already hold the next numbers.
     */
    private String nextFreeCode(String prefix, java.util.function.Supplier<String> draw) {
        String[] column = CODE_COLUMNS.getOrDefault(prefix, new String[]{"MaterialJpaEntity", "code"});
        while (true) {
            String code = draw.get();
            long used = em.createQuery("select count(e) from " + column[0] + " e where e." + column[1] + " = :code", Long.class)
                    .setParameter("code", code)
                    .getSingleResult();
            if (used == 0) return code;
        }
    }

    /** Entity and attribute holding the codes of each prefix; material type prefixes default to materials. */
    private static final Map<String, String[]> CODE_COLUMNS = Map.of(
            "EMP", new String[]{"EmployeeJpaEntity", "code"},
            "CAT", new String[]{"CategoryJpaEntity", "code"},
            "SUP", new String[]{"SupplierJpaEntity", "code"},
            "REQ", new String[]{"RequisitionJpaEntity", "requisitionCode"},
            "PO", new String[]{"PurchaseOrderJpaEntity", "orderCode"},
            "GR", new String[]{"GoodsReceiptJpaEntity", "receiptCode"},
            "INV", new String[]{"InvoiceJpaEntity", "invoiceCode"},
            "PAY", new String[]{"PaymentJpaEntity", "paymentCode"},
            "RTN", new String[]{"ReturnToVendorJpaEntity", "returnCode"});

    private void pendingCode(String prefix, LocalDate date, Consumer<String> setter) {
        pendingCodes.add(new PendingCode(prefix, date, setter));
    }

    private void persistLater(BaseJpaEntity entity, LocalDateTime created, LocalDateTime updated) {
        roots.add(entity);
        stamps.add(new Stamp(entity.getClass().getSimpleName(), entity.getId(), created, max(created, updated), null));
    }

    private void stampChildren(String entity, String parentField, UUID parentId, LocalDateTime created, LocalDateTime updated) {
        stamps.add(new Stamp(entity, parentId, created, max(created, updated), parentField));
    }

    /** Moves the last-update date of an entity already planned for persistence. */
    private void restamp(BaseJpaEntity entity, LocalDateTime updated) {
        for (int i = stamps.size() - 1; i >= 0; i--) {
            Stamp stamp = stamps.get(i);
            if (stamp.parentField() == null && stamp.id().equals(entity.getId())) {
                stamps.set(i, new Stamp(stamp.entity(), stamp.id(), stamp.created(), max(stamp.created(), updated), null));
                return;
            }
        }
    }

    /**
     * Auditing stamps every new row with the current time; the documents get back their own dates here.
     */
    private void applyStamps() {
        for (Stamp stamp : stamps) {
            String where = stamp.parentField() == null ? "e.id = :id" : "e." + stamp.parentField() + ".id = :id";
            em.createQuery("update " + stamp.entity() + " e set e.createdAt = :created, e.updatedAt = :updated where " + where)
                    .setParameter("created", stamp.created())
                    .setParameter("updated", stamp.updated())
                    .setParameter("id", stamp.id())
                    .executeUpdate();
        }
    }

    // ============================================================
    // HELPERS
    // ============================================================

    private Supplier weightedSupplier() {
        int total = suppliers.values().stream().mapToInt(s -> s.spec.orderWeight()).sum();
        int roll = random.nextInt(total);
        for (Supplier supplier : suppliers.values()) {
            roll -= supplier.spec.orderWeight();
            if (roll < 0) return supplier;
        }
        throw new IllegalStateException("No supplier to order from");
    }

    @SuppressWarnings("unchecked")
    private <T> T weighted(Object... valuesAndWeights) {
        int total = 0;
        for (int i = 1; i < valuesAndWeights.length; i += 2) total += (Integer) valuesAndWeights[i];
        int roll = random.nextInt(total);
        for (int i = 0; i < valuesAndWeights.length; i += 2) {
            roll -= (Integer) valuesAndWeights[i + 1];
            if (roll < 0) return (T) valuesAndWeights[i];
        }
        return (T) valuesAndWeights[0];
    }

    private Actor pickRequester() {
        return chance(0.15) ? admins.get(0) : pick(purchasers);
    }

    private Actor actorById(String id) {
        return java.util.stream.Stream.of(admins, purchasers, receivers).flatMap(List::stream)
                .filter(a -> a.id().equals(id)).findFirst().orElse(purchasers.get(0));
    }

    private <T> T pick(List<T> values) {
        return values.get(random.nextInt(values.size()));
    }

    private boolean chance(double probability) {
        return random.nextDouble() < probability;
    }

    private int between(int min, int max) {
        return max <= min ? min : min + random.nextInt(max - min + 1);
    }

    private static int niceQuantity(double raw) {
        if (raw < 10) return Math.max(1, (int) Math.round(raw));
        if (raw < 100) return Math.max(5, (int) Math.round(raw / 5) * 5);
        if (raw < 1000) return (int) Math.round(raw / 10) * 10;
        return (int) Math.round(raw / 50) * 50;
    }

    private String batchNumber(Mat mat, LocalDate date) {
        return String.format("LOT-%02d%02d%02d-%03d", date.getYear() % 100, date.getMonthValue(), date.getDayOfMonth(),
                between(1, 999));
    }

    private String storageLocation(Mat mat) {
        String zone = switch (mat.categoryKey()) {
            case "METAL" -> "A";
            case "PLASTIC" -> "B";
            case "ELEC", "SPARE", "TOOL" -> "C";
            case "PACK" -> "D";
            case "CHEM" -> "E";
            default -> "F";
        };
        return "WH1-" + zone + String.format("%02d", 1 + Math.abs(mat.entity.getName().hashCode()) % 12);
    }

    private String supplierInvoiceNumber(LocalDate date) {
        return "FA" + date.getYear() + "/" + String.format("%05d", between(1, 99999));
    }

    private static BigDecimal scale(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private static String truncate(String value, int length) {
        return value == null || value.length() <= length ? value : value.substring(0, length - 3) + "...";
    }

    private LocalDateTime at(LocalDate date, int hour) {
        return date.atTime(hour, random.nextInt(60));
    }

    private static LocalDate min(LocalDate a, LocalDate b) {
        return a.isBefore(b) ? a : b;
    }

    private static LocalDate max(LocalDate a, LocalDate b) {
        return a.isAfter(b) ? a : b;
    }

    private static LocalDateTime max(LocalDateTime a, LocalDateTime b) {
        return a.isAfter(b) ? a : b;
    }

    // ============================================================
    // RUN STATE TYPES
    // ============================================================

    private record Actor(String id, String name) {
    }

    private record PendingCode(String prefix, LocalDate date, Consumer<String> setter) {
    }

    /** Dates to restore on a row, or (with {@code parentField}) on every child row of a parent. */
    private record Stamp(String entity, UUID id, LocalDateTime created, LocalDateTime updated, String parentField) {
    }

    private record StockEvent(LocalDateTime at, StockMovementType type, int quantity, GoodsReceiptLineJpaEntity line,
                              GoodsReceiptJpaEntity gr) {
    }

    private record PayItem(InvoiceJpaEntity invoice, BigDecimal amount, LocalDate payDate, boolean partial) {
    }

    private static final class Supplier {
        final SupplierSpec spec;
        final SupplierJpaEntity entity;
        final List<Mat> materials = new ArrayList<>();

        Supplier(SupplierSpec spec, SupplierJpaEntity entity) {
            this.spec = spec;
            this.entity = entity;
        }
    }

    private static final class Mat {
        final MaterialSpec spec;
        final MaterialJpaEntity entity;
        final Supplier supplier;
        final CategoryJpaEntity category;
        final List<StockEvent> events = new ArrayList<>();

        Mat(MaterialSpec spec, MaterialJpaEntity entity, Supplier supplier, CategoryJpaEntity category) {
            this.spec = spec;
            this.entity = entity;
            this.supplier = supplier;
            this.category = category;
        }

        String categoryKey() {
            return spec.categoryKey();
        }
    }

    private static final class Order {
        final PurchaseOrderJpaEntity po;
        final Supplier supplier;
        final Actor receiver;
        final Map<PurchaseOrderLineJpaEntity, Mat> materials = new LinkedHashMap<>();
        final Map<PurchaseOrderLineJpaEntity, RequisitionLineJpaEntity> requisitionLines = new HashMap<>();
        final Map<PurchaseOrderLineJpaEntity, Integer> receivedByLine = new HashMap<>();
        final List<Receipt> receipts = new ArrayList<>();

        Order(PurchaseOrderJpaEntity po, Supplier supplier, Actor receiver) {
            this.po = po;
            this.supplier = supplier;
            this.receiver = receiver;
        }

        int received(PurchaseOrderLineJpaEntity line) {
            return receivedByLine.getOrDefault(line, 0);
        }
    }

    private static final class Receipt {
        final GoodsReceiptJpaEntity gr;
        final Order order;
        final LocalDate date;
        final boolean replacement;
        final Map<GoodsReceiptLineJpaEntity, Mat> materials = new HashMap<>();
        final Map<GoodsReceiptLineJpaEntity, PurchaseOrderLineJpaEntity> poLines = new HashMap<>();

        Receipt(GoodsReceiptJpaEntity gr, Order order, LocalDate date, boolean replacement) {
            this.gr = gr;
            this.order = order;
            this.date = date;
            this.replacement = replacement;
        }
    }
}
