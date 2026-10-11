package com.materia.backend.contexts.goodsReceipt.infrastructure.seed;

import com.materia.backend.common.infrastructure.seed.DevSeed;
import com.materia.backend.contexts.auth.application.dtos.UserSummary;
import com.materia.backend.contexts.auth.domain.ports.in.UserDirectoryUseCase;
import com.materia.backend.contexts.goodsReceipt.application.dtos.CreateGoodsReceiptInput;
import com.materia.backend.contexts.goodsReceipt.application.dtos.GoodsReceiptLineInput;
import com.materia.backend.contexts.goodsReceipt.domain.ports.in.GoodsReceiptUseCase;
import com.materia.backend.contexts.purchaseOrder.application.dtos.PurchaseOrderLineOutput;
import com.materia.backend.contexts.purchaseOrder.application.dtos.PurchaseOrderOutput;
import com.materia.backend.contexts.purchaseOrder.domain.ports.in.PurchaseOrderUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * Dev only: about {@link DevSeed#DOCUMENTS_PER_MODULE} goods receipts by the dev receiver on the orders assigned to them.
 * Some orders stay to be received, some receipts stay in draft or are cancelled, the others are received in full or in
 * part, some in two deliveries. About 5% of the goods on each line of a first delivery are rejected, for the returns
 * seeder. Skipped once any goods receipt exists.
 */
@Component
@Profile(DevSeed.PROFILE)
@ConditionalOnProperty(name = DevSeed.ENABLED_PROPERTY, havingValue = "true")
@Order(DevSeed.GOODS_RECEIPTS_ORDER)
public class DevGoodsReceiptsSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DevGoodsReceiptsSeeder.class);

    private enum Plan { NOT_YET, DRAFT, CANCELLED, FULL, PARTIAL, PARTIAL_THEN_REST }

    /** What happens to the first orders ready for receipt, in order code order. */
    private static final List<Plan> FIRST_PLANS = List.of(
            Plan.NOT_YET, Plan.NOT_YET, Plan.DRAFT, Plan.DRAFT, Plan.CANCELLED);

    /** Repeated over the other orders: two in five received in full, two in two deliveries, one only in part. */
    private static final List<Plan> CYCLE = List.of(
            Plan.FULL, Plan.PARTIAL_THEN_REST, Plan.FULL, Plan.PARTIAL, Plan.PARTIAL_THEN_REST);

    private static Plan planOf(int index) {
        return index < FIRST_PLANS.size()
                ? FIRST_PLANS.get(index)
                : CYCLE.get((index - FIRST_PLANS.size()) % CYCLE.size());
    }

    private final GoodsReceiptUseCase receipts;
    private final PurchaseOrderUseCase orders;
    private final UserDirectoryUseCase users;

    public DevGoodsReceiptsSeeder(GoodsReceiptUseCase receipts, PurchaseOrderUseCase orders, UserDirectoryUseCase users) {
        this.receipts = receipts;
        this.orders = orders;
        this.users = users;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!receipts.getAll().isEmpty()) {
            log.info("[DEV-SEED] Goods receipts already present, nothing seeded");
            return;
        }
        UserSummary receiver = users.findUserByEmail(DevSeed.RECEIVER_EMAIL).orElse(null);
        if (receiver == null) {
            log.warn("[DEV-SEED] No goods receipt seeded: the dev receiver is missing");
            return;
        }
        String receiverId = receiver.id().toString();
        List<PurchaseOrderOutput> ready = orders.getByStatus("READY_FOR_RECEIPT").stream()
                .filter(order -> receiverId.equals(order.getAssignedTo()))
                .sorted(Comparator.comparing(PurchaseOrderOutput::getOrderCode))
                .toList();

        int created = 0;
        for (int i = 0; i < ready.size(); i++) {
            int index = i;
            PurchaseOrderOutput order = ready.get(i);
            Plan plan = planOf(i);
            String document = "receipt of " + order.getOrderCode();
            switch (plan) {
                case NOT_YET -> { }
                case DRAFT -> {
                    if (DevSeed.attempt(log, document, () -> receipts.create(input(order, receiver, 100, false, index)))) {
                        created++;
                    }
                }
                case CANCELLED -> {
                    if (DevSeed.attempt(log, document, () -> {
                        UUID id = receipts.create(input(order, receiver, 100, false, index)).getId();
                        receipts.cancel(id, receiverId, "Delivery refused: wrong pallets delivered");
                    })) {
                        created++;
                    }
                }
                case FULL, PARTIAL -> {
                    int percent = plan == Plan.FULL ? 100 : 60;
                    if (DevSeed.attempt(log, document, () -> receive(order, receiver, percent, index))) {
                        created++;
                    }
                }
                case PARTIAL_THEN_REST -> {
                    if (DevSeed.attempt(log, document, () -> receive(order, receiver, 60, index))) {
                        created++;
                        if (DevSeed.attempt(log, "second " + document, () -> receipts.receiveRemaining(
                                order.getId().toString(), receiverId, receiver.displayName()))) {
                            created++;
                        }
                    }
                }
            }
        }
        log.info("[DEV-SEED] Created {} goods receipts on {} orders ready for receipt", created, ready.size());
    }

    private void receive(PurchaseOrderOutput order, UserSummary receiver, int percent, int index) {
        UUID id = receipts.create(input(order, receiver, percent, true, index)).getId();
        receipts.complete(id, receiver.id().toString());
    }

    /** One to two weeks after the order was placed, but not after today. */
    private static LocalDate deliveryDate(PurchaseOrderOutput order, int index) {
        LocalDate today = LocalDate.now();
        if (order.getOrderDate() == null) {
            return today;
        }
        LocalDate delivered = order.getOrderDate().plusDays(7 + index % 4 * 2);
        return delivered.isAfter(today) ? today : delivered;
    }

    /**
     * A delivery of {@code percent}% of every order line; with {@code withRejects}, about 5% of each line (at least one
     * unit) is rejected at the quality check.
     */
    private static CreateGoodsReceiptInput input(PurchaseOrderOutput order, UserSummary receiver, int percent,
                                                 boolean withRejects, int index) {
        CreateGoodsReceiptInput input = new CreateGoodsReceiptInput();
        input.setPurchaseOrderId(order.getId().toString());
        input.setPurchaseOrderCode(order.getOrderCode());
        input.setReceivedBy(receiver.id().toString());
        input.setReceivedByName(receiver.displayName());
        input.setUserId(receiver.id().toString());
        input.setReceiptDate(deliveryDate(order, index));
        input.setNotes("Delivery checked at dock " + (1 + index % 4));
        List<GoodsReceiptLineInput> lines = new ArrayList<>();
        for (PurchaseOrderLineOutput orderLine : order.getLines()) {
            int received = Math.max(1, orderLine.getQuantity() * percent / 100);
            int rejected = withRejects ? Math.min(received, Math.max(1, received * 5 / 100)) : 0;
            GoodsReceiptLineInput line = new GoodsReceiptLineInput();
            line.setPurchaseOrderLineId(orderLine.getId().toString());
            line.setMaterialCode(orderLine.getMaterialCode());
            line.setQuantityReceived(received);
            line.setQuantityRejected(rejected);
            line.setQualityStatus(rejected > 0 ? "PARTIAL" : "ACCEPTED");
            if (rejected > 0) {
                line.setRejectionReason(index % 2 == 0 ? "Damaged in transit" : "Out of specification");
                line.setQualityNotes("Found at the incoming inspection");
            }
            line.setStorageLocation("WH-MAIN");
            lines.add(line);
        }
        input.setLines(lines);
        return input;
    }
}
