package com.materia.backend.contexts.purchaseOrder.application.services;

import com.materia.backend.common.application.AbstractCrudApplicationService;
import com.materia.backend.contexts.goodsReceipt.domain.ports.in.GoodsReceiptUseCase;
import com.materia.backend.contexts.purchaseRequisition.application.dtos.RequisitionOutput;
import com.materia.backend.contexts.purchaseRequisition.domain.ports.in.RequisitionUseCase;
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
import com.materia.backend.contexts.purchaseOrder.domain.ports.out.ReceiverDirectory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Application service for purchase orders.
 */
@Service
public class PurchaseOrderService extends AbstractCrudApplicationService<
        PurchaseOrder,
        CreatePurchaseOrderInput,
        UpdatePurchaseOrderInput,
        PurchaseOrderOutput> implements PurchaseOrderUseCase {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PurchaseOrderMapper purchaseOrderMapper;
    private final PurchaseOrderCodeGeneratorService codeGenerator;
    private final GoodsReceiptUseCase goodsReceiptUseCase;
    private final RequisitionUseCase requisitionUseCase;
    private final PurchaseOrderEventPublisher eventPublisher;
    private final ReceiverDirectory receiverDirectory;
    private final com.materia.backend.contexts.purchaseOrder.domain.ports.out.OnOrderStock onOrderStock;

    public PurchaseOrderService(PurchaseOrderRepository purchaseOrderRepository,
                                PurchaseOrderMapper purchaseOrderMapper,
                                PurchaseOrderCodeGeneratorService codeGenerator,
                                GoodsReceiptUseCase goodsReceiptUseCase,
                                RequisitionUseCase requisitionUseCase,
                                PurchaseOrderEventPublisher eventPublisher,
                                ReceiverDirectory receiverDirectory) {
        this(purchaseOrderRepository, purchaseOrderMapper, codeGenerator, goodsReceiptUseCase, requisitionUseCase,
                eventPublisher, receiverDirectory, com.materia.backend.contexts.purchaseOrder.domain.ports.out.OnOrderStock.NONE);
    }

    @org.springframework.beans.factory.annotation.Autowired
    public PurchaseOrderService(PurchaseOrderRepository purchaseOrderRepository,
                                PurchaseOrderMapper purchaseOrderMapper,
                                PurchaseOrderCodeGeneratorService codeGenerator,
                                GoodsReceiptUseCase goodsReceiptUseCase,
                                RequisitionUseCase requisitionUseCase,
                                PurchaseOrderEventPublisher eventPublisher,
                                ReceiverDirectory receiverDirectory,
                                com.materia.backend.contexts.purchaseOrder.domain.ports.out.OnOrderStock onOrderStock) {
        super(purchaseOrderRepository, purchaseOrderMapper);
        this.onOrderStock = onOrderStock;
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.purchaseOrderMapper = purchaseOrderMapper;
        this.codeGenerator = codeGenerator;
        this.goodsReceiptUseCase = goodsReceiptUseCase;
        this.requisitionUseCase = requisitionUseCase;
        this.eventPublisher = eventPublisher;
        this.receiverDirectory = receiverDirectory;
    }

    @Override
    @Retryable(retryFor = DataIntegrityViolationException.class, maxAttempts = 3, backoff = @Backoff(delay = 100))
    @Transactional
    public PurchaseOrderOutput create(CreatePurchaseOrderInput request) {
        PurchaseOrder purchaseOrder = purchaseOrderMapper.toEntity(request);
        normalizeLines(purchaseOrder.getLines(), purchaseOrder.getCurrencyCode());
        purchaseOrder.setOrderCode(codeGenerator.generateCode());
        purchaseOrder.recalculateTotals();

        UUID requisitionId = purchaseOrder.getRequisitionId();
        if (requisitionId != null) {
            RequisitionOutput requisition = requisitionUseCase.getById(requisitionId);
            checkAgainstRequisition(purchaseOrder, requisition);
            purchaseOrder.setRequisitionCode(requisition.getRequisitionCode());
        }

        PurchaseOrder saved = saveEntity(purchaseOrder);
        // Ordered quantities are on order until received, released if the order is withdrawn.
        saved.getLines().stream().filter(java.util.Objects::nonNull)
                .forEach(line -> onOrderStock.add(line.getMaterialId(), line.getMaterialCode(), value(line.getQuantity())));
        if (requisitionId != null) {
            // Same transaction: if the requisition is not approved (or already converted), the order is rolled back.
            requisitionUseCase.convert(requisitionId, saved.getId().toString(),
                    saved.getOrderCode().getValue(), request.getUserId());
        }
        return toResponse(saved);
    }

    @Override
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
        eventPublisher.publish(new PurchaseOrderConfirmedEvent(saved.getId(), saved.getOrderCode().getValue(),
                saved.getSupplierId(), saved.getSupplierName(), userId));
        return toResponse(saved);
    }

    @Override
    @Transactional
    public PurchaseOrderOutput reject(UUID id, String userId, String reason) {
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
        goodsReceiptUseCase.receiveRemaining(id.toString(), receiverId, receiverName);
        return toResponse(getPurchaseOrderById(id));
    }

    @Override
    @Transactional
    public PurchaseOrderOutput cancel(UUID id, String userId, String reason) {
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

    /** An order withdrawn before any receipt hands its requisition back so it can be ordered again. */
    private void releaseRequisition(PurchaseOrder purchaseOrder, String userId) {
        if (purchaseOrder.getRequisitionId() == null) {
            return;
        }
        String orderId = purchaseOrder.getId().toString();
        RequisitionOutput requisition = requisitionUseCase.getById(purchaseOrder.getRequisitionId());
        // Orders created before conversion was transactional may reference a requisition that was never converted.
        if ("CONVERTED".equals(requisition.getStatus()) && orderId.equals(requisition.getPurchaseOrderId())) {
            requisitionUseCase.revertConversion(purchaseOrder.getRequisitionId(), orderId, userId);
        }
    }

    /** Releases each line's quantity not yet received (from validated receipts) from the stock on order. */
    private void releaseOnOrder(PurchaseOrder purchaseOrder) {
        java.util.Map<String, Integer> received = new java.util.HashMap<>();
        for (var receipt : goodsReceiptUseCase.getByPurchaseOrderId(purchaseOrder.getId().toString())) {
            if (!"COMPLETED".equals(receipt.getStatus()) && !"PARTIAL".equals(receipt.getStatus())) continue;
            for (var line : receipt.getLines()) {
                if (line.getPurchaseOrderLineId() == null) continue;
                received.merge(line.getPurchaseOrderLineId(), value(line.getQuantityReceived()), Integer::sum);
            }
        }
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
    private void checkAgainstRequisition(PurchaseOrder order, RequisitionOutput requisition) {
        String code = requisition.getRequisitionCode();
        if (requisition.getCurrencyCode() != null && order.getCurrencyCode() != null
                && !requisition.getCurrencyCode().equalsIgnoreCase(order.getCurrencyCode())) {
            throw new com.materia.backend.contexts.purchaseOrder.domain.exceptions.PurchaseOrderRequisitionMismatchException(
                    "The order must be in the requisition's currency (" + requisition.getCurrencyCode() + ")");
        }
        java.util.Set<UUID> suppliers = new java.util.HashSet<>();
        for (var line : requisition.getLines()) {
            if (line.getSupplierId() != null) suppliers.add(line.getSupplierId());
        }
        if (suppliers.size() > 1) {
            throw new com.materia.backend.contexts.purchaseOrder.domain.exceptions.PurchaseOrderRequisitionMismatchException(
                    "Requisition " + code + " names several suppliers: split it into one requisition per supplier before ordering");
        }
        if (suppliers.size() == 1 && !suppliers.contains(order.getSupplierId())) {
            throw new com.materia.backend.contexts.purchaseOrder.domain.exceptions.PurchaseOrderRequisitionMismatchException(
                    "Requisition " + code + " is for another supplier");
        }
        java.util.Map<UUID, com.materia.backend.contexts.purchaseRequisition.domain.entities.RequisitionLine> requested =
                new java.util.HashMap<>();
        for (var line : requisition.getLines()) {
            requested.put(line.getId(), line);
        }
        java.util.Set<UUID> ordered = new java.util.HashSet<>();
        for (PurchaseOrderLine line : order.getLines()) {
            var requestedLine = line.getRequisitionLineId() != null ? requested.get(line.getRequisitionLineId()) : null;
            if (requestedLine == null) {
                throw new com.materia.backend.contexts.purchaseOrder.domain.exceptions.PurchaseOrderRequisitionMismatchException(
                        "Line " + line.getLineNumber() + " (" + line.getMaterialCode() + ") is not a line of requisition " + code);
            }
            if (!ordered.add(requestedLine.getId())) {
                throw new com.materia.backend.contexts.purchaseOrder.domain.exceptions.PurchaseOrderRequisitionMismatchException(
                        "A requisition line is ordered twice (" + requestedLine.getMaterialCode() + ")");
            }
            if (requestedLine.getMaterialCode() == null
                    || !requestedLine.getMaterialCode().trim().equalsIgnoreCase(String.valueOf(line.getMaterialCode()).trim())) {
                throw new com.materia.backend.contexts.purchaseOrder.domain.exceptions.PurchaseOrderRequisitionMismatchException(
                        "Line " + line.getLineNumber() + " orders " + line.getMaterialCode() + " but the requisition asked for "
                                + requestedLine.getMaterialCode());
            }
            if (value(line.getQuantity()) > value(requestedLine.getQuantity())) {
                throw new com.materia.backend.contexts.purchaseOrder.domain.exceptions.PurchaseOrderRequisitionMismatchException(
                        "Line " + line.getLineNumber() + " orders " + line.getQuantity() + " " + line.getMaterialCode()
                                + ", more than the " + requestedLine.getQuantity() + " requested");
            }
        }
        if (ordered.size() != requested.size()) {
            String missing = requested.values().stream()
                    .filter(l -> !ordered.contains(l.getId()))
                    .map(com.materia.backend.contexts.purchaseRequisition.domain.entities.RequisitionLine::getMaterialCode)
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
