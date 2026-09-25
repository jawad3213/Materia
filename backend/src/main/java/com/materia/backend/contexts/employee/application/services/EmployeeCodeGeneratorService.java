package com.materia.backend.contexts.employee.application.services;

import com.materia.backend.contexts.employee.domain.valueObjects.EmployeeCode;
import com.materia.backend.contexts.masterData.domain.ports.out.CodeSequenceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 🔹 EMPLOYEE CODE GENERATOR SERVICE
 *
 * Generates enterprise employee codes adhering to Materia sequence:
 * Format: EMP-0001 (e.g. EMP-0001, EMP-0002) without year.
 * Uses pessimistic row locking via CodeSequenceRepository to guarantee conflict-free, gapless sequences.
 */
@Service
@Transactional
public class EmployeeCodeGeneratorService {

    private static final String PREFIX = "EMP";

    private final CodeSequenceRepository sequenceRepository;

    public EmployeeCodeGeneratorService(CodeSequenceRepository sequenceRepository) {
        this.sequenceRepository = sequenceRepository;
    }

    /**
     * Atomically increments the sequence and returns the formatted EmployeeCode.
     * Example: EMP-0001
     */
    public EmployeeCode generateCode() {
        int nextNumber = sequenceRepository.getNextValueAndIncrement(PREFIX);
        return EmployeeCode.fromPrefixAndNumber(PREFIX, nextNumber);
    }
}
