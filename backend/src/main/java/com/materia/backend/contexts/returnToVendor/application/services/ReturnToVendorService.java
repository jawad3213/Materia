package com.materia.backend.contexts.returnToVendor.application.services;

import com.materia.backend.common.application.AbstractCrudApplicationService;
import com.materia.backend.contexts.returnToVendor.application.dtos.CreateReturnToVendorInput;
import com.materia.backend.contexts.returnToVendor.application.dtos.ReturnToVendorLineInput;
import com.materia.backend.contexts.returnToVendor.application.dtos.ReturnToVendorOutput;
import com.materia.backend.contexts.returnToVendor.application.dtos.UpdateReturnToVendorInput;
import com.materia.backend.contexts.returnToVendor.application.mappers.ReturnToVendorMapper;
import com.materia.backend.contexts.returnToVendor.domain.entities.ReturnToVendor;
import com.materia.backend.contexts.returnToVendor.domain.entities.ReturnToVendorLine;
import com.materia.backend.contexts.returnToVendor.domain.enums.ResolutionType;
import com.materia.backend.contexts.returnToVendor.domain.enums.ReturnStatus;
import com.materia.backend.contexts.returnToVendor.domain.exceptions.ReturnToVendorInvalidLineException;
import com.materia.backend.contexts.returnToVendor.domain.exceptions.ReturnToVendorLineRequiredException;
import com.materia.backend.contexts.returnToVendor.domain.exceptions.ReturnToVendorNotFoundException;
import com.materia.backend.contexts.returnToVendor.domain.exceptions.ReturnToVendorNotModifiableException;
import com.materia.backend.contexts.returnToVendor.domain.exceptions.ReturnToVendorValidationException;
import com.materia.backend.contexts.returnToVendor.domain.events.ReturnToVendorCancelledEvent;
import com.materia.backend.contexts.returnToVendor.domain.events.ReturnToVendorCreatedEvent;
import com.materia.backend.contexts.returnToVendor.domain.events.ReturnToVendorResolvedEvent;
import com.materia.backend.contexts.returnToVendor.domain.events.ReturnToVendorSubmittedEvent;
import com.materia.backend.contexts.returnToVendor.domain.ports.in.ReturnToVendorUseCase;
import com.materia.backend.contexts.returnToVendor.domain.ports.out.ReturnToVendorRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@ConditionalOnBean(ReturnToVendorRepository.class)
public class ReturnToVendorService extends AbstractCrudApplicationService<
        ReturnToVendor,
        CreateReturnToVendorInput,
        UpdateReturnToVendorInput,
        ReturnToVendorOutput> implements ReturnToVendorUseCase {

    private final ReturnToVendorRepository returnToVendorRepository;
    private final ReturnToVendorMapper returnToVendorMapper;
    private final ReturnToVendorCodeGeneratorService codeGenerator;
    private final ApplicationEventPublisher eventPublisher;

    public ReturnToVendorService(ReturnToVendorRepository returnToVendorRepository,
                                 ReturnToVendorMapper returnToVendorMapper,
                                 ReturnToVendorCodeGeneratorService codeGenerator,
                                 ApplicationEventPublisher eventPublisher) {
        super(returnToVendorRepository, returnToVendorMapper);
        this.returnToVendorRepository = returnToVendorRepository;
        this.returnToVendorMapper = returnToVendorMapper;
        this.codeGenerator = codeGenerator;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Retryable(retryFor = DataIntegrityViolationException.class, maxAttempts = 3, backoff = @Backoff(delay = 100))
    @Transactional
    public ReturnToVendorOutput create(CreateReturnToVendorInput request) {
        String returnCode = resolveReturnCode(request);
        ReturnToVendor returnToVendor = returnToVendorMapper.toEntity(request);
        returnToVendorMapper.updateReturnCode(returnToVendor, returnCode);
        normalizeLines(returnToVendor.getLines());
        ReturnToVendor saved = saveEntity(returnToVendor);
        eventPublisher.publishEvent(new ReturnToVendorCreatedEvent(
                saved.getId(),
                saved.getReturnCode() != null ? saved.getReturnCode().getValue() : null,
                request.getUserId()));
        return toResponse(saved);
    }

    @Override
    @Transactional
    public ReturnToVendorOutput update(UUID id, CreateReturnToVendorInput request) {
        return update(id, toUpdateRequest(request));
    }

    @Override
    @Transactional
    public ReturnToVendorOutput update(UUID id, UpdateReturnToVendorInput request) {
        ReturnToVendor returnToVendor = getReturnToVendorById(id);
        if (!returnToVendor.isModifiable()) {
            throw new ReturnToVendorNotModifiableException("This return to vendor can no longer be modified");
        }

        returnToVendorMapper.updateEntity(returnToVendor, request);
        normalizeLines(returnToVendor.getLines());
        returnToVendor.setUpdatedAt(LocalDateTime.now());
        returnToVendor.setUpdatedBy(request.getUserId());
        return toResponse(saveEntity(returnToVendor));
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        ReturnToVendor returnToVendor = getReturnToVendorById(id);
        if (!returnToVendor.isModifiable()) {
            throw new ReturnToVendorNotModifiableException("This return to vendor can no longer be deleted");
        }
        returnToVendorRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void delete(UUID id, String userId) {
        delete(id);
    }

    @Override
    public ReturnToVendorOutput getById(UUID id) {
        return toResponse(getReturnToVendorById(id));
    }

    @Override
    public ReturnToVendorOutput getByCode(String code) {
        return toResponse(returnToVendorRepository.findByReturnCode(code)
                .orElseThrow(() -> new ReturnToVendorNotFoundException(code)));
    }

    @Override
    public List<ReturnToVendorOutput> getAll() {
        return getAllResponses();
    }

    @Override
    public List<ReturnToVendorOutput> getByGoodsReceiptId(String goodsReceiptId) {
        return toResponseList(returnToVendorRepository.findByGoodsReceiptId(goodsReceiptId));
    }

    @Override
    public List<ReturnToVendorOutput> getByPurchaseOrderId(String purchaseOrderId) {
        return toResponseList(returnToVendorRepository.findByPurchaseOrderId(purchaseOrderId));
    }

    @Override
    public List<ReturnToVendorOutput> getBySupplierId(String supplierId) {
        return toResponseList(returnToVendorRepository.findBySupplierId(supplierId));
    }

    @Override
    public List<ReturnToVendorOutput> getByStatus(ReturnStatus status) {
        return toResponseList(returnToVendorRepository.findByStatus(status));
    }

    @Override
    public List<ReturnToVendorOutput> search(String keyword) {
        return toResponseList(returnToVendorRepository.search(keyword));
    }

    @Override
    @Transactional
    public ReturnToVendorOutput addLine(UUID id, ReturnToVendorLineInput line, String userId) {
        ReturnToVendor returnToVendor = getReturnToVendorById(id);
        ensureModifiable(returnToVendor);
        returnToVendor.getLines().add(returnToVendorMapper.toLineEntity(line));
        normalizeLines(returnToVendor.getLines());
        returnToVendor.updateAudit(userId);
        return toResponse(saveEntity(returnToVendor));
    }

    @Override
    @Transactional
    public ReturnToVendorOutput removeLine(UUID id, int lineIndex, String userId) {
        ReturnToVendor returnToVendor = getReturnToVendorById(id);
        ensureModifiable(returnToVendor);
        if (returnToVendor.getLines().size() <= 1) {
            throw new ReturnToVendorLineRequiredException("A return to vendor must contain at least one line");
        }
        if (lineIndex < 0 || lineIndex >= returnToVendor.getLines().size()) {
            throw new ReturnToVendorInvalidLineException("Return-to-vendor line index is invalid");
        }

        returnToVendor.getLines().remove(lineIndex);
        normalizeLines(returnToVendor.getLines());
        returnToVendor.updateAudit(userId);
        return toResponse(saveEntity(returnToVendor));
    }

    @Override
    @Transactional
    public ReturnToVendorOutput submit(UUID id, String userId) {
        ReturnToVendor returnToVendor = getReturnToVendorById(id);
        normalizeLines(returnToVendor.getLines());
        returnToVendor.submit(userId);
        ReturnToVendor saved = saveEntity(returnToVendor);
        eventPublisher.publishEvent(new ReturnToVendorSubmittedEvent(
                saved.getId(),
                saved.getReturnCode() != null ? saved.getReturnCode().getValue() : null,
                userId));
        return toResponse(saved);
    }

    @Override
    @Transactional
    public ReturnToVendorOutput resolve(UUID id, String userId, ResolutionType resolutionType, String reference) {
        if (resolutionType == null) {
            throw new ReturnToVendorValidationException("Resolution type is required");
        }
        if (reference == null || reference.isBlank()) {
            throw new ReturnToVendorValidationException("Resolution reference is required");
        }

        ReturnToVendor returnToVendor = getReturnToVendorById(id);
        returnToVendor.resolve(userId, resolutionType, reference);
        ReturnToVendor saved = saveEntity(returnToVendor);
        eventPublisher.publishEvent(new ReturnToVendorResolvedEvent(
                saved.getId(),
                saved.getReturnCode() != null ? saved.getReturnCode().getValue() : null,
                resolutionType,
                userId));
        return toResponse(saved);
    }

    @Override
    @Transactional
    public ReturnToVendorOutput cancel(UUID id, String userId, String reason) {
        if (reason == null || reason.isBlank()) {
            throw new ReturnToVendorValidationException("Cancellation reason is required");
        }

        ReturnToVendor returnToVendor = getReturnToVendorById(id);
        returnToVendor.cancel(userId, reason);
        ReturnToVendor saved = saveEntity(returnToVendor);
        eventPublisher.publishEvent(new ReturnToVendorCancelledEvent(
                saved.getId(),
                saved.getReturnCode() != null ? saved.getReturnCode().getValue() : null,
                reason,
                userId));
        return toResponse(saved);
    }

    private ReturnToVendor getReturnToVendorById(UUID id) {
        return getEntityByIdOrThrow(id, () -> new ReturnToVendorNotFoundException(id));
    }

    private String resolveReturnCode(CreateReturnToVendorInput request) {
        if (request.getReturnCode() != null && !request.getReturnCode().isBlank()) {
            String code = request.getReturnCode().trim();
            if (returnToVendorRepository.existsByReturnCode(code)) {
                throw new ReturnToVendorValidationException("A return to vendor already uses this code");
            }
            return code;
        }
        return codeGenerator.generateCode().getValue();
    }

    private void normalizeLines(List<ReturnToVendorLine> lines) {
        if (lines == null || lines.isEmpty()) {
            throw new ReturnToVendorLineRequiredException("A return to vendor must contain at least one line");
        }

        for (int index = 0; index < lines.size(); index++) {
            ReturnToVendorLine line = lines.get(index);
            if (line == null) {
                throw new ReturnToVendorInvalidLineException("Return-to-vendor line cannot be null");
            }
            if (line.getId() == null) {
                line.setId(UUID.randomUUID());
            }
            line.setLineNumber(index + 1);
            if (line.getQuantityAlreadyReturned() == null) {
                line.setQuantityAlreadyReturned(0);
            }
        }
    }

    private void ensureModifiable(ReturnToVendor returnToVendor) {
        if (!returnToVendor.isModifiable()) {
            throw new ReturnToVendorNotModifiableException("This return to vendor can no longer be modified");
        }
    }

    private UpdateReturnToVendorInput toUpdateRequest(CreateReturnToVendorInput request) {
        UpdateReturnToVendorInput update = new UpdateReturnToVendorInput();
        update.setReturnDate(request.getReturnDate());
        update.setReturnReason(request.getReturnReason());
        update.setRejectionSummary(request.getRejectionSummary());
        update.setNotes(request.getNotes());
        update.setInternalNotes(request.getInternalNotes());
        update.setLines(request.getLines());
        update.setUserId(request.getUserId());
        return update;
    }
}
