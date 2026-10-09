package com.materia.backend.contexts.purchaseRequisition.infrastructure.seed;

import com.materia.backend.common.infrastructure.seed.DevSeed;
import com.materia.backend.contexts.auth.application.dtos.UserSummary;
import com.materia.backend.contexts.auth.domain.ports.in.UserDirectoryUseCase;
import com.materia.backend.contexts.masterData.application.dtos.material.MaterialOutput;
import com.materia.backend.contexts.masterData.domain.ports.in.MaterialUseCase;
import com.materia.backend.contexts.purchaseRequisition.application.dtos.CreateRequisitionInput;
import com.materia.backend.contexts.purchaseRequisition.application.dtos.RequisitionOutput;
import com.materia.backend.contexts.purchaseRequisition.domain.entities.RequisitionLine;
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
 * Dev only: requisitions raised by the dev purchaser in every state of the approval flow, approved or rejected by the
 * dev admin (an approver must not be the requester). Each one asks for active materials of its categories from a
 * single supplier, priced in MAD, so it can be ordered as is. Skipped once any requisition exists.
 */
@Component
@Profile(DevSeed.PROFILE)
@ConditionalOnProperty(name = DevSeed.ENABLED_PROPERTY, havingValue = "true")
@Order(DevSeed.REQUISITIONS_ORDER)
public class DevRequisitionsSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DevRequisitionsSeeder.class);

    private enum Outcome { DRAFT, SUBMITTED, APPROVED, REJECTED }

    private record Scenario(String title, String description, String justification, int daysUntilNeeded,
                            Outcome outcome, List<String> categories) {
    }

    private static final List<Scenario> SCENARIOS = List.of(
            new Scenario("Structural steel replenishment for Q4",
                    "Sheets and profiles for the production line", "Safety stock for the Q4 production plan",
                    30, Outcome.DRAFT, List.of("Steel & Structural Metals")),
            new Scenario("Components for the IoT controller batch",
                    "Electronic modules for the next hardware run", "Customer order for 500 controllers",
                    21, Outcome.SUBMITTED, List.of("Semiconductors & ICs")),
            new Scenario("Export packaging consumables",
                    "Boxes, wrap and pallet protection for the shipping hub", "Monthly warehouse consumption",
                    14, Outcome.APPROVED, List.of("Cardboard Boxes & Cartons", "Protective & Stretch Film")),
            new Scenario("Pallet and crate panels",
                    "Composite panels to build export crates in house", "Cheaper than buying finished crates",
                    45, Outcome.REJECTED, List.of("Wood & Composite Panels")),
            new Scenario(DevSeed.REQUISITION_TO_ORDER,
                    "Conductors and alloy parts for the assembly line", "Assembly line restart next month",
                    20, Outcome.APPROVED, List.of("Copper & Non-Ferrous Alloys")));

    private final RequisitionUseCase requisitions;
    private final MaterialUseCase materials;
    private final UserDirectoryUseCase users;

    public DevRequisitionsSeeder(RequisitionUseCase requisitions, MaterialUseCase materials, UserDirectoryUseCase users) {
        this.requisitions = requisitions;
        this.materials = materials;
        this.users = users;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!requisitions.getAll().isEmpty()) {
            log.info("[DEV-SEED] Requisitions already present, nothing seeded");
            return;
        }
        UserSummary purchaser = users.findUserByEmail(DevSeed.PURCHASER_EMAIL).orElse(null);
        UserSummary admin = users.findUserByEmail(DevSeed.ADMIN_EMAIL).orElse(null);
        List<MaterialOutput> orderable = materials.getAll().stream()
                .filter(m -> "ACTIVE".equals(m.getStatus()) && "MAD".equalsIgnoreCase(m.getCurrencyCode()))
                .filter(m -> m.getSupplierId() != null)
                .toList();
        if (purchaser == null || admin == null || orderable.isEmpty()) {
            log.warn("[DEV-SEED] No requisition seeded: the dev accounts or orderable materials are missing");
            return;
        }

        for (Scenario scenario : SCENARIOS) {
            RequisitionOutput requisition = requisitions.create(input(scenario, purchaser, linesFor(scenario, orderable)));
            UUID id = requisition.getId();
            String requester = purchaser.id().toString();
            String approver = admin.id().toString();
            switch (scenario.outcome()) {
                case DRAFT -> { }
                case SUBMITTED -> requisitions.submit(id, requester);
                case APPROVED -> {
                    requisitions.submit(id, requester);
                    requisitions.approve(id, approver, admin.displayName(), "Within the approved budget");
                }
                case REJECTED -> {
                    requisitions.submit(id, requester);
                    requisitions.reject(id, approver, admin.displayName(), "Budget frozen until next quarter");
                }
            }
        }
        log.info("[DEV-SEED] Created {} requisitions (draft, submitted, approved, rejected)", SCENARIOS.size());
    }

    /**
     * Up to two materials of the scenario's categories from the supplier offering the most of them (one supplier per
     * requisition, so it converts into one order). Falls back to any orderable material when the data has none.
     */
    private static List<MaterialOutput> linesFor(Scenario scenario, List<MaterialOutput> orderable) {
        List<MaterialOutput> candidates = orderable.stream()
                .filter(m -> scenario.categories().contains(m.getCategoryName()))
                .toList();
        if (candidates.isEmpty()) {
            candidates = orderable;
        }
        Map<String, List<MaterialOutput>> bySupplier = candidates.stream()
                .collect(Collectors.groupingBy(MaterialOutput::getSupplierId, LinkedHashMap::new, Collectors.toList()));
        List<MaterialOutput> best = bySupplier.values().stream()
                .max(Comparator.comparingInt(List::size))
                .orElseThrow();
        return best.subList(0, Math.min(2, best.size()));
    }

    private static CreateRequisitionInput input(Scenario scenario, UserSummary purchaser, List<MaterialOutput> lines) {
        LocalDate needed = LocalDate.now().plusDays(scenario.daysUntilNeeded());
        CreateRequisitionInput input = new CreateRequisitionInput();
        input.setTitle(scenario.title());
        input.setDescription(scenario.description());
        input.setJustification(scenario.justification());
        input.setRequesterId(purchaser.id().toString());
        input.setRequesterName(purchaser.displayName());
        input.setUserId(purchaser.id().toString());
        input.setRequiredDate(needed);
        input.setCurrencyCode("MAD");
        List<RequisitionLine> requisitionLines = new ArrayList<>();
        for (MaterialOutput material : lines) {
            Integer eoq = material.getEconomicOrderQuantity();
            RequisitionLine line = new RequisitionLine(material.getCode(), eoq != null && eoq > 0 ? eoq : 50);
            line.setMaterialId(material.getId());
            line.setSupplierId(UUID.fromString(material.getSupplierId()));
            line.setSupplierName(material.getSupplierName());
            line.setRequiredDate(needed);
            line.setStorageLocation("WH-MAIN");
            requisitionLines.add(line);
        }
        input.setLines(requisitionLines);
        return input;
    }
}
