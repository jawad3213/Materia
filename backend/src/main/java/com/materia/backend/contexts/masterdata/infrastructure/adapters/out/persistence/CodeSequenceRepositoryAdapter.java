package com.materia.backend.contexts.masterData.infrastructure.adapters.out.persistence;

import com.materia.backend.contexts.masterData.domain.ports.out.CodeSequenceRepository;
import com.materia.backend.contexts.masterData.infrastructure.adapters.out.persistence.entities.CodeSequenceJpaEntity;
import com.materia.backend.contexts.masterData.infrastructure.adapters.out.persistence.repositories.SpringDataCodeSequenceRepository;
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
