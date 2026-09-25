package com.materia.backend.contexts.masterData.application.services;

import com.materia.backend.contexts.masterData.domain.enums.MaterialType;
import com.materia.backend.contexts.masterData.domain.valueObjects.MaterialCode;
import com.materia.backend.contexts.masterData.domain.ports.out.CodeSequenceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Year;

@Service
@Transactional
public class MaterialCodeGeneratorService {

    private final CodeSequenceRepository sequenceRepository;

    public MaterialCodeGeneratorService(CodeSequenceRepository sequenceRepository) {
        this.sequenceRepository = sequenceRepository;
    }

    /**
     * Generates a code automatically based on MaterialType including the generation year
     */
    public MaterialCode generateCode(MaterialType type) {
        String prefix = getPrefix(type);
        int currentYear = Year.now().getValue();
        String sequenceKey = prefix + "-" + currentYear;
        int nextNumber = sequenceRepository.getNextValueAndIncrement(sequenceKey);
        return MaterialCode.fromPrefixYearAndNumber(prefix, currentYear, nextNumber);
    }

    /**
     * Returns the prefix based on MaterialType
     */
    private String getPrefix(MaterialType type) {
        if (type == null) return MaterialCode.DEFAULT_PREFIX;
        return type.getPrefix();
    }
}
