package com.materia.backend.contexts.masterdata.domain.ports.in;

import com.materia.backend.contexts.masterdata.domain.entities.Supplier;
import com.materia.backend.common.domain.BaseUseCase;

import java.util.List;
import java.util.UUID;

/**
 * Input Port (Use Case) for Supplier management
 * Hexagonal Architecture - Defines the use cases available to the outside
 */
public interface SupplierUseCase extends BaseUseCase<Supplier, UUID> {

    List<Supplier> searchSuppliers(String keyword);
}
