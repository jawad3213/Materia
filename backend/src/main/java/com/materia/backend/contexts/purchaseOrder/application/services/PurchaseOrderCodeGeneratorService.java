package com.materia.backend.contexts.purchaseOrder.application.services;

import com.materia.backend.contexts.masterData.domain.ports.out.CodeSequenceRepository;
import com.materia.backend.contexts.purchaseOrder.domain.valueObjects.OrderCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Generates stable purchase order business codes.
 */
@Service
@Transactional
public class PurchaseOrderCodeGeneratorService {

    private static final String PREFIX = "PO";

    private final CodeSequenceRepository sequenceRepository;

    public PurchaseOrderCodeGeneratorService(CodeSequenceRepository sequenceRepository) {
        this.sequenceRepository = sequenceRepository;
    }

    public OrderCode generateCode() {
        int nextNumber = sequenceRepository.getNextValueAndIncrement(PREFIX);
        return OrderCode.fromPrefixAndNumber(PREFIX, nextNumber);
    }
}
