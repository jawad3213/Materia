package com.materia.backend.contexts.payment.infrastructure.seed;

import com.materia.backend.common.infrastructure.seed.DevSeed;
import com.materia.backend.contexts.auth.application.dtos.UserSummary;
import com.materia.backend.contexts.auth.domain.ports.in.UserDirectoryUseCase;
import com.materia.backend.contexts.invoice.application.dtos.InvoiceOutput;
import com.materia.backend.contexts.invoice.domain.enums.InvoiceType;
import com.materia.backend.contexts.invoice.domain.ports.in.InvoiceUseCase;
import com.materia.backend.contexts.payment.application.dtos.CreatePaymentInput;
import com.materia.backend.contexts.payment.application.dtos.CreatePaymentLineInput;
import com.materia.backend.contexts.payment.domain.ports.in.PaymentUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * Dev only: about {@link DevSeed#DOCUMENTS_PER_MODULE} payments of the verified invoices by the dev admin, in every
 * state: completed (the invoice records the payment), pending, draft and cancelled, some invoices paid in two parts.
 * Skipped once any payment exists.
 */
@Component
@Profile(DevSeed.PROFILE)
@ConditionalOnProperty(name = DevSeed.ENABLED_PROPERTY, havingValue = "true")
@Order(DevSeed.PAYMENTS_ORDER)
public class DevPaymentsSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DevPaymentsSeeder.class);

    private enum State { DRAFT, PENDING, COMPLETED, CANCELLED }

    /** One payment of an invoice: its state and the share of the invoice total it pays (the last one pays the rest). */
    private record Step(State state, BigDecimal share) {
    }

    private static final BigDecimal ALL = BigDecimal.ONE;

    /** How each verified invoice is paid, repeated over the invoices. */
    private static final List<List<Step>> PATTERNS = List.of(
            List.of(new Step(State.COMPLETED, ALL)),
            List.of(new Step(State.COMPLETED, new BigDecimal("0.50")), new Step(State.PENDING, ALL)),
            List.of(new Step(State.CANCELLED, ALL), new Step(State.COMPLETED, ALL)),
            List.of(new Step(State.COMPLETED, new BigDecimal("0.60")), new Step(State.DRAFT, ALL)),
            List.of(new Step(State.COMPLETED, ALL)),
            List.of(new Step(State.DRAFT, ALL)),
            List.of(new Step(State.COMPLETED, new BigDecimal("0.50")), new Step(State.PENDING, ALL)),
            List.of(new Step(State.COMPLETED, new BigDecimal("0.60")), new Step(State.DRAFT, ALL)));

    private final PaymentUseCase payments;
    private final InvoiceUseCase invoices;
    private final UserDirectoryUseCase users;

    public DevPaymentsSeeder(PaymentUseCase payments, InvoiceUseCase invoices, UserDirectoryUseCase users) {
        this.payments = payments;
        this.invoices = invoices;
        this.users = users;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!payments.getAll().isEmpty()) {
            log.info("[DEV-SEED] Payments already present, nothing seeded");
            return;
        }
        UserSummary admin = users.findUserByEmail(DevSeed.ADMIN_EMAIL).orElse(null);
        if (admin == null) {
            log.warn("[DEV-SEED] No payment seeded: the dev admin is missing");
            return;
        }
        String treasurer = admin.id().toString();
        List<InvoiceOutput> payable = invoices.getByStatus("VERIFIED").stream()
                .filter(invoice -> invoice.getInvoiceType() == InvoiceType.STANDARD)
                .sorted(Comparator.comparing(InvoiceOutput::getInvoiceCode))
                .toList();

        int created = 0;
        for (int i = 0; i < payable.size() && created < DevSeed.DOCUMENTS_PER_MODULE; i++) {
            InvoiceOutput invoice = payable.get(i);
            BigDecimal total = invoice.getTotalAmountWithTax().getAmount();
            BigDecimal committed = BigDecimal.ZERO; // paid or set aside by an open payment
            for (Step step : PATTERNS.get(i % PATTERNS.size())) {
                if (created >= DevSeed.DOCUMENTS_PER_MODULE) break;
                BigDecimal amount = step.share().compareTo(ALL) == 0
                        ? total.subtract(committed)
                        : total.multiply(step.share()).setScale(2, RoundingMode.DOWN);
                if (amount.signum() <= 0) break;
                int number = created;
                boolean seeded = DevSeed.attempt(log, step.state() + " payment of " + invoice.getInvoiceCode(), () -> {
                    UUID id = payments.create(input(invoice, amount, treasurer)).getId();
                    switch (step.state()) {
                        case DRAFT -> { }
                        case CANCELLED -> payments.cancel(id, treasurer, "Wrong bank account, payment re-issued");
                        case PENDING -> payments.prepare(id, treasurer);
                        case COMPLETED -> {
                            payments.prepare(id, treasurer);
                            payments.complete(id, treasurer, "VIR-%06d".formatted(100 + number),
                                    "TX-%08d".formatted(4_200_000 + number), "BANK_TRANSFER");
                        }
                    }
                });
                if (!seeded) break; // the next payments of this invoice would be based on this one
                if (step.state() != State.CANCELLED) {
                    committed = committed.add(amount);
                }
                created++;
            }
        }
        log.info("[DEV-SEED] Created {} payments for {} verified invoices", created, payable.size());
    }

    private static CreatePaymentInput input(InvoiceOutput invoice, BigDecimal amount, String treasurer) {
        CreatePaymentInput input = new CreatePaymentInput();
        input.setSupplierId(invoice.getSupplierId());
        input.setSupplierName(invoice.getSupplierName());
        input.setCurrencyCode("MAD");
        input.setTotalAmount(amount);
        input.setNotes("Payment of " + invoice.getInvoiceCode());
        input.setUserId(treasurer);
        CreatePaymentLineInput line = new CreatePaymentLineInput();
        line.setInvoiceId(invoice.getId().toString());
        line.setAmount(amount);
        input.setLines(List.of(line));
        return input;
    }
}
