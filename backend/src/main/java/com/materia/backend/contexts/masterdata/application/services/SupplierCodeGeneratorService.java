package com.materia.backend.contexts.masterdata.application.services;

import com.materia.backend.contexts.masterdata.domain.ports.out.SupplierRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class SupplierCodeGeneratorService {

    private final SupplierRepository repository;
    private static final String PREFIX_SUPPLIER = "SUP";

    public SupplierCodeGeneratorService(SupplierRepository repository) {
        this.repository = repository;
    }

    /**
     * Generates a code automatically for supplier
     */
    public String generateCode() {
        int nextNumber = getNextSequenceNumber(PREFIX_SUPPLIER);
        return String.format("%s-%04d", PREFIX_SUPPLIER, nextNumber);
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
