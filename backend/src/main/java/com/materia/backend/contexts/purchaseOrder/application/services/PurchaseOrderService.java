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

    public PurchaseOrderService(PurchaseOrderRepository purchaseOrderRepository,
                                PurchaseOrderMapper purchaseOrderMapper,
                                PurchaseOrderCodeGeneratorService codeGenerator,
                                GoodsReceiptUseCase goodsReceiptUseCase,
                                RequisitionUseCase requisitionUseCase,
                                PurchaseOrderEventPublisher eventPublisher,
                                ReceiverDirectory receiverDirectory) {
        super(purchaseOrderRepository, purchaseOrderMapper);
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
            purchaseOrder.setRequisitionCode(requisition.getRequisitionCode());
        }

        PurchaseOrder saved = saveEntity(purchaseOrder);
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

        purchaseOrderMapper.updateEntity(purchaseOrder, request);
        purchaseOrder.setRequisitionId(requisitionId);
        purchaseOrder.setRequisitionCode(requisitionCode);
        normalizeLines(purchaseOrder.getLines(), purchaseOrder.getCurrencyCode());
        purchaseOrder.recalculateTotals();
        purchaseOrder.setUpdatedAt(LocalDateTime.now());
        purchaseOrder.setUpdatedBy(request.getUserId());
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
        return toResponse(saveEntity(purchaseOrder));
    }

    @Override
    @Transactional
    public PurchaseOrderOutput complete(UUID id, String userId) {
        PurchaseOrder purchaseOrder = getPurchaseOrderById(id);
        purchaseOrder.complete(userId);
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
