package com.materia.backend.contexts.masterData.application.services;

import com.materia.backend.contexts.masterData.domain.enums.MaterialType;
import com.materia.backend.contexts.masterData.domain.valueObjects.MaterialCode;
import com.materia.backend.contexts.masterData.domain.ports.out.CodeSequenceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class MaterialCodeGeneratorService {

    private final CodeSequenceRepository sequenceRepository;

    public MaterialCodeGeneratorService(CodeSequenceRepository sequenceRepository) {
        this.sequenceRepository = sequenceRepository;
    }

    /**
     * Generates a code automatically based on MaterialType
     */
    public MaterialCode generateCode(MaterialType type) {
        String prefix = getPrefix(type);
        int nextNumber = sequenceRepository.getNextValueAndIncrement(prefix);
        String codeValue = String.format("%s-%04d", prefix, nextNumber);
        return MaterialCode.of(codeValue);
    }

    /**
     * Returns the prefix based on MaterialType
     */
    private String getPrefix(MaterialType type) {
        if (type == null) return MaterialCode.DEFAULT_PREFIX;
        return type.getPrefix();
    }
}
