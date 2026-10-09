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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Dev only: a purchase order converted from the approved requisition {@link DevSeed#REQUISITION_TO_ORDER}, submitted by
 * the dev purchaser, confirmed by the dev admin and assigned to the dev receiver (ready for receipt), plus a draft
 * order for another supplier. Skipped once any purchase order exists.
 */
@Component
@Profile(DevSeed.PROFILE)
@ConditionalOnProperty(name = DevSeed.ENABLED_PROPERTY, havingValue = "true")
@Order(DevSeed.PURCHASE_ORDERS_ORDER)
public class DevPurchaseOrdersSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DevPurchaseOrdersSeeder.class);

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
        Optional<UserSummary> purchaser = users.findUserByEmail(DevSeed.PURCHASER_EMAIL);
        Optional<UserSummary> admin = users.findUserByEmail(DevSeed.ADMIN_EMAIL);
        Optional<UserSummary> receiver = users.findUserByEmail(DevSeed.RECEIVER_EMAIL);
        Optional<RequisitionOutput> requisition = requisitions.getByStatus("APPROVED").stream()
                .filter(r -> DevSeed.REQUISITION_TO_ORDER.equals(r.getTitle()))
                .findFirst();
        if (purchaser.isEmpty() || admin.isEmpty() || receiver.isEmpty() || requisition.isEmpty()) {
            log.warn("[DEV-SEED] No purchase order seeded: the dev accounts or the approved requisition are missing");
            return;
        }

        UUID readyOrder = orderRequisition(requisition.get(), purchaser.get());
        String adminId = admin.get().id().toString();
        orders.submit(readyOrder, purchaser.get().id().toString());
        orders.confirm(readyOrder, adminId);
        orders.assignReceiver(readyOrder, adminId, admin.get().displayName(),
                receiver.get().id().toString(), receiver.get().displayName());

        UUID orderedSupplier = requisition.get().getLineSummaries().get(0).supplierId();
        boolean draftCreated = draftOrderForAnotherSupplier(orderedSupplier, purchaser.get());

        log.info("[DEV-SEED] Created a purchase order ready for receipt{}", draftCreated ? " and a draft order" : "");
    }

    /** Orders exactly what the requisition asks for, at the materials' standard prices. */
    private UUID orderRequisition(RequisitionOutput requisition, UserSummary purchaser) {
        List<RequisitionLineSummary> requested = requisition.getLineSummaries();
        CreatePurchaseOrderInput input = header(requested.get(0).supplierId(), purchaser, 14);
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

    private boolean draftOrderForAnotherSupplier(UUID excludedSupplier, UserSummary purchaser) {
        Optional<MaterialOutput> material = materials.getAll().stream()
                .filter(m -> "ACTIVE".equals(m.getStatus()) && "MAD".equalsIgnoreCase(m.getCurrencyCode()))
                .filter(m -> m.getSupplierId() != null && !m.getSupplierId().equals(excludedSupplier.toString()))
                .findFirst();
        if (material.isEmpty()) {
            return false;
        }
        CreatePurchaseOrderInput input = header(UUID.fromString(material.get().getSupplierId()), purchaser, 30);
        input.setNotes("Draft order, still to be submitted");
        Integer eoq = material.get().getEconomicOrderQuantity();
        input.setLines(List.of(line(material.get().getCode(), eoq != null && eoq > 0 ? eoq : 25)));
        orders.create(input);
        return true;
    }

    private CreatePurchaseOrderInput header(UUID supplierId, UserSummary purchaser, int daysToDelivery) {
        SupplierOutput supplier = suppliers.getById(supplierId);
        CreatePurchaseOrderInput input = new CreatePurchaseOrderInput();
        input.setSupplierId(supplierId);
        input.setSupplierName(supplier.getName());
        input.setSupplierCode(supplier.getCode());
        input.setOrderDate(LocalDate.now());
        input.setExpectedDeliveryDate(LocalDate.now().plusDays(daysToDelivery));
        input.setPaymentTerms("NET_30");
        input.setPaymentDelayDays(30);
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
