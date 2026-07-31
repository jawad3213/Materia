package com.materia.backend.contexts.masterdata.application.services;

import com.materia.backend.contexts.masterdata.domain.enums.MaterialType;
import com.materia.backend.contexts.masterdata.domain.valueObjects.MaterialCode;
import com.materia.backend.contexts.masterdata.domain.ports.out.MaterialRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class MaterialCodeGeneratorService {

    private final MaterialRepository repository;

    public MaterialCodeGeneratorService(MaterialRepository repository) {
        this.repository = repository;
    }

    /**
     * Generates a code automatically based on MaterialType
     */
    public MaterialCode generateCode(MaterialType type) {
        String prefix = getPrefix(type);
        int nextNumber = getNextSequenceNumber(prefix);
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

    /**
     * Gets the next sequence number for the prefix
     */
    private int getNextSequenceNumber(String prefix) {
        var codes = repository.findCodesByPrefix(prefix);
        
        if (codes.isEmpty()) {
            return 1;
        }
        
        return codes.stream()
            .map(code -> code.replace(prefix + "-", ""))
            .filter(s -> s.matches("\\d{4}"))
            .mapToInt(Integer::parseInt)
            .max()
            .orElse(0) + 1;
    }
}
