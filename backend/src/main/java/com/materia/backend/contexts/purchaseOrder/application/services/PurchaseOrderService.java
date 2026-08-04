package com.materia.backend.contexts.purchaseOrder.application.services;

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
import com.materia.backend.contexts.purchaseOrder.domain.ports.out.PurchaseOrderRepository;
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

    public PurchaseOrderService(PurchaseOrderRepository purchaseOrderRepository,
                                PurchaseOrderMapper purchaseOrderMapper,
                                PurchaseOrderCodeGeneratorService codeGenerator) {
        super(purchaseOrderRepository, purchaseOrderMapper);
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.purchaseOrderMapper = purchaseOrderMapper;
        this.codeGenerator = codeGenerator;
    }

    @Override
    @Retryable(retryFor = DataIntegrityViolationException.class, maxAttempts = 3, backoff = @Backoff(delay = 100))
    @Transactional
    public PurchaseOrderOutput create(CreatePurchaseOrderInput request) {
        PurchaseOrder purchaseOrder = purchaseOrderMapper.toEntity(request);
        normalizeLines(purchaseOrder.getLines(), purchaseOrder.getCurrencyCode());
        purchaseOrder.setOrderCode(codeGenerator.generateCode());
        purchaseOrder.recalculateTotals();
        return toResponse(saveEntity(purchaseOrder));
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

        purchaseOrderMapper.updateEntity(purchaseOrder, request);
        normalizeLines(purchaseOrder.getLines(), purchaseOrder.getCurrencyCode());
        purchaseOrder.recalculateTotals();
        purchaseOrder.setUpdatedAt(LocalDateTime.now());
        purchaseOrder.setUpdatedBy(request.getUserId());
        return toResponse(saveEntity(purchaseOrder));
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        PurchaseOrder purchaseOrder = getPurchaseOrderById(id);
        if (!purchaseOrder.isModifiable()) {
            throw new PurchaseOrderNotModifiableException();
        }
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
        return toResponse(saveEntity(purchaseOrder));
    }

    @Override
    @Transactional
    public PurchaseOrderOutput confirm(UUID id, String userId) {
        PurchaseOrder purchaseOrder = getPurchaseOrderById(id);
        purchaseOrder.confirm(userId);
        return toResponse(saveEntity(purchaseOrder));
    }

    @Override
    @Transactional
    public PurchaseOrderOutput assignReceiver(UUID id, String userId, String userName, String assignedUserId, String assignedUserName) {
        PurchaseOrder purchaseOrder = getPurchaseOrderById(id);
        purchaseOrder.assignReceiver(userId, userName, assignedUserId, assignedUserName);
        return toResponse(saveEntity(purchaseOrder));
    }

    @Override
    @Transactional
    public PurchaseOrderOutput confirmReceipt(UUID id, String receiverId, String receiverName) {
        PurchaseOrder purchaseOrder = getPurchaseOrderById(id);
        purchaseOrder.confirmReceipt(receiverId, receiverName);
        return toResponse(saveEntity(purchaseOrder));
    }

    @Override
    @Transactional
    public PurchaseOrderOutput cancel(UUID id, String userId, String reason) {
        PurchaseOrder purchaseOrder = getPurchaseOrderById(id);
        purchaseOrder.cancel(userId, reason);
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
