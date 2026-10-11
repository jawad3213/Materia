package com.materia.backend.contexts.purchaseOrder.infrastructure.seed;

import com.materia.backend.common.domain.enums.CurrencyCode;
import com.materia.backend.common.domain.valueObjects.Money;
import com.materia.backend.common.infrastructure.seed.DevSeed;
import com.materia.backend.contexts.auth.application.dtos.UserSummary;
import com.materia.backend.contexts.auth.domain.ports.in.UserDirectoryUseCase;
import com.materia.backend.contexts.masterData.application.dtos.material.MaterialOutput;
import com.materia.backend.contexts.masterData.application.dtos.material.MaterialReference;
import com.materia.backend.contexts.masterData.application.dtos.supplier.SupplierOutput;
import com.materia.backend.contexts.masterData.domain.ports.in.MaterialInventoryUseCase;
import com.materia.backend.contexts.masterData.domain.ports.in.MaterialUseCase;
import com.materia.backend.contexts.masterData.domain.ports.in.SupplierUseCase;
import com.materia.backend.contexts.purchaseOrder.application.dtos.CreatePurchaseOrderInput;
import com.materia.backend.contexts.purchaseOrder.application.dtos.PurchaseOrderLineInput;
import com.materia.backend.contexts.purchaseOrder.domain.ports.in.PurchaseOrderUseCase;
import com.materia.backend.contexts.purchaseRequisition.application.dtos.RequisitionLineSummary;
import com.materia.backend.contexts.purchaseRequisition.application.dtos.RequisitionOutput;
import com.materia.backend.contexts.purchaseRequisition.domain.ports.in.RequisitionUseCase;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Dev only: {@link DevSeed#DOCUMENTS_PER_MODULE} purchase orders. Every approved requisition but the last
 * {@link DevSeed#APPROVED_REQUISITIONS_LEFT} is converted into an order; the rest are ordered directly. A few orders stay
 * in draft, submitted, confirmed or cancelled; all others are submitted by the dev purchaser, confirmed by the dev admin
 * and assigned to the dev receiver, ready for the goods receipt seeder. Skipped once any purchase order exists.
 */
@Component
@Profile(DevSeed.PROFILE)
@ConditionalOnProperty(name = DevSeed.ENABLED_PROPERTY, havingValue = "true")
@Order(DevSeed.PURCHASE_ORDERS_ORDER)
public class DevPurchaseOrdersSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DevPurchaseOrdersSeeder.class);

    private enum Stage { DRAFT, SUBMITTED, CONFIRMED, CANCELLED, READY_FOR_RECEIPT }

    /** Stages of the direct orders, in creation order; any further order is made ready for receipt. */
    private static final List<Stage> DIRECT_ORDER_STAGES =
            List.of(Stage.DRAFT, Stage.SUBMITTED, Stage.CONFIRMED, Stage.CANCELLED, Stage.DRAFT);

    private final PurchaseOrderUseCase orders;
    private final RequisitionUseCase requisitions;
    private final MaterialUseCase materials;
    private final MaterialInventoryUseCase inventory;
    private final SupplierUseCase suppliers;
    private final UserDirectoryUseCase users;

    public DevPurchaseOrdersSeeder(PurchaseOrderUseCase orders, RequisitionUseCase requisitions,
                                   MaterialUseCase materials, MaterialInventoryUseCase inventory,
                                   SupplierUseCase suppliers, UserDirectoryUseCase users) {
        this.orders = orders;
        this.requisitions = requisitions;
        this.materials = materials;
        this.inventory = inventory;
        this.suppliers = suppliers;
        this.users = users;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!orders.getAll().isEmpty()) {
            log.info("[DEV-SEED] Purchase orders already present, nothing seeded");
            return;
        }
        UserSummary purchaser = users.findUserByEmail(DevSeed.PURCHASER_EMAIL).orElse(null);
        UserSummary admin = users.findUserByEmail(DevSeed.ADMIN_EMAIL).orElse(null);
        UserSummary receiver = users.findUserByEmail(DevSeed.RECEIVER_EMAIL).orElse(null);
        List<List<MaterialOutput>> bySupplier = orderableMaterialsBySupplier();
        if (purchaser == null || admin == null || receiver == null || bySupplier.isEmpty()) {
            log.warn("[DEV-SEED] No purchase order seeded: the dev accounts or orderable materials are missing");
            return;
        }

        List<RequisitionOutput> approved = requisitions.getByStatus("APPROVED").stream()
                .sorted(Comparator.comparing(RequisitionOutput::getRequisitionCode))
                .toList();
        List<RequisitionOutput> toConvert = approved.subList(0, Math.max(0, approved.size() - DevSeed.APPROVED_REQUISITIONS_LEFT));

        int created = 0;
        int converted = 0;
        for (int i = 0; i < toConvert.size(); i++) {
            int index = i;
            RequisitionOutput requisition = toConvert.get(i);
            if (DevSeed.attempt(log, "order of requisition " + requisition.getRequisitionCode(), () -> {
                UUID order = orderRequisition(requisition, purchaser, index);
                makeReady(order, purchaser, admin, receiver);
            })) {
                converted++;
                created++;
            }
        }
        int directOrders = DevSeed.DOCUMENTS_PER_MODULE - toConvert.size();
        for (int direct = 0; direct < directOrders; direct++) {
            int index = direct;
            List<MaterialOutput> lines = bySupplier.get(direct % bySupplier.size());
            Stage stage = direct < DIRECT_ORDER_STAGES.size() ? DIRECT_ORDER_STAGES.get(direct) : Stage.READY_FOR_RECEIPT;
            if (DevSeed.attempt(log, stage + " direct order " + (direct + 1), () -> {
                UUID order = directOrder(lines, index, purchaser);
                moveTo(stage, order, purchaser, admin, receiver);
            })) {
                created++;
            }
        }
        log.info("[DEV-SEED] Created {} purchase orders ({} converted from requisitions)", created, converted);
    }

    private void moveTo(Stage stage, UUID order, UserSummary purchaser, UserSummary admin, UserSummary receiver) {
        String buyer = purchaser.id().toString();
        String manager = admin.id().toString();
        switch (stage) {
            case DRAFT -> { }
            case CANCELLED -> orders.cancel(order, buyer, "Supplier could not meet the delivery date");
            case SUBMITTED -> orders.submit(order, buyer);
            case CONFIRMED -> {
                orders.submit(order, buyer);
                orders.confirm(order, manager);
            }
            case READY_FOR_RECEIPT -> makeReady(order, purchaser, admin, receiver);
        }
    }

    private void makeReady(UUID order, UserSummary purchaser, UserSummary admin, UserSummary receiver) {
        String manager = admin.id().toString();
        orders.submit(order, purchaser.id().toString());
        orders.confirm(order, manager);
        orders.assignReceiver(order, manager, admin.displayName(), receiver.id().toString(), receiver.displayName());
    }

    /**
     * Orders exactly what the requisition asks for, at the materials' standard prices. Dated today: the requisition was
     * only approved today.
     */
    private UUID orderRequisition(RequisitionOutput requisition, UserSummary purchaser, int index) {
        List<RequisitionLineSummary> requested = requisition.getLineSummaries();
        CreatePurchaseOrderInput input = header(requested.get(0).supplierId(), purchaser, LocalDate.now(), index);
        input.setRequisitionId(requisition.getId());
        input.setRequisitionCode(requisition.getRequisitionCode());
        input.setNotes("Converted from requisition " + requisition.getRequisitionCode());
        List<PurchaseOrderLineInput> lines = new ArrayList<>();
        for (RequisitionLineSummary line : requested) {
            PurchaseOrderLineInput orderLine = line(line.materialCode(), line.quantity());
            orderLine.setRequisitionLineId(line.id());
            lines.add(orderLine);
        }
        input.setLines(lines);
        return orders.create(input).getId();
    }

    /** An order without requisition, placed in the last weeks: the oldest first, one every two days. */
    private UUID directOrder(List<MaterialOutput> materialsOfSupplier, int direct, UserSummary purchaser) {
        LocalDate orderDate = DevSeed.daysAgo(Math.max(1, 45 - direct * 2));
        CreatePurchaseOrderInput input = header(UUID.fromString(materialsOfSupplier.get(0).getSupplierId()), purchaser,
                orderDate, direct);
        input.setNotes("Direct order, no requisition");
        List<PurchaseOrderLineInput> lines = new ArrayList<>();
        int count = 1 + direct % 2;
        for (int i = 0; i < Math.min(count, materialsOfSupplier.size()); i++) {
            MaterialOutput material = materialsOfSupplier.get((direct + i) % materialsOfSupplier.size());
            Integer eoq = material.getEconomicOrderQuantity();
            int base = eoq != null && eoq > 0 ? eoq : 50;
            lines.add(line(material.getCode(), Math.max(10, base * (2 + direct % 3) / 4)));
        }
        input.setLines(lines);
        return orders.create(input).getId();
    }

    /** Active materials priced in MAD, one group per supplier. */
    private List<List<MaterialOutput>> orderableMaterialsBySupplier() {
        Map<String, List<MaterialOutput>> groups = materials.getAll().stream()
                .filter(m -> "ACTIVE".equals(m.getStatus()) && "MAD".equalsIgnoreCase(m.getCurrencyCode()))
                .filter(m -> m.getSupplierId() != null)
                .collect(Collectors.groupingBy(MaterialOutput::getSupplierId, LinkedHashMap::new, Collectors.toList()));
        return new ArrayList<>(groups.values());
    }

    /** An order placed on {@code orderDate}, delivered two to four weeks later. */
    private CreatePurchaseOrderInput header(UUID supplierId, UserSummary purchaser, LocalDate orderDate, int index) {
        SupplierOutput supplier = suppliers.getById(supplierId);
        CreatePurchaseOrderInput input = new CreatePurchaseOrderInput();
        input.setSupplierId(supplierId);
        input.setSupplierName(supplier.getName());
        input.setSupplierCode(supplier.getCode());
        input.setOrderDate(orderDate);
        input.setExpectedDeliveryDate(orderDate.plusDays(14 + index % 3 * 7));
        input.setPaymentTerms(index % 2 == 0 ? "NET_30" : "NET_60");
        input.setPaymentDelayDays(index % 2 == 0 ? 30 : 60);
        input.setIncoterm("DAP");
        input.setDeliveryTerms("DAP Casablanca main warehouse");
        input.setCurrencyCode("MAD");
        input.setOrderedBy(purchaser.id().toString());
        input.setOrderedByName(purchaser.displayName());
        input.setUserId(purchaser.id().toString());
        return input;
    }

    private PurchaseOrderLineInput line(String materialCode, Integer quantity) {
        MaterialReference material = inventory.findMaterialByCode(materialCode).orElseThrow(
                () -> new IllegalStateException("Seeded material " + materialCode + " not found"));
        PurchaseOrderLineInput line = new PurchaseOrderLineInput();
        line.setMaterialId(material.id());
        line.setMaterialCode(material.code());
        line.setMaterialName(material.name());
        line.setUnitOfMeasure(material.unitOfMeasureCode());
        line.setQuantity(quantity);
        line.setUnitPrice(material.standardPrice() != null ? material.standardPrice() : Money.zero(CurrencyCode.MAD));
        return line;
    }
}
