package com.materia.backend.contexts.masterData.application.services;

import com.materia.backend.contexts.masterData.domain.ports.out.CodeSequenceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CategoryCodeGeneratorService {

    private final CodeSequenceRepository sequenceRepository;
    private static final String PREFIX_CATEGORY = "CAT";

    public CategoryCodeGeneratorService(CodeSequenceRepository sequenceRepository) {
        this.sequenceRepository = sequenceRepository;
    }

    /**
     * Generates a code automatically for category
     */
    public String generateCode() {
        int nextNumber = sequenceRepository.getNextValueAndIncrement(PREFIX_CATEGORY);
        return String.format("%s-%04d", PREFIX_CATEGORY, nextNumber);
    }
}
