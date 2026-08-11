package com.materia.backend.contexts.payement.domain.ports.in;

import com.materia.backend.common.domain.BaseUseCase;
import com.materia.backend.contexts.payement.application.dtos.CreatePaymentInput;
import com.materia.backend.contexts.payement.application.dtos.PaymentOutput;
import com.materia.backend.contexts.payement.application.dtos.UpdatePaymentInput;

import java.util.List;
import java.util.UUID;

/**
 * Input port for payment use cases.
 */
public interface PaymentUseCase extends BaseUseCase<CreatePaymentInput, PaymentOutput, UUID> {

    PaymentOutput update(UUID id, UpdatePaymentInput request);

    List<PaymentOutput> getByStatus(String status);

    List<PaymentOutput> getBySupplierId(String supplierId);

    List<PaymentOutput> searchByKeyword(String keyword);

    PaymentOutput prepare(UUID id, String userId);

    PaymentOutput complete(UUID id, String userId, String bankReference, String transactionId, String paymentMethod);

    PaymentOutput cancel(UUID id, String userId, String reason);
}
