package com.materia.backend.contexts.purchaseRequisition.infrastructure.seed;

import com.materia.backend.common.infrastructure.seed.DevSeed;
import com.materia.backend.contexts.auth.application.dtos.UserSummary;
import com.materia.backend.contexts.auth.domain.ports.in.UserDirectoryUseCase;
import com.materia.backend.contexts.masterData.application.dtos.material.MaterialOutput;
import com.materia.backend.contexts.masterData.domain.ports.in.MaterialUseCase;
import com.materia.backend.contexts.purchaseRequisition.application.dtos.CreateRequisitionInput;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Dev only: {@link DevSeed#DOCUMENTS_PER_MODULE} requisitions raised by the dev purchaser in every state, approved or
 * rejected by the dev admin (an approver must not be the requester). Most approved ones are then converted into orders by
 * the purchase order seeder; the last {@link DevSeed#APPROVED_REQUISITIONS_LEFT} stay approved. Each requisition asks
 * for active materials of its theme's categories from a single supplier, priced in MAD, so it can be ordered as is.
 * Skipped once any requisition exists.
 */
@Component
@Profile(DevSeed.PROFILE)
@ConditionalOnProperty(name = DevSeed.ENABLED_PROPERTY, havingValue = "true")
@Order(DevSeed.REQUISITIONS_ORDER)
public class DevRequisitionsSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DevRequisitionsSeeder.class);

    private enum Outcome { APPROVED, DRAFT, SUBMITTED, REJECTED, CANCELLED }

    private record Theme(String title, String description, String justification, List<String> categories) {
    }

    private static final List<Theme> THEMES = List.of(
            new Theme("Structural steel replenishment", "Sheets, rods and profiles for the production line",
                    "Safety stock for the production plan", List.of("Steel & Structural Metals")),
            new Theme("Microcontrollers for the IoT controller batch", "Electronic modules for the next hardware run",
                    "Customer order for 500 controllers", List.of("Semiconductors & ICs")),
            new Theme("Export packaging consumables", "Boxes, wrap and pallet protection for the shipping hub",
                    "Monthly warehouse consumption", List.of("Cardboard Boxes & Cartons", "Protective & Stretch Film")),
            new Theme("Copper & alloys for the assembly line", "Conductors and alloy parts for the assembly line",
                    "Assembly line restart", List.of("Copper & Non-Ferrous Alloys")),
            new Theme("Cables and passives for control cabinets", "Wiring and passive components for cabinet builds",
                    "Three cabinets ordered by the plant", List.of("Connectors, Cables & Wire", "Passive Components")),
            new Theme("Pallet and crate panels", "Composite panels to build export crates in house",
                    "Cheaper than buying finished crates", List.of("Wood & Composite Panels")),
            new Theme("RFID labels for warehouse tracking", "Labels and tags for the new tracking system",
                    "Inventory accuracy project", List.of("Labels, RFID & Identification")),
            new Theme("Technical polymers for machined parts", "Polymer stock for the machining workshop",
                    "Replace metal parts by lighter polymer parts", List.of("Plastics & Technical Polymers")));

    /** How many requisitions end in each state but approved; all the others are approved. */
    private static final Map<Outcome, Integer> NOT_APPROVED = Map.of(
            Outcome.DRAFT, 4, Outcome.SUBMITTED, 4, Outcome.REJECTED, 3, Outcome.CANCELLED, 2);

    /**
     * How many requisitions end in each state. APPROVED includes those the order seeder converts, and takes what
     * {@link #NOT_APPROVED} leaves of {@link DevSeed#DOCUMENTS_PER_MODULE}.
     */
    private static final Map<Outcome, Integer> OUTCOMES = outcomes();

    private static Map<Outcome, Integer> outcomes() {
        Map<Outcome, Integer> outcomes = new EnumMap<>(NOT_APPROVED);
        int others = NOT_APPROVED.values().stream().mapToInt(Integer::intValue).sum();
        outcomes.put(Outcome.APPROVED, Math.max(0, DevSeed.DOCUMENTS_PER_MODULE - others));
        return outcomes;
    }

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

        String requester = purchaser.id().toString();
        String approver = admin.id().toString();
        List<Outcome> outcomes = new ArrayList<>();
        OUTCOMES.forEach((outcome, count) -> outcomes.addAll(Collections.nCopies(count, outcome)));

        int created = 0;
        for (int i = 0; i < outcomes.size(); i++) {
            int index = i;
            Theme theme = THEMES.get(i % THEMES.size());
            int batch = i / THEMES.size() + 1;
            String title = batch == 1 ? theme.title() : theme.title() + " (batch " + batch + ")";
            Outcome outcome = outcomes.get(i);
            boolean seeded = DevSeed.attempt(log, outcome + " requisition \"" + title + "\"", () -> {
                UUID id = requisitions.create(input(theme, title, index, purchaser, linesFor(theme, orderable))).getId();
                switch (outcome) {
                    case DRAFT -> { }
                    case CANCELLED -> { // only a submitted or approved requisition can be cancelled
                        requisitions.submit(id, requester);
                        requisitions.cancel(id, requester, "Need covered by an existing stock transfer");
                    }
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
            });
            if (seeded) created++;
        }
        log.info("[DEV-SEED] Created {} requisitions {}", created, OUTCOMES);
    }

    /**
     * Up to two materials of the theme's categories from the supplier offering the most of them (one supplier per
     * requisition, so it converts into one order). Falls back to any orderable material when the data has none.
     */
    private static List<MaterialOutput> linesFor(Theme theme, List<MaterialOutput> orderable) {
        List<MaterialOutput> candidates = orderable.stream()
                .filter(m -> theme.categories().contains(m.getCategoryName()))
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

    private static CreateRequisitionInput input(Theme theme, String title, int index, UserSummary purchaser,
                                                List<MaterialOutput> lines) {
        LocalDate needed = LocalDate.now().plusDays(7 + (index * 11) % 50);
        CreateRequisitionInput input = new CreateRequisitionInput();
        input.setTitle(title);
        input.setDescription(theme.description());
        input.setJustification(theme.justification());
        input.setRequesterId(purchaser.id().toString());
        input.setRequesterName(purchaser.displayName());
        input.setUserId(purchaser.id().toString());
        input.setRequiredDate(needed);
        input.setCurrencyCode("MAD");
        List<RequisitionLine> requisitionLines = new ArrayList<>();
        for (MaterialOutput material : lines) {
            Integer eoq = material.getEconomicOrderQuantity();
            int base = eoq != null && eoq > 0 ? eoq : 50;
            RequisitionLine line = new RequisitionLine(material.getCode(), Math.max(10, base * (2 + index % 3) / 4));
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
