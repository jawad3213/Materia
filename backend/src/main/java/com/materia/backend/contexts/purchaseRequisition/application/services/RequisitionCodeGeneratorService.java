package com.materia.backend.contexts.purchaseRequisition.application.services;

import com.materia.backend.contexts.purchaseRequisition.domain.ports.out.RequisitionCodeSequenceRepository;
import com.materia.backend.contexts.purchaseRequisition.domain.valueObjects.RequisitionCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Generates stable purchase requisition business codes.
 */
@Service
@Transactional
public class RequisitionCodeGeneratorService {

    private static final String PREFIX = "REQ";

    private final RequisitionCodeSequenceRepository sequenceRepository;

    public RequisitionCodeGeneratorService(RequisitionCodeSequenceRepository sequenceRepository) {
        this.sequenceRepository = sequenceRepository;
    }

    public RequisitionCode generateCode() {
        int nextNumber = sequenceRepository.getNextValueAndIncrement(PREFIX);
        return RequisitionCode.fromPrefixAndNumber(PREFIX, nextNumber);
    }
}
