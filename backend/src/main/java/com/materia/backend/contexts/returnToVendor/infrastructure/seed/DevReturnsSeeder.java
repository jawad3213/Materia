package com.materia.backend.contexts.returnToVendor.infrastructure.seed;

import com.materia.backend.common.infrastructure.seed.DevSeed;
import com.materia.backend.contexts.auth.application.dtos.UserSummary;
import com.materia.backend.contexts.auth.domain.ports.in.UserDirectoryUseCase;
import com.materia.backend.contexts.goodsReceipt.application.dtos.GoodsReceiptLineOutput;
import com.materia.backend.contexts.goodsReceipt.application.dtos.GoodsReceiptOutput;
import com.materia.backend.contexts.goodsReceipt.domain.ports.in.GoodsReceiptUseCase;
import com.materia.backend.contexts.returnToVendor.application.dtos.CreateReturnToVendorInput;
import com.materia.backend.contexts.returnToVendor.application.dtos.ReturnToVendorLineInput;
import com.materia.backend.contexts.returnToVendor.domain.enums.ResolutionType;
import com.materia.backend.contexts.returnToVendor.domain.ports.in.ReturnToVendorUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Dev only: up to {@link DevSeed#DOCUMENTS_PER_MODULE} returns of the goods rejected on the validated receipts, one per
 * rejected line (a second one for the rest of the line when more are needed), in every state: draft, pending, resolved
 * by a credit note or a replacement (which reopens the order), cancelled. Skipped once any return exists.
 */
@Component
@Profile(DevSeed.PROFILE)
@ConditionalOnProperty(name = DevSeed.ENABLED_PROPERTY, havingValue = "true")
@Order(DevSeed.RETURNS_ORDER)
public class DevReturnsSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DevReturnsSeeder.class);

    private enum Outcome { DRAFT, PENDING, CREDIT_NOTE, REPLACEMENT, CANCELLED }

    /** Repeated over the returns: 4 credit notes, 3 drafts, 3 pending, 3 cancelled and 2 replacements in 15. */
    private static final List<Outcome> CYCLE = List.of(
            Outcome.CREDIT_NOTE, Outcome.DRAFT, Outcome.PENDING, Outcome.CANCELLED, Outcome.CREDIT_NOTE,
            Outcome.REPLACEMENT, Outcome.DRAFT, Outcome.PENDING, Outcome.CREDIT_NOTE, Outcome.CANCELLED,
            Outcome.DRAFT, Outcome.CREDIT_NOTE, Outcome.PENDING, Outcome.REPLACEMENT, Outcome.CANCELLED);

    private static final Set<String> VALIDATED = Set.of("COMPLETED", "PARTIAL");

    private record Rejected(GoodsReceiptOutput receipt, GoodsReceiptLineOutput line, int quantity) {
    }

    private final ReturnToVendorUseCase returns;
    private final GoodsReceiptUseCase receipts;
    private final UserDirectoryUseCase users;

    public DevReturnsSeeder(ReturnToVendorUseCase returns, GoodsReceiptUseCase receipts, UserDirectoryUseCase users) {
        this.returns = returns;
        this.receipts = receipts;
        this.users = users;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!returns.getAll().isEmpty()) {
            log.info("[DEV-SEED] Returns already present, nothing seeded");
            return;
        }
        UserSummary purchaser = users.findUserByEmail(DevSeed.PURCHASER_EMAIL).orElse(null);
        if (purchaser == null) {
            log.warn("[DEV-SEED] No return seeded: the dev purchaser is missing");
            return;
        }
        List<Rejected> toReturn = rejectedGoods();
        String userId = purchaser.id().toString();
        int created = 0;
        for (int i = 0; i < toReturn.size(); i++) {
            int index = i;
            Rejected rejected = toReturn.get(i);
            Outcome outcome = CYCLE.get(i % CYCLE.size());
            String reference = "%s-%04d".formatted(outcome == Outcome.REPLACEMENT ? "RPL" : "AV", i + 1);
            if (DevSeed.attempt(log, outcome + " return on " + rejected.receipt().getReceiptCode(), () -> {
                UUID id = returns.create(input(rejected, userId, index)).getId();
                switch (outcome) {
                    case DRAFT -> { }
                    case CANCELLED -> returns.cancel(id, userId, "Supplier accepted a discount instead");
                    case PENDING -> returns.submit(id, userId);
                    case CREDIT_NOTE -> {
                        returns.submit(id, userId);
                        returns.resolve(id, userId, ResolutionType.CREDIT_NOTE, reference, "Credit note issued");
                    }
                    case REPLACEMENT -> {
                        returns.submit(id, userId);
                        returns.resolve(id, userId, ResolutionType.REPLACEMENT, reference, "Replacement shipped next week");
                    }
                }
            })) {
                created++;
            }
        }
        log.info("[DEV-SEED] Created {} returns to vendor", created);
    }

    /**
     * The rejected lines of the validated receipts; when they are fewer than the target, the lines with several rejected
     * units are split into two returns.
     */
    private List<Rejected> rejectedGoods() {
        List<Rejected> lines = new ArrayList<>();
        receipts.getAll().stream()
                .filter(receipt -> VALIDATED.contains(receipt.getStatus()))
                .sorted(Comparator.comparing(GoodsReceiptOutput::getReceiptCode))
                .forEach(receipt -> receipt.getLines().stream()
                        .filter(line -> line.getQuantityRejected() != null && line.getQuantityRejected() > 0)
                        .forEach(line -> lines.add(new Rejected(receipt, line, line.getQuantityRejected()))));
        List<Rejected> result = new ArrayList<>();
        int spare = DevSeed.DOCUMENTS_PER_MODULE - lines.size();
        for (Rejected rejected : lines) {
            if (spare > 0 && rejected.quantity() > 1) {
                int first = rejected.quantity() / 2;
                result.add(new Rejected(rejected.receipt(), rejected.line(), first));
                result.add(new Rejected(rejected.receipt(), rejected.line(), rejected.quantity() - first));
                spare--;
            } else {
                result.add(rejected);
            }
        }
        return result.subList(0, Math.min(result.size(), DevSeed.DOCUMENTS_PER_MODULE));
    }

    private static CreateReturnToVendorInput input(Rejected rejected, String userId, int index) {
        CreateReturnToVendorInput input = new CreateReturnToVendorInput();
        input.setGoodsReceiptId(rejected.receipt().getId().toString());
        input.setReturnDate(rejected.receipt().getReceiptDate()); // sent back the day the goods were rejected
        input.setReturnReason(rejected.line().getRejectionReason() != null
                ? rejected.line().getRejectionReason() : "Rejected at goods receipt");
        input.setNotes("Collected by the supplier's carrier");
        input.setUserId(userId);
        ReturnToVendorLineInput line = new ReturnToVendorLineInput();
        line.setGoodsReceiptLineId(rejected.line().getId().toString());
        line.setQuantityToReturn(rejected.quantity());
        line.setDefectDescription(index % 2 == 0 ? "Dented packaging, product deformed" : "Dimensions out of tolerance");
        input.setLines(List.of(line));
        return input;
    }
}
