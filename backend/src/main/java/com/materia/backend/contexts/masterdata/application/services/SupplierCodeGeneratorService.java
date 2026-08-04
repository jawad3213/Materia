package com.materia.backend.contexts.masterData.application.services;

import com.materia.backend.contexts.masterData.domain.ports.out.CodeSequenceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class SupplierCodeGeneratorService {

    private final CodeSequenceRepository sequenceRepository;
    private static final String PREFIX_SUPPLIER = "SUP";

    public SupplierCodeGeneratorService(CodeSequenceRepository sequenceRepository) {
        this.sequenceRepository = sequenceRepository;
    }

    /**
     * Generates a code automatically for supplier
     */
    public String generateCode() {
        int nextNumber = sequenceRepository.getNextValueAndIncrement(PREFIX_SUPPLIER);
        return String.format("%s-%04d", PREFIX_SUPPLIER, nextNumber);
    }
}
