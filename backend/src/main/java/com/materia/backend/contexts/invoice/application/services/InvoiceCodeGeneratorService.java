package com.materia.backend.contexts.invoice.application.services;

import com.materia.backend.contexts.masterData.domain.ports.out.CodeSequenceRepository;
import com.materia.backend.contexts.invoice.domain.valueObjects.InvoiceCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Generates stable invoice business codes.
 */
@Service
@Transactional
public class InvoiceCodeGeneratorService {

    private static final String PREFIX = "INV";

    private final CodeSequenceRepository sequenceRepository;

    public InvoiceCodeGeneratorService(CodeSequenceRepository sequenceRepository) {
        this.sequenceRepository = sequenceRepository;
    }

    public InvoiceCode generateCode() {
        int nextNumber = sequenceRepository.getNextValueAndIncrement(PREFIX);
        return InvoiceCode.fromPrefixAndNumber(PREFIX, nextNumber);
    }
}
