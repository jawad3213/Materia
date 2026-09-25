package com.materia.backend.contexts.purchaseRequisition.application.services;

import com.materia.backend.contexts.masterData.domain.ports.out.CodeSequenceRepository;
import com.materia.backend.contexts.purchaseRequisition.domain.valueObjects.RequisitionCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Year;

/**
 * Generates stable purchase requisition business codes.
 */
@Service
@Transactional
public class RequisitionCodeGeneratorService {

    private static final String PREFIX = "REQ";

    private final CodeSequenceRepository sequenceRepository;

    public RequisitionCodeGeneratorService(CodeSequenceRepository sequenceRepository) {
        this.sequenceRepository = sequenceRepository;
    }

    public RequisitionCode generateCode() {
        int currentYear = Year.now().getValue();
        String sequenceKey = PREFIX + "-" + currentYear;
        int nextNumber = sequenceRepository.getNextValueAndIncrement(sequenceKey);
        return RequisitionCode.fromPrefixYearAndNumber(PREFIX, currentYear, nextNumber);
    }
}
