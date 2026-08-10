package com.materia.backend.contexts.returnToVendor.application.services;

import com.materia.backend.contexts.masterData.domain.ports.out.CodeSequenceRepository;
import com.materia.backend.contexts.returnToVendor.domain.valueObjects.ReturnCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ReturnToVendorCodeGeneratorService {

    private static final String PREFIX = "RTN";

    private final CodeSequenceRepository sequenceRepository;

    public ReturnToVendorCodeGeneratorService(CodeSequenceRepository sequenceRepository) {
        this.sequenceRepository = sequenceRepository;
    }

    public ReturnCode generateCode() {
        int nextNumber = sequenceRepository.getNextValueAndIncrement(PREFIX);
        return ReturnCode.fromPrefixAndNumber(PREFIX, nextNumber);
    }
}
