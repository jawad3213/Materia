package com.materia.backend.contexts.purchaseOrder.application.services;

import com.materia.backend.common.audit.application.AuditContext;
import com.materia.backend.common.application.AbstractCrudApplicationService;
import com.materia.backend.contexts.purchaseOrder.application.dtos.CreatePurchaseOrderInput;
import com.materia.backend.contexts.purchaseOrder.application.dtos.PurchaseOrderOutput;
import com.materia.backend.contexts.purchaseOrder.application.dtos.UpdatePurchaseOrderInput;
import com.materia.backend.contexts.purchaseOrder.application.mappers.PurchaseOrderMapper;
import com.materia.backend.contexts.purchaseOrder.domain.entities.PurchaseOrder;
import com.materia.backend.contexts.purchaseOrder.domain.entities.PurchaseOrderLine;
import com.materia.backend.contexts.purchaseOrder.domain.enums.DeliveryStatus;
import com.materia.backend.contexts.purchaseOrder.domain.enums.OrderStatus;
import com.materia.backend.contexts.purchaseOrder.domain.exceptions.PurchaseOrderInvalidLineException;
import com.materia.backend.contexts.purchaseOrder.domain.exceptions.PurchaseOrderLineRequiredException;
import com.materia.backend.contexts.purchaseOrder.domain.exceptions.PurchaseOrderNotFoundException;
import com.materia.backend.contexts.purchaseOrder.domain.exceptions.PurchaseOrderNotModifiableException;
import com.materia.backend.contexts.purchaseOrder.domain.exceptions.PurchaseOrderValidationException;
import com.materia.backend.contexts.purchaseOrder.domain.ports.in.PurchaseOrderUseCase;
import com.materia.backend.contexts.purchaseOrder.domain.events.PurchaseOrderConfirmedEvent;
import com.materia.backend.contexts.purchaseOrder.domain.events.PurchaseOrderSubmittedEvent;
import com.materia.backend.contexts.purchaseOrder.domain.ports.out.PurchaseOrderEventPublisher;
import com.materia.backend.contexts.purchaseOrder.domain.ports.out.PurchaseOrderRepository;
import com.materia.backend.contexts.purchaseOrder.domain.ports.out.ReceiptRecords;
import com.materia.backend.contexts.purchaseOrder.domain.ports.out.ReceiverDirectory;
import com.materia.backend.contexts.purchaseOrder.domain.ports.out.RequisitionSource;
import com.materia.backend.contexts.purchaseOrder.domain.ports.out.RequisitionSource.RequestedLine;
import com.materia.backend.contexts.purchaseOrder.domain.ports.out.RequisitionSource.RequisitionToOrder;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;
import java.math.BigDecimal;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Application service for purchase orders.
 */
@Service
// Reads run in a read-only transaction (open-in-view is off, so lazy data needs one); writes declare their own.
@Transactional(readOnly = true)
public class PurchaseOrderService extends AbstractCrudApplicationService<
        PurchaseOrder,
        CreatePurchaseOrderInput,
        UpdatePurchaseOrderInput,
        PurchaseOrderOutput> implements PurchaseOrderUseCase {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PurchaseOrderMapper purchaseOrderMapper;
    private final PurchaseOrderCodeGeneratorService codeGenerator;
    private final ReceiptRecords receiptRecords;
    private final RequisitionSource requisitionSource;
    private final PurchaseOrderEventPublisher eventPublisher;
    private final ReceiverDirectory receiverDirectory;
    private final com.materia.backend.contexts.purchaseOrder.domain.ports.out.OnOrderStock onOrderStock;

    private com.materia.backend.common.domain.fx.ReportingFx reportingFx;

    /** Values committed documents in the reporting currency; absent in unit tests that do not need it. */
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    public void setReportingFx(com.materia.backend.common.domain.fx.ReportingFx reportingFx) {
        this.reportingFx = reportingFx;
    }

    private void valueInReportingCurrency(java.util.UUID id) {
        if (reportingFx != null && id != null) {
            reportingFx.valueDocument(com.materia.backend.common.domain.fx.FxDocument.PURCHASE_ORDER, id);
        }
    }


    private BigDecimal defaultTaxRate = new BigDecimal("20");

    public PurchaseOrderService(PurchaseOrderRepository purchaseOrderRepository,
                                PurchaseOrderMapper purchaseOrderMapper,
                                PurchaseOrderCodeGeneratorService codeGenerator,
                                ReceiptRecords receiptRecords,
                                RequisitionSource requisitionSource,
                                PurchaseOrderEventPublisher eventPublisher,
                                ReceiverDirectory receiverDirectory) {
        this(purchaseOrderRepository, purchaseOrderMapper, codeGenerator, receiptRecords, requisitionSource,
                eventPublisher, receiverDirectory, com.materia.backend.contexts.purchaseOrder.domain.ports.out.OnOrderStock.NONE);
    }

    @org.springframework.beans.factory.annotation.Autowired
    public PurchaseOrderService(PurchaseOrderRepository purchaseOrderRepository,
                                PurchaseOrderMapper purchaseOrderMapper,
                                PurchaseOrderCodeGeneratorService codeGenerator,
                                ReceiptRecords receiptRecords,
                                RequisitionSource requisitionSource,
                                PurchaseOrderEventPublisher eventPublisher,
                                ReceiverDirectory receiverDirectory,
                                com.materia.backend.contexts.purchaseOrder.domain.ports.out.OnOrderStock onOrderStock) {
        super(purchaseOrderRepository, purchaseOrderMapper);
        this.onOrderStock = onOrderStock;
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.purchaseOrderMapper = purchaseOrderMapper;
        this.codeGenerator = codeGenerator;
        this.receiptRecords = receiptRecords;
        this.requisitionSource = requisitionSource;
        this.eventPublisher = eventPublisher;
        this.receiverDirectory = receiverDirectory;
    }

    @Override
    @Retryable(retryFor = DataIntegrityViolationException.class, maxAttempts = 3, backoff = @Backoff(delay = 100))
    @Transactional
    public PurchaseOrderOutput create(CreatePurchaseOrderInput request) {
        PurchaseOrder purchaseOrder = purchaseOrderMapper.toEntity(request);
        normalizeLines(purchaseOrder.getLines(), purchaseOrder.getCurrencyCode());
        applyTaxRates(purchaseOrder);
        purchaseOrder.setOrderCode(codeGenerator.generateCode());
        purchaseOrder.recalculateTotals();

        UUID requisitionId = purchaseOrder.getRequisitionId();
        if (requisitionId != null) {
            RequisitionToOrder requisition = requisitionSource.get(requisitionId);
            checkAgainstRequisition(purchaseOrder, requisition);
            purchaseOrder.setRequisitionCode(requisition.code());
        }

        PurchaseOrder saved = saveEntity(purchaseOrder);
        // Ordered quantities are on order until received, released if the order is withdrawn.
        saved.getLines().stream().filter(java.util.Objects::nonNull)
                .forEach(line -> onOrderStock.add(line.getMaterialId(), line.getMaterialCode(), value(line.getQuantity())));
        if (requisitionId != null) {
            // Same transaction: if the requisition is not approved (or already converted), the order is rolled back.
            requisitionSource.markConverted(requisitionId, saved.getId().toString(),
                    saved.getOrderCode().getValue(), request.getUserId());
        }
        return toResponse(saved);
    }

    @Override
    @Transactional
    public PurchaseOrderOutput update(UUID id, CreatePurchaseOrderInput request) {
        UpdatePurchaseOrderInput updateRequest = new UpdatePurchaseOrderInput();
        updateRequest.setRequisitionId(request.getRequisitionId());
        updateRequest.setRequisitionCode(request.getRequisitionCode());
        updateRequest.setSupplierId(request.getSupplierId());
        updateRequest.setSupplierName(request.getSupplierName());
        updateRequest.setSupplierCode(request.getSupplierCode());
        updateRequest.setOrderDate(request.getOrderDate());
        updateRequest.setExpectedDeliveryDate(request.getExpectedDeliveryDate());
        updateRequest.setPaymentTerms(request.getPaymentTerms());
        updateRequest.setPaymentDelayDays(request.getPaymentDelayDays());
        updateRequest.setDeliveryTerms(request.getDeliveryTerms());
        updateRequest.setIncoterm(request.getIncoterm());
        updateRequest.setCurrencyCode(request.getCurrencyCode());
        updateRequest.setTaxAmount(request.getTaxAmount());
        updateRequest.setShippingCost(request.getShippingCost());
        updateRequest.setOrderedBy(request.getOrderedBy());
        updateRequest.setOrderedByName(request.getOrderedByName());
        updateRequest.setApprovedBy(request.getApprovedBy());
        updateRequest.setApprovedByName(request.getApprovedByName());
        updateRequest.setNotes(request.getNotes());
        updateRequest.setInternalNotes(request.getInternalNotes());
        updateRequest.setLines(request.getLines());
        updateRequest.setUserId(request.getUserId());
        return update(id, updateRequest);
    }

    @Override
    @Transactional
    public PurchaseOrderOutput update(UUID id, UpdatePurchaseOrderInput request) {
        PurchaseOrder purchaseOrder = getPurchaseOrderById(id);
        if (!purchaseOrder.isModifiable()) {
            throw new PurchaseOrderNotModifiableException();
        }
        if (request.getRequisitionId() != null
                && !request.getRequisitionId().equals(purchaseOrder.getRequisitionId())) {
            throw new PurchaseOrderValidationException("The originating requisition cannot be changed");
        }
        UUID requisitionId = purchaseOrder.getRequisitionId();
        String requisitionCode = purchaseOrder.getRequisitionCode();
        // A draft has no receipt: its whole quantity is on order. Release the old lines, add the new ones below.
        purchaseOrder.getLines().stream().filter(java.util.Objects::nonNull)
                .forEach(line -> onOrderStock.release(line.getMaterialId(), line.getMaterialCode(), value(line.getQuantity())));

        purchaseOrderMapper.updateEntity(purchaseOrder, request);
        purchaseOrder.setRequisitionId(requisitionId);
        purchaseOrder.setRequisitionCode(requisitionCode);
        normalizeLines(purchaseOrder.getLines(), purchaseOrder.getCurrencyCode());
        applyTaxRates(purchaseOrder);
        purchaseOrder.recalculateTotals();
        purchaseOrder.setUpdatedAt(LocalDateTime.now());
        purchaseOrder.setUpdatedBy(request.getUserId());
        purchaseOrder.getLines().stream().filter(java.util.Objects::nonNull)
                .forEach(line -> onOrderStock.add(line.getMaterialId(), line.getMaterialCode(), value(line.getQuantity())));
        return toResponse(saveEntity(purchaseOrder));
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        delete(id, null);
    }

    @Override
    @Transactional
    public void delete(UUID id, String userId) {
        PurchaseOrder purchaseOrder = getPurchaseOrderById(id);
        if (!purchaseOrder.isModifiable()) {
            throw new PurchaseOrderNotModifiableException();
        }
        releaseRequisition(purchaseOrder, userId);
        releaseOnOrder(purchaseOrder);
        purchaseOrderRepository.deleteById(id);
    }

    @Override
    public PurchaseOrderOutput getById(UUID id) {
        return toResponse(getPurchaseOrderById(id));
    }

    @Override
    public PurchaseOrderOutput getByCode(String code) {
        return toResponse(
                purchaseOrderRepository.findByCode(code)
                        .orElseThrow(() -> new PurchaseOrderNotFoundException(code))
        );
    }

    @Override
    public List<PurchaseOrderOutput> getAll() {
        return getAllResponses();
    }

    @Override
    public List<PurchaseOrderOutput> getByStatus(String status) {
        OrderStatus orderStatus = purchaseOrderMapper.toOrderStatus(status);
        return toResponseList(purchaseOrderRepository.findByStatus(orderStatus));
    }

    @Override
    public List<PurchaseOrderOutput> getByDeliveryStatus(String deliveryStatus) {
        DeliveryStatus status = purchaseOrderMapper.toDeliveryStatus(deliveryStatus);
        return toResponseList(purchaseOrderRepository.findByDeliveryStatus(status));
    }

    @Override
    public List<PurchaseOrderOutput> getBySupplierId(UUID supplierId) {
        return toResponseList(purchaseOrderRepository.findBySupplierId(supplierId));
    }

    @Override
    public List<PurchaseOrderOutput> getByRequisitionId(UUID requisitionId) {
        return toResponseList(purchaseOrderRepository.findByRequisitionId(requisitionId));
    }

    @Override
    public List<PurchaseOrderOutput> searchByKeyword(String keyword) {
        return toResponseList(purchaseOrderRepository.search(keyword));
    }

    @Override
    @Transactional
    public PurchaseOrderOutput submit(UUID id, String userId) {
        PurchaseOrder purchaseOrder = getPurchaseOrderById(id);
        purchaseOrder.submit(userId);
        PurchaseOrder saved = saveEntity(purchaseOrder);
        eventPublisher.publish(new PurchaseOrderSubmittedEvent(saved.getId(), saved.getOrderCode().getValue(),
                saved.getSupplierId(), saved.getSupplierName(), userId));
        return toResponse(saved);
    }

    @Override
    @Transactional
    public PurchaseOrderOutput confirm(UUID id, String userId) {
        PurchaseOrder purchaseOrder = getPurchaseOrderById(id);
        purchaseOrder.confirm(userId);
        PurchaseOrder saved = saveEntity(purchaseOrder);
        // Confirmed: the order is now valued at the rate of its order date.
        valueInReportingCurrency(saved.getId());
        eventPublisher.publish(new PurchaseOrderConfirmedEvent(saved.getId(), saved.getOrderCode().getValue(),
                saved.getSupplierId(), saved.getSupplierName(), userId));
        return toResponse(saved);
    }

    @Override
    @Transactional
    public PurchaseOrderOutput reject(UUID id, String userId, String reason) {
        AuditContext.because(reason);
        PurchaseOrder purchaseOrder = getPurchaseOrderById(id);
        purchaseOrder.reject(userId, reason);
        releaseRequisition(purchaseOrder, userId);
        releaseOnOrder(purchaseOrder);
        return toResponse(saveEntity(purchaseOrder));
    }

    @Override
    @Transactional
    public PurchaseOrderOutput assignReceiver(UUID id, String userId, String userName, String assignedUserId, String assignedUserName) {
        PurchaseOrder purchaseOrder = getPurchaseOrderById(id);
        ReceiverDirectory.Receiver receiver = receiverDirectory.findAssignableReceiver(assignedUserId)
                .orElseThrow(() -> new PurchaseOrderValidationException(
                        "The selected user is not an active receiver"));
        purchaseOrder.assignReceiver(userId, userName, receiver.id(), receiver.name());
        return toResponse(saveEntity(purchaseOrder));
    }

    @Override
    public List<ReceiverDirectory.Receiver> getAssignableReceivers() {
        return receiverDirectory.findAssignableReceivers();
    }

    @Override
    @Transactional
    public PurchaseOrderOutput confirmReceipt(UUID id, String receiverId, String receiverName) {
        getPurchaseOrderById(id);
        // Receiving goes through a goods receipt so stock and order status move together.
        receiptRecords.receiveRemaining(id.toString(), receiverId, receiverName);
        return toResponse(getPurchaseOrderById(id));
    }

    @Override
    @Transactional
    public PurchaseOrderOutput cancel(UUID id, String userId, String reason) {
        AuditContext.because(reason);
        PurchaseOrder purchaseOrder = getPurchaseOrderById(id);
        purchaseOrder.cancel(userId, reason);
        releaseRequisition(purchaseOrder, userId);
        releaseOnOrder(purchaseOrder);
        return toResponse(saveEntity(purchaseOrder));
    }

    @Override
    @Transactional
    public PurchaseOrderOutput complete(UUID id, String userId) {
        PurchaseOrder purchaseOrder = getPurchaseOrderById(id);
        purchaseOrder.complete(userId);
        // Closing short: what will never arrive is no longer on order.
        releaseOnOrder(purchaseOrder);
        return toResponse(saveEntity(purchaseOrder));
    }

    @Override
    @Transactional
    public PurchaseOrderOutput updateDeliveryStatus(UUID id, String deliveryStatus, String userId) {
        PurchaseOrder purchaseOrder = getPurchaseOrderById(id);
        purchaseOrder.updateDeliveryStatus(purchaseOrderMapper.toDeliveryStatus(deliveryStatus), userId);
        return toResponse(saveEntity(purchaseOrder));
    }

    /** VAT rate of an order line that does not give one, in percent. */
    @Value("${app.tax.default-rate:20}")
    public void setDefaultTaxRate(BigDecimal defaultTaxRate) {
        this.defaultTaxRate = defaultTaxRate;
    }

    /**
     * Gives every line a VAT rate (the default when it has none), so the order's VAT is the sum of its lines'.
     * A request that only sets the order's tax amount, with no rate on any line, keeps that amount as before.
     */
    private void applyTaxRates(PurchaseOrder purchaseOrder) {
        List<PurchaseOrderLine> lines = purchaseOrder.getLines();
        if (lines == null || lines.isEmpty()) return;
        boolean anyRate = lines.stream().anyMatch(l -> l != null && l.getTaxRate() != null);
        if (!anyRate && purchaseOrder.getTaxAmount() != null) return;
        for (PurchaseOrderLine line : lines) {
            if (line == null) continue;
            if (line.getTaxRate() == null) line.setTaxRate(defaultTaxRate);
            line.calculateLineTotal();
        }
    }

    /** An order withdrawn before any receipt hands its requisition back so it can be ordered again. */
    private void releaseRequisition(PurchaseOrder purchaseOrder, String userId) {
        if (purchaseOrder.getRequisitionId() == null) {
            return;
        }
        String orderId = purchaseOrder.getId().toString();
        RequisitionToOrder requisition = requisitionSource.get(purchaseOrder.getRequisitionId());
        // Orders created before conversion was transactional may reference a requisition that was never converted.
        if ("CONVERTED".equals(requisition.status()) && orderId.equals(requisition.purchaseOrderId())) {
            requisitionSource.revertConversion(purchaseOrder.getRequisitionId(), orderId, userId);
        }
    }

    /** Releases each line's quantity not yet received (from validated receipts) from the stock on order. */
    private void releaseOnOrder(PurchaseOrder purchaseOrder) {
        java.util.Map<String, Integer> received = receiptRecords.receivedQuantitiesByOrderLine(purchaseOrder.getId().toString());
        for (PurchaseOrderLine line : purchaseOrder.getLines()) {
            if (line == null) continue;
            int remaining = value(line.getQuantity()) - received.getOrDefault(line.getId().toString(), 0);
            onOrderStock.release(line.getMaterialId(), line.getMaterialCode(), remaining);
        }
    }

    /**
     * An order created from a requisition must order exactly what was requested: every requisition line once,
     * the same material, no more than the requested quantity, in the requisition's currency, from the one
     * supplier the requisition names (a requisition that mixes suppliers must be split before ordering).
     */
    private void checkAgainstRequisition(PurchaseOrder order, RequisitionToOrder requisition) {
        String code = requisition.code();
        if (requisition.currencyCode() != null && order.getCurrencyCode() != null
                && !requisition.currencyCode().equalsIgnoreCase(order.getCurrencyCode())) {
            throw new com.materia.backend.contexts.purchaseOrder.domain.exceptions.PurchaseOrderRequisitionMismatchException(
                    "The order must be in the requisition's currency (" + requisition.currencyCode() + ")");
        }
        java.util.Set<UUID> suppliers = new java.util.HashSet<>();
        for (RequestedLine line : requisition.lines()) {
            if (line.supplierId() != null) suppliers.add(line.supplierId());
        }
        if (suppliers.size() > 1) {
            throw new com.materia.backend.contexts.purchaseOrder.domain.exceptions.PurchaseOrderRequisitionMismatchException(
                    "Requisition " + code + " names several suppliers: split it into one requisition per supplier before ordering");
        }
        if (suppliers.size() == 1 && !suppliers.contains(order.getSupplierId())) {
            throw new com.materia.backend.contexts.purchaseOrder.domain.exceptions.PurchaseOrderRequisitionMismatchException(
                    "Requisition " + code + " is for another supplier");
        }
        java.util.Map<UUID, RequestedLine> requested = new java.util.HashMap<>();
        for (RequestedLine line : requisition.lines()) {
            requested.put(line.id(), line);
        }
        java.util.Set<UUID> ordered = new java.util.HashSet<>();
        for (PurchaseOrderLine line : order.getLines()) {
            var requestedLine = line.getRequisitionLineId() != null ? requested.get(line.getRequisitionLineId()) : null;
            if (requestedLine == null) {
                throw new com.materia.backend.contexts.purchaseOrder.domain.exceptions.PurchaseOrderRequisitionMismatchException(
                        "Line " + line.getLineNumber() + " (" + line.getMaterialCode() + ") is not a line of requisition " + code);
            }
            if (!ordered.add(requestedLine.id())) {
                throw new com.materia.backend.contexts.purchaseOrder.domain.exceptions.PurchaseOrderRequisitionMismatchException(
                        "A requisition line is ordered twice (" + requestedLine.materialCode() + ")");
            }
            if (requestedLine.materialCode() == null
                    || !requestedLine.materialCode().trim().equalsIgnoreCase(String.valueOf(line.getMaterialCode()).trim())) {
                throw new com.materia.backend.contexts.purchaseOrder.domain.exceptions.PurchaseOrderRequisitionMismatchException(
                        "Line " + line.getLineNumber() + " orders " + line.getMaterialCode() + " but the requisition asked for "
                                + requestedLine.materialCode());
            }
            if (value(line.getQuantity()) > value(requestedLine.quantity())) {
                throw new com.materia.backend.contexts.purchaseOrder.domain.exceptions.PurchaseOrderRequisitionMismatchException(
                        "Line " + line.getLineNumber() + " orders " + line.getQuantity() + " " + line.getMaterialCode()
                                + ", more than the " + requestedLine.quantity() + " requested");
            }
        }
        if (ordered.size() != requested.size()) {
            String missing = requested.values().stream()
                    .filter(l -> !ordered.contains(l.id()))
                    .map(RequestedLine::materialCode)
                    .collect(java.util.stream.Collectors.joining(", "));
            throw new com.materia.backend.contexts.purchaseOrder.domain.exceptions.PurchaseOrderRequisitionMismatchException(
                    "Every requisition line must be ordered (a requisition converts once); missing: " + missing);
        }
    }

    private static int value(Integer quantity) {
        return quantity != null ? quantity : 0;
    }

    private PurchaseOrder getPurchaseOrderById(UUID id) {
        return getEntityByIdOrThrow(id, () -> new PurchaseOrderNotFoundException(id));
    }

    private void normalizeLines(List<PurchaseOrderLine> lines, String orderCurrencyCode) {
        if (lines == null || lines.isEmpty()) {
            throw new PurchaseOrderLineRequiredException();
        }

        for (int i = 0; i < lines.size(); i++) {
            PurchaseOrderLine line = lines.get(i);
            if (line == null) {
                throw new PurchaseOrderInvalidLineException("Purchase order line cannot be null");
            }

            if (line.getUnitPrice() != null) {
                String unitPriceCurrency = line.getUnitPrice().getCurrencyCode();
                if (line.getCurrencyCode() == null || line.getCurrencyCode().isBlank()) {
                    line.setCurrencyCode(unitPriceCurrency);
                } else if (!line.getCurrencyCode().equalsIgnoreCase(unitPriceCurrency)) {
                    throw new PurchaseOrderValidationException("Line currency code must match unit price currency");
                }
            } else if (line.getCurrencyCode() != null && !line.getCurrencyCode().isBlank()) {
                throw new PurchaseOrderValidationException("Unit price is required when a line currency is provided");
            } else if (orderCurrencyCode != null && !orderCurrencyCode.isBlank()) {
                line.setCurrencyCode(orderCurrencyCode);
            }

            if (line.getId() == null) {
                line.setId(UUID.randomUUID());
            }

            line.setLineNumber(i + 1);
            line.calculateLineTotal();
        }
    }
}
