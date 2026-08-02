package com.materia.backend.contexts.purchaseRequisition.domain.ports.out;

/**
 * Output port for requisition code sequence persistence.
 */
public interface RequisitionCodeSequenceRepository {

    int getNextValueAndIncrement(String prefix);
}
