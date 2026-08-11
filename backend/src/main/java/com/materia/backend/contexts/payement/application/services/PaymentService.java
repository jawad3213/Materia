package com.materia.backend.contexts.payement.application.services;

import com.materia.backend.common.application.AbstractCrudApplicationService;
import com.materia.backend.contexts.payement.application.dtos.CreatePaymentInput;
import com.materia.backend.contexts.payement.application.dtos.PaymentOutput;
import com.materia.backend.contexts.payement.application.dtos.UpdatePaymentInput;
import com.materia.backend.contexts.payement.application.mappers.PaymentMapper;
import com.materia.backend.contexts.payement.domain.entities.Payment;
import com.materia.backend.contexts.payement.domain.entities.PaymentLine;
import com.materia.backend.contexts.payement.domain.enums.PaymentStatus;
import com.materia.backend.contexts.payement.domain.exceptions.PaymentNotFoundException;
import com.materia.backend.contexts.payement.domain.exceptions.PaymentNotModifiableException;
import com.materia.backend.contexts.payement.domain.exceptions.PaymentValidationException;
import com.materia.backend.contexts.payement.domain.ports.in.PaymentUseCase;
import com.materia.backend.contexts.payement.domain.ports.out.PaymentPort;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class PaymentService extends AbstractCrudApplicationService<
        Payment, CreatePaymentInput, UpdatePaymentInput, PaymentOutput> implements PaymentUseCase {

    private final PaymentPort paymentPort;
    private final PaymentMapper paymentMapper;
    private final PaymentCodeGeneratorService codeGeneratorService;

    public PaymentService(PaymentPort paymentPort, PaymentMapper paymentMapper, PaymentCodeGeneratorService codeGeneratorService) {
        super(paymentPort, paymentMapper);
        this.paymentPort = paymentPort;
        this.paymentMapper = paymentMapper;
        this.codeGeneratorService = codeGeneratorService;
    }

    @Override
    @Retryable(retryFor = DataIntegrityViolationException.class, maxAttempts = 3, backoff = @Backoff(delay = 100))
    @Transactional
    public PaymentOutput create(CreatePaymentInput request) {
        Payment payment = paymentMapper.toEntity(request);
        if (payment.getPaymentCode() == null) {
            payment.setPaymentCode(codeGeneratorService.generateCode());
        }
        normalizeLines(payment.getLines(), payment.getCurrencyCode());
        return toResponse(saveEntity(payment));
    }

    @Override
    public PaymentOutput update(UUID id, CreatePaymentInput request) {
        UpdatePaymentInput updateRequest = new UpdatePaymentInput();
        updateRequest.setNotes(request.getNotes());
        updateRequest.setInternalNotes(request.getInternalNotes());
        updateRequest.setUserId(request.getUserId());
        return update(id, updateRequest);
    }

    @Override
    @Transactional
    public PaymentOutput update(UUID id, UpdatePaymentInput request) {
        Payment payment = getPaymentById(id);
        if (!payment.isModifiable()) {
            throw new PaymentNotModifiableException(payment.getId().toString(), payment.getStatus().name());
        }

        paymentMapper.updateEntity(payment, request);
        payment.setUpdatedAt(LocalDateTime.now());
        if (request instanceof com.materia.backend.common.application.BaseInput) {
            payment.setUpdatedBy(request.getUserId());
        }
        return toResponse(saveEntity(payment));
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        Payment payment = getPaymentById(id);
        if (!payment.isModifiable()) {
            throw new PaymentNotModifiableException(payment.getId().toString(), payment.getStatus().name());
        }
        paymentPort.deleteById(id);
    }

    @Override
    public PaymentOutput getById(UUID id) {
        return toResponse(getPaymentById(id));
    }

    @Override
    public PaymentOutput getByCode(String code) {
        return toResponse(
                paymentPort.findByCode(code)
                        .orElseThrow(() -> new PaymentNotFoundException("Code", code))
        );
    }

    @Override
    public List<PaymentOutput> getAll() {
        return getAllResponses();
    }

    @Override
    public List<PaymentOutput> getByStatus(String status) {
        PaymentStatus paymentStatus = PaymentStatus.valueOf(status.toUpperCase());
        return toResponseList(paymentPort.findByStatus(paymentStatus));
    }

    @Override
    public List<PaymentOutput> getBySupplierId(String supplierId) {
        return toResponseList(paymentPort.findBySupplierId(supplierId));
    }

    @Override
    public List<PaymentOutput> searchByKeyword(String keyword) {
        return toResponseList(paymentPort.searchByKeyword(keyword));
    }

    @Override
    @Transactional
    public PaymentOutput prepare(UUID id, String userId) {
        Payment payment = getPaymentById(id);
        payment.prepare(userId);
        return toResponse(saveEntity(payment));
    }

    @Override
    @Transactional
    public PaymentOutput complete(UUID id, String userId, String bankReference, String transactionId, String paymentMethod) {
        Payment payment = getPaymentById(id);
        payment.markAsCompleted(userId, bankReference, transactionId, paymentMethod);
        
        // Mark all lines as paid when the payment is completed
        if (payment.getLines() != null) {
            payment.getLines().forEach(PaymentLine::markAsPaid);
        }
        
        return toResponse(saveEntity(payment));
    }

    @Override
    @Transactional
    public PaymentOutput cancel(UUID id, String userId, String reason) {
        Payment payment = getPaymentById(id);
        payment.cancel(userId, reason);
        return toResponse(saveEntity(payment));
    }

    private Payment getPaymentById(UUID id) {
        return getEntityByIdOrThrow(id, () -> new PaymentNotFoundException(id.toString()));
    }

    private void normalizeLines(List<PaymentLine> lines, String currencyCode) {
        if (lines == null || lines.isEmpty()) {
            throw new PaymentValidationException("Un paiement doit contenir au moins une ligne (facture)");
        }

        for (int i = 0; i < lines.size(); i++) {
            PaymentLine line = lines.get(i);
            if (line == null) {
                throw new PaymentValidationException("La ligne de paiement " + (i + 1) + " ne peut pas être nulle");
            }

            if (line.getId() == null) {
                line.setId(UUID.randomUUID());
            }

            line.setLineNumber(i + 1);
            if (line.getCurrencyCode() == null) {
                line.setCurrencyCode(currencyCode);
            }
        }
    }
}
