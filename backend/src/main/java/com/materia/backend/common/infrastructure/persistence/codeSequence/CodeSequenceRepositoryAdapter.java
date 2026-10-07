package com.materia.backend.common.infrastructure.persistence.codeSequence;

import com.materia.backend.common.domain.ports.out.CodeSequenceRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
public class CodeSequenceRepositoryAdapter implements CodeSequenceRepository {

    private final SpringDataCodeSequenceRepository springDataRepository;

    public CodeSequenceRepositoryAdapter(SpringDataCodeSequenceRepository springDataRepository) {
        this.springDataRepository = springDataRepository;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public int getNextValueAndIncrement(String prefix) {
        CodeSequenceJpaEntity sequence = springDataRepository.findByPrefixForUpdate(prefix)
                .orElse(new CodeSequenceJpaEntity(prefix, 1));
        
        int currentValue = sequence.getNextVal();
        sequence.setNextVal(currentValue + 1);
        springDataRepository.save(sequence);
        
        return currentValue;
    }
}
