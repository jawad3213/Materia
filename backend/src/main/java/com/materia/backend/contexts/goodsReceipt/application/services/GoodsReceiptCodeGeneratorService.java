package com.materia.backend.contexts.goodsReceipt.application.services;

import com.materia.backend.contexts.goodsReceipt.domain.valueObjects.ReceiptCode;
import com.materia.backend.contexts.masterData.domain.ports.out.CodeSequenceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Year;

@Service
@Transactional
public class GoodsReceiptCodeGeneratorService {

    private static final String PREFIX = "GR";

    private final CodeSequenceRepository sequenceRepository;

    public GoodsReceiptCodeGeneratorService(CodeSequenceRepository sequenceRepository) {
        this.sequenceRepository = sequenceRepository;
    }

    public ReceiptCode generateCode() {
        int currentYear = Year.now().getValue();
        String sequenceKey = PREFIX + "-" + currentYear;
        int nextNumber = sequenceRepository.getNextValueAndIncrement(sequenceKey);
        return ReceiptCode.fromPrefixYearAndNumber(PREFIX, currentYear, nextNumber);
    }
}
