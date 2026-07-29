package com.materia.backend.contexts.masterdata.domain.ports.in;

import com.materia.backend.contexts.masterdata.domain.entities.Supplier;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Input Port (Use Case) for Supplier management
 * Hexagonal Architecture - Defines the use cases available to the outside
 */
public interface SupplierUseCase {

    Supplier createSupplier(Supplier supplier);

    Supplier updateSupplier(UUID id, Supplier supplierDetails);

    void deleteSupplier(UUID id);

    Optional<Supplier> getSupplier(UUID id);

    Optional<Supplier> getSupplierByCode(String code);

    List<Supplier> getAllSuppliers();

    List<Supplier> searchSuppliers(String keyword);
}
