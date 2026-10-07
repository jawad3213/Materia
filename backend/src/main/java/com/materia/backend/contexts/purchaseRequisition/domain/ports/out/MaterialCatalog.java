package com.materia.backend.contexts.purchaseRequisition.domain.ports.out;

import com.materia.backend.contexts.purchaseRequisition.domain.valueObjects.MaterialSnapshot;

import java.util.Optional;
import java.util.UUID;

/** The materials a requisition can ask for. */
public interface MaterialCatalog {

    Optional<MaterialSnapshot> findById(UUID materialId);

    Optional<MaterialSnapshot> findByCode(String materialCode);
}
