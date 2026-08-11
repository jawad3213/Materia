package com.materia.backend.contexts.payement.application.services;

import com.materia.backend.contexts.masterData.domain.ports.out.CodeSequenceRepository;
import com.materia.backend.contexts.payement.domain.valueObjects.PaymentCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Generates stable payment business codes.
 */
@Service
@Transactional
public class PaymentCodeGeneratorService {

    private static final String PREFIX = "PAY";

    private final CodeSequenceRepository sequenceRepository;

    public PaymentCodeGeneratorService(CodeSequenceRepository sequenceRepository) {
        this.sequenceRepository = sequenceRepository;
    }

    public PaymentCode generateCode() {
        int nextNumber = sequenceRepository.getNextValueAndIncrement(PREFIX);
        return PaymentCode.fromPrefixAndNumber(PREFIX, nextNumber);
    }
}
