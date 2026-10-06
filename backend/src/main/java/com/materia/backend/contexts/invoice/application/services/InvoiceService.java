package com.materia.backend.contexts.invoice.application.services;

import com.materia.backend.common.application.AbstractCrudApplicationService;
import com.materia.backend.contexts.invoice.application.dtos.CreateInvoiceInput;
import com.materia.backend.contexts.invoice.application.dtos.InvoiceOutput;
import com.materia.backend.contexts.invoice.application.dtos.UpdateInvoiceInput;
import com.materia.backend.contexts.invoice.application.mappers.InvoiceMapper;
import com.materia.backend.contexts.invoice.domain.entities.Invoice;
import com.materia.backend.contexts.invoice.domain.entities.InvoiceLine;
import com.materia.backend.contexts.invoice.domain.enums.InvoiceStatus;
import com.materia.backend.contexts.invoice.domain.exceptions.InvoiceLineValidationException;
import com.materia.backend.contexts.invoice.domain.exceptions.InvoiceNotFoundException;
import com.materia.backend.contexts.invoice.domain.exceptions.InvoiceNotModifiableException;
import com.materia.backend.contexts.invoice.domain.exceptions.InvoiceRuleViolationException;
import com.materia.backend.contexts.invoice.domain.exceptions.InvoiceSupplierMismatchException;
import com.materia.backend.contexts.invoice.domain.enums.InvoiceType;
import com.materia.backend.contexts.goodsReceipt.domain.entities.GoodsReceipt;
import com.materia.backend.contexts.goodsReceipt.domain.entities.GoodsReceiptLine;
import com.materia.backend.contexts.goodsReceipt.domain.enums.ReceiptStatus;
import com.materia.backend.contexts.goodsReceipt.domain.ports.out.GoodsReceiptRepository;
import com.materia.backend.contexts.purchaseOrder.domain.entities.PurchaseOrder;
import com.materia.backend.contexts.purchaseOrder.domain.entities.PurchaseOrderLine;
import com.materia.backend.contexts.purchaseOrder.domain.enums.OrderStatus;
import com.materia.backend.contexts.purchaseOrder.domain.ports.out.PurchaseOrderRepository;
import com.materia.backend.contexts.invoice.domain.exceptions.InvoiceValidationException;
import com.materia.backend.contexts.invoice.domain.ports.in.InvoiceUseCase;
import com.materia.backend.contexts.invoice.domain.ports.out.InvoiceRepository;
import com.materia.backend.contexts.invoice.domain.ports.out.UserDirectory;
import com.materia.backend.common.domain.enums.CurrencyCode;
import com.materia.backend.common.domain.valueObjects.Money;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class InvoiceService extends AbstractCrudApplicationService<
        Invoice, CreateInvoiceInput, UpdateInvoiceInput, InvoiceOutput> implements InvoiceUseCase {

    /** Orders that have received goods; nothing can be invoiced before a receipt. */
    private static final Set<OrderStatus> INVOICEABLE_ORDER_STATUSES =
            EnumSet.of(OrderStatus.PARTIALLY_RECEIVED, OrderStatus.COMPLETED);

    private static final Set<ReceiptStatus> VALIDATED_RECEIPT_STATUSES =
            EnumSet.of(ReceiptStatus.COMPLETED, ReceiptStatus.PARTIAL);

    private final InvoiceRepository invoiceRepository;
    private final InvoiceMapper invoiceMapper;
    private final InvoiceCodeGeneratorService codeGenerator;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final GoodsReceiptRepository goodsReceiptRepository;
    private final UserDirectory userDirectory;
    private BigDecimal priceTolerancePercent = InvoiceLine.DEFAULT_PRICE_TOLERANCE_PERCENT;

    public InvoiceService(InvoiceRepository invoiceRepository,
                          InvoiceMapper invoiceMapper,
                          InvoiceCodeGeneratorService codeGenerator,
                          PurchaseOrderRepository purchaseOrderRepository,
                          GoodsReceiptRepository goodsReceiptRepository,
                          UserDirectory userDirectory) {
        super(invoiceRepository, invoiceMapper);
        this.invoiceRepository = invoiceRepository;
        this.invoiceMapper = invoiceMapper;
        this.codeGenerator = codeGenerator;
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.goodsReceiptRepository = goodsReceiptRepository;
        this.userDirectory = userDirectory;
    }

    /** How far, in percent, an invoiced unit price may be from the order price before verification is refused. */
    @Value("${app.invoice.price-tolerance-percent:2}")
    public void setPriceTolerancePercent(BigDecimal priceTolerancePercent) {
        this.priceTolerancePercent = priceTolerancePercent != null ? priceTolerancePercent : InvoiceLine.DEFAULT_PRICE_TOLERANCE_PERCENT;
    }

    @Override
    @Retryable(retryFor = DataIntegrityViolationException.class, maxAttempts = 3, backoff = @Backoff(delay = 100))
    @Transactional
    public InvoiceOutput create(CreateInvoiceInput request) {
        Invoice invoice = invoiceMapper.toEntity(request);
        invoice.setInvoiceCode(codeGenerator.generateCode());
        normalizeLines(invoice.getLines(), invoice.getCurrencyCode());
        matchAgainstOrder(invoice);
        invoice.recalculateTotals();
        return toResponse(saveEntity(invoice));
    }

    @Override
    public InvoiceOutput update(UUID id, CreateInvoiceInput request) {
        UpdateInvoiceInput updateRequest = new UpdateInvoiceInput();
        updateRequest.setPurchaseOrderId(request.getPurchaseOrderId());
        updateRequest.setPurchaseOrderCode(request.getPurchaseOrderCode());
        updateRequest.setGoodsReceiptId(request.getGoodsReceiptId());
        updateRequest.setGoodsReceiptCode(request.getGoodsReceiptCode());
        updateRequest.setSupplierId(request.getSupplierId());
        updateRequest.setSupplierName(request.getSupplierName());
        updateRequest.setSupplierCode(request.getSupplierCode());
        updateRequest.setInvoiceType(request.getInvoiceType());
        updateRequest.setExternalReference(request.getExternalReference());
        updateRequest.setInvoiceDate(request.getInvoiceDate());
        updateRequest.setDueDate(request.getDueDate());
        updateRequest.setCurrencyCode(request.getCurrencyCode());
        updateRequest.setNotes(request.getNotes());
        updateRequest.setInternalNotes(request.getInternalNotes());
        updateRequest.setLines(request.getLines());
        updateRequest.setUserId(request.getUserId());
        return update(id, updateRequest);
    }

    @Override
    @Transactional
    public InvoiceOutput update(UUID id, UpdateInvoiceInput request) {
        Invoice invoice = getInvoiceById(id);
        if (!invoice.isModifiable()) {
            throw new InvoiceNotModifiableException(invoice.getId().toString(), invoice.getStatus().name());
        }

        String purchaseOrderId = invoice.getPurchaseOrderId();
        invoiceMapper.updateEntity(invoice, request);
        if (purchaseOrderId != null && !purchaseOrderId.equals(invoice.getPurchaseOrderId())) {
            throw new InvoiceRuleViolationException("The purchase order of an invoice cannot be changed");
        }
        normalizeLines(invoice.getLines(), invoice.getCurrencyCode());
        matchAgainstOrder(invoice);
        invoice.recalculateTotals();
        invoice.setUpdatedAt(LocalDateTime.now());
        invoice.setUpdatedBy(request.getUserId());
        return toResponse(saveEntity(invoice));
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        Invoice invoice = getInvoiceById(id);
        if (invoice.getStatus() != InvoiceStatus.DRAFT) {
            throw new InvoiceNotModifiableException(invoice.getId().toString(), invoice.getStatus().name());
        }
        invoiceRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public InvoiceOutput getById(UUID id) {
        return toResponse(getInvoiceById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public InvoiceOutput getByCode(String code) {
        return toResponse(
                invoiceRepository.findByInvoiceCode(code)
                        .orElseThrow(() -> new InvoiceNotFoundException("Code", code))
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvoiceOutput> getAll() {
        return getAllResponses();
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvoiceOutput> getByStatus(String status) {
        InvoiceStatus invoiceStatus = InvoiceStatus.valueOf(status.toUpperCase());
        return toResponseList(invoiceRepository.findByStatus(invoiceStatus));
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvoiceOutput> getBySupplierId(String supplierId) {
        return toResponseList(invoiceRepository.findBySupplierId(supplierId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvoiceOutput> getByPurchaseOrderId(String purchaseOrderId) {
        return toResponseList(invoiceRepository.findByPurchaseOrderId(purchaseOrderId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvoiceOutput> searchByKeyword(String keyword) {
        return toResponseList(invoiceRepository.search(keyword));
    }

    @Override
    @Transactional
    public InvoiceOutput submit(UUID id, String userId) {
        Invoice invoice = getInvoiceById(id);
        invoice.submit(userId);
        return toResponse(saveEntity(invoice));
    }

    @Override
    @Transactional
    public InvoiceOutput verify(UUID id, String userId) {
        Invoice invoice = getInvoiceById(id);
        // Receipts may have been validated since the invoice was saved: match again before verifying.
        matchAgainstOrder(invoice);
        // The name shown on the invoice comes from the verifier's account, never from the request.
        invoice.verify(userId, userDirectory.displayName(userId));
        return toResponse(saveEntity(invoice));
    }

    @Override
    @Transactional
    public InvoiceOutput pay(UUID id, String userId, Double amount) {
        Invoice invoice = getInvoiceById(id);
        CurrencyCode currency = CurrencyCode.valueOf(invoice.getCurrencyCode() != null ? invoice.getCurrencyCode() : "MAD");
        invoice.pay(userId, userDirectory.displayName(userId), Money.of(amount, currency));
        return toResponse(saveEntity(invoice));
    }

    @Override
    @Transactional
    public InvoiceOutput cancel(UUID id, String userId, String reason) {
        Invoice invoice = getInvoiceById(id);
        invoice.cancel(userId, reason);
        return toResponse(saveEntity(invoice));
    }

    private Invoice getInvoiceById(UUID id) {
        return getEntityByIdOrThrow(id, () -> new InvoiceNotFoundException(id.toString()));
    }

    /**
     * Three-way match: checks the invoice against its purchase order (status, supplier, currency, lines)
     * and records, per line, what was ordered, received and is still billable. Material details are
     * taken from the order, which is authoritative.
     */
    private void matchAgainstOrder(Invoice invoice) {
        PurchaseOrder order = loadOrder(invoice.getPurchaseOrderId());
        if (!INVOICEABLE_ORDER_STATUSES.contains(order.getStatus())) {
            throw new InvoiceRuleViolationException(
                    "Purchase order " + order.getOrderCode().getValue() + " has not received any goods and cannot be invoiced");
        }
        if (!order.getSupplierId().toString().equals(invoice.getSupplierId())) {
            throw new InvoiceSupplierMismatchException(invoice.getSupplierId(), order.getSupplierId().toString());
        }
        if (invoice.getCurrencyCode() == null || !invoice.getCurrencyCode().equalsIgnoreCase(order.getCurrencyCode())) {
            throw new InvoiceRuleViolationException(
                    "The invoice currency must be the purchase order currency (" + order.getCurrencyCode() + ")");
        }
        invoice.setPurchaseOrderCode(order.getOrderCode().getValue());
        invoice.setSupplierName(order.getSupplierName());

        String orderId = order.getId().toString();
        Map<String, PurchaseOrderLine> orderLines = order.getLines().stream()
                .collect(Collectors.toMap(l -> l.getId().toString(), Function.identity()));
        Map<String, Integer> received = acceptedQuantities(orderId);
        Map<String, Integer> invoiced = invoicedQuantities(orderId, invoice.getId());
        boolean creditNote = invoice.getInvoiceType() == InvoiceType.CREDIT_NOTE;

        for (InvoiceLine line : invoice.getLines()) {
            PurchaseOrderLine orderLine = orderLines.get(line.getPurchaseOrderLineId());
            if (orderLine == null) {
                throw new InvoiceRuleViolationException(
                        "Invoice line " + line.getLineNumber() + " does not reference a line of purchase order "
                                + order.getOrderCode().getValue());
            }
            line.setMaterialCode(orderLine.getMaterialCode());
            if (orderLine.getMaterialName() != null) line.setMaterialName(orderLine.getMaterialName());
            if (orderLine.getUnitOfMeasure() != null) line.setUnitOfMeasure(orderLine.getUnitOfMeasure());

            String key = orderLine.getId().toString();
            int receivedQty = received.getOrDefault(key, 0);
            int alreadyInvoiced = invoiced.getOrDefault(key, 0);
            int billable = creditNote ? alreadyInvoiced : receivedQty - alreadyInvoiced;
            line.recordMatch(orderLine.getQuantity(), receivedQty, billable, orderLine.getUnitPrice(), priceTolerancePercent);
        }
        invoice.refreshDiscrepancies();
    }

    private PurchaseOrder loadOrder(String purchaseOrderId) {
        UUID id;
        try {
            id = UUID.fromString(purchaseOrderId);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new InvoiceValidationException("purchaseOrderId", "Invalid purchase order ID: " + purchaseOrderId);
        }
        return purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new InvoiceValidationException("purchaseOrderId", "Purchase order not found: " + purchaseOrderId));
    }

    /** Quantity accepted per purchase-order line on validated receipts. */
    private Map<String, Integer> acceptedQuantities(String purchaseOrderId) {
        Map<String, Integer> accepted = new HashMap<>();
        for (GoodsReceipt receipt : goodsReceiptRepository.findByPurchaseOrderId(purchaseOrderId)) {
            if (!VALIDATED_RECEIPT_STATUSES.contains(receipt.getStatus())) continue;
            for (GoodsReceiptLine line : receipt.getLines()) {
                if (line.getPurchaseOrderLineId() == null) continue;
                int qty = line.getQuantityAccepted() != null
                        ? line.getQuantityAccepted()
                        : value(line.getQuantityReceived()) - value(line.getQuantityRejected());
                accepted.merge(line.getPurchaseOrderLineId(), qty, Integer::sum);
            }
        }
        return accepted;
    }

    /** Net quantity per purchase-order line on the order's other live invoices; credit notes subtract. */
    private Map<String, Integer> invoicedQuantities(String purchaseOrderId, UUID excludedInvoiceId) {
        Map<String, Integer> invoiced = new HashMap<>();
        for (Invoice other : invoiceRepository.findByPurchaseOrderId(purchaseOrderId)) {
            if (other.getId().equals(excludedInvoiceId) || other.getStatus() == InvoiceStatus.CANCELLED) continue;
            int sign = other.getInvoiceType() == InvoiceType.CREDIT_NOTE ? -1 : 1;
            for (InvoiceLine line : other.getLines()) {
                if (line.getPurchaseOrderLineId() == null) continue;
                invoiced.merge(line.getPurchaseOrderLineId(), sign * value(line.getQuantityInvoiced()), Integer::sum);
            }
        }
        return invoiced;
    }

    private static int value(Integer quantity) {
        return quantity != null ? quantity : 0;
    }

    private void normalizeLines(List<InvoiceLine> lines, String orderCurrencyCode) {
        if (lines == null || lines.isEmpty()) {
            throw new InvoiceValidationException("Lines", "Invoice line required");
        }

        for (int i = 0; i < lines.size(); i++) {
            InvoiceLine line = lines.get(i);
            if (line == null) {
                throw new InvoiceLineValidationException(i, "Invoice line cannot be null");
            }

            if (line.getId() == null) {
                line.setId(UUID.randomUUID());
            }

            line.setLineNumber(i + 1);
            if (line.getCurrencyCode() == null) {
                line.setCurrencyCode(orderCurrencyCode);
            }
        }
    }
}
