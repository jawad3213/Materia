package com.materia.backend.contexts.invoice.infrastructure.seed;

import com.materia.backend.common.domain.valueObjects.Money;
import com.materia.backend.common.infrastructure.seed.DevSeed;
import com.materia.backend.contexts.auth.application.dtos.UserSummary;
import com.materia.backend.contexts.auth.domain.ports.in.UserDirectoryUseCase;
import com.materia.backend.contexts.goodsReceipt.application.dtos.GoodsReceiptLineOutput;
import com.materia.backend.contexts.goodsReceipt.application.dtos.GoodsReceiptOutput;
import com.materia.backend.contexts.goodsReceipt.domain.ports.in.GoodsReceiptUseCase;
import com.materia.backend.contexts.invoice.application.dtos.CreateInvoiceInput;
import com.materia.backend.contexts.invoice.application.dtos.InvoiceLineInput;
import com.materia.backend.contexts.invoice.application.dtos.InvoiceLineOutput;
import com.materia.backend.contexts.invoice.application.dtos.InvoiceOutput;
import com.materia.backend.contexts.invoice.domain.enums.InvoiceType;
import com.materia.backend.contexts.invoice.domain.ports.in.InvoiceUseCase;
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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Dev only: about {@link DevSeed#DOCUMENTS_PER_MODULE} supplier invoices. Each validated receipt is invoiced for the
 * goods it accepted, at the order price with 20% VAT, entered by the dev purchaser and verified by the dev admin; two
 * credit notes correct verified invoices. Most invoices end verified (ready for the payments seeder), the others in
 * draft, submitted or cancelled. Skipped once any invoice exists.
 */
@Component
@Profile(DevSeed.PROFILE)
@ConditionalOnProperty(name = DevSeed.ENABLED_PROPERTY, havingValue = "true")
@Order(DevSeed.INVOICES_ORDER)
public class DevInvoicesSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DevInvoicesSeeder.class);

    private enum Outcome { DRAFT, SUBMITTED, VERIFIED, CANCELLED }

    private static final Set<String> VALIDATED = Set.of("COMPLETED", "PARTIAL");
    private static final BigDecimal VAT_RATE = new BigDecimal("0.20");
    private static final int CREDIT_NOTES = 2;

    private final InvoiceUseCase invoices;
    private final GoodsReceiptUseCase receipts;
    private final UserDirectoryUseCase users;

    public DevInvoicesSeeder(InvoiceUseCase invoices, GoodsReceiptUseCase receipts, UserDirectoryUseCase users) {
        this.invoices = invoices;
        this.receipts = receipts;
        this.users = users;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!invoices.getAll().isEmpty()) {
            log.info("[DEV-SEED] Invoices already present, nothing seeded");
            return;
        }
        UserSummary purchaser = users.findUserByEmail(DevSeed.PURCHASER_EMAIL).orElse(null);
        UserSummary admin = users.findUserByEmail(DevSeed.ADMIN_EMAIL).orElse(null);
        if (purchaser == null || admin == null) {
            log.warn("[DEV-SEED] No invoice seeded: the dev accounts are missing");
            return;
        }
        String clerk = purchaser.id().toString();
        String verifier = admin.id().toString();

        List<GoodsReceiptOutput> validated = receipts.getAll().stream()
                .filter(receipt -> VALIDATED.contains(receipt.getStatus()))
                .sorted(Comparator.comparing(GoodsReceiptOutput::getReceiptCode))
                .limit(DevSeed.DOCUMENTS_PER_MODULE - CREDIT_NOTES)
                .toList();

        int created = 0;
        int attempted = 0;
        List<InvoiceOutput> verified = new ArrayList<>();
        for (GoodsReceiptOutput receipt : validated) {
            CreateInvoiceInput input = standardInvoice(receipt, clerk);
            if (input.getLines().isEmpty()) {
                continue; // everything on this receipt was rejected
            }
            Outcome outcome = outcomeOf(attempted++);
            if (DevSeed.attempt(log, outcome + " invoice of " + receipt.getReceiptCode(), () -> {
                UUID id = invoices.create(input).getId();
                switch (outcome) {
                    case DRAFT -> { }
                    case CANCELLED -> invoices.cancel(id, clerk, "Duplicate of a paper invoice already entered");
                    case SUBMITTED -> invoices.submit(id, clerk);
                    case VERIFIED -> {
                        invoices.submit(id, clerk);
                        verified.add(invoices.verify(id, verifier));
                    }
                }
            })) {
                created++;
            }
        }

        for (int i = 0; i < CREDIT_NOTES && i < verified.size(); i++) {
            InvoiceOutput invoice = verified.get(i);
            boolean verify = i == 0;
            if (DevSeed.attempt(log, "credit note on " + invoice.getInvoiceCode(), () -> {
                UUID id = invoices.create(creditNote(invoice, clerk)).getId();
                invoices.submit(id, clerk);
                if (verify) {
                    invoices.verify(id, verifier);
                }
            })) {
                created++;
            }
        }
        log.info("[DEV-SEED] Created {} invoices ({} verified standard invoices)", created, verified.size());
    }

    /** Mostly verified; a few submitted, in draft or cancelled. */
    private static Outcome outcomeOf(int index) {
        return switch (index % 9) {
            case 3 -> Outcome.SUBMITTED;
            case 6 -> index % 2 == 0 ? Outcome.DRAFT : Outcome.SUBMITTED;
            case 8 -> index % 2 == 0 ? Outcome.CANCELLED : Outcome.DRAFT;
            default -> Outcome.VERIFIED;
        };
    }

    /** The supplier's invoice for what the receipt accepted, at the order price, with 20% VAT. */
    private static CreateInvoiceInput standardInvoice(GoodsReceiptOutput receipt, String clerk) {
        CreateInvoiceInput input = header(receipt.getPurchaseOrderId(), receipt.getPurchaseOrderCode(),
                receipt.getSupplierId(), receipt.getSupplierName(), clerk, receipt.getReceiptDate());
        input.setGoodsReceiptId(receipt.getId().toString());
        input.setGoodsReceiptCode(receipt.getReceiptCode());
        input.setInvoiceType(InvoiceType.STANDARD);
        input.setExternalReference("FA-%s".formatted(receipt.getReceiptCode()));
        List<InvoiceLineInput> lines = new ArrayList<>();
        for (GoodsReceiptLineOutput line : receipt.getLines()) {
            int accepted = line.getQuantityAccepted() != null ? line.getQuantityAccepted()
                    : value(line.getQuantityReceived()) - value(line.getQuantityRejected());
            if (accepted <= 0 || line.getUnitPrice() == null) continue;
            lines.add(line(line.getPurchaseOrderLineId(), line.getId().toString(), line.getMaterialCode(),
                    line.getMaterialName(), line.getUnitOfMeasure(), accepted, line.getUnitPrice()));
        }
        input.setLines(lines);
        return input;
    }

    /** A credit note for one unit of the first line of a verified invoice (a price or quality allowance). */
    private static CreateInvoiceInput creditNote(InvoiceOutput invoice, String clerk) {
        CreateInvoiceInput input = header(invoice.getPurchaseOrderId(), invoice.getPurchaseOrderCode(),
                invoice.getSupplierId(), invoice.getSupplierName(), clerk, invoice.getInvoiceDate());
        input.setInvoiceType(InvoiceType.CREDIT_NOTE);
        input.setExternalReference("AV-%s".formatted(invoice.getInvoiceCode()));
        input.setNotes("Allowance agreed with the supplier on " + invoice.getInvoiceCode());
        InvoiceLineOutput first = invoice.getLines().get(0);
        input.setLines(List.of(line(first.getPurchaseOrderLineId(), first.getGoodsReceiptLineId(),
                first.getMaterialCode(), first.getMaterialName(), first.getUnitOfMeasure(), 1, first.getUnitPrice())));
        return input;
    }

    /** Issued on {@code invoiceDate} (never before the goods it bills), due 30 days later. */
    private static CreateInvoiceInput header(String orderId, String orderCode, String supplierId, String supplierName,
                                             String clerk, LocalDate invoiceDate) {
        LocalDate issued = invoiceDate != null ? invoiceDate : LocalDate.now();
        CreateInvoiceInput input = new CreateInvoiceInput();
        input.setPurchaseOrderId(orderId);
        input.setPurchaseOrderCode(orderCode);
        input.setSupplierId(supplierId);
        input.setSupplierName(supplierName);
        input.setCurrencyCode("MAD");
        input.setInvoiceDate(issued);
        input.setDueDate(issued.plusDays(30));
        input.setUserId(clerk);
        return input;
    }

    private static InvoiceLineInput line(String orderLineId, String receiptLineId, String materialCode,
                                         String materialName, String unit, int quantity, Money unitPrice) {
        InvoiceLineInput line = new InvoiceLineInput();
        line.setPurchaseOrderLineId(orderLineId);
        line.setGoodsReceiptLineId(receiptLineId);
        line.setMaterialCode(materialCode);
        line.setMaterialName(materialName);
        line.setUnitOfMeasure(unit);
        line.setQuantityInvoiced(quantity);
        line.setUnitPrice(unitPrice);
        BigDecimal net = unitPrice.getAmount().multiply(BigDecimal.valueOf(quantity));
        line.setTaxAmount(Money.of(net.multiply(VAT_RATE).setScale(2, RoundingMode.HALF_UP), unitPrice.getCurrency()));
        return line;
    }

    private static int value(Integer quantity) {
        return quantity != null ? quantity : 0;
    }
}
