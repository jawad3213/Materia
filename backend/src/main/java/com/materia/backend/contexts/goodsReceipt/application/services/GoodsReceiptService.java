package com.materia.backend.contexts.goodsReceipt.application.services;

import com.materia.backend.contexts.goodsReceipt.application.dtos.CreateGoodsReceiptInput;
import com.materia.backend.contexts.goodsReceipt.application.dtos.GoodsReceiptLineInput;
import com.materia.backend.contexts.goodsReceipt.application.dtos.GoodsReceiptOutput;
import com.materia.backend.contexts.goodsReceipt.application.dtos.UpdateGoodsReceiptInput;
import com.materia.backend.contexts.goodsReceipt.application.mappers.GoodsReceiptMapper;
import com.materia.backend.contexts.goodsReceipt.domain.entities.GoodsReceipt;
import com.materia.backend.contexts.goodsReceipt.domain.entities.GoodsReceiptLine;
import com.materia.backend.contexts.goodsReceipt.domain.enums.ReceiptStatus;
import com.materia.backend.contexts.goodsReceipt.domain.events.GoodsReceiptCompletedEvent;
import com.materia.backend.contexts.goodsReceipt.domain.events.GoodsReceiptCreatedEvent;
import com.materia.backend.contexts.goodsReceipt.domain.events.GoodsReceiptPartialEvent;
import com.materia.backend.contexts.goodsReceipt.domain.events.GoodsReceiptRejectedEvent;
import com.materia.backend.contexts.goodsReceipt.domain.exceptions.GoodsReceiptLineRequiredException;
import com.materia.backend.contexts.goodsReceipt.domain.exceptions.GoodsReceiptNotFoundException;
import com.materia.backend.contexts.goodsReceipt.domain.exceptions.GoodsReceiptNotModifiableException;
import com.materia.backend.contexts.goodsReceipt.domain.exceptions.GoodsReceiptValidationException;
import com.materia.backend.contexts.goodsReceipt.domain.ports.in.GoodsReceiptUseCase;
import com.materia.backend.contexts.goodsReceipt.domain.ports.out.GoodsReceiptEventPublisher;
import com.materia.backend.contexts.goodsReceipt.domain.ports.out.GoodsReceiptRepository;
import com.materia.backend.contexts.goodsReceipt.domain.valueObjects.ReceiptCode;
import com.materia.backend.contexts.masterData.domain.entities.Material;
import com.materia.backend.contexts.masterData.domain.ports.out.MaterialRepository;
import com.materia.backend.contexts.purchaseOrder.domain.entities.PurchaseOrder;
import com.materia.backend.contexts.purchaseOrder.domain.entities.PurchaseOrderLine;
import com.materia.backend.contexts.purchaseOrder.domain.enums.DeliveryStatus;
import com.materia.backend.contexts.purchaseOrder.domain.enums.OrderStatus;
import com.materia.backend.contexts.purchaseOrder.domain.ports.out.PurchaseOrderRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/** Application service for the goods receipt lifecycle. */
@Service
public class GoodsReceiptService implements GoodsReceiptUseCase {

    private final GoodsReceiptRepository goodsReceiptRepository;
    private final GoodsReceiptMapper goodsReceiptMapper;
    private final GoodsReceiptEventPublisher eventPublisher;
    private final GoodsReceiptCodeGeneratorService codeGenerator;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final MaterialRepository materialRepository;

    public GoodsReceiptService(GoodsReceiptRepository goodsReceiptRepository,
                               GoodsReceiptMapper goodsReceiptMapper,
                               GoodsReceiptEventPublisher eventPublisher,
                               GoodsReceiptCodeGeneratorService codeGenerator,
                               PurchaseOrderRepository purchaseOrderRepository,
                               MaterialRepository materialRepository) {
        this.goodsReceiptRepository = goodsReceiptRepository;
        this.goodsReceiptMapper = goodsReceiptMapper;
        this.eventPublisher = eventPublisher;
        this.codeGenerator = codeGenerator;
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.materialRepository = materialRepository;
    }

    @Override
    @Retryable(retryFor = DataIntegrityViolationException.class, maxAttempts = 3, backoff = @Backoff(delay = 100))
    @Transactional
    public GoodsReceiptOutput create(CreateGoodsReceiptInput request) {
        String receiptCode = resolveReceiptCode(request);
        GoodsReceipt goodsReceipt = goodsReceiptMapper.toEntity(request);
        goodsReceipt.setReceiptCode(ReceiptCode.of(receiptCode));
        validateAndHydrateFromPurchaseOrder(goodsReceipt, request.getUserId());
        normalizeLines(goodsReceipt.getLines());
        goodsReceipt.recalculateTotals();

        GoodsReceipt saved = goodsReceiptRepository.save(goodsReceipt);
        eventPublisher.publish(new GoodsReceiptCreatedEvent(
                saved.getId(),
                saved.getReceiptCode().getValue(),
                saved.getPurchaseOrderId(),
                saved.getPurchaseOrderCode(),
                saved.getReceivedBy(),
                saved.getReceivedByName()
        ));
        return goodsReceiptMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public GoodsReceiptOutput update(UUID id, CreateGoodsReceiptInput request) {
        return update(id, toUpdateRequest(request));
    }

    @Override
    @Transactional
    public GoodsReceiptOutput update(UUID id, UpdateGoodsReceiptInput request) {
        GoodsReceipt goodsReceipt = getGoodsReceiptById(id);
        if (!goodsReceipt.getStatus().isModifiable()) {
            throw new GoodsReceiptNotModifiableException("This goods receipt can no longer be modified");
        }
        if (request.getPurchaseOrderId() != null
                && !request.getPurchaseOrderId().equals(goodsReceipt.getPurchaseOrderId())) {
            throw new GoodsReceiptValidationException("The purchase order cannot be changed after receipt creation");
        }

        if (request.getReceiptCode() != null && !request.getReceiptCode().isBlank()) {
            String code = request.getReceiptCode().trim();
            if (!goodsReceipt.getReceiptCode().getValue().equals(code)
                    && goodsReceiptRepository.existsByReceiptCode(code)) {
                throw new GoodsReceiptValidationException("A goods receipt already uses this code");
            }
        }

        goodsReceiptMapper.updateEntity(goodsReceipt, request);
        validateAndHydrateFromPurchaseOrder(goodsReceipt, request.getUserId());
        normalizeLines(goodsReceipt.getLines());
        goodsReceipt.recalculateTotals();
        goodsReceipt.setUpdatedAt(LocalDateTime.now());
        goodsReceipt.setUpdatedBy(request.getUserId());
        return goodsReceiptMapper.toResponse(goodsReceiptRepository.save(goodsReceipt));
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        GoodsReceipt goodsReceipt = getGoodsReceiptById(id);
        if (!goodsReceipt.getStatus().isModifiable()) {
            throw new GoodsReceiptNotModifiableException("This goods receipt can no longer be deleted");
        }
        goodsReceiptRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void delete(UUID id, String userId) {
        GoodsReceipt goodsReceipt = getGoodsReceiptById(id);
        validateAssignedReceiver(goodsReceipt, userId);
        if (!goodsReceipt.getStatus().isModifiable()) {
            throw new GoodsReceiptNotModifiableException("This goods receipt can no longer be deleted");
        }
        goodsReceiptRepository.deleteById(id);
    }

    @Override
    public GoodsReceiptOutput getById(UUID id) {
        return goodsReceiptMapper.toResponse(getGoodsReceiptById(id));
    }

    @Override
    public GoodsReceiptOutput getByCode(String code) {
        return goodsReceiptMapper.toResponse(goodsReceiptRepository.findByReceiptCode(code)
                .orElseThrow(() -> new GoodsReceiptNotFoundException("Goods receipt not found: " + code)));
    }

    @Override
    public List<GoodsReceiptOutput> getAll() {
        return goodsReceiptMapper.toResponseList(goodsReceiptRepository.findAll());
    }

    @Override
    public List<GoodsReceiptOutput> getByPurchaseOrderId(String purchaseOrderId) {
        return goodsReceiptMapper.toResponseList(goodsReceiptRepository.findByPurchaseOrderId(purchaseOrderId));
    }

    @Override
    public List<GoodsReceiptOutput> getByStatus(ReceiptStatus status) {
        return goodsReceiptMapper.toResponseList(goodsReceiptRepository.findByStatus(status));
    }

    @Override
    public List<GoodsReceiptOutput> getByReceiverId(String receiverId) {
        return goodsReceiptMapper.toResponseList(goodsReceiptRepository.findByReceivedBy(receiverId));
    }

    @Override
    public List<GoodsReceiptOutput> search(String keyword) {
        return goodsReceiptMapper.toResponseList(goodsReceiptRepository.search(keyword));
    }

    @Override
    @Transactional
    public GoodsReceiptOutput addLine(UUID id, GoodsReceiptLineInput line, String userId) {
        GoodsReceipt goodsReceipt = getGoodsReceiptById(id);
        validateAssignedReceiver(goodsReceipt, userId);
        goodsReceipt.addLine(goodsReceiptMapper.toLineEntity(line));
        validateAndHydrateFromPurchaseOrder(goodsReceipt, userId);
        normalizeLines(goodsReceipt.getLines());
        goodsReceipt.recalculateTotals();
        goodsReceipt.updateAudit(userId);
        return goodsReceiptMapper.toResponse(goodsReceiptRepository.save(goodsReceipt));
    }

    @Override
    @Transactional
    public GoodsReceiptOutput removeLine(UUID id, int lineIndex, String userId) {
        GoodsReceipt goodsReceipt = getGoodsReceiptById(id);
        validateAssignedReceiver(goodsReceipt, userId);
        if (goodsReceipt.getLines().size() <= 1) {
            throw new GoodsReceiptLineRequiredException("A goods receipt must contain at least one line");
        }

        goodsReceipt.removeLine(lineIndex);
        normalizeLines(goodsReceipt.getLines());
        goodsReceipt.recalculateTotals();
        goodsReceipt.updateAudit(userId);
        return goodsReceiptMapper.toResponse(goodsReceiptRepository.save(goodsReceipt));
    }

    @Override
    @Transactional
    public GoodsReceiptOutput complete(UUID id, String userId) {
        GoodsReceipt goodsReceipt = getGoodsReceiptById(id);
        PurchaseOrder purchaseOrder = validateAndHydrateFromPurchaseOrder(goodsReceipt, userId);
        normalizeLines(goodsReceipt.getLines());
        goodsReceipt.recalculateTotals();
        goodsReceipt.complete(userId);
        applyAcceptedStock(goodsReceipt, userId);
        GoodsReceipt saved = goodsReceiptRepository.save(goodsReceipt);
        updatePurchaseOrderDeliveryStatus(purchaseOrder, saved, userId);

        if (saved.getStatus() == ReceiptStatus.PARTIAL) {
            int totalPending = Math.max(0, saved.getTotalQuantityOrdered() - saved.getTotalQuantityReceived());
            eventPublisher.publish(new GoodsReceiptPartialEvent(
                    saved.getId(),
                    saved.getReceiptCode().getValue(),
                    saved.getPurchaseOrderId(),
                    saved.getTotalQuantityOrdered(),
                    saved.getTotalQuantityReceived(),
                    totalPending,
                    userId
            ));
        } else {
            eventPublisher.publish(new GoodsReceiptCompletedEvent(
                    saved.getId(),
                    saved.getReceiptCode().getValue(),
                    saved.getPurchaseOrderId(),
                    saved.getTotalQuantityReceived(),
                    saved.getTotalQuantityAccepted(),
                    userId
            ));
        }

        if (saved.getTotalQuantityRejected() != null && saved.getTotalQuantityRejected() > 0) {
            eventPublisher.publish(new GoodsReceiptRejectedEvent(
                    saved.getId(),
                    saved.getReceiptCode().getValue(),
                    saved.getPurchaseOrderId(),
                    saved.getTotalQuantityRejected(),
                    saved.getDiscrepancyNotes(),
                    userId
            ));
        }

        return goodsReceiptMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public GoodsReceiptOutput cancel(UUID id, String userId, String reason) {
        GoodsReceipt goodsReceipt = getGoodsReceiptById(id);
        validateAssignedReceiver(goodsReceipt, userId);
        goodsReceipt.cancel(userId, reason);
        return goodsReceiptMapper.toResponse(goodsReceiptRepository.save(goodsReceipt));
    }

    private GoodsReceipt getGoodsReceiptById(UUID id) {
        return goodsReceiptRepository.findById(id)
                .orElseThrow(() -> new GoodsReceiptNotFoundException("Goods receipt not found: " + id));
    }

    private String resolveReceiptCode(CreateGoodsReceiptInput request) {
        if (request.getReceiptCode() != null && !request.getReceiptCode().isBlank()) {
            String code = request.getReceiptCode().trim();
            if (goodsReceiptRepository.existsByReceiptCode(code)) {
                throw new GoodsReceiptValidationException("A goods receipt already uses this code");
            }
            return code;
        }
        return codeGenerator.generateCode().getValue();
    }

    private void normalizeLines(List<GoodsReceiptLine> lines) {
        if (lines == null || lines.isEmpty()) {
            throw new GoodsReceiptLineRequiredException("A goods receipt must contain at least one line");
        }

        for (int index = 0; index < lines.size(); index++) {
            GoodsReceiptLine line = lines.get(index);
            if (line.getId() == null) {
                line.setId(UUID.randomUUID());
            }
            line.setLineNumber(index + 1);
            line.calculateAcceptedQuantity();
            line.calculatePendingQuantity();
        }
    }

    /** Applies the purchase order as the authoritative source for receipt header and line data. */
    private PurchaseOrder validateAndHydrateFromPurchaseOrder(GoodsReceipt goodsReceipt, String userId) {
        if (userId == null || userId.isBlank()) {
            throw new AccessDeniedException("An authenticated user is required to process a goods receipt");
        }

        PurchaseOrder purchaseOrder = getPurchaseOrder(goodsReceipt.getPurchaseOrderId());
        if (purchaseOrder.getStatus() != OrderStatus.READY_FOR_RECEIPT
                && purchaseOrder.getStatus() != OrderStatus.PARTIALLY_RECEIVED) {
            throw new GoodsReceiptValidationException("The purchase order is not ready for receipt");
        }
        if (purchaseOrder.getAssignedTo() == null || !purchaseOrder.getAssignedTo().equals(userId)) {
            throw new AccessDeniedException("You are not assigned to receive this purchase order");
        }
        if (goodsReceipt.getReceivedBy() == null || !goodsReceipt.getReceivedBy().equals(userId)) {
            throw new AccessDeniedException("The goods receipt receiver must be the assigned purchase-order receiver");
        }

        goodsReceipt.setPurchaseOrderId(purchaseOrder.getId().toString());
        goodsReceipt.setPurchaseOrderCode(purchaseOrder.getOrderCode().getValue());
        goodsReceipt.setSupplierId(purchaseOrder.getSupplierId().toString());
        goodsReceipt.setSupplierName(purchaseOrder.getSupplierName());
        goodsReceipt.setExpectedDeliveryDate(purchaseOrder.getExpectedDeliveryDate());

        Map<String, PurchaseOrderLine> orderLines = purchaseOrder.getLines().stream()
                .collect(Collectors.toMap(line -> line.getId().toString(), line -> line));
        Map<String, Integer> previouslyReceived = receivedQuantitiesByPurchaseOrderLine(
                purchaseOrder.getId().toString(), goodsReceipt.getId());
        Map<String, Integer> receiptQuantities = new HashMap<>();

        for (GoodsReceiptLine receiptLine : goodsReceipt.getLines()) {
            String purchaseOrderLineId = receiptLine.getPurchaseOrderLineId();
            PurchaseOrderLine orderLine = orderLines.get(purchaseOrderLineId);
            if (orderLine == null) {
                throw new GoodsReceiptValidationException("Each goods receipt line must reference a purchase-order line");
            }
            if (!orderLine.getMaterialCode().equals(receiptLine.getMaterialCode())) {
                throw new GoodsReceiptValidationException(
                        "Receipt material does not match purchase-order line " + orderLine.getLineNumber());
            }

            validateLineQuantities(receiptLine, orderLine.getQuantity());
            receiptQuantities.merge(purchaseOrderLineId, receiptLine.getQuantityReceived(), Integer::sum);

            receiptLine.setQuantityOrdered(orderLine.getQuantity());
            receiptLine.setMaterialId(orderLine.getMaterialId());
            receiptLine.setMaterialName(orderLine.getMaterialName());
            receiptLine.setUnitOfMeasure(orderLine.getUnitOfMeasure());
            receiptLine.setUnitPrice(orderLine.getUnitPrice());
            receiptLine.setSupplierId(purchaseOrder.getSupplierId().toString());
            receiptLine.setSupplierName(purchaseOrder.getSupplierName());
        }

        for (Map.Entry<String, Integer> entry : receiptQuantities.entrySet()) {
            PurchaseOrderLine orderLine = orderLines.get(entry.getKey());
            int alreadyReceived = previouslyReceived.getOrDefault(entry.getKey(), 0);
            if (alreadyReceived + entry.getValue() > orderLine.getQuantity()) {
                throw new GoodsReceiptValidationException(
                        "Received quantity exceeds the remaining quantity for purchase-order line "
                                + orderLine.getLineNumber());
            }
        }
        return purchaseOrder;
    }

    private void validateAssignedReceiver(GoodsReceipt goodsReceipt, String userId) {
        PurchaseOrder purchaseOrder = getPurchaseOrder(goodsReceipt.getPurchaseOrderId());
        if (userId == null || userId.isBlank() || !userId.equals(purchaseOrder.getAssignedTo())) {
            throw new AccessDeniedException("You are not assigned to this goods receipt");
        }
    }

    private PurchaseOrder getPurchaseOrder(String purchaseOrderId) {
        try {
            return purchaseOrderRepository.findById(UUID.fromString(purchaseOrderId))
                    .orElseThrow(() -> new GoodsReceiptValidationException("Purchase order not found: " + purchaseOrderId));
        } catch (IllegalArgumentException exception) {
            throw new GoodsReceiptValidationException("Invalid purchase order ID: " + purchaseOrderId);
        }
    }

    private void validateLineQuantities(GoodsReceiptLine line, int orderedQuantity) {
        int receivedQuantity = line.getQuantityReceived() != null ? line.getQuantityReceived() : 0;
        int rejectedQuantity = line.getQuantityRejected() != null ? line.getQuantityRejected() : 0;
        if (rejectedQuantity > receivedQuantity) {
            throw new GoodsReceiptValidationException("Rejected quantity cannot exceed received quantity");
        }
        if (receivedQuantity > orderedQuantity) {
            throw new GoodsReceiptValidationException("Received quantity cannot exceed ordered quantity");
        }
    }

    private Map<String, Integer> receivedQuantitiesByPurchaseOrderLine(String purchaseOrderId, UUID excludedReceiptId) {
        return goodsReceiptRepository.findByPurchaseOrderId(purchaseOrderId).stream()
                .filter(receipt -> !receipt.getId().equals(excludedReceiptId))
                .filter(receipt -> receipt.getStatus() == ReceiptStatus.COMPLETED
                        || receipt.getStatus() == ReceiptStatus.PARTIAL)
                .flatMap(receipt -> receipt.getLines().stream())
                .filter(line -> line.getPurchaseOrderLineId() != null)
                .collect(Collectors.groupingBy(
                        GoodsReceiptLine::getPurchaseOrderLineId,
                        Collectors.summingInt(line -> line.getQuantityReceived() != null ? line.getQuantityReceived() : 0)
                ));
    }

    private void applyAcceptedStock(GoodsReceipt goodsReceipt, String userId) {
        for (GoodsReceiptLine line : goodsReceipt.getLines()) {
            int acceptedQuantity = line.getQuantityAccepted() != null ? line.getQuantityAccepted() : 0;
            if (acceptedQuantity <= 0) {
                continue;
            }

            Material material = findMaterial(line);
            int stockBefore = material.getCurrentStock() != null ? material.getCurrentStock() : 0;
            material.increaseStock(acceptedQuantity, "Goods receipt " + goodsReceipt.getReceiptCode().getValue());
            material.setUpdatedBy(userId);
            Material savedMaterial = materialRepository.save(material);
            line.setStockBefore(stockBefore);
            line.setStockAfter(savedMaterial.getCurrentStock());
        }
    }

    private Material findMaterial(GoodsReceiptLine line) {
        if (line.getMaterialId() != null) {
            return materialRepository.findById(line.getMaterialId())
                    .orElseThrow(() -> new GoodsReceiptValidationException(
                            "Material not found: " + line.getMaterialCode()));
        }
        return materialRepository.findByCode(line.getMaterialCode())
                .orElseThrow(() -> new GoodsReceiptValidationException("Material not found: " + line.getMaterialCode()));
    }

    private void updatePurchaseOrderDeliveryStatus(PurchaseOrder purchaseOrder,
                                                   GoodsReceipt completedReceipt,
                                                   String userId) {
        Map<String, Integer> receivedQuantities = receivedQuantitiesByPurchaseOrderLine(
                purchaseOrder.getId().toString(), completedReceipt.getId());
        for (GoodsReceiptLine line : completedReceipt.getLines()) {
            receivedQuantities.merge(line.getPurchaseOrderLineId(), line.getQuantityReceived(), Integer::sum);
        }

        boolean fullyReceived = purchaseOrder.getLines().stream().allMatch(line ->
                receivedQuantities.getOrDefault(line.getId().toString(), 0) >= line.getQuantity());
        purchaseOrder.updateDeliveryStatus(fullyReceived ? DeliveryStatus.DELIVERED : DeliveryStatus.PARTIAL, userId);
        purchaseOrderRepository.save(purchaseOrder);
    }

    private UpdateGoodsReceiptInput toUpdateRequest(CreateGoodsReceiptInput request) {
        UpdateGoodsReceiptInput update = new UpdateGoodsReceiptInput();
        update.setReceiptCode(request.getReceiptCode());
        update.setPurchaseOrderId(request.getPurchaseOrderId());
        update.setPurchaseOrderCode(request.getPurchaseOrderCode());
        update.setReceiptDate(request.getReceiptDate());
        update.setExpectedDeliveryDate(request.getExpectedDeliveryDate());
        update.setReceivedBy(request.getReceivedBy());
        update.setReceivedByName(request.getReceivedByName());
        update.setNotes(request.getNotes());
        update.setSupplierId(request.getSupplierId());
        update.setSupplierName(request.getSupplierName());
        update.setDiscrepancyNotes(request.getDiscrepancyNotes());
        update.setLines(request.getLines());
        update.setUserId(request.getUserId());
        return update;
    }
}
