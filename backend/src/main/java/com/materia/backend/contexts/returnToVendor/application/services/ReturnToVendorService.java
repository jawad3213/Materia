package com.materia.backend.contexts.returnToVendor.application.services;

import com.materia.backend.contexts.goodsReceipt.domain.entities.GoodsReceipt;
import com.materia.backend.contexts.goodsReceipt.domain.entities.GoodsReceiptLine;
import com.materia.backend.contexts.goodsReceipt.domain.enums.ReceiptStatus;
import com.materia.backend.contexts.goodsReceipt.domain.ports.out.GoodsReceiptRepository;
import com.materia.backend.contexts.masterData.domain.entities.Material;
import com.materia.backend.contexts.masterData.domain.ports.out.MaterialRepository;
import com.materia.backend.contexts.purchaseOrder.domain.entities.PurchaseOrder;
import com.materia.backend.contexts.purchaseOrder.domain.enums.OrderStatus;
import com.materia.backend.contexts.purchaseOrder.domain.ports.out.PurchaseOrderRepository;
import com.materia.backend.contexts.returnToVendor.application.dtos.CreateReturnToVendorInput;
import com.materia.backend.contexts.returnToVendor.application.dtos.ReturnToVendorLineInput;
import com.materia.backend.contexts.returnToVendor.application.dtos.ReturnToVendorOutput;
import com.materia.backend.contexts.returnToVendor.application.dtos.UpdateReturnToVendorInput;
import com.materia.backend.contexts.returnToVendor.application.mappers.ReturnToVendorMapper;
import com.materia.backend.contexts.returnToVendor.domain.entities.ReturnToVendor;
import com.materia.backend.contexts.returnToVendor.domain.entities.ReturnToVendorLine;
import com.materia.backend.contexts.returnToVendor.domain.enums.ResolutionType;
import com.materia.backend.contexts.returnToVendor.domain.enums.ReturnStatus;
import com.materia.backend.contexts.returnToVendor.domain.events.ReturnToVendorCancelledEvent;
import com.materia.backend.contexts.returnToVendor.domain.events.ReturnToVendorCreatedEvent;
import com.materia.backend.contexts.returnToVendor.domain.events.ReturnToVendorResolvedEvent;
import com.materia.backend.contexts.returnToVendor.domain.events.ReturnToVendorSubmittedEvent;
import com.materia.backend.contexts.returnToVendor.domain.exceptions.ReturnToVendorBusinessException;
import com.materia.backend.contexts.returnToVendor.domain.exceptions.ReturnToVendorInvalidLineException;
import com.materia.backend.contexts.returnToVendor.domain.exceptions.ReturnToVendorInvalidQuantityException;
import com.materia.backend.contexts.returnToVendor.domain.exceptions.ReturnToVendorLineRequiredException;
import com.materia.backend.contexts.returnToVendor.domain.exceptions.ReturnToVendorNotFoundException;
import com.materia.backend.contexts.returnToVendor.domain.exceptions.ReturnToVendorNotModifiableException;
import com.materia.backend.contexts.returnToVendor.domain.exceptions.ReturnToVendorValidationException;
import com.materia.backend.contexts.returnToVendor.domain.ports.in.ReturnToVendorUseCase;
import com.materia.backend.contexts.returnToVendor.domain.ports.out.ReturnToVendorRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Returns to vendor, tied to the procurement chain:
 * <ul>
 *   <li>a return is prepared from a completed goods receipt and can only send back what that receipt rejected,
 *   less what other open or resolved returns already cover; materials, prices and the supplier come from the
 *   receipt, never from the client;</li>
 *   <li>rejected goods never entered stock, so shipping them back moves no stock;</li>
 *   <li>a replacement reopens the purchase order and puts the quantity back on order, so the assigned receiver
 *   receives the replacement against the same order;</li>
 *   <li>a credit note records the supplier's number and the value of the goods at purchase-order prices.</li>
 * </ul>
 */
@Service
@Transactional(readOnly = true)
public class ReturnToVendorService implements ReturnToVendorUseCase {

    static final String DEFAULT_LINE_REASON = "Rejected at goods receipt";

    private static final Set<ReceiptStatus> RETURNABLE_RECEIPTS = EnumSet.of(ReceiptStatus.COMPLETED, ReceiptStatus.PARTIAL);
    private static final Set<OrderStatus> REOPENABLE_ORDERS =
            EnumSet.of(OrderStatus.PARTIALLY_RECEIVED, OrderStatus.RECEIVED, OrderStatus.COMPLETED);

    private final ReturnToVendorRepository returnRepository;
    private final ReturnToVendorMapper mapper;
    private final ReturnToVendorCodeGeneratorService codeGenerator;
    private final ApplicationEventPublisher eventPublisher;
    private final GoodsReceiptRepository goodsReceiptRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final MaterialRepository materialRepository;

    public ReturnToVendorService(ReturnToVendorRepository returnRepository,
                                 ReturnToVendorMapper mapper,
                                 ReturnToVendorCodeGeneratorService codeGenerator,
                                 ApplicationEventPublisher eventPublisher,
                                 GoodsReceiptRepository goodsReceiptRepository,
                                 PurchaseOrderRepository purchaseOrderRepository,
                                 MaterialRepository materialRepository) {
        this.returnRepository = returnRepository;
        this.mapper = mapper;
        this.codeGenerator = codeGenerator;
        this.eventPublisher = eventPublisher;
        this.goodsReceiptRepository = goodsReceiptRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.materialRepository = materialRepository;
    }

    // ============================================================
    // COMMANDS
    // ============================================================

    @Override
    @Retryable(retryFor = DataIntegrityViolationException.class, maxAttempts = 3, backoff = @Backoff(delay = 100))
    @Transactional
    public ReturnToVendorOutput create(CreateReturnToVendorInput request) {
        String userId = requireUser(request.getUserId());
        GoodsReceipt receipt = getReturnableReceipt(request.getGoodsReceiptId());
        Optional<PurchaseOrder> order = findPurchaseOrder(receipt.getPurchaseOrderId());

        ReturnToVendor returnToVendor = ReturnToVendor.builder()
                .returnCode(codeGenerator.generateCode())
                .goodsReceiptId(receipt.getId().toString())
                .goodsReceiptCode(receipt.getReceiptCode() != null ? receipt.getReceiptCode().getValue() : null)
                .purchaseOrderId(receipt.getPurchaseOrderId())
                .purchaseOrderCode(receipt.getPurchaseOrderCode())
                .supplierId(receipt.getSupplierId())
                .supplierName(receipt.getSupplierName())
                .supplierCode(order.map(PurchaseOrder::getSupplierCode).orElse(null))
                .currencyCode(order.map(PurchaseOrder::getCurrencyCode).orElseGet(() -> currencyOf(receipt)))
                .returnDate(request.getReturnDate())
                .returnReason(trimToNull(request.getReturnReason()))
                .rejectionSummary(trimToNull(request.getRejectionSummary()))
                .notes(trimToNull(request.getNotes()))
                .internalNotes(trimToNull(request.getInternalNotes()))
                .lines(buildLines(receipt, request.getLines(), null, userId))
                .createdBy(userId)
                .build();

        ReturnToVendor saved = returnRepository.save(returnToVendor);
        eventPublisher.publishEvent(new ReturnToVendorCreatedEvent(saved.getId(), codeOf(saved), userId));
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional
    public ReturnToVendorOutput update(UUID id, UpdateReturnToVendorInput request) {
        String userId = requireUser(request.getUserId());
        ReturnToVendor returnToVendor = getReturn(id);
        ensureModifiable(returnToVendor);
        GoodsReceipt receipt = getReturnableReceipt(returnToVendor.getGoodsReceiptId());

        if (request.getReturnReason() != null) {
            if (request.getReturnReason().isBlank()) {
                throw new ReturnToVendorValidationException("The return reason is required");
            }
            returnToVendor.setReturnReason(request.getReturnReason().trim());
        }
        if (request.getReturnDate() != null) returnToVendor.setReturnDate(request.getReturnDate());
        if (request.getRejectionSummary() != null) returnToVendor.setRejectionSummary(trimToNull(request.getRejectionSummary()));
        if (request.getNotes() != null) returnToVendor.setNotes(trimToNull(request.getNotes()));
        if (request.getInternalNotes() != null) returnToVendor.setInternalNotes(trimToNull(request.getInternalNotes()));
        if (request.getLines() != null && !request.getLines().isEmpty()) {
            returnToVendor.setLines(buildLines(receipt, request.getLines(), returnToVendor.getId(), userId));
        }
        returnToVendor.updateAudit(userId);
        return mapper.toResponse(returnRepository.save(returnToVendor));
    }

    @Override
    @Transactional
    public void delete(UUID id, String userId) {
        requireUser(userId);
        ReturnToVendor returnToVendor = getReturn(id);
        if (!returnToVendor.isModifiable()) {
            throw new ReturnToVendorNotModifiableException("Only a draft return can be deleted");
        }
        returnRepository.deleteById(id);
    }

    @Override
    @Transactional
    public ReturnToVendorOutput submit(UUID id, String userId) {
        requireUser(userId);
        ReturnToVendor returnToVendor = getReturn(id);
        returnToVendor.submit(userId);
        ReturnToVendor saved = returnRepository.save(returnToVendor);
        eventPublisher.publishEvent(new ReturnToVendorSubmittedEvent(saved.getId(), codeOf(saved), userId));
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional
    public ReturnToVendorOutput resolve(UUID id, String userId, ResolutionType resolutionType, String reference,
                                        String supplierResponse) {
        requireUser(userId);
        ReturnToVendor returnToVendor = getReturn(id);
        returnToVendor.resolve(userId, resolutionType, reference, supplierResponse);
        if (resolutionType == ResolutionType.REPLACEMENT) {
            expectReplacement(returnToVendor, userId);
        }
        ReturnToVendor saved = returnRepository.save(returnToVendor);
        eventPublisher.publishEvent(new ReturnToVendorResolvedEvent(saved.getId(), codeOf(saved), resolutionType, userId));
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional
    public ReturnToVendorOutput cancel(UUID id, String userId, String reason) {
        requireUser(userId);
        ReturnToVendor returnToVendor = getReturn(id);
        returnToVendor.cancel(userId, reason);
        ReturnToVendor saved = returnRepository.save(returnToVendor);
        eventPublisher.publishEvent(new ReturnToVendorCancelledEvent(saved.getId(), codeOf(saved), reason, userId));
        return mapper.toResponse(saved);
    }

    // ============================================================
    // QUERIES
    // ============================================================

    @Override
    public ReturnToVendorOutput getById(UUID id) {
        return mapper.toResponse(getReturn(id));
    }

    @Override
    public ReturnToVendorOutput getByCode(String code) {
        return mapper.toResponse(returnRepository.findByReturnCode(code)
                .orElseThrow(() -> new ReturnToVendorNotFoundException(code)));
    }

    @Override
    public List<ReturnToVendorOutput> getAll() {
        return mapper.toResponseList(returnRepository.findAll());
    }

    @Override
    public List<ReturnToVendorOutput> getByGoodsReceiptId(String goodsReceiptId) {
        return mapper.toResponseList(returnRepository.findByGoodsReceiptId(goodsReceiptId));
    }

    @Override
    public List<ReturnToVendorOutput> getByPurchaseOrderId(String purchaseOrderId) {
        return mapper.toResponseList(returnRepository.findByPurchaseOrderId(purchaseOrderId));
    }

    @Override
    public List<ReturnToVendorOutput> getBySupplierId(String supplierId) {
        return mapper.toResponseList(returnRepository.findBySupplierId(supplierId));
    }

    @Override
    public List<ReturnToVendorOutput> getByStatus(ReturnStatus status) {
        return mapper.toResponseList(returnRepository.findByStatus(status));
    }

    @Override
    public List<ReturnToVendorOutput> search(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return getAll();
        }
        return mapper.toResponseList(returnRepository.search(keyword.trim()));
    }

    // ============================================================
    // CHAIN RULES
    // ============================================================

    /**
     * Builds the lines from the goods receipt. Each requested line must be a rejected line of that receipt,
     * appear once, and not exceed what is still returnable: rejected minus what the receipt's other
     * non-cancelled returns hold.
     */
    private List<ReturnToVendorLine> buildLines(GoodsReceipt receipt, List<ReturnToVendorLineInput> inputs,
                                                UUID excludedReturnId, String userId) {
        if (inputs == null || inputs.isEmpty()) {
            throw new ReturnToVendorLineRequiredException("A return must contain at least one line");
        }

        Map<String, GoodsReceiptLine> receiptLines = receipt.getLines().stream()
                .filter(line -> line.getId() != null)
                .collect(Collectors.toMap(line -> line.getId().toString(), Function.identity()));
        Map<String, Integer> held = heldByOtherReturns(receipt.getId().toString(), excludedReturnId);
        Set<String> seen = new HashSet<>();
        List<ReturnToVendorLine> lines = new ArrayList<>();

        for (ReturnToVendorLineInput input : inputs) {
            String receiptLineId = input != null ? input.getGoodsReceiptLineId() : null;
            GoodsReceiptLine receiptLine = receiptLineId != null ? receiptLines.get(receiptLineId) : null;
            if (receiptLine == null) {
                throw new ReturnToVendorInvalidLineException("Each return line must be a line of goods receipt "
                        + codeOf(receipt));
            }
            if (!seen.add(receiptLineId)) {
                throw new ReturnToVendorInvalidLineException("Material " + receiptLine.getMaterialCode()
                        + " appears more than once in the return");
            }

            int rejected = receiptLine.getQuantityRejected() != null ? receiptLine.getQuantityRejected() : 0;
            if (rejected <= 0) {
                throw new ReturnToVendorInvalidLineException("Nothing was rejected for material "
                        + receiptLine.getMaterialCode() + " on goods receipt " + codeOf(receipt));
            }
            int available = Math.max(0, rejected - held.getOrDefault(receiptLineId, 0));
            Integer quantity = input.getQuantityToReturn();
            if (quantity == null || quantity <= 0) {
                throw new ReturnToVendorInvalidQuantityException("The quantity to return must be positive for material "
                        + receiptLine.getMaterialCode());
            }
            if (quantity > available) {
                throw new ReturnToVendorInvalidQuantityException("Only " + available + " " + unitOf(receiptLine)
                        + " of material " + receiptLine.getMaterialCode() + " can still be returned");
            }

            lines.add(ReturnToVendorLine.builder()
                    .lineNumber(lines.size() + 1)
                    .goodsReceiptLineId(receiptLineId)
                    .purchaseOrderLineId(receiptLine.getPurchaseOrderLineId())
                    .materialId(receiptLine.getMaterialId() != null ? receiptLine.getMaterialId().toString() : null)
                    .materialCode(receiptLine.getMaterialCode())
                    .materialName(receiptLine.getMaterialName() != null ? receiptLine.getMaterialName() : receiptLine.getMaterialCode())
                    .unitOfMeasure(receiptLine.getUnitOfMeasure())
                    .rejectedQuantity(rejected)
                    .quantityToReturn(quantity)
                    .unitPrice(receiptLine.getUnitPrice() != null ? receiptLine.getUnitPrice().getAmount() : null)
                    .rejectionReason(firstNonBlank(input.getRejectionReason(), receiptLine.getRejectionReason(), DEFAULT_LINE_REASON))
                    .qualityNotes(firstNonBlank(input.getQualityNotes(), receiptLine.getQualityNotes(), null))
                    .defectDescription(trimToNull(input.getDefectDescription()))
                    .notes(trimToNull(input.getNotes()))
                    .createdBy(userId)
                    .build());
        }
        return lines;
    }

    /** Quantity per goods-receipt line already held by the receipt's other returns that are not cancelled. */
    private Map<String, Integer> heldByOtherReturns(String goodsReceiptId, UUID excludedReturnId) {
        return returnRepository.findByGoodsReceiptId(goodsReceiptId).stream()
                .filter(other -> !other.getId().equals(excludedReturnId))
                .filter(ReturnToVendor::holdsQuantities)
                .flatMap(other -> other.getLines().stream())
                .filter(line -> line.getGoodsReceiptLineId() != null && line.getQuantityToReturn() != null)
                .collect(Collectors.groupingBy(ReturnToVendorLine::getGoodsReceiptLineId,
                        Collectors.summingInt(ReturnToVendorLine::getQuantityToReturn)));
    }

    /**
     * The supplier replaces the goods: the purchase order expects them again. It is reopened for receipt and the
     * returned quantity goes back on order for each material.
     */
    private void expectReplacement(ReturnToVendor returnToVendor, String userId) {
        PurchaseOrder order = findPurchaseOrder(returnToVendor.getPurchaseOrderId())
                .orElseThrow(() -> new ReturnToVendorBusinessException(
                        "The purchase order of this return no longer exists, so no replacement can be received",
                        "RETURN_TO_VENDOR_RULE_VIOLATION"));
        if (!REOPENABLE_ORDERS.contains(order.getStatus())) {
            throw new ReturnToVendorBusinessException("Purchase order " + returnToVendor.getPurchaseOrderCode()
                    + " is " + order.getStatus() + " and cannot receive a replacement", "RETURN_TO_VENDOR_RULE_VIOLATION");
        }
        order.reopenForReplacement(userId);
        purchaseOrderRepository.save(order);

        for (ReturnToVendorLine line : returnToVendor.getLines()) {
            findMaterial(line).ifPresent(material -> {
                material.addStockOnOrder(line.getQuantityToReturn());
                material.setUpdatedBy(userId);
                materialRepository.save(material);
            });
        }
    }

    private Optional<Material> findMaterial(ReturnToVendorLine line) {
        Optional<Material> byId = parseUuid(line.getMaterialId()).flatMap(materialRepository::findById);
        if (byId.isPresent()) {
            return byId;
        }
        return line.getMaterialCode() != null ? materialRepository.findByCode(line.getMaterialCode()) : Optional.empty();
    }

    // ============================================================
    // HELPERS
    // ============================================================

    private GoodsReceipt getReturnableReceipt(String goodsReceiptId) {
        GoodsReceipt receipt = parseUuid(goodsReceiptId)
                .flatMap(goodsReceiptRepository::findById)
                .orElseThrow(() -> new ReturnToVendorValidationException("Goods receipt not found: " + goodsReceiptId));
        if (!RETURNABLE_RECEIPTS.contains(receipt.getStatus())) {
            throw new ReturnToVendorBusinessException("Only a completed goods receipt can have its rejected goods returned",
                    "RETURN_TO_VENDOR_RULE_VIOLATION");
        }
        return receipt;
    }

    private Optional<PurchaseOrder> findPurchaseOrder(String purchaseOrderId) {
        return parseUuid(purchaseOrderId).flatMap(purchaseOrderRepository::findById);
    }

    private ReturnToVendor getReturn(UUID id) {
        return returnRepository.findById(id).orElseThrow(() -> new ReturnToVendorNotFoundException(id));
    }

    private static void ensureModifiable(ReturnToVendor returnToVendor) {
        if (!returnToVendor.isModifiable()) {
            throw new ReturnToVendorNotModifiableException("Only a draft return can be modified");
        }
    }

    private static String requireUser(String userId) {
        if (userId == null || userId.isBlank()) {
            throw new AccessDeniedException("An authenticated user is required");
        }
        return userId;
    }

    private static String currencyOf(GoodsReceipt receipt) {
        return receipt.getLines().stream()
                .map(GoodsReceiptLine::getUnitPrice)
                .filter(price -> price != null)
                .map(price -> price.getCurrencyCode())
                .findFirst()
                .orElse(null);
    }

    private static String codeOf(ReturnToVendor returnToVendor) {
        return returnToVendor.getReturnCode() != null ? returnToVendor.getReturnCode().getValue() : null;
    }

    private static String codeOf(GoodsReceipt receipt) {
        return receipt.getReceiptCode() != null ? receipt.getReceiptCode().getValue() : String.valueOf(receipt.getId());
    }

    private static String unitOf(GoodsReceiptLine line) {
        return line.getUnitOfMeasure() != null ? line.getUnitOfMeasure() : "unit(s)";
    }

    private static Optional<UUID> parseUuid(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(UUID.fromString(value.trim()));
        } catch (IllegalArgumentException exception) {
            return Optional.empty();
        }
    }

    private static String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private static String firstNonBlank(String first, String second, String fallback) {
        if (first != null && !first.isBlank()) return first.trim();
        if (second != null && !second.isBlank()) return second.trim();
        return fallback;
    }
}
