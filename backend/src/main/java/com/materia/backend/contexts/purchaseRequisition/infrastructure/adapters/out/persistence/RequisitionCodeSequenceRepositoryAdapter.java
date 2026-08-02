package com.materia.backend.contexts.purchaseRequisition.infrastructure.adapters.out.persistence;

import com.materia.backend.contexts.purchaseRequisition.domain.ports.out.RequisitionCodeSequenceRepository;
import com.materia.backend.contexts.purchaseRequisition.infrastructure.adapters.out.persistence.entities.RequisitionCodeSequenceJpaEntity;
import com.materia.backend.contexts.purchaseRequisition.infrastructure.adapters.out.persistence.repositories.SpringDataRequisitionCodeSequenceRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persistence adapter for requisition code sequences.
 */
@Component
public class RequisitionCodeSequenceRepositoryAdapter implements RequisitionCodeSequenceRepository {

    private final SpringDataRequisitionCodeSequenceRepository springDataRepository;

    public RequisitionCodeSequenceRepositoryAdapter(SpringDataRequisitionCodeSequenceRepository springDataRepository) {
        this.springDataRepository = springDataRepository;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public int getNextValueAndIncrement(String prefix) {
        RequisitionCodeSequenceJpaEntity sequence = springDataRepository.findByPrefixForUpdate(prefix)
                .orElse(new RequisitionCodeSequenceJpaEntity(prefix, 1));

        int currentValue = sequence.getNextVal();
        sequence.setNextVal(currentValue + 1);
        springDataRepository.save(sequence);

        return currentValue;
    }
}
