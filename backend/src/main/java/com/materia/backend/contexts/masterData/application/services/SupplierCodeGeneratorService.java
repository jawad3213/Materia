package com.materia.backend.contexts.masterData.application.services;

import com.materia.backend.contexts.masterData.domain.ports.out.CodeSequenceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Year;

@Service
@Transactional
public class SupplierCodeGeneratorService {

    private final CodeSequenceRepository sequenceRepository;
    private static final String PREFIX_SUPPLIER = "SUP";

    public SupplierCodeGeneratorService(CodeSequenceRepository sequenceRepository) {
        this.sequenceRepository = sequenceRepository;
    }

    /**
     * Generates a code automatically for supplier including generation year
     */
    public String generateCode() {
        int currentYear = Year.now().getValue();
        String sequenceKey = PREFIX_SUPPLIER + "-" + currentYear;
        int nextNumber = sequenceRepository.getNextValueAndIncrement(sequenceKey);
        return String.format("%s-%d-%04d", PREFIX_SUPPLIER, currentYear, nextNumber);
    }
}
