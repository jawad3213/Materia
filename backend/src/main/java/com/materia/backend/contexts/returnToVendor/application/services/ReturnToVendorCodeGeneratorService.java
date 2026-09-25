package com.materia.backend.contexts.returnToVendor.application.services;

import com.materia.backend.contexts.masterData.domain.ports.out.CodeSequenceRepository;
import com.materia.backend.contexts.returnToVendor.domain.valueObjects.ReturnCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Year;

@Service
@Transactional
public class ReturnToVendorCodeGeneratorService {

    private static final String PREFIX = "RTN";

    private final CodeSequenceRepository sequenceRepository;

    public ReturnToVendorCodeGeneratorService(CodeSequenceRepository sequenceRepository) {
        this.sequenceRepository = sequenceRepository;
    }

    public ReturnCode generateCode() {
        int currentYear = Year.now().getValue();
        String sequenceKey = PREFIX + "-" + currentYear;
        int nextNumber = sequenceRepository.getNextValueAndIncrement(sequenceKey);
        return ReturnCode.fromPrefixYearAndNumber(PREFIX, currentYear, nextNumber);
    }
}
