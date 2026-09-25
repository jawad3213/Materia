package com.materia.backend.contexts.invoice.application.services;

import com.materia.backend.contexts.masterData.domain.ports.out.CodeSequenceRepository;
import com.materia.backend.contexts.invoice.domain.valueObjects.InvoiceCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Year;

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
        int currentYear = Year.now().getValue();
        String sequenceKey = PREFIX + "-" + currentYear;
        int nextNumber = sequenceRepository.getNextValueAndIncrement(sequenceKey);
        return InvoiceCode.fromPrefixYearAndNumber(PREFIX, currentYear, nextNumber);
    }
}
